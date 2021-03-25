package com.rheotv.android.ui.activities.player.activity;

import android.animation.LayoutTransition;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.Observable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.streamUpdates.StreamEventResponse;
import com.rheotv.android.databinding.FragmentStreamPlayerContainerBinding;
import com.rheotv.android.helpers.grpc.ChatHelper;
import com.rheotv.android.services.PlayerHeadHolder;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.player.activity.newPlayer.StreamPlayerAdapterV2;
import com.rheotv.android.ui.activities.player.activity.newPlayer.StreamPlayerFragmentV2;
import com.rheotv.android.ui.activities.player.activity.newPlayer.ViewPagerMediatorV2;
import com.rheotv.android.ui.activities.player.activity.streamplayer.adapter.VideoFilterRecyclerAdapter;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.fragments.VideoAlertDialogFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.recyclerdecorators.HorizontalSpacesItemDecoration;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

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

import eightbitlab.com.blurview.RenderScriptBlur;
import goChat.Services;

public class StreamPlayerContainerFragment extends BaseFragment<FragmentStreamPlayerContainerBinding, StreamPlayerContainerViewModel>
        implements ViewPagerMediator.ViewPagerOnPageSelectedListener, VideoAlertDialogFragment.VideoAlertStayClickListener {
    public static final String TAG = "StreamPlayerContanrFrag";
    public static boolean isInfoVisible = true;
    private HashMap<String, Object> baseProperties;

    @Inject
    StreamPlayerContainerViewModel mViewModel;
    FragmentStreamPlayerContainerBinding mBinding;
    private ViewPagerMediatorV2 mViewPagerMediator;
    private String sourceScreen;
    private boolean mIdFromDeeplink;
    private ChatHelper grpcConnectionHelper;

    boolean shouldLoadMore;
    private boolean isFromPlayerHeadWidget;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private AlertDialog overlayAlertDialog;
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    private PlayerHeadHolder currentPlayerHeadHolder;
    private boolean shouldAskOverlayPermission = true;
    private Runnable customRoomAction;
    private boolean isFirstPage = true;
    private StreamPlayerFragment mCurrentFragment;
    private StreamPlayerFragmentV2 mCurrentFragmentV2;

    private final BroadcastReceiver customRoomListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String postId = intent.getStringExtra(AppConstants.KEY_POST_ID);
            showCustomRoom(postId, intent.getIntExtra(AppConstants.ARG_NOTIFICATION_ID, 0));
        }
    };

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_stream_player_container;
    }

    @Override
    public StreamPlayerContainerViewModel getViewModel() {
        getDataFromIntent(getArguments());
        baseProperties = new HashMap<>();
        baseProperties.put(AppConstants.SCREEN_SOURCE, sourceScreen);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        return mViewModel;
    }

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkConnected() && mViewModel != null && isAdded() && !isRemoving()) {
                checkInternetAvailability();
                mViewModel.fetchVideos();
            }
        }
    };
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent);
//        if (EventBus.getDefault().isRegistered(mViewPagerMediator)) {
//            EventBus.getDefault().unregister(mViewPagerMediator);
//        }
//        mViewPagerMediator.detach();
//        mViewPagerMediator = null;
//        setupViewPager();
//        getDataFromIntent(intent);
//    }

    private void getDataFromIntent(Bundle intent) {
        if (intent != null) {
            // Analytics Source screen
            if ((intent.containsKey(AppConstants.ARG_SHOW_TAG_OPTIONS)))
                mViewModel.showTagOptions = intent.getBoolean(AppConstants.ARG_SHOW_TAG_OPTIONS);

            if (intent.containsKey(AppConstants.SCREEN_SOURCE))
                sourceScreen = intent.getString(AppConstants.SCREEN_SOURCE);

            // Adding a post
            if (intent.containsKey(AppConstants.ARG_POST)) {
                mViewModel.setPost(intent.getParcelable(AppConstants.ARG_POST));
            }

            // Adding next url
            if (intent.containsKey(AppConstants.ARG_NEXT_URL)) {
                mViewModel.setNextUrl(intent.getString(AppConstants.ARG_NEXT_URL));
            }

            // Adding game id
            if (intent.containsKey(AppConstants.ARG_GAME_ID)) {
                mViewModel.setGameId(intent.getString(AppConstants.ARG_GAME_ID));
            }

            // Whether Fetch Live Listing
            if (intent.containsKey(AppConstants.ARG_SHOW_LIVE)) {
                mViewModel.setLive(intent.getBoolean(AppConstants.ARG_SHOW_LIVE, true));
            }

            if (intent.containsKey(AppConstants.ARG_LOAD_MORE)) {
                shouldLoadMore = intent.getBoolean(AppConstants.ARG_LOAD_MORE, true);
            }

            if (intent.containsKey(AppConstants.ARG_POST_POSITION)) {
                mViewModel.currentIndex = intent.getInt(AppConstants.ARG_POST_POSITION, 0);
            }

            if (intent.containsKey(AppConstants.ARG_FROM_DEEPLINK))
                mIdFromDeeplink = intent.getBoolean(AppConstants.ARG_FROM_DEEPLINK, false);

            if (intent.containsKey(AppConstants.ARG_FOR_CUSTOM_ROOM) && intent.getBoolean(AppConstants.ARG_FOR_CUSTOM_ROOM, false) && mViewPagerMediator != null) {
                showCustomRoom(mViewModel.posts.getValue().get(0).getId(), -1);
            }

            // Adding a post list
            if (intent.containsKey(AppConstants.ARG_POST_LIST)) {
                List<PostObject> list;
                if ((list = intent.getParcelableArrayList(AppConstants.ARG_POST_LIST)) != null) {
                    mViewModel.posts.setValue(list);
                    mViewModel.setLive(mViewModel.posts.getValue().get(0).isLive());
                } else {
                    mViewModel.fetchVideos();
                }
            } else {
                mViewModel.fetchVideos();
            }

            mViewModel.getAnalyticsEventsList();
        }
    }

    private Queue<Runnable> actionQueue = new LinkedList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusModel.OpenPostWitId eventBusModel) {
        if (eventBusModel == null) return;
        if (isResumed() && mViewModel != null) {
            shouldLoadMore = eventBusModel.getLoadMore();
            Log.i(TAG, "Inverted Post Id " + eventBusModel.getPostId());
            loadCurrentPost(new PostObject(eventBusModel.getPostId()));
        } else {
            actionQueue.add(() -> onMessageEvent(eventBusModel));
        }
    }

    private void adjustShimmerHeight() {
        if (mBinding == null) return;
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mBinding.loader.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
        }
        layoutParams.bottomMargin = ViewUtils.dpToPx(56);
        mBinding.loader.setLayoutParams(layoutParams);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusModel.ShowTags eventBusModel) {
        if (eventBusModel == null) return;
        mBinding.getRoot().post(() -> {
//            adjustTagRecyclerMargin();
            if (mBinding.tagRecyclerView.getVisibility() == View.VISIBLE)
                mBinding.tagRecyclerView.setVisibility(View.GONE);
            else
                mBinding.tagRecyclerView.setVisibility(View.VISIBLE);
        });
    }

    private void loadCurrentPost(PostObject postObject) {
        mViewModel.setPost(postObject);
        mViewModel.currentIndex = 0;
        postObject.setShowTagOptions(mViewModel.showTagOptions);
        mViewPagerMediator.addForcedPost(postObject);
    }

    public void setFirstPage(boolean firstPage) {
        isFirstPage = firstPage;
    }

    private void showCustomRoom(String Id, int notificationId) {
        Toast.makeText(getContext(), "showCustomRoom", Toast.LENGTH_LONG).show();
        if (mViewPagerMediator == null) return;
        boolean hasItem = mViewPagerMediator.showAction(Id, notificationId);
        if (!hasItem) {
            mViewModel.setPost(new PostObject(Id));
            customRoomAction = () -> mViewPagerMediator.showAction(Id, notificationId);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        if (mBinding.getRoot() instanceof ViewGroup) {
            LayoutTransition layoutTransition = ((ViewGroup) mBinding.getRoot()).getLayoutTransition();
            if (layoutTransition != null) {
                layoutTransition.setAnimateParentHierarchy(false);
            }
        }
        setupViewPager();
        setupView();
        setUpObserverForLiveData();
        checkInternetAvailability();
    }

    private void checkInternetAvailability() {
        if (NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext())) {
            mBinding.viewPager.setVisibility(View.VISIBLE);
            mBinding.offlineMode.setLayoutVisible(false);
        } else {
            mBinding.viewPager.setVisibility(View.GONE);
            mBinding.offlineMode.setLayoutVisible(true);
        }
    }

    private void setupView() {
        if (getActivity() instanceof HomeActivity) {
            adjustShimmerHeight();
        }
        VideoFilterRecyclerAdapter adapter = new VideoFilterRecyclerAdapter();
        mBinding.tagRecyclerView.setVisibility(View.GONE);
        mBinding.tagRecyclerView.addItemDecoration(new HorizontalSpacesItemDecoration(ViewUtils.dpToPx(8)));
        mBinding.tagRecyclerView.setAdapter(adapter);
        if (getActivity() instanceof StreamPlayerActivity) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mBinding.tagRecyclerView.getLayoutParams();
            layoutParams.topMargin = ViewUtils.getStatusBarHeight(getActivity()) + ViewUtils.dpToPx(18);
            mBinding.tagRecyclerView.setLayoutParams(layoutParams);
        }
        adapter.setItemSelectedListener((gameTag) -> {
            // mViewModel.setGameId(gameTag.getTag() != null && !gameTag.getTag().isEmpty() ? gameTag.getTag() : "");
            mViewModel.setPost(null);
            mViewModel.setNextUrl(null);
            mViewModel.setSlug(gameTag.getTag());
            mViewPagerMediator.clearList();
            mViewModel.fetchVideos();
            return null;
        });
        List<VideoFilterRecyclerAdapter.VideoFilter> list =
                new ArrayList<>(VideoFilterRecyclerAdapter.VideoFilter.Companion.getDefault());
        adapter.submitList(list);
        mBinding.offlineMode.getRoot().setElevation(ViewUtils.dpToPx(3));
        mBinding.offlineMode.getRoot().setTranslationZ(ViewUtils.dpToPx(3));
        mBinding.offlineMode.retryButton.setOnClickListener(v -> mViewModel.fetchVideos());
        mBinding.errorText.setOnClickListener(v -> {
            v.setVisibility(View.GONE);
            mViewModel.fetchVideos();
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getActivity() != null)
//            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        StreamPlayerContainerFragment.isInfoVisible = true;
        grpcConnectionHelper = ChatHelper.getInstance(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        if (getContext() != null && mBinding.offlineMode.getRoot().getVisibility() == View.VISIBLE && NetworkUtils.isNetworkConnected(getContext()) && mViewModel != null)
            mViewModel.fetchVideos();
        runPendingTasks();
    }

    private void runPendingTasks() {
        if (!actionQueue.isEmpty()) {
            if (handler == null) handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Runnable runnable = actionQueue.poll();
                    if (runnable != null) {
                        runnable.run();
                    }
                    if (!actionQueue.isEmpty()) {
                        handler.postDelayed(this, 1000);
                    }
                }
            }, 1000);
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onStart() {
        super.onStart();
        try {
            if (!EventBus.getDefault().isRegistered(mViewPagerMediator)) {
                EventBus.getDefault().register(mViewPagerMediator);
            }
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        grpcConnectionHelper.connectToGroup("all", new ChatHelperCallbacks() {
            @Override
            public void onMessageSend(Services.ChatMessage chatMessage) {
                if (chatMessage != null && chatMessage.getSender() != null && !chatMessage.getSender().isEmpty()) {
                    if (AppConstants.MSG_DYNAMIC_PLAYER_ACTIONS.equalsIgnoreCase(chatMessage.getMsgType())) {
                        StreamEventResponse response = gson.fromJson(chatMessage.getMessage(), StreamEventResponse.class);
                        if (AppConstants.MSG_DYNAMIC_FEED.equalsIgnoreCase(response.getType())) {
                            handler.post(() -> {
                                updatePlayerList(response.getText());
                            });
                        }
                    }
                }
            }

            @Override
            public void onMessageDelete(Services.ChatMessage chatMessage) {

            }

            @Override
            public void waitAndReconnect() {

            }

            @Override
            public void updateLiveCount(String liveCount) {

            }

            @Override
            public void setUpViewersRequest() {

            }

            @Override
            public void showToast(String message) {

            }

            @Override
            public void onConnectionComplete() {

            }
        });
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(customRoomListener, new IntentFilter(AppConstants.FILTER_CUSTOM_ROOM));
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private Gson gson = new Gson();

    private void updatePlayerList(String jsonData) {
        try {
            PostObject postObject = gson.fromJson(jsonData, PostObject.class);
            postObject.setShowTagOptions(mViewModel.showTagOptions);
            mViewPagerMediator.updateList(postObject);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(mViewPagerMediator))
            EventBus.getDefault().unregister(mViewPagerMediator);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        grpcConnectionHelper.closeGroupConnection();
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(customRoomListener);
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(networkStateReceiver);
        }
        removeIntroLayout(false);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        grpcConnectionHelper = null;
        AppUtilsKt.INSTANCE.runGC();
    }


    private void setUpObserverForLiveData() {
        mViewModel.posts.observe(getViewLifecycleOwner(), list -> {
            mBinding.tagRecyclerView.setVisibility(View.GONE);
            for (PostObject postObject : list) {
                postObject.setShowTagOptions(mViewModel.showTagOptions);
            }
            if (mViewModel.isRefreshing()) {
                mViewPagerMediator.clearList();
                mViewModel.setRefreshing(false);
            }
            mViewPagerMediator.updateAdapter(list);

            if (customRoomAction != null) {
                customRoomAction.run();
                customRoomAction = null;
            }
            mViewModel.setLoading(false);
        });
        mViewModel.loadingMutableLiveData.observe(getViewLifecycleOwner(), loading -> {
            checkInternetAvailability();
            if (loading)
                mBinding.loader.setVisibility(View.VISIBLE);
            else
                mBinding.loader.setVisibility(View.GONE);
        });
        mViewModel.showError.observe(getViewLifecycleOwner(), showError -> {
            checkInternetAvailability();
            if (showError == null || !isNetworkConnected())
                return;
            if (showError) {
                mBinding.errorText.setVisibility(View.VISIBLE);
            } else {
                mBinding.errorText.setVisibility(View.GONE);
            }
            mViewModel.setLoading(false);
        });
        mViewModel.retryRequestTime.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mViewModel.retryRequestTime.get() != null && mViewModel.retryRequestTime.get() < 10) {
                    fireFetchDataRequest(mViewModel.retryRequestTime.get());
                }
            }
        });
    }

    private void fireFetchDataRequest(int retryCount) {
        Log.i("Firing_request_in", (long) (Math.pow(2, retryCount) * 1000) + " ms");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtils.isNetworkConnected(getContext()))
                    mViewModel.fetchVideos();
            }
        }, (long) (Math.pow(2, retryCount) * 1000));
    }

    private void setUpTransparentToolbar(int orientation) {
//        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        requireActivity().getWindow().setNavigationBarColor(Color.TRANSPARENT);
//        requireActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            View decorView = requireActivity().getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
//            View decorView = requireActivity().getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    // Set the content to appear under the system bars so that the
//                    // content doesn't resize when the system bars hide and show.
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            // Hide the nav bar and status bar
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//            requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUpTransparentToolbar(newConfig.orientation);

    }

    private void setupViewPager() {
        Log.i(TAG, "stream_activity: setupViewPager");
        StreamPlayerAdapterV2 adapter = new StreamPlayerAdapterV2(getChildFragmentManager(), getLifecycle(), new ArrayList<>(), sourceScreen);
        if (mViewPagerMediator == null) {
            mViewPagerMediator = new ViewPagerMediatorV2(mBinding.viewPager, adapter, mViewModel.currentIndex, this);
            mViewPagerMediator.attach();
            AppUtilsKt.INSTANCE.manageViewPagerDrag(mBinding.viewPager);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCurrentFragment(EventBusModel.UpdateStreamFragment eventBusModel) {
        mCurrentFragmentV2 = eventBusModel.getStreamPlayerFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void loadIntroVideoAndGameRules(EventBusModel.LoadIntroAndGameRules eventBusModel) {
        String introVideoUrl = eventBusModel.getIntroVideoUrl();
        String gameVideoUrl = eventBusModel.getGameRulesVideoUrl();
        String gameName = eventBusModel.getGameName();
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("intro_video_url", introVideoUrl);
        properties.put("game_rules_video_url", gameVideoUrl);
        properties.put("game_name", gameName);
        properties.put("author_name", eventBusModel.getAuthorName());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_INTRO_VIDEO_SHOWN, properties);
        //Toast.makeText(getContext(), introVideoUrl + ":" + gameVideoUrl, Toast.LENGTH_LONG).show();
        if ((introVideoUrl != null && !introVideoUrl.isEmpty()) || (gameVideoUrl != null && gameVideoUrl.isEmpty())) {
            mBinding.introLayout.setVisibility(View.VISIBLE);

            blurView();
            if (videoPlayer == null) {
                setupPlayer();
            }
            mBinding.skipBtn.setOnClickListener(v -> {
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_INTRO_VIDEO_SKIP_CLICKED, properties);
                removeIntroLayout(true);
            });
            if (introVideoUrl != null && !introVideoUrl.isEmpty()) {
                buildPlayer(introVideoUrl);
                if (gameVideoUrl != null && !gameVideoUrl.isEmpty()) {
                    mBinding.gameRulesBtn.setVisibility(View.VISIBLE);
                    mBinding.gameRulesBtn.setText("❓How to play " + gameName);
                    mBinding.gameRulesBtn.setOnClickListener(v -> {
                        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_HOW_TO_PLAY_CLICKED, properties);
                        buildPlayer(gameVideoUrl);
                    });
                } else {
                    mBinding.gameRulesBtn.setVisibility(View.GONE);
                }
            } else {
                mBinding.gameRulesBtn.setVisibility(View.GONE);
                buildPlayer(gameVideoUrl);
            }

        } else {
            mBinding.introLayout.setVisibility(View.GONE);
        }
    }

    private void removeIntroLayout(boolean startPlayer) {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.release();
            videoPlayer = null;
        }
        if (playerView != null) {
            mBinding.playerContainer.removeView(playerView);
            playerView = null;
        }
        if (startPlayer)
            mCurrentFragmentV2.addPlayerAndStartVideo();
        mBinding.introLayout.setVisibility(View.GONE);
    }

    public StreamPlayerFragmentV2 getPlayerFragment() {
        return mCurrentFragmentV2;
    }

    public Map<String, Object> getBaseProperties() {
        return baseProperties != null ? baseProperties : new HashMap<>();
    }

//    @Override
//    public void onBackPressed() {
//        if (mBinding.viewPager.getAdapter() instanceof StreamPlayerAdapter) {
//            StreamPlayerAdapter adapter = (StreamPlayerAdapter) mBinding.viewPager.getAdapter();
//            StreamPlayerFragment streamPlayerFragment = adapter.getItem(mBinding.viewPager.getCurrentItem());
//            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                return;
//            }
//
//            if (shouldAskOverlayPermission) {
//                askOverlayPermissionAndReward(streamPlayerFragment);
//                return;
//            }
//        }
//
//        checkAndFinish();
//    }

    private void onNewIntentTask() {
//        startActivity(new Builder(requireActivity()).addPost("4604fa0d-0b72-4c5c-a19c-5da68eaa8106").build());
    }

//    private void checkAndFinish() {
//        try {
//            if (mIdFromDeeplink) {
//                startTabContainerActivity();
//            } else {
//                onBackPressed();
//            }
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
//    }

//    private void startTabContainerActivity() {
//        Intent intent = TabContainerActivity.newIntent(requireActivity());
//        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        startActivity(intent);
//    }

    private void showRewardAlert(StreamPlayerFragment streamPlayerFragment) {
        VideoAlertDialogFragment videoAlertDialogFragment;
        if (getChildFragmentManager().findFragmentByTag(AppConstants.ALERT_VIDEO_REWARD_TAG) != null)
            videoAlertDialogFragment = (VideoAlertDialogFragment) getChildFragmentManager().findFragmentByTag(AppConstants.ALERT_VIDEO_REWARD_TAG);
        else
            videoAlertDialogFragment = VideoAlertDialogFragment.newInstance(streamPlayerFragment.TIME_UNTIL_FINISH, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        if (videoAlertDialogFragment != null && (videoAlertDialogFragment.isAdded() || videoAlertDialogFragment.isVisible()))
            return;
        if (videoAlertDialogFragment == null) return;
        videoAlertDialogFragment.show(this.getChildFragmentManager(), AppConstants.ALERT_VIDEO_REWARD_TAG, streamPlayerFragment.TIME_UNTIL_FINISH);
        streamPlayerFragment.shouldShowTenMinuteAlert = false;
    }

    @Override
    public void onVideoAlertStayClicked() {

    }

    @Override
    public void onVideoAlertExitClicked() {
//        onBackPressed();
    }

//    private void askOverlayPermissionAndReward(StreamPlayerFragment streamPlayerFragment) {
//        if (streamPlayerFragment == null) return;
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(requireActivity())) {
//                long lastShownTimeDiff = (System.currentTimeMillis() - sharedPrefsUtils.getLongPreference(requireActivity(), SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, 0));
//                if (lastShownTimeDiff >= TimeUtils.MILLIS_IN_DAY) {
//                    sharedPrefsUtils.setLongPreference(requireActivity(), SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, System.currentTimeMillis());
//                    showPermissionRequiredDialog(streamPlayerFragment.getPlayerHolder());
//                } else if (CommonUtils.isUserLoggedin() && RewardManager.getInstance().isTenMinuteStreamRewardAvailable() && (streamPlayerFragment != null && streamPlayerFragment.shouldShowTenMinuteAlert)) {
//                    showRewardAlert(streamPlayerFragment);
//                } else {
//                    checkAndFinish();
//                }
//            } else {
//                startPlayerService(streamPlayerFragment.getPlayerHolder());
//                if (isFromPlayerHeadWidget && !((RheoTvApp) getApplication()).isActivityInBackStack(TabContainerActivity.class)) {
//                    startTabContainerActivity();
//                }
//                checkAndFinish();
//            }
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
//    }

//    private void showPermissionRequiredDialog(PlayerHeadHolder holder) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.AlertDialogStyle);
//        builder.setCancelable(true);
//        View dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.overlay_permission_dialog_layout, null);
//
//        dialogView.findViewById(R.id.allow_action).setOnClickListener(view -> {
//            try {
//                currentPlayerHeadHolder = holder;
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + requireActivity().getPackageName()));
//                startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
//
//                SegmentTracker.getInstance(requireActivity()).trackEvent(SegmentConstants.EVENT_OVERLAY_PERMISSION_GO_TO_SETTINGS_CLICK, baseProperties);
//
//                if (overlayAlertDialog != null) overlayAlertDialog.dismiss();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//        dialogView.findViewById(R.id.cancel_action).setOnClickListener(view -> {
//            SegmentTracker.getInstance(requireActivity()).trackEvent(SegmentConstants.EVENT_OVERLAY_PERMISSION_CANCEL_CLICK, baseProperties);
//            if (overlayAlertDialog != null) overlayAlertDialog.dismiss();
////            checkAndFinish();
//        });
//        builder.setView(dialogView);
//        overlayAlertDialog = builder.show();
//        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_OVERLAY_PERMISSION_ALERT_DIALOG, baseProperties);
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(getClass().getName(), "onActivityResult_player_called");
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            //Check if the permission is granted or not.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (Settings.canDrawOverlays(requireActivity())) {
//                    if (currentPlayerHeadHolder != null) {
//                        startPlayerService(currentPlayerHeadHolder);
//                        checkAndFinish();
//                    }
//                } else {
//                    Toast.makeText(this,
//                            "Draw over other app permission not available. Closing the application",
//                            Toast.LENGTH_SHORT).show();
//                }
            }

        }
    }

    private void startPlayerService(PlayerHeadHolder holder) {
        Log.i(getClass().getName(), "startPlayerService_called " + (holder == null));
        if (holder == null || holder.getPost().getVideoUrl() == null || holder.getPost().getVideoUrl().isEmpty())
            return;
        holder.setFromDeeplink(mIdFromDeeplink);
        PlayerHeadServiceHelper.getInstance().startPlayerHeadService(holder);
    }

    @Override
    public void onPageSelected() {
        Log.i(TAG, "load_more_post: " + (mBinding.viewPager.getCurrentItem() >= mBinding.viewPager.getAdapter().getItemCount() - 4) + " and " + shouldLoadMore);
        if (mBinding.viewPager.getCurrentItem() >= mBinding.viewPager.getAdapter().getItemCount() - 4 && shouldLoadMore) {
            mViewModel.fetchVideos();
        }
    }

    @Override
    public void onPageUnselected() {
        isFirstPage = false;
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public void sendUpdateCustomRoomFragmentMessageInChat() {
        if (mBinding.viewPager.getAdapter() instanceof StreamPlayerAdapter) {
            StreamPlayerAdapter adapter = (StreamPlayerAdapter) mBinding.viewPager.getAdapter();
            StreamPlayerFragment streamPlayerFragment = adapter.getItem(mBinding.viewPager.getCurrentItem());
            if (streamPlayerFragment == null) return;
            streamPlayerFragment.sendRefreshCustomRoomMessage();
        }
    }

    public void refreshPage() {
//        Toast.makeText(requireContext(), "Refreshing Player Page", Toast.LENGTH_SHORT).show();
        try {
            if (mViewModel == null || isStateSaved() || !isAdded()) return;
            if (mBinding.tagRecyclerView.getAdapter() != null) {
                mBinding.tagRecyclerView.smoothScrollToPosition(0);
                ((VideoFilterRecyclerAdapter) mBinding.tagRecyclerView.getAdapter()).resetFilterSelection();
            }
            mViewModel.setRefreshing(true);
            mViewModel.setPost(null);
            mViewModel.setNextUrl(null);
            mViewModel.setSlug(AppConstants.LIVE_GAME_ID);
            mViewModel.fetchVideos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Builder {
        private Context mContext;
        private ArrayList<PostObject> mPostList = null;
        private int mPostIndex = -1;
        private String mPaginationUrl = null;
        private String mSourceName = null;
        private boolean mLoadMore = true;
        private String mGameId = null;
        private boolean mIsFromDeepLink = false;
        private boolean isForCustomRoom = false;
        private boolean shouldShowTagOptions = false;

        public Builder addPostList(ArrayList<PostObject> postList) {
            if (mPostList != null && !mPostList.isEmpty()) {
                while (mPostList.iterator().hasNext()) {
                    PostObject item = mPostList.iterator().next();
                    for (PostObject newItem : postList) {
                        if (item.getId() != null && item.getId().equalsIgnoreCase(newItem.getId())) {
                            mPostList.remove(item);
                        }
                    }
                }
                mPostList.addAll(postList);
                return this;
            }
            mPostList = postList;
            return this;
        }

        public Builder addPost(PostObject postObject) {
            if (mPostList == null) {
                mPostList = new ArrayList<>();
            }
            if (mPostList.indexOf(postObject) < 0) {
                mPostList.add(postObject);
            }
            mPostIndex = mPostList.indexOf(postObject);
            return this;
        }

        public Builder addPost(String postId) {
            if (postId == null || postId.isEmpty()) {
                return this;
            }
            addPost(new PostObject(postId));
            return this;
        }

        public Builder addPost(String postId, long durationTs) {
            if (postId == null || postId.isEmpty()) {
                return this;
            }
            PostObject obj = new PostObject(postId);
            obj.setResumePosition(durationTs);
            addPost(obj);
            return this;
        }

        public Builder addPaginationUrl(String paginationUrl) {
            mPaginationUrl = paginationUrl;
            if (mPaginationUrl != null) {
                mLoadMore = true;
            }
            return this;
        }

        public Builder addSourceScreenName(String sourceName) {
            mSourceName = sourceName;
            return this;
        }

        public Builder addLoadMore(boolean loadMore) {
            mLoadMore = loadMore;
            return this;
        }

        public Builder addGameId(String gameId) {
            mGameId = gameId;
            return this;
        }


        public Builder addFromDeepLink(boolean isFromDeepLink) {
            mIsFromDeepLink = isFromDeepLink;
            return this;
        }

        public Builder setForCustomRoom(boolean isForCustomRoom) {
            this.isForCustomRoom = isForCustomRoom;
            return this;
        }

        public Builder setShowTagOptions(boolean shouldShowTagOptions) {
            this.shouldShowTagOptions = shouldShowTagOptions;
            return this;
        }

        public Bundle buildExtras() {
            Bundle intent = new Bundle();
            if (mPostList != null && !mPostList.isEmpty()) {
                intent.putParcelableArrayList(AppConstants.ARG_POST_LIST, mPostList);
            }
            if (mPostIndex >= 0) {
                intent.putInt(AppConstants.ARG_POST_POSITION, mPostIndex);
            }
            if (mPaginationUrl != null && !mPaginationUrl.isEmpty()) {
                intent.putString(AppConstants.ARG_NEXT_URL, mPaginationUrl);
            }
            if (mSourceName != null) {
                intent.putString(AppConstants.SCREEN_SOURCE, mSourceName);
            }
            if (mGameId != null) {
                intent.putString(AppConstants.ARG_GAME_ID, mGameId);
            }
            intent.putBoolean(AppConstants.ARG_FOR_CUSTOM_ROOM, isForCustomRoom);
            intent.putBoolean(AppConstants.ARG_LOAD_MORE, mLoadMore);
            intent.putBoolean(AppConstants.ARG_FROM_DEEPLINK, mIsFromDeepLink);
            intent.putBoolean(AppConstants.ARG_SHOW_TAG_OPTIONS, shouldShowTagOptions);
            return intent;
        }

        public StreamPlayerContainerFragment build() {
            StreamPlayerContainerFragment streamPlayerContainerFragment = new StreamPlayerContainerFragment();
            streamPlayerContainerFragment.setArguments(buildExtras());
            return streamPlayerContainerFragment;
        }
    }

    public static void startActivity(Context context, PostObject post, ArrayList<PostObject> postList, String nextUrl, String sourceScreen) {
        if (context == null) throw new IllegalStateException("context cannot be null.");
        Intent intent = new Intent(context, StreamPlayerContainerFragment.class);
        if (postList == null) {
            postList = new ArrayList<>();
            postList.add(post);
        } else {
            if (postList.isEmpty() || !postList.contains(post)) {
                postList.add(post);
            }
        }
        intent.putParcelableArrayListExtra(AppConstants.ARG_POST_LIST, postList);
        intent.putExtra(AppConstants.ARG_POST_POSITION, postList.indexOf(post));
        if (nextUrl != null && !nextUrl.isEmpty()) {
            intent.putExtra(AppConstants.ARG_NEXT_URL, nextUrl);
        }

        if (sourceScreen != null)
            intent.putExtra(AppConstants.SCREEN_SOURCE, sourceScreen);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    public void changeViewPagerRotation() {
        mBinding.viewPager.setRotationY(180);
    }

    public static Intent getNotificationIntent(int notificationId, String postId) {
        Intent intent = new Intent(AppConstants.FILTER_CUSTOM_ROOM);
        intent.putExtra(AppConstants.ARG_NOTIFICATION_ID, notificationId);
        intent.putExtra(AppConstants.KEY_POST_ID, postId);
        return intent;
    }

    SimpleExoPlayer videoPlayer;
    PlayerView playerView;
    boolean durationSet;

    public void setupPlayer() {

        playerView = new PlayerView(getContext());
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
        /* This is 50000 milliseconds in ExoPlayer 2.9.6 */
        final int loadControlBufferMs = 15000;

        /* Configure the DefaultLoadControl to use the same value for */
        builder.setBufferDurationsMs(loadControlBufferMs,
                loadControlBufferMs,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
        DefaultLoadControl loadControl = builder.createDefaultLoadControl();

        //Create the player using ExoPlayerFactory

        videoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(),
                new DefaultRenderersFactory(getContext()), trackSelector, loadControl);

        // Disable Player Control
        playerView.setUseController(false);
        // Bind the player to the view.
        playerView.setPlayer(videoPlayer);
        // Turn on Volume

        videoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups,
                                        TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        mBinding.progressBar.setVisibility(View.VISIBLE);
                        Log.e(TAG, "onPlayerStateChanged: Buffering video.");
                        break;
                    case Player.STATE_ENDED:
                        mBinding.progressBar.setVisibility(View.GONE);
                        removeIntroLayout(true);
                        break;
                    case Player.STATE_IDLE:

                        break;
                    case Player.STATE_READY:
                        if (!durationSet) {
                            startTimeForDuration(videoPlayer.getDuration());
                        }
                        mBinding.progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "onPlayerStateChanged: Ready to play.");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mBinding.playerContainer.addView(playerView, lp);
    }

    CountDownTimer timer;

    private void startTimeForDuration(long durationSet) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(durationSet, 1000) {

            public void onTick(long millisUntilFinished) {
                mBinding.duration.setText(millisUntilFinished / 1000 + "");
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                mBinding.duration.setVisibility(View.GONE);
            }

        }.start();

    }

    public void buildPlayer(String mediaUrl) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                getContext(), Util.getUserAgent(getContext(), "Rheo"));

        if (mediaUrl != null) {
            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(mediaUrl));
            if (videoPlayer != null) {
                durationSet = false;
                videoPlayer.prepare(videoSource);
                videoPlayer.setPlayWhenReady(true);
            }
        }
    }

    private void blurView() {
        try {
            float radius = 20f;
            View decorView = getActivity().getWindow().getDecorView();
            ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
            Drawable windowBackground = decorView.getBackground();
            mBinding.blurView.setupWith(rootView)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                    .setBlurRadius(radius)
                    .setBlurAutoUpdate(true)
                    .setHasFixedTransformationMatrix(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}