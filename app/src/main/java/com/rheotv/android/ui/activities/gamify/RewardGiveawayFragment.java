package com.rheotv.android.ui.activities.gamify;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.RewardRedeemFragmentBinding;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.ListHolder;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.recyclerdecorators.VerticalLinearItemDecoration;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import static com.rheotv.android.ui.adapters.PostListAdapter.LIVE_VIDEO_TAG;

public class RewardGiveawayFragment extends BaseFragment<RewardRedeemFragmentBinding, RewardGiveawayViewModel> {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    PostListAdapter adapter;

    private RewardGiveawayViewModel mViewModel;
    private String mSource;
    private HashMap<String, Object> baseProperties = new HashMap<>();
    private boolean isLoading = true;
    private boolean isPlayerChildView;

    public static RewardGiveawayFragment newInstance(String source) {
        RewardGiveawayFragment fragment = new RewardGiveawayFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.reward_redeem_fragment;
    }

    @Override
    public RewardGiveawayViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(RewardGiveawayViewModel.class);
        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            mSource = getArguments().getString(AppConstants.SCREEN_SOURCE);
        baseProperties.put(AppConstants.SCREEN_SOURCE, mSource);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_REDEEM_GIVEAWAY);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_REDEEM_GIVEAWAY, baseProperties);

        setupViews();
    }

    private void pausePlayer() {
        if (adapter != null && adapter.mPlayer != null) {
            adapter.mPlayer.setPlayWhenReady(false);
        }
    }

    private void shareBranchLink(PostObject post) {
        if (post == null || post.getAuthor() == null || post.getAuthor().getUser() == null || post.getAuthor().getUser().getUsername() == null)
            return;
        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.BRANCH_POST_SOURCE_URL, post.getShareUrl());
        map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_LIVE_STREAM);
        FirebaseDynamicLinkUtils.share(getContext(),
                post.getAuthor().getCampaignInfo(),
                "player_live_share",
                post.getAuthor().getUser().getUsername() + " is Live on Rheo TV",
                "Watch " + post.getAuthor().getUser().getUsername() + "playing " + post.getGame() + " live on Rheo TV",
                post.getThumbnail(),
                map,
                post.getShareUrl(),
                true, post.isLive(), post.getAuthor().getUser().getUsername());
        HashMap<String, Object> properties = new HashMap<>(map);
        properties.put("author", post.getAuthor().getUser().getUsername());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_POST_SHARE_CLICK, properties);
    }

    private void makeReportPostRequest(String id) {
        mViewModel.reportPost(id);
    }

    private void onMoreOptionsClick(String id) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext()).setTitle(getString(R.string.report_this_title)).setMessage(getString(R.string.report_content)).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                makeReportPostRequest(id);
            }
        }).setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    private void openGame(String game, String gameId) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("game", game);
        properties.put("id", gameId);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_GAME_TAG_CLICKED, properties);

        Intent intent = new Intent(getActivity(), UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, game);
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, gameId);
        intent.putExtra(AppConstants.SCREEN_SOURCE, mSource);
        startActivity(intent);
    }

    private void setupViews() {
//        adapter.setListener(this::onGameSelected);
        if (getContext() == null) return;
        adapter.setListener(new PostListAdapter.BlogAdapterListener() {
            @Override
            public void onItemClick(String id, PostObject post) {
                HashMap<String, Object> properties = new HashMap<>();
                properties.put("post_id", id);
                properties.put("post_click_source", "single item");
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FEED_POST_CLICKED, properties);
                pausePlayer();
                PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
                ListHolder.getInstance().extractPostIds(new ArrayList<>());
                if (getContext() != null) {
                    StreamPlayerActivity.Companion.startActivity(getContext(),
                            new StreamPlayerContainerFragment.Builder()
                                    .addPost(post)
                                    .addPaginationUrl(mViewModel.getNextUrl())
                                    .addSourceScreenName(SegmentConstants.SCREEN_REDEEM_GIVEAWAY)
                                    .buildExtras());
                }
            }

            @Override
            public void onRetryClick() {
                getViewDataBinding().redeemRecyclerView.setVisibility(View.VISIBLE);
                getViewDataBinding().redeemRecyclerView.setVisibility(View.GONE);
            }

            @Override
            public void onLikeButtonClicked(String body, Result post) {

            }

            @Override
            public void onShareButtonClicked(PostObject post) {
                shareBranchLink(post);
            }

            @Override
            public void onAuthorClicked(String userName) {
                if (getContext() == null) return;
                Intent intent = ProfileActivity.getCallingIntent(getContext());
                intent.putExtra("author_name", userName);
                intent.putExtra(AppConstants.SCREEN_SOURCE, mSource);
                getContext().startActivity(intent);
            }

            @Override
            public void onCarouselItemClicked(String id, List<PostObject> results, PostObject postObject, String carouselTitle) {

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
                onMoreOptionsClick(id);
            }

            @Override
            public void onSuperPrimeReminderListener(PostObject result) {

            }

            @Override
            public void onSuperStreamerCardClick(String id) {

            }

            @Override
            public void onGameClicked(String game, String gameId) {
                openGame(game, gameId);
            }

            @Override
            public void onDeleteVideoClicked(String id, int position) {

            }

            @Override
            public void onDownloadVideoClicked(String id, int position) {

            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setAutoMeasureEnabled(true);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        getViewDataBinding().messageTextView.setVisibility(View.GONE);
        getViewDataBinding().redeemRecyclerView.setLayoutManager(linearLayoutManager);
        getViewDataBinding().redeemRecyclerView.addItemDecoration(new VerticalLinearItemDecoration(ViewUtils.dpToPx(16)));
        getViewDataBinding().redeemRecyclerView.setAdapter(adapter);
        mViewModel.loadGiveaway();
        mViewModel.getGiveawayResult().observe(getViewLifecycleOwner(), list -> {
            isLoading = false;
            adapter.setShowLoadingView(false);
            adapter.showShimmerLoading(false);
            adapter.addItems(list, -1);
        });
        getViewDataBinding().redeemRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

                if (!isLoading && mViewModel.getNextUrl() != null && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    Log.i("Next url ", mViewModel.getNextUrl());
                    isLoading = true;
                    adapter.setShowLoadingView(true);
                    mViewModel.loadMoreGiveaway();
                }
            }
        });
        getViewDataBinding().redeemRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                if (view.getTag() != null && view.getTag().toString().equalsIgnoreCase(LIVE_VIDEO_TAG)) {
                    isPlayerChildView = true;
                    if (adapter.mPlayer != null && !PlayerHeadServiceHelper.getInstance().isServiceRunning()) {
                        adapter.mPlayer.setPlayWhenReady(true);
                    }
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (view.getTag() != null && view.getTag().toString().equalsIgnoreCase(LIVE_VIDEO_TAG)) {
                    if (adapter.mPlayer != null && !adapter.isUpdatingPlayer()) {
                        isPlayerChildView = false;
                        adapter.mPlayer.setPlayWhenReady(false);
                    }
                }
            }
        });
        getViewDataBinding().swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.main_color));
        getViewDataBinding().swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoading) {
                    //stopPlayerAndRelease();
                    adapter.releasePlayer();
                    adapter.clearItems();
                    getViewDataBinding().swipeRefresh.setRefreshing(false);
                    mViewModel.setNextUrl(null);
                    mViewModel.loadGiveaway();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null && isPlayerChildView && !PlayerHeadServiceHelper.getInstance().isServiceRunning()) {
            //mBlogAdapter.mPlayer.setPlayWhenReady(true);
            adapter.updatePlayerView();
        }
    }
}
