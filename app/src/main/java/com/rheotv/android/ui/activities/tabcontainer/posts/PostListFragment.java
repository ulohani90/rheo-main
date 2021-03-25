/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 03/01/19 01:16 AM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer.posts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.ObservableField;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.chip.Chip;
import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.objects.TagResults;
import com.rheotv.android.data.network.models.postlisting.responses.PostListingResponse;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.databinding.PostListingBinding;
import com.rheotv.android.helpers.AlarmReceiver;
import com.rheotv.android.ui.activities.alertInformation.AlertInformationActivity;
import com.rheotv.android.ui.activities.gamify.RewardsActivity;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardActivity;
import com.rheotv.android.ui.activities.moments.view.activities.MomentsActivity;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.search.SearchActivity;
import com.rheotv.android.ui.activities.story.CreateStoryActivity;
import com.rheotv.android.ui.activities.story.StoryActivity;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.activities.tabcontainer.posts.multiviewitem.seemore.SeeMoreFragment;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.CarouselAdapter;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.adapters.StoryAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.decorators.PostItemDecorator;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.TimeUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.inject.Inject;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rheotv.android.app.RheoTvApp.getNonUiContext;
import static com.rheotv.android.ui.adapters.PostListAdapter.LIVE_VIDEO_TAG;
import static com.rheotv.android.utils.PlayerHeadServiceHelper.PLAYER_SERVICE_STOP_BROADCAST;


public class PostListFragment extends BaseFragment<PostListingBinding, PostViewModel>
        implements PostListNavigator, PostListAdapter.BlogAdapterListener,
        CarouselAdapter.CarouselItemClickListener,
        StoryAdapter.OnStoryInteractionListener, LoginFragmentBottomDialog.LoginFragmentCallback {

    private static final int PERMISSION_REQUEST_CODE_EXT_STORAGE = 111;
    private String CATEGORY_KEY = "";
    private String bodyDownload = "";
    private Result resultDownload;
    SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    public ObservableField<Integer> recyclerViewChildrenCount = new ObservableField<>(0);

    private LoginFragmentBottomDialog loginDialogFragment;

    public PostListFragment() {
    }

    public String getCategoryKey() {
        return CATEGORY_KEY;
    }

    public void setCategoryKey(String categoryKey) {
        CATEGORY_KEY = categoryKey;
    }


    PostListAdapter mBlogAdapter;

    @Inject
    CarouselAdapter mCarouselAdapter;

    private StoryAdapter storyAdapter;


    PostListingBinding mFragmentBlogBinding;

    LinearLayoutManager mLayoutManager;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private PostViewModel mBlogViewModel;

    private Context context;

    private boolean isPlayerChildView;

    boolean isLoading = true;

    ProgressDialog progressDialog;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    private int storyContainerHeight;
    private int storyRecyclerViewHeight;
    private boolean isTagsAdded = false;
    private boolean isStoriesAdded = false;
    private boolean isFeedDecoratorAdded = false;

    private boolean isStoryLoading = false;

    protected Queue<Runnable> actionQueue = new LinkedList<>();

    public static PostListFragment newInstance(String screenSource) {
        Bundle args = new Bundle();
        args.putString(AppConstants.SCREEN_SOURCE, screenSource);
        PostListFragment fragment = new PostListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.post_listing;
    }

    @Override
    public PostViewModel getViewModel() {
        try {
            mBlogViewModel = ViewModelProviders.of(this, mViewModelFactory).get(PostViewModel.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mBlogViewModel;

    }

    private void checkInternetAvailability() {
        if (NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext())) {
            mFragmentBlogBinding.noInternetLayout.setLayoutVisible(false);
        } else {
            mFragmentBlogBinding.noInternetLayout.setLayoutVisible(true);
        }
    }

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkConnected() && mBlogViewModel != null && isAdded() && !isRemoving()) {
                checkInternetAvailability();
                mBlogViewModel.fetchHomePage(false);
                mBlogViewModel.loadStoryAuthor(true);
            }
        }
    };

    public PostListAdapter getPostListadapter() {
        return mBlogAdapter;
    }

    @Override
    public void handleError(Throwable throwable) {
        mFragmentBlogBinding.blogRecyclerView.setVisibility(View.GONE);
        mFragmentBlogBinding.searchAutocomplete.setVisibility(View.GONE);
        mFragmentBlogBinding.noInternetLayout.setLayoutVisible(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }


    @Override
    public void hidePaginationLoader() {
        mBlogAdapter.setShowLoadingView(false);
        mBlogAdapter.showShimmerLoading(false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mBlogViewModel.setNavigator(this);
        mCarouselAdapter.setListener(this);
        if (getArguments() != null) {
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        }
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_FEED);
        mBlogViewModel.properties = baseProperties;

        loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        loginDialogFragment.setmCallback(this);

        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_FEED, baseProperties);
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(loginBroadcast, new IntentFilter(AppConstants.UPDATE_STORY_BROADCAST_FILTER));
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

    }

    public void onRewardClicked() {
        Map<String, Object> map = new HashMap<>(baseProperties);
        map.put("is_first", CommonUtils.isFirstTimeCoinsClicked());
        map.put("total_coins_count", RewardManager.getInstance().getTotalCoins());
        SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_TOTAL_COIN_CLICKED, map);
        CommonUtils.setFirstTimeCoinsClicked();
        if (CommonUtils.isUserLoggedin()) {
            Intent intent = new Intent(getContext(), RewardsActivity.class);
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
            startActivity(intent);
        } else
            openLoginFlow(getString(R.string.login_to_get_reward_message));
    }


    public void openLoginFlow(String rewardMessage) {
        try {
            if (mFragmentBlogBinding == null || mBlogViewModel == null || isStateSaved()) return;
            if (loginDialogFragment.isAdded() || loginDialogFragment.isVisible())
                return;

            loginDialogFragment.setRewardText(rewardMessage);
            loginDialogFragment.showNoAddToBackStack(this.getChildFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(String id, PostObject post) {

       /* HashMap<String, Object> properties = new HashMap<>();
        properties.put("post_id", id);
        properties.put("post_click_source", "single item");
        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_FEED_POST_CLICKED, properties);
        pausePlayer();
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        ListHolder.getInstance().extractPostIds(new ArrayList<>());
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.KEY_POST_ID, id);
        bundle.putString(AppConstants.ARG_TITLE, post.getTitle());
        bundle.putString(AppConstants.ARG_THUMBNAIL, post.getThumbnail());
        context.startActivity(PlayerActivity.newIntent(context, SegmentConstants.SCREEN_NAME_FEED).putExtras(bundle));*/

        if (getContext() == null) return;
        StreamPlayerActivity.Companion.startActivity(getContext(),
                new StreamPlayerContainerFragment.Builder()
                        .addPost(post)
                        .addGameId(AppConstants.LIVE_GAME_ID)
                        .addSourceScreenName(SegmentConstants.SCREEN_NAME_FEED)
                        .addLoadMore(true)
                        .buildExtras());
//        startActivity(intent);
    }

    @Override
    public void onRetryClick() {
        mBlogViewModel.fetchHomePage(false);
        mBlogViewModel.loadStoryAuthor(true);
        mFragmentBlogBinding.blogRecyclerView.setVisibility(View.VISIBLE);
        mFragmentBlogBinding.searchAutocomplete.setVisibility(View.VISIBLE);
        mFragmentBlogBinding.noInternetLayout.setLayoutVisible(false);
    }

    @Override
    public void onLikeButtonClicked(String body, Result post) {
        //mBlogViewModel.onLikeItemClicked(body, post);
    }

    @Override
    public void onShareButtonClicked(PostObject post) {
        shareBranchLink(post);
    }

    private void shareBranchLink(PostObject post) {
        if (post == null || post.getAuthor() == null || post.getAuthor().getUser() == null || post.getAuthor().getUser().getUsername() == null)
            return;
        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.BRANCH_POST_SOURCE_URL, post.getShareUrl());
        map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_LIVE_STREAM);
        FirebaseDynamicLinkUtils.share(context,
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
        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_POST_SHARE_CLICK, properties);
    }

    @Override
    public void onAuthorClicked(String userName) {
        mBlogViewModel.onAuthorClicked(userName);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentBlogBinding = getViewDataBinding();
        shouldRefreshStories = true;
        setUp();
        subscribeToLiveData();
        mBlogViewModel.setCategoryName(CATEGORY_KEY);
    }

    @Override
    public void updateBlog(PostListingResponse blogList) {
    }

    @Override
    public void switchFragment(String id) {
        Intent intent = ProfileActivity.getCallingIntent(context);
        intent.putExtra("author_name", id);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_FEED);
        context.startActivity(intent);
        //((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.frame_container, ProfileContainerFragment.newInstance(id)).addToBackStack("Author").commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusModel.UpdateCoin updateCoin) {
        if (mBlogViewModel == null || mFragmentBlogBinding == null || isRemoving() || isStateSaved())
            return;
        if (updateCoin != null) {
            if (CommonUtils.isUserLoggedin()) {
                if (!isResumed()) {
                    actionQueue.add(() -> mFragmentBlogBinding.totalCoinCountTextView.setText(RewardManager.getInstance().getTotalCoins()));
                } else
                    mFragmentBlogBinding.totalCoinCountTextView.setText(RewardManager.getInstance().getTotalCoins());
            } else
                mFragmentBlogBinding.totalCoinCountTextView.setText("");
        }
    }

    @Override
    public void notifyDataSetFromStorage() {
//        mBlogAdapter.notifyDataSetChanged();
    }


    private void setUp() {
        if (CommonUtils.isUserLoggedin()) {
            mFragmentBlogBinding.totalCoinCountTextView.setText(RewardManager.getInstance().getTotalCoins());
        }
        mFragmentBlogBinding.leaderBoardButton.setOnClickListener(v -> onLeaderboardClick());
        mFragmentBlogBinding.clipImageButton.setOnClickListener(v -> onClipButtonClick());
        mFragmentBlogBinding.totalCoinCountTextView.setOnClickListener(v -> onRewardClicked());
        mLayoutManager = new LinearLayoutManager(context);
        mLayoutManager.setAutoMeasureEnabled(true);
        mBlogAdapter = new PostListAdapter(getActivity(), new ArrayList<>());
        mBlogAdapter.setListener(this);
        mBlogAdapter.setStoryListener(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mFragmentBlogBinding.blogRecyclerView.setLayoutManager(mLayoutManager);
        mFragmentBlogBinding.blogRecyclerView.setNestedScrollingEnabled(true);
        mFragmentBlogBinding.blogRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFragmentBlogBinding.blogRecyclerView.setAdapter(mBlogAdapter);
        mFragmentBlogBinding.blogRecyclerView.setHasFixedSize(true);
        mFragmentBlogBinding.blogRecyclerView.addItemDecoration(new PostItemDecorator((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics())));
        mFragmentBlogBinding.blogRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

                // Load more if we have reach the end to the recyclerView
                if (!isLoading && mBlogViewModel.nextUrl != null && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    Log.i("Next url ", mBlogViewModel.nextUrl);
                    isLoading = true;
                    mBlogAdapter.setShowLoadingView(true);
                    mBlogViewModel.fetchHomePage(true);
                }
            }
        });


        mFragmentBlogBinding.blogRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                if (view.getTag() != null && view.getTag().toString().equalsIgnoreCase(LIVE_VIDEO_TAG)) {
                    isPlayerChildView = true;
                    if (mBlogAdapter.mPlayer != null && !PlayerHeadServiceHelper.getInstance().isServiceRunning()) {
                        mBlogAdapter.mPlayer.setPlayWhenReady(true);
                    }
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (view.getTag() != null && view.getTag().toString().equalsIgnoreCase(LIVE_VIDEO_TAG)) {
                    if (mBlogAdapter.mPlayer != null && !mBlogAdapter.isUpdatingPlayer()) {
                        isPlayerChildView = false;
                        mBlogAdapter.mPlayer.setPlayWhenReady(false);
                    }
                }
            }
        });
        mFragmentBlogBinding.swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.main_color));
        mFragmentBlogBinding.swipeRefresh.setOnRefreshListener(this::refresh);
        mFragmentBlogBinding.noInternetLayout.retryButton.setOnClickListener(v -> refresh());
        mFragmentBlogBinding.searchAutocomplete.setOnClickListener(v -> SearchActivity.startMe(context));
    }

    private void refresh() {
        if (!isLoading) {
            //stopPlayerAndRelease();
            mBlogAdapter.releasePlayer();
            mBlogAdapter.clearItems();
            mBlogViewModel.clearTags();
            mBlogViewModel.setIsLoading(true);
            mBlogViewModel.setNextUrl(null);
            onRetryClick();
            mFragmentBlogBinding.swipeRefresh.setRefreshing(false);
        }
    }

//    private void calculateHeaderHeights() {
//        if (isStoriesAdded && isTagsAdded) {
//
//            ViewTreeObserver observer = mFragmentBlogBinding.containerLayout.getViewTreeObserver();
//            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    storyContainerHeight = mFragmentBlogBinding.containerLayout.getHeight();
//                    mFragmentBlogBinding.swipeRefresh.setProgressViewOffset(true, storyContainerHeight, storyContainerHeight + CommonUtils.toPix(140));
//
//                    if (!isFeedDecoratorAdded) {
//                        isFeedDecoratorAdded = true;
//                        mFragmentBlogBinding.blogRecyclerView.addItemDecoration(new PostItemDecorator((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()), storyContainerHeight));
//                    }
//
//                    Log.i(getClass().getSimpleName(), "containerLayout_ViewTreeObserver: " + storyContainerHeight);
//                    mFragmentBlogBinding.containerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                }
//            });
//
//            ViewTreeObserver storyTreeObserver = mFragmentBlogBinding.storyRv.getViewTreeObserver();
//            storyTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    storyRecyclerViewHeight = mFragmentBlogBinding.storyRv.getHeight();
//                    Log.i(getClass().getSimpleName(), "storyRv_ViewTreeObserver" + storyRecyclerViewHeight);
//                    mFragmentBlogBinding.storyRv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                }
//            });
//
//        }
//
//
//    }
//
//    private void hideViews() {
//        mFragmentBlogBinding.containerLayout.animate().translationY(-storyRecyclerViewHeight).setInterpolator(new AccelerateInterpolator(2)).start();
//        mFragmentBlogBinding.storyRv.animate().translationY(-storyRecyclerViewHeight).setInterpolator(new AccelerateInterpolator(2)).start();
//    }
//
//    private void showViews() {
//        mFragmentBlogBinding.containerLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
//        mFragmentBlogBinding.storyRv.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
//    }

    @Override
    public void stopLoading() {
        isLoading = false;
        mFragmentBlogBinding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public void showProgressBarLoading(String message) {
        progressDialog = ProgressDialog.show(getContext(), null, message);
    }

    @Override
    public void hideProgressBarLoading() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void subscribeToLiveData() {
        mBlogViewModel.getBlogListLiveData().observe(getViewLifecycleOwner(), blogs -> {
            isLoading = false;
            mBlogAdapter.setShowLoadingView(false);
            mBlogAdapter.showShimmerLoading(false);
            //mBlogViewModel.addBlogItemsToList(blogs);
            mBlogAdapter.addItems(blogs, -1);
        });

//        mBlogViewModel.tags.observe(getViewLifecycleOwner(), this::setUpTags);
        mBlogViewModel.storyAuthors.observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                if (shouldRefreshStories) {
                    shouldRefreshStories = false;
                    // mBlogAdapter.removeStories(0);
                }

                if (CommonUtils.isUserLoggedin()) {
                    list.add(0, CommonUtils.getSelfStoryProfile());
                } else {
                    list.add(0, CommonUtils.getWithoutLoginSelfStoryProfile());
                }

                mBlogAdapter.addStories(list);
            }
        });

        mBlogViewModel.pagedStoryAuthors.observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                isStoryLoading = false;
                mBlogAdapter.addPageStory(list);
            }
        });

        mBlogViewModel.storyLoading.observe(getViewLifecycleOwner(), status -> {
            mBlogAdapter.showStoryLoading(false);
        });
    }

    @Override
    public void onAddNewStoryClicked() {
        if (CommonUtils.isUserLoggedin()) {
            pausePlayer();
            PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_ADD_STORY_PROFILE_CLICKED, baseProperties);
            Intent intent = new Intent(getContext(), CreateStoryActivity.class);
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_FEED);
            startActivity(intent);
        } else {
            handleLogin();
        }
    }

    @Override
    public void onStoryClicked(ProfileResult profileResult, int position) {
        pausePlayer();
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();

        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("author", profileResult.getUser().getUsername());

        if (CommonUtils.isFirstStoryClickedNotTracked()) {
            CommonUtils.setFirstStoryClickedEventTracked();
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FIRST_STORY_AUTHOR_CLICKED, properties);
        }

        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_AUTHOR_CLICKED, properties);

        Intent intent = new Intent(getContext(), StoryActivity.class);
        intent.putExtra("author", profileResult.getUser().getUsername());
        ArrayList<ProfileResult> authorList = mBlogViewModel.storyAuthors.getValue();
        if (authorList != null && !authorList.isEmpty()) {
            if (authorList.get(0).getId().equalsIgnoreCase(CommonUtils.getAuthorId()) || "me".equalsIgnoreCase(authorList.get(0).getId())) {
                intent.putExtra(StoryActivity.ARG_AUTHOR_LIST, new ArrayList<>(mBlogViewModel.storyAuthors.getValue().subList(1, authorList.size())));
                intent.putExtra(StoryActivity.ARG_AUTHOR_INDEX, position - 1);
            } else {
                intent.putExtra(StoryActivity.ARG_AUTHOR_LIST, mBlogViewModel.storyAuthors.getValue());
                intent.putExtra(StoryActivity.ARG_AUTHOR_INDEX, position);
            }
        }

        intent.putExtra(StoryActivity.ARG_AUTHOR_ID, profileResult.getId());
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_FEED);
        intent.putExtra(AppConstants.ARG_NEXT_AUTHOR_URL, mBlogViewModel.nextStoryAuthorUrl);
        startActivity(intent);
    }

    @Override
    public void loadNextStory() {
        if (!isStoryLoading) {
            if (mBlogViewModel.nextStoryAuthorUrl != null) {
                isStoryLoading = true;
                mBlogViewModel.loadStoryAuthor(false);
            } else {
                mBlogAdapter.showStoryLoading(false);
            }
        }
    }

    private void setUpTags(ArrayList<TagResults> tags) {
        if (tags == null) return;
        mFragmentBlogBinding.tagChipGroup.removeAllViews();

//        Log.i(getClass().getSimpleName(), "setUpTags: " + new Gson().toJson(tags));
        for (TagResults tag : tags) {
            Chip chip = new Chip(getBaseActivity(), null, R.attr.chipChoiceFilterStyle);
            chip.setTag(tag.getSlug());
            chip.setText(tag.getTagName());
            try {
                loadChipIconGlide(chip, tag.getImageUrl());
            } catch (Exception e) {
                e.printStackTrace();
            }


            chip.setOnClickListener(v -> {
                String id = v.getTag().toString();

                if (mBlogViewModel.selectedTags.containsKey(id)) {
                    mBlogViewModel.selectedTags.remove(id);
                } else {
                    mBlogViewModel.selectedTags.clear();
                    mBlogViewModel.selectedTags.put(id, id);
                }

                mBlogAdapter.releasePlayer();
                mBlogAdapter.clearItems();
                mBlogViewModel.setIsLoading(true);
                mBlogViewModel.setNextUrl(null);
                onRetryClick();
                mFragmentBlogBinding.swipeRefresh.setRefreshing(false);
                HashMap<String, Object> properties = new HashMap<>(baseProperties);
                properties.put("tag", id);
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_POST_FILTER_TAG_CLICKED, properties);
            });

            mFragmentBlogBinding.tagChipGroup.addView(chip);
        }
    }

    private void loadChipIconGlide(Chip chip, String url) {
        Glide.with(this)
                .load(url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mFragmentBlogBinding.getRoot().post(() -> {
                            if (resource instanceof BitmapDrawable) {
                                Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                if (!bitmap.isRecycled())
                                    chip.setChipIcon(resource);
                                else
                                    loadChipIconPicasso(chip, url);
                            }
                        });
                        return true;
                    }
                }).submit();
    }

    private void loadChipIconPicasso(Chip chip, String url) {
        Picasso.get().load(url).into(new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (!bitmap.isRecycled()) {
                    chip.setChipIcon(new BitmapDrawable(getResources(), bitmap));
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    @Override
    public void setRecyclerViewChildrenCount() {
        recyclerViewChildrenCount.set(mFragmentBlogBinding.blogRecyclerView.getChildCount());
    }

    @Override
    public void handleLogin() {
        try {
            ((TabContainerActivity) getBaseActivity()).launchLogInFragment();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override

    public void onCarouselItemClicked(String id, List<PostObject> results, PostObject post, String title) {
//        HashMap<String, Object> properties = new HashMap<>(baseProperties);
//        properties.put("post_id", id);
//        properties.put("post_click_source", title);
//        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_FEED_POST_CLICKED, properties);
//
        pausePlayer();
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        if (getContext() == null) return;
        StreamPlayerContainerFragment.Builder builder = new StreamPlayerContainerFragment.Builder()
                .addPostList(new ArrayList<>(results))
                .addPost(post)
                .addSourceScreenName(SegmentConstants.SCREEN_NAME_FEED);
        if (post.isLive()) {
            builder.addLoadMore(true)
                    .addGameId(AppConstants.LIVE_GAME_ID);
        }
        StreamPlayerActivity.Companion.startActivity(getContext(), builder.buildExtras());
    }

    @Override
    public void onSuperStreamerCardClick(String id) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("post_id", id);
        properties.put("post_click_source", "home super streamer card");
        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_FEED_POST_CLICKED, properties);

        pausePlayer();
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        if (getContext() == null) return;
        StreamPlayerActivity.Companion.startActivity(getContext(),
                new StreamPlayerContainerFragment.Builder()
                        .addPost(id)
                        .addSourceScreenName(SegmentConstants.SCREEN_NAME_FEED)
                        .buildExtras());
    }


    @Override
    public void onSingleItemInCarousel() {

    }

    @Override
    public void onMoreOptionBtnClicked(String id) {
        onMoreOptionsClick(id);
    }


    private void makeReportPostRequest(String id) {
        mBlogViewModel.reportPost(id);
    }

    @Override
    public void showToast() {
        Toast.makeText(getContext(), getString(R.string.post_report_success), Toast.LENGTH_LONG).show();
    }

    @Override
    public void setEnableClips(boolean enableClips) {
        // ((TabContainerActivity) getActivity()).changeClipsBtnState(enableClips);
    }

    @Override
    public void setEnableGoLive(boolean enableGoLive) {
        // ((TabContainerActivity) getActivity()).changeGoLiveState(enableGoLive);
    }

    @Override
    public void onMultiViewItemClicked(String id, List<PostObject> results) {
        pausePlayer();
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        if (getContext() == null) return;
        StreamPlayerContainerFragment.Builder builder = new StreamPlayerContainerFragment.Builder()
                .addPostList(new ArrayList<>(results))
                .addSourceScreenName(SegmentConstants.SCREEN_NAME_FEED);
        for (PostObject object : results) {
            if (object.getId() != null && object.getId().equalsIgnoreCase(id)) {
                builder.addPost(object);
                break;
            }
        }
//        Intent intent = new Intent(getContext(), StreamPlayerContainerFragment.class);
//        intent.putParcelableArrayListExtra(AppConstants.ARG_POST_LIST, new ArrayList<>(results));
//        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_FEED);
//        startActivity(intent);
//        startActivity(builder.build());
        StreamPlayerActivity.Companion.startActivity(getContext(), builder.buildExtras());
    }

    @Override
    public void onSeeMoreClicked(List<PostObject> result) {
        SeeMoreFragment seeMoreFragment = SeeMoreFragment.newInstance();
        loadFragment(seeMoreFragment, false, false, R.id.frame_container);

    }

    public void loadFragment(@NonNull Fragment fragment, boolean addToBackStack, boolean animate, int container) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(container, fragment);
        transaction.commit();
    }

    public void onLeaderboardClick() {
        Map<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("is_first", CommonUtils.isFirstTimeLeaderBoardClicked());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_LEADERBOARD_PAGE_CLICKED, properties);
        CommonUtils.setFirstTimeLeaderBoardClicked();
        Intent intent = new Intent(getActivity(), LeaderBoardActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_FEED);
        startActivity(intent);
    }

    private void onClipButtonClick() {
//        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CLIPS_CLICKED_FROM_EXPLORE_SECTION, baseProperties);
//        Intent intent = new Intent(getActivity(), ClipsActivity.class);
//        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_FEED);
//        startActivity(intent);
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_MOMENTS_SECTION_CLICKED, new HashMap<>());
        MomentsActivity.Companion.startMe(requireContext());
    }

    @Override
    public void onLeaderboardClicked(String id) {

    }

    @Override
    public void onSeeAllClicked(String game, String id) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("game", game);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_SEE_ALL_CLICKED, properties);

        Intent intent = new Intent(getActivity(), UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, game);
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, id);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_FEED);
        startActivity(intent);
    }

    @Override
    public void onGameClicked(String game, String gameId) {
        if (gameId.equalsIgnoreCase("top_clips")) {
            SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_MOMENTS_SECTION_CLICKED, new HashMap<>());
            MomentsActivity.Companion.startMe(requireContext());
        } else {
            HashMap<String, Object> properties = new HashMap<>(baseProperties);
            properties.put("game", game);
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_GAME_TAG_CLICKED, properties);
            Intent intent = new Intent(getActivity(), UniversalActivity.class);
            intent.putExtra(AppConstants.SEE_ALL_TYPE, game);
            intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, gameId);
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_FEED);
            startActivity(intent);
        }
    }

    @Override
    public void onDeleteVideoClicked(String id, int position) {

    }


    @Override
    public void onDownloadVideoClicked(String id, int position) {

    }

    @Override
    public void onAlertCardClicked() {
        Intent intent = new Intent(getActivity(), AlertInformationActivity.class);
        startActivity(intent);
    }

    @Override
    public void onFollowBtnClicked(String author, int authorId, boolean isFollowed, OnFollowActionCompleteListener listener) {
        Map<String, Object> map = new HashMap<>();
        map.put("is_first", CommonUtils.isFirstTimeFollow());
        map.put("author", author);
        map.put("source", SegmentConstants.SCREEN_NAME_CLIPS);
        map.put("userId", authorId + "");
        map.put("followAction", !isFollowed ? "true" : "false");
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
        CommonUtils.setFirstTimeFollow();
        mBlogViewModel.onFollowClicked(authorId, isFollowed, listener);
    }

    @Override
    public void onProfileViewAction(String authorUsername, OnFollowActionCompleteListener listener) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("author", authorUsername);
        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_TOP_STREAMER_CARD_CLICK, properties);

        Intent intent = ProfileActivity.getCallingIntent(context);
        // intent.putExtra("follow_action_listener", listener);
        intent.putExtra("author_name", authorUsername);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_FEED);
        context.startActivity(intent);
        // ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.frame_container, ProfileContainerFragment.newInstance(authorUsername)).addToBackStack("Author").commit();

    }

    public void onMoreOptionsClick(String id) {
        new AlertDialog.Builder(getContext()).setTitle(getString(R.string.report_this_title)).setMessage(getString(R.string.report_content)).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                makeReportPostRequest(id);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    @Override
    public void onMoreOptionsBtnClick(String id) {
        onMoreOptionsClick(id);
    }

    @Override
    public void onSuperPrimeReminderListener(PostObject result) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("author", result.getAuthor().getUser().getUsername());
        properties.put("post_id", result.getId());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_REMIND_ME_BUTTON_CLICK, properties);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", result.getAuthor().getUser().getUsername() + " will be live in 5 mins");
        intent.putExtra("body", "Watch " + result.getAuthor().getUser().getUsername() + " streaming " + result.getGame() + " live");
        intent.putExtra("image_url", result.getThumbnail());
        intent.putExtra("post_id", result.getId());
        intent.putExtra("target_url", result.getShareUrl());
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, (result.getStartFrom() - (15 * TimeUtils.MILLIS_AN_HOUR)), alarmIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, (result.getStartFrom() - (5 * TimeUtils.MILLIS_IN_A_MIN)), alarmIntent);
        } else {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, (result.getStartFrom() - (5 * TimeUtils.MILLIS_IN_A_MIN)), alarmIntent);
        }
        //  alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +
        //         60 * 1000, alarmIntent);
        Toast.makeText(getContext(), "You will be notified 5 mins before the stream starts", Toast.LENGTH_LONG).show();
    }

    public void smootScrollRVToTop() {
        if (mFragmentBlogBinding != null && mFragmentBlogBinding.blogRecyclerView != null)
            mFragmentBlogBinding.blogRecyclerView.scrollToPosition(0);
    }


    @Override
    public void onStop() {
        //pausePlayer();
        if (mBlogAdapter != null) {
            mBlogAdapter.releasePlayer();
        }
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(playerHeadStateReceiver);
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onStop();
    }

    public void pausePlayer() {
        if (mBlogAdapter != null && mBlogAdapter.mPlayer != null) {
            mBlogAdapter.mPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPlayerChildView && !PlayerHeadServiceHelper.getInstance().isServiceRunning()) {
            //mBlogAdapter.mPlayer.setPlayWhenReady(true);
            mBlogAdapter.updatePlayerView();
        }

        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(playerHeadStateReceiver, new IntentFilter(PLAYER_SERVICE_STOP_BROADCAST));
        }

        if (shouldRefreshStories) {
            mBlogViewModel.loadStoryAuthor(true);
        }
        mBlogViewModel.fetchHomePage(false);
        mBlogViewModel.loadStoryAuthor(true);
        if (getContext() != null && mFragmentBlogBinding.noInternetLayout.getRoot().getVisibility() == View.VISIBLE && NetworkUtils.isNetworkConnected(getContext())) {
            checkInternetAvailability();
            onRetryClick();
        }
    }


    BroadcastReceiver playerHeadStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBlogAdapter != null && isPlayerChildView) {
                //((SimpleExoPlayer) mBlogAdapter.mPlayer).seekToDefaultPosition();
                //mBlogAdapter.mPlayer.setPlayWhenReady(true);
                mBlogAdapter.updatePlayerView();
            }
        }
    };

    private boolean shouldRefreshStories = false;
    BroadcastReceiver loginBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            shouldRefreshStories = true;
        }
    };

    @Override
    public void onDestroy() {
        mBlogAdapter.releasePlayer();
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(loginBroadcast);
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(networkStateReceiver);
        }
        super.onDestroy();
        AppUtilsKt.INSTANCE.runGC();
    }

    @Override
    public void onLoginSuccess() {
        if (getContext() == null || mFragmentBlogBinding == null || mBlogViewModel == null || isStateSaved())
            return;
        mBlogViewModel.loadStoryAuthor(true);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(AppConstants.UPDATE_STORY_BROADCAST_FILTER));
    }

    @Override
    public void onLoginDialogClose() {

    }
}