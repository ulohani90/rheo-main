package com.rheotv.android.ui.activities.streamEnd;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.ActivityStreamEndBinding;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.activities.tabcontainer.profile.videos.VideoFragmentAdapter;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.ui.customViews.HidingScrollListener;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.recyclerdecorators.BottomSpacingDecoration;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class StreamEndActivity extends BaseActivity<ActivityStreamEndBinding, StreamEndViewModel> implements PostListAdapter.BlogAdapterListener {

    @Inject
    StreamEndViewModel mViewModel;

    @Inject
    VideoFragmentAdapter mAdapter;

    private ActivityStreamEndBinding mBinding;

    private HashMap<String, Object> baseProperties = new HashMap<>();

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_stream_end;
    }

    @Override
    public StreamEndViewModel getViewModel() {
        mViewModel.loadSimilarPost();
        mViewModel.blogListLiveData.observe(this, list -> {
            mAdapter.addItems(list);
        });
        return mViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);
        mAdapter.setListener(this);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.margin_24);
        mBinding.recyclerView.addItemDecoration(new BottomSpacingDecoration(spacingInPixels));
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideOnScroll();
            }

            @Override
            public void onShow() {
                showOnScroll();
            }
        });
        mBinding.streamerButton.setOnClickListener(v -> showLiveGames());

        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE)) {
            baseProperties.put(AppConstants.SCREEN_SOURCE, getIntent().getStringExtra(AppConstants.SCREEN_SOURCE));
        }

        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_STREAM_END);
        SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_STREAM_END, baseProperties);
        EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
    }

    private void showLiveGames() {
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_STREAM_END_SEE_ALL_CLICKED, new HashMap<>());
        Intent intent = new Intent(this, UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, "live");
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, "is_live");
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_STREAM_END);
        startActivity(intent);
    }

    private void hideOnScroll() {
        mBinding.imageViewRobot.animate().translationX(-(mBinding.imageViewRobot.getMeasuredWidth() + CommonUtils.toPix(40)));
        mBinding.streamerButton.animate().translationY((mBinding.streamerButton.getMeasuredHeight() + CommonUtils.toPix(40)));
    }

    private void showOnScroll() {
        mBinding.imageViewRobot.animate().translationX(0);
        mBinding.streamerButton.animate().translationY(0);
    }

    @Override
    public void onItemClick(String id, PostObject post) {
        HashMap<String, Object> properties = baseProperties;
        properties.put("postId", id);
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_STREAM_END_POST_CLICKED, properties);

        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        StreamPlayerActivity.Companion.startActivity(this,
                new StreamPlayerContainerFragment.Builder()
                        .addPostList(new ArrayList<>(mAdapter.getmPostList()))
                        .addPost(post)
                        .addPaginationUrl(mViewModel.getNextUrl())
                        .addPaginationUrl(mViewModel.getNextUrl())
                        .addSourceScreenName(SegmentConstants.SCREEN_STREAM_END)
                        .buildExtras());
    }

    @Override
    public void onRetryClick() {

    }

    @Override
    public void onLikeButtonClicked(String body, Result post) {

    }

    @Override
    public void onShareButtonClicked(PostObject post) {
        HashMap<String, Object> properties = baseProperties;
        if (post == null || post.getAuthor() == null || post.getAuthor().getUser() == null || post.getAuthor().getUser().getUsername() == null)
            return;
        properties.put("postId", post.getId());
        properties.put("author_name", post.getAuthor().getUser().getUsername());
        properties.put("post_title", post.getTitle());
        properties.put("game", post.getGame().getName());
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_STREAM_END_SHARE_CLICKED, properties);

        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.BRANCH_POST_SOURCE_URL, post.getShareUrl());
        map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_LIVE_STREAM);
        FirebaseDynamicLinkUtils.share(this, post.getAuthor().getCampaignInfo(), "player_live_share", post.getAuthor().getUser().getUsername() + " is Live on Rheo TV",
                "Watch " + post.getAuthor().getUser().getUsername() + "playing " + post.getGame() + " live on Rheo TV",
                post.getThumbnail(), map, post.getShareUrl(), true, post.isLive(), post.getAuthor().getUser().getUsername());
    }

    @Override
    public void onAuthorClicked(String userName) {
        Intent intent = ProfileActivity.getCallingIntent(this);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_STREAM_END);
        intent.putExtra("author_name", userName);
        startActivity(intent);
    }

    @Override
    public void onCarouselItemClicked(String id, List<PostObject> results, PostObject post, String title) {

    }

    @Override
    public void onMultiViewItemClicked(String id, List<PostObject> results) {

    }

    @Override
    public void onSeeMoreClicked(List<PostObject> result) {

    }

    @Override
    public void onLeaderboardClicked(String id) {

    }

    @Override
    public void onSeeAllClicked(String game, String id) {

    }

    @Override
    public void onAlertCardClicked() {

    }

    @Override
    public void onFollowBtnClicked(String author, int authorId, boolean isFollowed, OnFollowActionCompleteListener listener) {

    }

    @Override
    public void onProfileViewAction(String authorUsername, OnFollowActionCompleteListener listener) {

    }

    @Override
    public void onMoreOptionsBtnClick(String id) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.report_this_title))
                .setMessage(getString(R.string.report_content)).setPositiveButton("Yes", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            mViewModel.reportPost(id);
        }).setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    @Override
    public void onSuperPrimeReminderListener(PostObject result) {

    }

    @Override
    public void onSuperStreamerCardClick(String id) {

    }

    @Override
    public void onGameClicked(String game, String gameId) {
        HashMap<String, Object> properties = baseProperties;
        properties.put("game", game);
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_GAME_TAG_CLICKED, properties);

        Intent intent = new Intent(this, UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, game);
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, gameId);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_STREAM_END);
        startActivity(intent);
    }

    @Override
    public void onDeleteVideoClicked(String id, int position) {

    }

    @Override
    public void onDownloadVideoClicked(String id, int position) {

    }
}
