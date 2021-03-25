/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 03/01/19 01:16 AM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer.trending;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.PostListingResponse;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.TrendingListingBinding;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardActivity;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.customViews.scrolllisteners.EndlessRecyclerViewScrollListener;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class TrendingListFragment extends BaseFragment<TrendingListingBinding, TrendingViewModel>
        implements TrendingListNavigator, PostListAdapter.BlogAdapterListener {

    @Inject
    PostListAdapter mBlogAdapter;
    Context context;
    TrendingListingBinding mFragmentBlogBinding;

    LinearLayoutManager mLayoutManager;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private TrendingViewModel mBlogViewModel;
    public SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    private String bodyDownload = "";
    private Result resultDownload;
    private static final int PERMISSION_REQUEST_CODE_EXT_STORAGE = 112;

    public static TrendingListFragment newInstance() {
        Bundle args = new Bundle();
        TrendingListFragment fragment = new TrendingListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.trending_listing;
    }

    @Override
    public TrendingViewModel getViewModel() {
        mBlogViewModel = ViewModelProviders.of(this, mViewModelFactory).get(TrendingViewModel.class);
        return mBlogViewModel;
    }

    @Override
    public void handleError(Throwable throwable) {
        mFragmentBlogBinding.blogRecyclerView.setVisibility(View.GONE);
        mFragmentBlogBinding.noInternetLayout.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "Connection Issue. Please try again later!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBlogViewModel.setNavigator(this);
        mBlogAdapter.setListener(this);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_TRENDING, new HashMap<>());
    }

    @Override
    public void onItemClick(String id, PostObject post) {
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        if (getContext() == null) return;
        StreamPlayerActivity.Companion.startActivity(getContext(),
                new StreamPlayerContainerFragment.Builder()
                        .addPost(post)
                        .addSourceScreenName(SegmentConstants.SCREEN_NAME_TRENDING)
                        .buildExtras());
    }

    @Override
    public void onRetryClick() {
        mBlogViewModel.fetchTrendingPosts(0, false);
        mFragmentBlogBinding.blogRecyclerView.setVisibility(View.VISIBLE);
        mFragmentBlogBinding.noInternetLayout.setVisibility(View.GONE);
    }

    @Override
    public void onLikeButtonClicked(String body, Result post) {
        mBlogViewModel.onLikeItemClicked(body, post);
    }

    @Override
    public void onShareButtonClicked(PostObject post) {
        sharePost(post);
    }

    private void sharePost(PostObject post) {
        if (post == null || post.getAuthor() == null || post.getAuthor().getUser() == null || post.getAuthor().getUser().getUsername() == null)
            return;
        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.BRANCH_POST_SOURCE_URL, post.getShareUrl());
        map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_LIVE_STREAM);
        FirebaseDynamicLinkUtils.share(context, post.getAuthor().getCampaignInfo(), "player_live_share", post.getAuthor().getUser().getUsername() + " is Live on Rheo TV",
                "Watch " + post.getAuthor().getUser().getUsername() + "playing " + post.getGame() + " live on Rheo TV",
                post.getThumbnail(), map, post.getShareUrl(), true, post.isLive(), post.getAuthor().getUser().getUsername());
    }

    @Override
    public void onAuthorClicked(String authorId) {
        mBlogViewModel.onAuthorClicked(authorId);
    }


    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(RheoTvApp.getNonUiContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_EXT_STORAGE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        mBlogViewModel.onDownloadClicked(bodyDownload, resultDownload, context);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                                showMessageOKCancel(getResources().getString(R.string.photo_upload_permission),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_EXT_STORAGE);
                                            }
                                        });
                                return;
                            } else {
                                Toast.makeText(context, RheoTvApp.getNonUiContext().getResources().getString(R.string.photo_upload_permission), Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.all_good), okListener)
                .create()
                .show();
    }

    @Override
    public void onCarouselItemClicked(String id, List<PostObject> results, PostObject post, String carouselTitle) {

    }

    @Override
    public void onMultiViewItemClicked(String id, List<PostObject> results) {

    }

    public void onCarouselVotingItemClicked(String tag, String id, Result result) {

    }

    @Override
    public void onSeeMoreClicked(List<PostObject> result) {

    }

    public void onVotingResultShareClicked() {

    }


    @Override
    public void onLeaderboardClicked(String id) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("is_first", CommonUtils.isFirstTimeLeaderBoardClicked());
        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_LEADERBOARD_PAGE_CLICKED, properties);
        CommonUtils.setFirstTimeLeaderBoardClicked();
        Intent intent = new Intent(getActivity(), LeaderBoardActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TRENDING);
        startActivity(intent);
    }

    @Override
    public void onSeeAllClicked(String game, String id) {
        Intent intent = new Intent(getActivity(), UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, game);
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, id);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TRENDING);
        startActivity(intent);
    }

    @Override
    public void onAlertCardClicked() {

    }

    @Override
    public void onFollowBtnClicked(String author, int id, boolean isFollowed, OnFollowActionCompleteListener listener) {

    }

    @Override
    public void onProfileViewAction(String authorUsername, OnFollowActionCompleteListener listener) {

    }

    @Override
    public void onMoreOptionsBtnClick(String id) {

    }

    @Override
    public void onSuperPrimeReminderListener(PostObject result) {

    }

    @Override
    public void onSuperStreamerCardClick(String id) {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentBlogBinding = getViewDataBinding();
        setUp();
        subscribeToLiveData();
    }

    @Override
    public void updateBlog(PostListingResponse blogList) {
//        mBlogAdapter.addItems(blogList.getResults());
    }

    @Override
    public void updateCoins() {
    }

    @Override
    public void switchFragment(String id) {
        Intent intent = ProfileActivity.getCallingIntent(context);
        intent.putExtra("author_name", id);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TRENDING);
        context.startActivity(intent);
        // ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.frame_container, ProfileContainerFragment.newInstance(id)).addToBackStack("Author").commit();
    }

    private void setUp() {
        mLayoutManager = new LinearLayoutManager(context);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mFragmentBlogBinding.blogRecyclerView.setLayoutManager(mLayoutManager);
        mFragmentBlogBinding.blogRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFragmentBlogBinding.blogRecyclerView.setAdapter(mBlogAdapter);
        mFragmentBlogBinding.blogRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (totalItemsCount > 1) {
                    mBlogViewModel.fetchTrendingPosts(totalItemsCount, true);
                }
            }
        });
        mFragmentBlogBinding.swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.main_color));
        mFragmentBlogBinding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRetryClick();
                mBlogViewModel.getBlogObservableList().clear();
                mBlogViewModel.setIsLoading(true);
                mBlogViewModel.fetchTrendingPosts(0, false);
                mFragmentBlogBinding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void subscribeToLiveData() {
        mBlogViewModel.getBlogListLiveData().observe(getViewLifecycleOwner(), blogs -> mBlogViewModel.addBlogItemsToList(blogs));
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onGameClicked(String game, String gameId) {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("game", game);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_GAME_TAG_CLICKED, properties);

        Intent intent = new Intent(getActivity(), UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, game);
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, gameId);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TRENDING);
        startActivity(intent);
    }

    @Override
    public void onDeleteVideoClicked(String id, int position) {

    }


    @Override
    public void onDownloadVideoClicked(String id, int position) {

    }
}
