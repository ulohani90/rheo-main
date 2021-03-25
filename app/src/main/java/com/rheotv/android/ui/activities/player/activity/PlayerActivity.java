

package com.rheotv.android.ui.activities.player.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.github.vkay94.dtpv.SeekListener;
import com.github.vkay94.dtpv.youtube.YouTubeOverlay;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.rheotv.android.BR;
import com.rheotv.android.BuildConfig;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.TimerObj;
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.score.ScoreboardResponse;
import com.rheotv.android.data.network.models.stickers.Sticker;
import com.rheotv.android.databinding.ActivityPlayerV2Binding;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.helpers.grpc.ChatHelper;
import com.rheotv.android.ui.activities.gamify.RewardsTabAdapter;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.scoreboard.ScoreFragment;
import com.rheotv.android.ui.activities.streamEnd.StreamEndActivity;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.ChatListAdapter;
import com.rheotv.android.ui.adapters.PlayerListAdapter;
import com.rheotv.android.ui.adapters.ScorecardAdapter;
import com.rheotv.android.ui.adapters.StickersRvAdapter;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.ui.customViews.OnSwipeActionListener;
import com.rheotv.android.ui.customViews.Tooltip.SimpleTooltip;
import com.rheotv.android.ui.customViews.bottomSheetMenu.BottomSheetMenuDialog;
import com.rheotv.android.ui.customViews.bottomSheetMenu.Option;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.ui.fragments.ScratchCardNavigator;
import com.rheotv.android.ui.fragments.ScratchDialogFragment;
import com.rheotv.android.ui.fragments.VideoAlertDialogFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.MojoTimer;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.TimeUtils;
import com.rheotv.android.utils.ViewAnimationUtils;
import com.rheotv.android.utils.hourglass.Hourglass;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import goChat.Services;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON;
import static com.rheotv.android.app.RheoTvApp.getNonUiContext;
import static com.rheotv.android.utils.AppConstants.ARG_GLOBAL_VIDEO_REWARD_TIME;
import static com.rheotv.android.utils.AppConstants.MSG_SCORE;
import static com.rheotv.android.utils.AppConstants.REWARD_TYPE_FIRST_COMMENT;
import static com.rheotv.android.utils.AppConstants.REWARD_TYPE_TEN_MINUTE_STREAM;

public class PlayerActivity extends BaseActivity<ActivityPlayerV2Binding, PlayerViewModel>
        implements HasAndroidInjector, PlayerNavigator, PlayerListAdapter.PlayerAdapterListener,
        ChatListAdapter.ChatItemClickListener, StickersRvAdapter.OnStickersClickListener,
        ScratchCardNavigator,
        ChatListFragment.OnChatListFragmentInteractionListener,
        RequestPlayFragment.OnRequestToPlayFragmentInteractionListener,
        VideoRewardFragment.OnVideoRewardFragmentInteractionListener,
        VideoAlertDialogFragment.VideoAlertStayClickListener, LoginFragmentBottomDialog.LoginFragmentCallback {//implements HasAndroidInjector {

    @Inject
    PlayerViewModel playerViewModel;

    @Inject
    PlayerListAdapter playerListAdapter;

    @Inject
    ScorecardAdapter scorecardAdapter;

    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;

    LinearLayoutManager mLayoutManager;
    private boolean isBottom = true;

    SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    private boolean playWhenReady = true;
    private List timeSentRecord = new ArrayList();
    private int currentWindow = 0;
    private long playbackPosition = 0l;
    private MojoTimer timer;
    private TimerTask timerTask;
    private ActivityPlayerV2Binding activityPlayerBinding;
    private String source;
    private String category;
    private ExoPlayer player;
    private String itemId;
    private String topSource;
    private String topSourceHomeCardType;
    private String topSourceHomeDynamicTab;
    private Result currentPlayingPost;
    private HashMap<String, String> extraSharedLinkParam = new HashMap<>();
    private LoginFragmentBottomDialog loginDialogFragment;
    private boolean isFirstTime = true;
    private String qualityFormat = "";
    private int mScratchCardBottomMargin = AppConstants.PORTRAIT_SCRATCH_CARD_BOTTOM_MARGIN;

    private boolean isSafeChatAdded = false;
    private String pageSource;

    private boolean isExitBtnClicked = false;

    private boolean isLoading = false;

    private String TAG = PlayerActivity.class.getCanonicalName();

    boolean isInitialChatProcessed = false;

    private HashMap<String, Object> baseProperties = new HashMap<>();

    ScratchDialogFragment scratchDialogFragment;

    long timeElapsed = 0;

    long timeElapsedOnWidget = 0;

    long videoBufferingStartTime = 0;

    long videoBufferingTime = 0;

    boolean isVideoPlayingStarted;
    private int mSelectedTab = 0;

    private boolean isInitialStrikerSent = false;


    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    private final String ARG_PLAYBACK_POSITION = "arg_playback_position";
    private final String ARG_SELECTED_TAB = "arg_selected_tab";
    private final String ARG_IS_TWO_SECOND_COUNTED = "arg_is_two_second_counted";
    private final String ARG_TIME_UNTIL_FINISH = "arg_time_until_finish";
    private final String ARG_SHOULD_SHOW_SCRATCH = "arg_should_show_scratch";

    private boolean isFilling = true;
    private String[] heartEmojiText = {"Kya baat hai", "Maza aa gya yaar", "Ye Mast tha", "Epic yaar", "Just Amazing", "Superb"};
    float stickersLayoutHeight;
    boolean isFromPlayerHeadWidget;

    private ChatListFragment chatListFragment;
    private ChatListFragment chatListFragmentLand;
    private VideoRewardFragment videoRewardFragment;
    private RequestPlayFragment requestPlayFragment;
    private VideoAlertDialogFragment videoAlertDialogFragment;

    private String postId;

    private TextView liveViewersCountText;

    Handler hideOverlayHandler = new Handler();
    private boolean isTwoSecondViewCounted;

    boolean landscapePlayerMinClicked;
    // boolean isControllerHidden = false;

    private long resumePosition;

    private int resumeWindow;

    boolean inErrorState;

    private boolean shouldShowScoreCard = false;

    private Handler endStreamHandler = new Handler();
    private Runnable streamEndRunnable = new Runnable() {
        @Override
        public void run() {
            playerViewModel.checkStreamEnded();
        }
    };

    public static Intent newIntent(Context context, String source) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, source);
        return intent;
    }

    boolean isPlayerDraggedBehind;
    private boolean isFromDeepLink = false;

    public static void setInitialPost(Object o) {

    }

    @Override
    public Result getCurrentPlayingPost() {
        return currentPlayingPost;
    }


    private boolean updateView() {
        if (currentPlayingPost != null) {
            // if(currentPlayingPost.getStartDate())
            if (TimeUtils.hasStreamNotStarted(currentPlayingPost.getStartFrom())) {
                activityPlayerBinding.playerLayout.setVisibility(View.GONE);
                activityPlayerBinding.futureStreamLayout.setVisibility(View.VISIBLE);
                BindingUtils.setImageUrlUsingCache(activityPlayerBinding.coverPic, currentPlayingPost.getCarouselThumbnail(), true);
                setCountdownForStream();
                return true;
            } else {
                activityPlayerBinding.playerLayout.setVisibility(View.VISIBLE);
                activityPlayerBinding.futureStreamLayout.setVisibility(View.GONE);
                return false;
            }

        }
        return false;
    }

    CountDownTimer countDownTimer;

    private void setCountdownForStream() {
        final long[] timeLeft = {currentPlayingPost.getStartFrom() - System.currentTimeMillis()};
        countDownTimer = new CountDownTimer(timeLeft[0], 1000) {

            public void onTick(long millisUntilFinished) {
                List<TimerObj> objs = TimeUtils.getTimerObjsList(timeLeft[0] / 1000);
                setCountDownData(objs);
                timeLeft[0] -= 1000;
            }

            public void onFinish() {
                //startsIn.set(mContext.getString(R.string.view_leaderboard));
                updateView();
                initializePlayer(false);
            }

        }.start();
    }

    private void setCountDownData(List<TimerObj> objs) {
        if (objs.size() >= 1) {
            activityPlayerBinding.layout1.setVisibility(View.VISIBLE);
            activityPlayerBinding.layout1Value.setText(objs.get(0).getValue());
            activityPlayerBinding.layout1Label.setText(objs.get(0).getType());

            if (objs.size() >= 2) {
                activityPlayerBinding.layout2.setVisibility(View.VISIBLE);
                activityPlayerBinding.layout2Value.setText(objs.get(1).getValue());
                activityPlayerBinding.layout2Label.setText(objs.get(1).getType());
                activityPlayerBinding.separator12.setVisibility(View.VISIBLE);

                if (objs.size() >= 3) {
                    activityPlayerBinding.layout3.setVisibility(View.VISIBLE);
                    activityPlayerBinding.layout3Value.setText(objs.get(2).getValue());
                    activityPlayerBinding.layout3Label.setText(objs.get(2).getType());
                    activityPlayerBinding.separator23.setVisibility(View.VISIBLE);

                    if (objs.size() >= 4) {
                        activityPlayerBinding.layout4.setVisibility(View.VISIBLE);
                        activityPlayerBinding.layout4Value.setText(objs.get(3).getValue());
                        activityPlayerBinding.layout4Label.setText(objs.get(3).getType());
                        activityPlayerBinding.separator34.setVisibility(View.VISIBLE);

                    } else {
                        activityPlayerBinding.layout4.setVisibility(View.GONE);

                    }

                } else {
                    activityPlayerBinding.layout3.setVisibility(View.GONE);
                    activityPlayerBinding.layout4.setVisibility(View.GONE);

                }

            } else {
                activityPlayerBinding.layout2.setVisibility(View.GONE);
                activityPlayerBinding.layout3.setVisibility(View.GONE);
                activityPlayerBinding.layout4.setVisibility(View.GONE);

            }

        } else {
            activityPlayerBinding.layout1.setVisibility(View.GONE);
            activityPlayerBinding.layout2.setVisibility(View.GONE);
            activityPlayerBinding.layout3.setVisibility(View.GONE);
            activityPlayerBinding.layout4.setVisibility(View.GONE);
        }
    }


    private void restartActivity() {
        Intent intent = getIntent();
        startActivity(intent);
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_player_v2;
    }

    @Override
    public PlayerViewModel getViewModel() {
        return playerViewModel;
    }

    Handler handler;

    int stickersSize;

    long currentState = 0;
    private boolean shouldShowTenMinuteAlert = false;
    private boolean rewardTimerComplete = false;

    ImageView showHideChatBtn;

    View streamerInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            makeStatusBar();
//            stickersSize = (getResources().getDisplayMetrics().widthPixels - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 3;
//        } else {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            stickersSize = (getResources().getDisplayMetrics().widthPixels - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 5;
//        }

        updateConfig();
        activityPlayerBinding = getViewDataBinding();
        loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        loginDialogFragment.setmCallback(this);
        handler = new Handler();

        String requestedVideoFormat = sharedPrefsUtils.getStringPreference(this, SharedPrefsUtils.VIDEO_FORMAT_REQUESTED);
        if (requestedVideoFormat != null) {
            qualityFormat = requestedVideoFormat;
            setCheckId();
        }
        if (savedInstanceState != null) {
            currentState = savedInstanceState.getLong("current_state");
            qualityFormat = savedInstanceState.getString("quality_format");
            resumePosition = savedInstanceState.getLong(ARG_PLAYBACK_POSITION);
            mSelectedTab = savedInstanceState.getInt(ARG_SELECTED_TAB);
            isTwoSecondViewCounted = savedInstanceState.getBoolean(ARG_IS_TWO_SECOND_COUNTED, false);
            TIME_UNTIL_FINISH = savedInstanceState.getLong(ARG_TIME_UNTIL_FINISH);
            shouldShowTenMinuteAlert = savedInstanceState.getBoolean(ARG_SHOULD_SHOW_SCRATCH);
        } else {
            activateExitAlert();
        }

        if (playerHeadService != null) {
            stopService(playerHeadService);
        }
        playerListAdapter.setListener(this);
        playerViewModel.setPlayerNavigator(this);


        playerViewModel.isStreamEnded.observe(this, flag -> {
            if (flag)
                streamEnded();
            else
                endStreamHandler.postDelayed(streamEndRunnable, 10000);
        });

        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getIntent().getStringExtra(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);

        try {
            if (getIntent() != null) {
                if (getIntent().hasExtra(AppConstants.ARG_TITLE))
                    // activityPlayerBinding.shimmerLayout.videoTitleTvPlaceholder.setText(getIntent().getStringExtra(AppConstants.ARG_TITLE));
                    if (getIntent().hasExtra(AppConstants.ARG_THUMBNAIL))
                        BindingUtils.setImageUrlUsingCache(activityPlayerBinding.placeholderThumbnail, getIntent().getStringExtra(AppConstants.ARG_THUMBNAIL), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*if (activityPlayerBinding.stickerIcon != null) {
            activityPlayerBinding.stickerIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    animateStickersRvIn();
                }
            });
        }*/

//        RewardManager.getInstance().setRecentlyRewarded(true);
        //TODO  new Handler().postDelayed(this::updateContainVisibility, 5000);

//        broadCast();

//        setRewards();
    }

    private void openScoreActivity() {
        SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_SCOREBOARD_CARD_CLICKED, baseProperties);
        Intent intent = new Intent(this, ScoreFragment.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        intent.putExtra(AppConstants.KEY_POST_ID, currentPlayingPost.getId());
        intent.putExtra(AppConstants.ARG_SCORECARD_TEAMS, playerViewModel.scoreboardResponse);
        startActivity(intent);
    }

    private void updateConfig() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mScratchCardBottomMargin = AppConstants.PORTRAIT_SCRATCH_CARD_BOTTOM_MARGIN;
            makeStatusBar();
            stickersSize = (getResources().getDisplayMetrics().widthPixels - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 3;
        } else {
            mScratchCardBottomMargin = AppConstants.LANDSCAPE_SCRATCH_CARD_BOTTOM_MARGIN;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            stickersSize = (getResources().getDisplayMetrics().widthPixels - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 5;
        }
    }

    private void streamEnded() {
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_STREAM_ENDED, baseProperties);
        Intent intent = new Intent(this, StreamEndActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        startActivity(intent);
        finish();
    }

    private void makeStatusBar() {
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        } else {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        }
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        //setMarginTop(activityPlayerBinding.videoView);
    }

    private void setMarginTop(View view) {
        try {
            ViewGroup.MarginLayoutParams menuLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            menuLayoutParams.setMargins(0, CommonUtils.toPix(24), 0, 0);
            view.setLayoutParams(menuLayoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCheckId() {
        switch (qualityFormat) {
            case "auto":
                checkId = R.id.action_auto;
                break;
            case "high":
                checkId = R.id.action_high;
                break;
            case "medium":
                checkId = R.id.action_medium;
                break;
            case "low":
                checkId = R.id.action_low;
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        isVisible = true;
        fetchSenderSource();
        startFresh();

        /*Log.i(getClass().getName(), "in_onStart: " + (currentPlayingPost == null) + " and " + streamTimer.isRunning() + " and " + streamTimer.isPaused());
        if (currentPlayingPost != null && !isConnectionRequestMade) {
            isConnectionRequestMade = true;
            ChatHelper.getInstance(this).setPostChatJoinTask(this, currentPlayingPost);
        } else {
            isConnectionRequestMade = false;
        }*/
    }


    @Override
    public void onResume() {
        super.onResume();
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
    }

    /*
    Fetch Post id and other details from the bundle
     */
    private void fetchSenderSource() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            postId = bundle.getString(AppConstants.KEY_POST_ID);
            if (bundle.containsKey(AppConstants.ARG_FROM_DEEPLINK))
                isFromDeepLink = bundle.getBoolean(AppConstants.ARG_FROM_DEEPLINK);

            if (postId == null) {
                Toast.makeText(getNonUiContext(), "Unable to Play this Video.", Toast.LENGTH_SHORT).show();
                finishCheck();
            }
            Log.i(PlayerActivity.class.getCanonicalName(), "postId: " + postId);
            landscapePlayerMinClicked = bundle.getBoolean(AppConstants.KEY_LANDSCAPE_MIN_PLAYER_CLICK);
            removeBooleanExtra(AppConstants.KEY_LANDSCAPE_MIN_PLAYER_CLICK);
            isFromPlayerHeadWidget = bundle.getBoolean("player_head_open");
            timeElapsedOnWidget = bundle.getLong("time_elapsed");
            long ttl = bundle.getLong(ARG_GLOBAL_VIDEO_REWARD_TIME, 0);
            if (ttl > 0)
                TIME_UNTIL_FINISH = ttl;
        }
        if (isFromPlayerHeadWidget)
            isTwoSecondViewCounted = true;

        Log.i(getClass().getSimpleName(), "globalVideoRewardTime " + TIME_UNTIL_FINISH);
    }

    Handler posHandler;

    private void finishCheck() {
        if (isFromDeepLink) {
            startTabContainerActivity();
        } else {
            finish();
        }
    }

    /**
     * Make a server request for fetching the complete post info
     */
    public void startFresh() {
        fetchPostFromServer(postId);
        /*if (pageSource != null && !TextUtils.isEmpty(pageSource)) {
            fetchPostFromServer(source);

        } else {
            if (itemId != null) {

                topSource = sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.BOTTOM_NAV_TAB_SELECTED);
                topSourceHomeDynamicTab = sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.DYNAMIC_TAB_SELECTED);
                setUp();
                setDataAndViews(ListHolder.getInstance().getPostList());
                setCurrentPlayingPost(playerViewModel.getCurrentPlayingPost());
                subscribeToLiveData();
                updateList();
//            topSourceHomeCardType = sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.HOME_CARD_TYPE);
            } else {
                topSource = "notification";
                topSourceHomeCardType = "";
                if (source == null || source.isEmpty() || source == "/") {
                    Intent intent = new Intent(getApplicationContext(), TabContainerActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                fetchPostFromServer(source);
            }
        }*/

    }

    /**
     * Fetch post from server
     *
     * @param uid
     */
    private void fetchPostFromServer(String uid) {
        playerViewModel.getPostInfoFromServer(uid);
    }

    /**
     * On Post received from server
     *
     * @param currentPlayingPost
     */
    public void setCurrentPlayingPost(Result currentPlayingPost) {
        this.currentPlayingPost = currentPlayingPost;
        playerViewModel.currentPost = currentPlayingPost;
        if (currentPlayingPost != null) {

            timeElapsed = 0;
            videoBufferingStartTime = System.currentTimeMillis();

            //Track Post on Segment

            baseProperties.put("is_live", currentPlayingPost.getIsLive());
            baseProperties.put("type", currentPlayingPost.getIsLive() ? "live" : "fullRecorded");
            baseProperties.put("postId", currentPlayingPost.getId());
            baseProperties.put("title", currentPlayingPost.getTitle());
            baseProperties.put("language", currentPlayingPost.getLanguage());
            baseProperties.put("in_window_mode", false);
            if (currentPlayingPost.getAuthor() != null && currentPlayingPost.getAuthor().getUser() != null) {
                baseProperties.put("username", currentPlayingPost.getAuthor().getUser().getUsername());
                baseProperties.put("name", currentPlayingPost.getAuthor().getUser().getUserFullName());
                baseProperties.put("author_id", currentPlayingPost.getAuthor().getUser().getId());
                baseProperties.put("author", currentPlayingPost.getAuthor().getUser().getUsername());
            }
            baseProperties.put("game_id", currentPlayingPost.getGameId());
            baseProperties.put("game_name", currentPlayingPost.getGame());
            baseProperties.put("orientation", getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape");
            baseProperties.put("isLoggedIn", CommonUtils.isUserLoggedin());
            SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER, baseProperties);
            playerViewModel.updateFields(currentPlayingPost);
            setDataAndViews();
            setUp();

            initializePlayer(false);
            updateDuration();
            activityPlayerBinding.videoView.showController();
            isControllerShowing = true;
            hideOverlayHandler.postDelayed(playerControllerHandleRunnable, 2000);
            ChatHelper.getInstance(this).setPostChatJoinTask(this, currentPlayingPost);
            handleChat();
            activityPlayerBinding.scoreRvLand.setAdapter(scorecardAdapter);
            activityPlayerBinding.scoreRv.setAdapter(scorecardAdapter);
            if (landscapePlayerMinClicked) {
                onMinimizePlayerClicked();
            }
            /*if (!isConnectionRequestMade) {
                Log.i(getClass().getName(), "checking_in_post_update");
                isConnectionRequestMade = true;
                ChatHelper.getInstance(this).setPostChatJoinTask(this, currentPlayingPost);
            }*/

        } else {
            Toast.makeText(this, getString(R.string.error_message), Toast.LENGTH_LONG).show();
            finishCheck();
        }
    }

    @Override
    public void updateFollowStatus(boolean isFollowed) {
        currentPlayingPost.setFollowed(isFollowed);
        TextView followBtn = activityPlayerBinding.videoView.findViewById(R.id.follow_btn);
        if (followBtn != null) {
            Log.i("Follow Btn state ", currentPlayingPost.isFollowed() ? "Following" : "Follow");
            followBtn.setBackground(getDrawable(currentPlayingPost.isFollowed() ? R.drawable.follow_selected_bg :
                    R.drawable.follow_normal_bg));
            followBtn.setText(currentPlayingPost.isFollowed() ? "Following" : "Follow");
            followBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playerViewModel.startToggleFlow(view);
                    currentPlayingPost.setFollowed(!currentPlayingPost.isFollowed());
                    followBtn.setBackground(getDrawable(currentPlayingPost.isFollowed() ? R.drawable.follow_selected_bg :
                            R.drawable.follow_normal_bg));
                    followBtn.setText(currentPlayingPost.isFollowed() ? "Following" : "Follow");
                }
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUp() {

        //Video View Clicks
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        liveViewersCountText = activityPlayerBinding.videoView.findViewById(R.id.live_viewers_count);

        activityPlayerBinding.videoView.setOnTouchListener(new OnSwipeActionListener(this) {
            @Override
            public void onSwipeDown() {
                //custom action
//                onBackPressed();
            }

            @Override
            public void onSwipeLeft() {
                //TODO : play next video
                SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_PLAYER_SWIPE_LEFT, baseProperties);
                playerViewModel.playNextVideo();
            }

            @Override
            public void onSwipeUp() {
                //custom action
            }

            @Override
            public void onSwipeRight() {
                //prev video (if available) otherwise take back to home screen (onbackpressed)
                SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_PLAYER_SWIPE_RIGHT, baseProperties);
                playerViewModel.playPreviousVideo();
            }


            @Override
            public void performTouch() {
                //activityPlayerBinding.videoView.performClick();
            }


        });


        activityPlayerBinding.videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleItemsVisiblity();
                //updateContainVisibility();
            }
        });
        activityPlayerBinding.videoView.setControllerShowTimeoutMs(0);

        //Rotate clicked
        activityPlayerBinding.videoView.findViewById(R.id.rotate_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_PLAYER_CHANGE_ORIENTATION, baseProperties);
                manageOrientation();
            }
        });

        //Settings click
        activityPlayerBinding.videoView.findViewById(R.id.settings_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_PLAYER_SETTINGS_CLICK, baseProperties);
                settingsClicked();
            }
        });

        //Minimize player clicked
        activityPlayerBinding.videoView.findViewById(R.id.minimize_player).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_PLAYER_EXIT_CLICK, baseProperties);
                onMinimizePlayerClicked();
            }
        });

        //Share button clicked
        activityPlayerBinding.videoView.findViewById(R.id.share_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleShareClick(currentPlayingPost);
            }
        });

        //More options button click
        activityPlayerBinding.videoView.findViewById(R.id.more_options_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMoreOptionsClicked();
            }
        });

        showHideChatBtn = activityPlayerBinding.videoView.findViewById(R.id.chat_state_btn);
        handleChatButtonState(getResources().getConfiguration().orientation);
        showHideChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onChatStateButtonClicked();
            }
        });
        streamerInfoLayout = activityPlayerBinding.videoView.findViewById(R.id.streamer_info_layout);
        handleStreamerInfoLayout(getResources().getConfiguration().orientation);
        BindingUtils.setProfileImageUrlFromCache(activityPlayerBinding.videoView.findViewById(R.id.user_profile_image), currentPlayingPost.getAuthor().getProfilePic(), true);
        ((TextView) activityPlayerBinding.videoView.findViewById(R.id.user_name_tv)).setText(currentPlayingPost.getAuthor().getUser().getUsername());
        int totalFollowers = currentPlayingPost.getAuthor().getFollowersCount();
        ((TextView) activityPlayerBinding.videoView.findViewById(R.id.user_followers_count)).setText(CommonUtils.getPlural("Follower", totalFollowers, ((totalFollowers / 1000 >= 1) ? (totalFollowers / 1000) + "." + ((totalFollowers % 1000) / 100) + "K" : totalFollowers + "")));


        streamerInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAuthorClicked(currentPlayingPost.getAuthor().getUser().getUsername());
            }
        });

        //Chat State button click
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {


        } else {
            BindingUtils.setProfileImageUrlFromCache(activityPlayerBinding.postUserProfile, currentPlayingPost.getAuthor().getProfilePic(), true);
        }

        if (currentPlayingPost.getIsLive()) {
            activityPlayerBinding.videoView.findViewById(R.id.live_layout).setVisibility(View.VISIBLE);
            activityPlayerBinding.videoView.findViewById(R.id.total_views).setVisibility(View.GONE);
            activityPlayerBinding.videoView.findViewById(R.id.recorded_layout).setVisibility(View.VISIBLE);
            activityPlayerBinding.videoView.findViewById(R.id.exo_duration).setVisibility(View.GONE);
            activityPlayerBinding.videoView.findViewById(R.id.exo_position).setVisibility(View.GONE);
            activityPlayerBinding.videoView.findViewById(R.id.exo_play).setVisibility(View.GONE);
            ((ImageView) activityPlayerBinding.videoView.findViewById(R.id.exo_play)).setImageResource(0);
            ((ImageView) activityPlayerBinding.videoView.findViewById(R.id.exo_pause)).setImageResource(0);
            ((ImageView) activityPlayerBinding.videoView.findViewById(R.id.exo_rew)).setImageResource(0);
            ((ImageView) activityPlayerBinding.videoView.findViewById(R.id.exo_ffwd)).setImageResource(0);
            activityPlayerBinding.videoView.findViewById(R.id.exo_rew).setVisibility(View.GONE);
            activityPlayerBinding.videoView.findViewById(R.id.exo_ffwd).setVisibility(View.GONE);
            activityPlayerBinding.videoView.setUseController(false);
        } else {
            int totalNumViews = currentPlayingPost.getTotalViews();
            TextView totalViews = activityPlayerBinding.videoView.findViewById(R.id.total_views);
            totalViews.setText(CommonUtils.getPlural("View", totalNumViews, (totalNumViews / 1000 >= 1) ? (totalNumViews / 1000) + "." + ((totalNumViews % 1000) / 100) + "K" : totalNumViews + ""));
            totalViews.setVisibility(View.VISIBLE);
            activityPlayerBinding.videoView.findViewById(R.id.exo_duration).setVisibility(View.VISIBLE);
            activityPlayerBinding.videoView.findViewById(R.id.exo_position).setVisibility(View.VISIBLE);
            activityPlayerBinding.videoView.findViewById(R.id.live_layout).setVisibility(View.GONE);
            activityPlayerBinding.videoView.findViewById(R.id.recorded_layout).setVisibility(View.VISIBLE);
            activityPlayerBinding.videoView.findViewById(R.id.exo_play).setVisibility(View.VISIBLE);
            activityPlayerBinding.videoView.findViewById(R.id.exo_pause).setVisibility(View.VISIBLE);
            activityPlayerBinding.videoView.findViewById(R.id.exo_rew).setVisibility(View.VISIBLE);
            activityPlayerBinding.videoView.findViewById(R.id.exo_ffwd).setVisibility(View.VISIBLE);
            ((ImageView) activityPlayerBinding.videoView.findViewById(R.id.exo_rew)).setImageResource(R.drawable.ic_rewind_white_24dp);
            ((ImageView) activityPlayerBinding.videoView.findViewById(R.id.exo_ffwd)).setImageResource(R.drawable.ic_fast_forward_white_24dp);
            activityPlayerBinding.videoView.setUseController(true);
        }

        containerLayoutHandler.postDelayed(containerLayoutRunner, 5000);
        ViewTreeObserver observer = activityPlayerBinding.postTitleHolderLayout.getViewTreeObserver();
        titleHolderLayoutHeight = activityPlayerBinding.postTitleHolderLayout.getHeight();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (observer.isAlive()) {
                    titleHolderLayoutHeight = activityPlayerBinding.postTitleHolderLayout.getHeight();
                    observer.removeOnGlobalLayoutListener(this);
                }
            }
        });
        activityPlayerBinding.videoView.findViewById(R.id.go_to_live_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player != null) {
                    isPlayerDraggedBehind = false;
                    player.seekToDefaultPosition();
                    resumePosition = 0;
                    activityPlayerBinding.videoView.findViewById(R.id.go_to_live_btn).setVisibility(View.GONE);
                }

            }
        });

        activityPlayerBinding.scoreHolderView.setOnClickListener(v -> {
            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_SCOREBOARD_INDICATOR_CLICKED, baseProperties);
            ViewAnimationUtils.reveal(activityPlayerBinding.scorecardLayout, activityPlayerBinding.postTitleHolderLayout);
        });

        activityPlayerBinding.closeButton.setOnClickListener(v -> {
            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_SCOREBOARD_CLOSE_CLICKED, baseProperties);
            ViewAnimationUtils.unReveal(activityPlayerBinding.scorecardLayout, activityPlayerBinding.postTitleHolderLayout);
        });

        activityPlayerBinding.scoreIndicatorLayout.setOnClickListener(v -> {
            ViewAnimationUtils.reveal(activityPlayerBinding.scorecardLayoutLand, activityPlayerBinding.scoreIndicatorLayout);
        });

        activityPlayerBinding.closeButtonLand.setOnClickListener(v -> {
            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_SCOREBOARD_CLOSE_CLICKED, baseProperties);
            ViewAnimationUtils.unReveal(activityPlayerBinding.scorecardLayoutLand, activityPlayerBinding.scoreIndicatorLayout);
        });

        activityPlayerBinding.scorecardLayout.setOnClickListener(v -> openScoreActivity());
        activityPlayerBinding.arrowButton.setOnClickListener(v -> openScoreActivity());
        activityPlayerBinding.arrowButtonLand.setOnClickListener(v -> openScoreActivity());
        activityPlayerBinding.descriptionTextView.setOnClickListener(v -> showDescription());

        /*GestureDetectorCompat rewindGestureDetector = new GestureDetectorCompat(PlayerActivity.this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {

                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }
        });
        rewindGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                activityPlayerBinding.videoView.onTouchEvent(motionEvent);
                handleItemsVisiblity();
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent motionEvent) {
                Log.d("TEST", "onDoubleTap");
                activityPlayerBinding.videoView.getPlayer().seekTo(((SimpleExoPlayer) activityPlayerBinding.videoView.getPlayer()).getCurrentPosition() - 10000);
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                return false;
            }
        });

        activityPlayerBinding.rewindLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                rewindGestureDetector.onTouchEvent(event);
                return true;
            }
        });

        GestureDetectorCompat forwardGestureDetector = new GestureDetectorCompat(PlayerActivity.this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {

                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }
        });
        forwardGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                activityPlayerBinding.videoView.onTouchEvent(motionEvent);
                handleItemsVisiblity();
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent motionEvent) {
                Log.d("TEST", "onDoubleTap");
                activityPlayerBinding.videoView.getPlayer().seekTo(((SimpleExoPlayer) activityPlayerBinding.videoView.getPlayer()).getCurrentPosition() + 10000);
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                return false;
            }
        });

        activityPlayerBinding.forwardLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                forwardGestureDetector.onTouchEvent(event);
                return true;
            }
        });*/
        initializeDoubleTapPlayerView();
    }

    private void showDescription() {
        new DescriptionBottomSheetDialog.Builder()
                .addDescription(playerViewModel.content.get())
                .addSource(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER)
                .build()
                .show(getSupportFragmentManager(), AppConstants.ARG_DESCRIPTION);
    }


    private void initializeDoubleTapPlayerView() {

        activityPlayerBinding.youtubeDoubleTap.setPlayerView(activityPlayerBinding.videoView);
        int animationDuration = 800;
        activityPlayerBinding.youtubeDoubleTap.setAnimationDuration(animationDuration);
        int fastForwardRewindDuration = 10000;
        activityPlayerBinding.youtubeDoubleTap.setFastForwardRewindDuration(fastForwardRewindDuration);

        SeekListener listener = new SeekListener() {

            @Override
            public void seekBackward(long duration) {

            }

            @Override
            public void seekForward(long duration) {

            }

            @Override
            public void onVideoEndReached() {
                Toast.makeText(
                        PlayerActivity.this,
                        "Video end reached", Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onVideoStartReached() {
                playWhenReady = false;
                if (player != null)
                    player.setPlayWhenReady(false);
                Toast.makeText(
                        PlayerActivity.this,
                        "Video start reached", Toast.LENGTH_SHORT
                ).show();
            }
        };

        YouTubeOverlay.PerformListener performListener = new YouTubeOverlay.PerformListener() {
            @Override
            public void onAnimationEnd() {
                activityPlayerBinding.youtubeDoubleTap.setVisibility(View.GONE);
                activityPlayerBinding.videoView.setUseController(true);
                if (isControllerShowing) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        if (isChatShown) {
                            showChatFrameContainer();
                        }
                    }
                    isControllerShowing = false;
                    hideOverlayHandler.removeCallbacks(playerControllerHandleRunnable);
                }
                if (player != null && playWhenReady) {
                    //activityPlayerBinding.videoView.showController();
                }
            }

            @Override
            public void onAnimationStart() {
                // Do UI changes when double tapping / animation starts including showing the overlay
                activityPlayerBinding.videoView.setUseController(false);
                activityPlayerBinding.youtubeDoubleTap.setVisibility(View.VISIBLE);

            }
        };
        activityPlayerBinding.youtubeDoubleTap.setSeekListener(listener);
        activityPlayerBinding.youtubeDoubleTap.setPerformListener(performListener);


        activityPlayerBinding.videoView.activateDoubleTap(true)
                .setDoubleTapDelay(650)
                .setDoubleTapListener(activityPlayerBinding.youtubeDoubleTap);
    }

    private void handleStreamerInfoLayout(int orientation) {
        if (streamerInfoLayout == null) return;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            streamerInfoLayout.setVisibility(View.GONE);
        } else {
            streamerInfoLayout.setVisibility(View.VISIBLE);
        }
    }

    private void handleChatButtonState(int orientation) {
        if (showHideChatBtn == null) return;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            showHideChatBtn.setVisibility(View.GONE);
        } else {
            showHideChatBtn.setVisibility(View.VISIBLE);
        }
    }

    long maxPlayerPos;

    Runnable r = new Runnable() {
        @Override
        public void run() {
            if (player != null && currentPlayingPost != null && currentPlayingPost.getIsLive()) {
                long currentPos = player.getContentPosition();
                //Log.d("Current_pos", player.getContentPosition() + "ms");
                if (maxPlayerPos - currentPos > 120000) {
                    if (currentPos != 0) {
                        isPlayerDraggedBehind = true;
                        Log.i("Player_State", "Dragged behind");
                        activityPlayerBinding.videoView.findViewById(R.id.go_to_live_btn).setVisibility(View.VISIBLE);
                        maxPlayerPos = currentPos;
                    } else {
                        activityPlayerBinding.videoView.findViewById(R.id.go_to_live_btn).setVisibility(View.GONE);
                    }
                } else {
                    maxPlayerPos = currentPos;
                }
                if (posHandler != null) {
                    posHandler.postDelayed(this, 200);
                }
            }
        }
    };


    /**
     * Change Orientation
     */
    public void manageOrientation() {
        //updateExtras();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (currentPlayingPost.getVideoMode().equalsIgnoreCase("landscape")) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                adjustPlayerHeight();
            }
        } else {

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    public void removeBooleanExtra(String key) {
        Bundle bundle = getIntent().getExtras();
        bundle.remove(key);
        //getIntent().putExtras(bundle);
        getIntent().removeExtra(key);
    }

    public void updateExtrasWithBoolean(String key, boolean value) {
        Bundle bundle = getIntent().getExtras();
        bundle.putBoolean(key, value);
        getIntent().putExtras(bundle);
    }

    /**
     * Settings clicked to change video resolution
     */

    private int checkId = -1;

    @Override
    public void settingsClicked() {
        int menu = R.menu.menu_video_resolution_recorded;
        if (currentPlayingPost != null && currentPlayingPost.getIsLive()) {
            menu = R.menu.menu_video_resolution_live;
        }
        new BottomSheetMenuDialog.Builder()
                .add(menu)
                .header("Video Quality")
                .setCheckable(true)
                .setSpanner(BottomSheetMenuDialog.Builder.SPANNER_BRACKET_ROUND)
                .setCheckedId(checkId)
                .setListener(this::onResolutionItemClick)
                .show(getSupportFragmentManager(), "BottomSheetMenuDialog");

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Choose video mode");
//        Log.i(getClass().getName(), "settingsClicked_1: " + playbackPosition + " and CurrentPosition : " + player.getCurrentPosition());
//
//        String[] animals = {"High", "Medium", "Low"};
//        builder.setSingleChoiceItems(animals, qualityFormat.equalsIgnoreCase("high") ? 0 : (qualityFormat.equalsIgnoreCase("medium") ? 1 : (qualityFormat.equalsIgnoreCase("low") ? 2 : -1)), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int which) {
//                switch (which) {
//                    case 0: {
//                        qualityFormat = "high";
//                        break;
//                    }
//                    case 1: {
//                        qualityFormat = "medium";
//                        break;
//                    }
//                    case 2: {
//                        qualityFormat = "low";
//                        break;
//                    }
//                }
//                sharedPrefsUtils.setStringPreference(PlayerActivity.this, SharedPrefsUtils.VIDEO_FORMAT_REQUESTED, qualityFormat);
//                playbackPosition = currentPlayingPost.getIsLive() ? 0L : player.getCurrentPosition();
//
//                Log.i(getClass().getName(), "settingsClicked_2 : " + playbackPosition + " and CurrentPosition : " + player.getCurrentPosition());
//                initializePlayer();
//                dialogInterface.dismiss();
//            }
//        });
//
//
//        AlertDialog dialog = builder.create();
//
//        dialog.show();
    }

    private void onResolutionItemClick(String tag, Option option) {
        switch (option.getId()) {
            case R.id.action_auto:
                qualityFormat = "auto";
                break;
            case R.id.action_high: {
                qualityFormat = "high";
                break;
            }
            case R.id.action_medium: {
                qualityFormat = "medium";
                break;
            }
            case R.id.action_low: {
                qualityFormat = "low";
                break;
            }
        }
        checkId = option.getId();
        sharedPrefsUtils.setStringPreference(PlayerActivity.this, SharedPrefsUtils.VIDEO_FORMAT_REQUESTED, qualityFormat);
        playbackPosition = currentPlayingPost.getIsLive() ? 0L : (player != null ? player.getCurrentPosition() : 0L);

        if (currentPlayingPost.getIsLive()) {
            if (isPlayerDraggedBehind) {
                resumePosition = (player != null) ? player.getContentPosition() : 0L;
            } else {
                isInitialLoad = true;
                resumePosition = 0;
                maxPlayerPos = 0;
            }
        } else {
            resumePosition = (player != null) ? player.getContentPosition() : 0L;
        }

        recordResolutionChangeEvent();

        if (option.getId() != -1)
            initializePlayer(true);
    }

    private void recordResolutionChangeEvent() {
        HashMap<String, Object> resProperties = baseProperties;
        resProperties.put("resolution", qualityFormat);
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_VIDEO_RESOLUTION_CHANGE, resProperties);
    }

    /**
     * Minimize player button clicked
     */
    private void onMinimizePlayerClicked() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            checkForOverlayAndShowOverlay();
        } else {
            updateExtrasWithBoolean(AppConstants.KEY_LANDSCAPE_MIN_PLAYER_CLICK, true);
            manageOrientation();
        }
    }

    /**
     * Share post button clicked
     *
     * @param post
     */
    @Override
    public void handleShareClick(Result post) {
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_PLAYER_SHARE_CLICKED, baseProperties);

        if (!CommonUtils.isUserLoggedin()) {
            openLoginFlow();
        } else {
            shareBranchLink(post);
        }

        //ShareTaskHelper.getNewInstance(this).share(this, post.getShareMessageBody(this), ShareTaskHelper.ShareTarget.Others);
    }

    private void shareBranchLink(Result post) {
        if (post == null || post.getAuthor() == null || post.getAuthor().getUser() == null || post.getAuthor().getUser().getUsername() == null)
            return;
        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.BRANCH_POST_SOURCE_URL, post.getShareUrl());
        map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_LIVE_STREAM);
        FirebaseDynamicLinkUtils.share(this, post.getAuthor().getCampaignInfo(), "player_live_share", post.getAuthor().getUser().getUsername() + " is Live on Rheo TV",
                "Watch " + post.getAuthor().getUser().getUsername() + "playing " + playerViewModel.game.get() + " live on Rheo TV",
                post.getThumbnail(), map, post.getShareUrl(), true,post.getIsLive(),post.getAuthor().getUser().getUsername());
    }


    @Override
    public void onMoreOptionsClicked() {
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_REPORT_POST_CLICKED, baseProperties);
        new AlertDialog.Builder(this).setTitle(getString(R.string.report_this_title)).setMessage(getString(R.string.report_content)).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_POST_REPORTED, baseProperties);
                dialogInterface.dismiss();
                if (currentPlayingPost != null)
                    playerViewModel.reportPost(currentPlayingPost.getId());
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_REPORT_POST_DISMISSED, baseProperties);
                dialogInterface.dismiss();
            }
        }).show();

    }

    boolean isChatShown = false;

    /**
     * Chat state button click handle chat window incoming and outgoing
     */
    private void onChatStateButtonClicked() {
        if (!isChatShown) {
            setUpChatFragmemt();
            isChatShown = true;
            showHideChatBtn.setImageResource(R.drawable.ic_hide_chat_white_24dp);
        } else {
            removeChatFragment();
            showHideChatBtn.setImageResource(R.drawable.ic_show_chat_white_24dp);
            isChatShown = false;
        }
    }

    public void showChatFrameContainer() {
        Log.i(TAG, "ChatFrameState Show Chat frame");
        activityPlayerBinding.chatContainer.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(activityPlayerBinding.chatContainer, View.ALPHA, 0, 1);
        animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Log.i(TAG, "ChatFrameState Chat frame visible");
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        animator.start();
    }

    public void hideChatFrameContainer() {
        Log.i(TAG, "ChatFrameState Hiding Chat frame");
        ObjectAnimator animator = ObjectAnimator.ofFloat(activityPlayerBinding.chatContainer, View.ALPHA, 1, 0);
        animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Log.i(TAG, "ChatFrameState Chat frame gone");
                //activityPlayerBinding.chatContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        animator.start();
    }

    private void setUpChatFragmemt() {
        if (activityPlayerBinding.chatContainer == null) return;
        activityPlayerBinding.chatContainer.setVisibility(View.VISIBLE);
        activityPlayerBinding.chatContainer.setAlpha(1.0f);
        int width = getResources().getDisplayMetrics().widthPixels;
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) activityPlayerBinding.chatContainer.getLayoutParams();

        lp.width = width / 2;
        lp.horizontalBias = 0;
        activityPlayerBinding.chatContainer.setLayoutParams(lp);
        chatListFragmentLand = ChatListFragment.newInstance(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        getSupportFragmentManager().beginTransaction().add(R.id.chat_container, chatListFragmentLand, CHAT_FRAGMENT_TAG).commitAllowingStateLoss();
    }


    private void removeChatFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CHAT_FRAGMENT_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment);
        }
        activityPlayerBinding.chatContainer.removeAllViews();
    }

    DebugTextViewHelper debugTextViewHelper;


    private void initializePlayer(boolean needRefresh) {
        try {
            /*activityPlayerBinding.errorText.setVisibility(View.GONE);
//            stopTimer();
            sharedPrefsUtils.setIntegerPreference(getNonUiContext()
                    , AppConstants.VIDEO_PLAYED_COUNT
                    , sharedPrefsUtils.getIntegerPreference(getNonUiContext(), AppConstants.VIDEO_PLAYED_COUNT, 0) + 1);

            if (!sharedPrefsUtils.getBooleanPreference(getNonUiContext(), AppConstants.VIDEO_PLAYED, false)) {
                AnalyticsHelper.getInstance(getNonUiContext()).sendVideoPlayFirstTime();
                sharedPrefsUtils.setBooleanPreference(getNonUiContext(), AppConstants.VIDEO_PLAYED, true);
            }*/

            if (player == null) {
                TrackSelection.Factory factory = new AdaptiveTrackSelection.Factory();

                player = getPlayer(new DefaultTrackSelector(factory));

                if (currentPlayingPost.getVolume() == null) {
                    ((SimpleExoPlayer) player).setVolume(10.0f);
                } else {
                    ((SimpleExoPlayer) player).setVolume(Float.parseFloat(currentPlayingPost.getVolume()));
                }

                //activityPlayerBinding.videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                activityPlayerBinding.videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

                activityPlayerBinding.videoView.setPlayer(player);
                activityPlayerBinding.youtubeDoubleTap.setPlayer(player);

                 /*TextView debugTV = new TextView(this);
                    activityPlayerBinding.videoView.getOverlayFrameLayout().addView(debugTV);
                    debugTextViewHelper = new DebugTextViewHelper((SimpleExoPlayer) player, debugTV);
                    debugTextViewHelper.start();*/
                //((SimpleExoPlayer) player).addAnalyticsListener(new EventLogger(selector));
                player.setPlayWhenReady(playWhenReady);
            }
            String uriString = "";
            if (currentPlayingPost.getVideoUrl() != null) {
                if (TimeUtils.hasStreamNotStarted(currentPlayingPost.getStartFrom())) {
                    uriString = currentPlayingPost.getPromoVideoUrl();
                } else {
                    if (qualityFormat.isEmpty()) {
                        setQualityFormat(currentPlayingPost.getVideoUrl());
                        uriString = currentPlayingPost.getVideoUrl();
                    } else {
                        uriString = updateUrlWithCurrentFormat(CommonUtils.getUrlWithoutParameters(currentPlayingPost.getVideoUrl()));
                    }
                }
            } else {
                playerViewModel.playNextVideo();
            }
            if (uriString != null && !uriString.isEmpty()) {
                Uri uri = Uri.parse(uriString);
                //Uri uri = Uri.parse("http://52.66.235.163/live/data104/medium/manifest.m3u8");
                MediaSource mediaSource = buildMediaSource(uri);
                boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
                player.prepare(mediaSource, !haveResumePosition, true);
                if (currentPlayingPost.getIsLive()) {
                    if (resumePosition != 0) {
                        player.seekTo(resumeWindow, resumePosition);
                        isPlayerDraggedBehind = true;
                        activityPlayerBinding.videoView.findViewById(R.id.go_to_live_btn).setVisibility(View.VISIBLE);
                    } else {
                        isPlayerDraggedBehind = false;
                        activityPlayerBinding.videoView.findViewById(R.id.go_to_live_btn).setVisibility(View.GONE);
                        player.seekToDefaultPosition();
                    }
                } else {
                    if (haveResumePosition) {
                        player.seekTo(resumeWindow, resumePosition);
                    }
                }
                posHandler = new Handler();
                posHandler.postDelayed(r, 200);
            }

            Log.i(getClass().getName(), "initializePlayer: playbackPosition - " + playbackPosition + " currentWindow " + currentWindow + " playWhenReady " + playWhenReady);


            //player.seekTo(currentWindow, currentPlayingPost.getIsLive() ? currentState : playbackPosition);

            timeSentRecord.add(TIME_SENT.First);
            activityPlayerBinding.videoView.setVisibility(View.VISIBLE);
            activityPlayerBinding.executePendingBindings();

            activityPlayerBinding.videoView.setUseController(true);

        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    boolean isInitialLoad = false;

    String errorString;

    private SimpleExoPlayer getPlayer(DefaultTrackSelector selector) {

        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();

        final int loadControlBufferMs = 50000; /* This is 50000 milliseconds in ExoPlayer 2.9.6 */


        /* Configure the DefaultLoadControl to use the same value for */
        builder.setBufferDurationsMs(loadControlBufferMs,
                loadControlBufferMs,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
        DefaultLoadControl loadControl = builder.createDefaultLoadControl();


        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this,
                new DefaultRenderersFactory(this, EXTENSION_RENDERER_MODE_ON), selector
                , loadControl);

//        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this);


        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
                super.onTimelineChanged(timeline, manifest, reason);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    if (updateView()) {

                    } else {
                        streamTimer.stopTimer();
                        Log.i(getClass().getName(), "stream_timer_stopping_1 " + streamTimer.isRunning());

                        playerViewModel.playNextVideo();
                    }
                } else if (playWhenReady && playbackState == Player.STATE_READY) {
                    endStreamHandler.removeCallbacks(streamEndRunnable);

                    trackFirstEventWatchStream();


                    if (isInitialLoad) {
                        player.seekToDefaultPosition();
                        isInitialLoad = false;
                    }
                    if (!isVideoPlayingStarted) {
                        isVideoPlayingStarted = true;
                        if (activityPlayerBinding.placeholderThumbnail != null)
                            activityPlayerBinding.placeholderThumbnail.setVisibility(View.GONE);
                    }
//                    activityPlayerBinding.errorText.setVisibility(View.GONE);
                    Log.d(RheoTvApp.TAG, "playing");

                    if (!streamTimer.isRunning())
                        streamTimer.startTimer();
                    else if (streamTimer.isPaused())
                        streamTimer.resumeTimer();

                    Log.i(getClass().getName(), "stream_timer_starting " + streamTimer.isRunning() + " and " + streamTimer.isPaused());
                    if (activityPlayerBinding.playerProgressBar != null)
                        activityPlayerBinding.playerProgressBar.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = activityPlayerBinding.videoView.getLayoutParams();
                    int width = getResources().getDisplayMetrics().widthPixels;

                    int orientation = getResources().getConfiguration().orientation;

                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        activityPlayerBinding.videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
                    } else {
                        int height = 0;
                        if (currentPlayingPost != null && currentPlayingPost.getVideoMode() != null && currentPlayingPost.getVideoMode().equalsIgnoreCase("landscape")) {
                            height = (width * 9) / 16;

                        } else {
                            height = width;
                            // ((SimpleExoPlayer) player).setVideoScalingMode(C.VIDEO_SCALING_MODE_);
                        }
                        layoutParams.height = height;
                        activityPlayerBinding.videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                    }

                    activityPlayerBinding.videoView.setLayoutParams(layoutParams);
                } else if (playWhenReady) {
                    if (playbackState == Player.STATE_BUFFERING) {
                        //SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_VIDEO_PLAYER_BUFFERING, baseProperties);
                        if (activityPlayerBinding.playerProgressBar != null)
                            activityPlayerBinding.playerProgressBar.setVisibility(View.VISIBLE);
                        if (currentPlayingPost.getIsLive())
                            endStreamHandler.postDelayed(streamEndRunnable, 10000);

                        streamTimer.pauseTimer();

                    }
                } else {
//                    stopTimer();
                    streamTimer.pauseTimer();
                    Log.i(getClass().getName(), "stream_timer_stopping_2 " + streamTimer.isRunning());
                }
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                if (inErrorState) {
                    updateResumePosition();
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                super.onPlayerError(error);
                if (error.type == ExoPlaybackException.TYPE_RENDERER) {
                    Exception cause = error.getRendererException();
                    if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                        // Special case for decoder initialization failures.
                        MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                                (MediaCodecRenderer.DecoderInitializationException) cause;
                        if (decoderInitializationException.diagnosticInfo == null) {
                            if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                                errorString = getString(R.string.error_querying_decoders);
                            } else if (decoderInitializationException.secureDecoderRequired) {
                                errorString = getString(R.string.error_no_secure_decoder,
                                        decoderInitializationException.mimeType);
                            } else {
                                errorString = getString(R.string.error_no_decoder,
                                        decoderInitializationException.mimeType);
                            }
                        } else {
                            errorString = getString(R.string.error_instantiating_decoder,
                                    decoderInitializationException.diagnosticInfo);
                        }
                    }

                    if (errorString != null)
                        showToast(errorString);

                    inErrorState = true;
                    if (isBehindLiveWindow(error)) {
                        clearResumePosition();
                        initializePlayer(false);
                    } else {
                        updateResumePosition();
                    }

                } else if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                    String cause = error.getCause().getLocalizedMessage();
                    Log.i(TAG, "Caught Source Exception");

                    if (cause != null && cause.equalsIgnoreCase("Response code: 404")) {
                        updateResumePosition();
                        resumePosition += 4000;
                        //initializePlayer(false);
                        activityPlayerBinding.videoView.getPlayer().seekTo(resumeWindow, resumePosition);
                    }
                }

                //updateResumePosition();
//                activityPlayerBinding.errorText.setVisibility(View.VISIBLE);
                /*inErrorState = true;
                switch (error.type) {

                    case ExoPlaybackException.TYPE_SOURCE:
                        if (isBehindLiveWindow(error)) {
                            clearResumePosition();
                            initializePlayer();
                        } else {
                            updateResumePosition();
                            *//*updateButtonVisibilities();
                            showControls();*//*
                        }
                        break;
                    case ExoPlaybackException.TYPE_RENDERER:
//                        activityPlayerBinding.errorText.setText("Uh Oh! Please try again in sometime.");

                        releasePlayer();
                        initializePlayer();
                        break;
                    case ExoPlaybackException.TYPE_UNEXPECTED:
//                        activityPlayerBinding.errorText.setText("Uh Oh! Please try again in sometime.");
                        releasePlayer();
                        initializePlayer();
                        break;
                }*/
            }
        });
        return player;
    }

    private void trackFirstEventWatchStream() {
        if (!isFirstWatchEventTracked) {
            isFirstWatchEventTracked = true;
            if (CommonUtils.isFirstWatchEventNotTracked()) {
                CommonUtils.setFirstWatchEventTracked();
                SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_FIRST_WATCH_STREAM, baseProperties);
            }
        }
    }

    private void trackEventWatchStream30Secs(long ttl) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties
                .put("time_elapsed", ttl);
        SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_WATCH_STREAM_30_SECS, properties);
    }

    private void trackEventWatchStream5mins(long ttl) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties
                .put("time_elapsed", ttl);
        SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_WATCH_STREAM_5_MINS, properties);
    }


    private void updateResumePosition() {
        resumeWindow = player != null ? player.getCurrentWindowIndex() : 0;
        resumePosition = Math.max(0, player != null ? player.getContentPosition() : 0L);
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    boolean isControllerShowing = false;

    Runnable playerControllerHandleRunnable = new Runnable() {
        @Override
        public void run() {
            if (isControllerShowing) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (isChatShown) {
                        showChatFrameContainer();
                    }
                }
                activityPlayerBinding.videoView.hideController();
                isControllerShowing = false;
            }
        }
    };

    private boolean isScoreShown = false;

    private void handleItemsVisiblity() {
        if (isControllerShowing) {
            //Controls will hide here
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (isChatShown) {
                    showChatFrameContainer();
                }

                if (shouldShowScoreCard) {
                    if (isScoreShown) {
                        activityPlayerBinding.scorecardLayoutLand.setVisibility(View.VISIBLE);
                        isScoreShown = false;
                        activityPlayerBinding.scoreIndicatorLayout.setVisibility(View.GONE);
                    } else {
                        activityPlayerBinding.scoreIndicatorLayout.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (activityPlayerBinding.scorecardLayout.getVisibility() != View.VISIBLE) {
                    if (activityPlayerBinding.postTitleHolderLayout.getVisibility() != View.GONE) {
                        ViewAnimationUtils.collapse(activityPlayerBinding.postTitleHolderLayout);
                        containerLayoutHandler.removeCallbacks(containerLayoutRunner);
                    }
                } else {
                    containerLayoutHandler.postDelayed(containerLayoutRunner, 5000);
                }
            }
            hideOverlayHandler.removeCallbacks(playerControllerHandleRunnable);
            //activityPlayerBinding.videoView.hideController();
            isControllerShowing = false;
        } else {
            //Controls will show here
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (isChatShown) {
                    hideChatFrameContainer();
                }

                if (shouldShowScoreCard) {
                    activityPlayerBinding.scoreIndicatorLayout.setVisibility(View.GONE);
                    if (activityPlayerBinding.scorecardLayoutLand.getVisibility() == View.VISIBLE) {
                        activityPlayerBinding.scorecardLayoutLand.setVisibility(View.GONE);
                        isScoreShown = true;
                    }
                }
            } else {
                if (activityPlayerBinding.scorecardLayout.getVisibility() != View.VISIBLE) {
                    if (activityPlayerBinding.postTitleHolderLayout.getVisibility() == View.VISIBLE) {
                        containerLayoutHandler.removeCallbacks(containerLayoutRunner);
                    } else {
                        ViewAnimationUtils.expand(activityPlayerBinding.postTitleHolderLayout, titleHolderLayoutHeight);
                    }
                    containerLayoutHandler.postDelayed(containerLayoutRunner, 5000);
                    hideOverlayHandler.postDelayed(playerControllerHandleRunnable, 5000);
                }
            }

            // activityPlayerBinding.videoView.showController();
            isControllerShowing = true;
        }
    }


    private Runnable containerLayoutRunner = new Runnable() {
        @Override
        public void run() {
            if (activityPlayerBinding.postTitleHolderLayout.getVisibility() == View.VISIBLE) {
                titleHolderLayoutHeight = activityPlayerBinding.postTitleHolderLayout.getHeight();
                ViewAnimationUtils.collapse(activityPlayerBinding.postTitleHolderLayout);
            }
        }
    };

    private Handler containerLayoutHandler = new Handler();

    int titleHolderLayoutHeight;

    private long TOTAL_PROGRESS_TIME = 0;
    private long TIME_UNTIL_FINISH = 0;
    private long TIME_DELAY_TO_SHOW_VIDEO_ALERT = 0;
    private CountDownTimer mVideoRewardCountDownTimer;

//    private void setRewards() {
//        TOTAL_PROGRESS_TIME = RewardManager.getInstance().getVideoRewardActivationTime();
//        TIME_DELAY_TO_SHOW_VIDEO_ALERT = RewardManager.getInstance().getVideoRewardAlertDelayTime();
//        TIME_UNTIL_FINISH = 0;
//
//        // show tool tip to user
//        if (RewardManager.getInstance().isFirstCommentRewardAvailable() && CommonUtils.showVideoTooltip(this)) {
//            Log.i(getClass().getName(), "setRewards isFirstCommentRewardAvailable");
//            showTooltip(getString(R.string.first_comment_reward_alert), activityPlayerBinding.chatboxLL, Gravity.TOP, 5000, REWARD_TYPE_TEN_MINUTE_STREAM);
//        }
//
//        if (RewardManager.getInstance().isTenMinuteStreamRewardAvailable() && CommonUtils.showCommentTooltip(this)) {
//            showTooltip(getString(R.string.watch_video_tooltip).replace("$n", CommonUtils.convertToMmSs(TOTAL_PROGRESS_TIME)), activityPlayerBinding.rewardProgressBar, Gravity.BOTTOM, 5000, REWARD_TYPE_FIRST_COMMENT);
//        }
//
//        activityPlayerBinding.rewardProgressBar.setShowTimer(false);
//        playerViewModel.showWatchVideoReward.set(RewardManager.getInstance().isTenMinuteStreamRewardAvailable());
//        mVideoRewardCountDownTimer = new CountDownTimer(TOTAL_PROGRESS_TIME, 10) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                TIME_UNTIL_FINISH = millisUntilFinished;
//                activityPlayerBinding.rewardProgressBar.setProgress(((float) TOTAL_PROGRESS_TIME - millisUntilFinished) / TOTAL_PROGRESS_TIME, millisUntilFinished);
//            }
//
//            @Override
//            public void onFinish() {
//                if (!rewardTimerComplete) {
//                    rewardTimerComplete = true;
//                    shouldShowTenMinuteAlert = false;
//                    activityPlayerBinding.rewardProgressBar.setProgress(1f, TOTAL_PROGRESS_TIME);
//                    playerViewModel.showWatchVideoReward.set(false);
//
//                    checkAndShowVideoReward();
//                }
//            }
//        };
//        if (RewardManager.getInstance().isTenMinuteStreamRewardAvailable()) {
//            mVideoRewardCountDownTimer.start();
//            activateExitAlert();
//        }
//    }

    private void activateExitAlert() {
        new Handler().postDelayed(() -> {
            if (CommonUtils.isUserLoggedin() && RewardManager.getInstance().isTenMinuteStreamRewardAvailable() && !shouldShowTenMinuteAlert)
                shouldShowTenMinuteAlert = true;
        }, RewardManager.getInstance().getVideoRewardAlertDelayTime());

        if (activityPlayerBinding.rewardProgressBar != null)
            activityPlayerBinding.rewardProgressBar.setOnClickListener((View) -> recordRewardTimeClickEvent());
    }

    private void recordRewardTimeClickEvent() {
        HashMap<String, Object> prp = baseProperties;
        prp.put("postId", playerViewModel.getCurrentPlayingPostId());
        prp.put("userName", CommonUtils.getUserName(this));
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_VIDEO_PROGRESS_CLICKED, prp);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        //if (getSupportFragmentManager().findFragmentByTag("Author") != null)
        //   getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("Author")).commitAllowingStateLoss();
        if (player != null) {
            player.release();
            player = null;
        }

        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getIntent().getStringExtra(AppConstants.SCREEN_SOURCE));

        resumePosition = 0;
        maxPlayerPos = 0;
        //isInitialChatProcessed = false;
//        isTwoSecondViewCounted = true;


        fetchSenderSource();
        startFresh();
//        setRewards();
        super.onNewIntent(intent);
    }



   /* private void setPlayerViewBehaviour() {
        if (currentPlayingPost != null) {
            if (currentPlayingPost.getIsLive()) {
                // do normal behaviour
                ImageButton play = (ImageButton) activityPlayerBinding.videoView.findViewById(R.id.exo_play);
                setImageButtonEnabled(false, play, play.getDrawable());
                ImageButton pause = (ImageButton) activityPlayerBinding.videoView.findViewById(R.id.exo_pause);
                setImageButtonEnabled(false, pause, pause.getDrawable());
                ImageButton fwd = (ImageButton) activityPlayerBinding.videoView.findViewById(R.id.exo_ffwd);
                setImageButtonEnabled(false, fwd, fwd.getDrawable());
                ImageButton rew = (ImageButton) activityPlayerBinding.videoView.findViewById(R.id.exo_rew);
                setImageButtonEnabled(false, rew, rew.getDrawable());
                activityPlayerBinding.videoView.findViewById(R.id.progress_indicator_layout).setVisibility(View.GONE);

            } else {
                ImageButton play = (ImageButton) activityPlayerBinding.videoView.findViewById(R.id.exo_play);
                setImageButtonEnabled(true, play, play.getDrawable());
                ImageButton pause = (ImageButton) activityPlayerBinding.videoView.findViewById(R.id.exo_pause);
                setImageButtonEnabled(true, pause, pause.getDrawable());
                ImageButton fwd = (ImageButton) activityPlayerBinding.videoView.findViewById(R.id.exo_ffwd);
                setImageButtonEnabled(true, fwd, fwd.getDrawable());
                ImageButton rew = (ImageButton) activityPlayerBinding.videoView.findViewById(R.id.exo_rew);
                setImageButtonEnabled(true, rew, rew.getDrawable());
                activityPlayerBinding.videoView.findViewById(R.id.progress_indicator_layout).setVisibility(View.VISIBLE);
            }

        }
    }*/

    public void setImageButtonEnabled(boolean enabled, ImageButton item,
                                      Drawable originalIcon) {
        item.setClickable(enabled);
        item.setEnabled(enabled);
        Drawable icon = enabled ? convertDrawableToWhiteScale(originalIcon) : convertDrawableToGrayScale(originalIcon);
        item.setImageDrawable(icon);
    }


    public static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }

    public static Drawable convertDrawableToWhiteScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        return res;
    }

    int newChatCount = 0;

    public void updateNewChat(Services.ChatMessage note) {
        Log.i(getClass().getSimpleName(), "updateNewChat type:" + note.getMsgType() + " and message:" + note.getMessage());
        if (note.getMsgType().equalsIgnoreCase(MSG_SCORE)) {
            updateScorecard(note.getMessage());
            return;
        }

        if (chatListFragmentLand != null && chatListFragmentLand.isAdded())
            chatListFragmentLand.onMessageSend(note);

        if (chatListFragment != null && chatListFragment.isAdded()) {
            chatListFragment.onMessageSend(note);
            return;
        }
        /*this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (chatLayoutManager != null) {
                    Log.d(RheoTvApp.TAG, "Message : " + note.getMessage());
                    if (note.getSender() != null && !note.getSender().isEmpty()) {
                        Log.i(getClass().getName(), "first_chat_update " + note.getSender() + " and " + CommonUtils.getUserName(PlayerActivity.this) + " and " + RewardManager.getInstance().isFirstCommentRewardAvailable());
                        if (note.getSender().equals(CommonUtils.getUserName(PlayerActivity.this))) {
                            checkAndShowCommentReward();
                        }

                        // && !note.getUsername().equals(CommonUtils.getUserName(getBaseContext()))
                        if (note.getMsgType().equalsIgnoreCase("deleted") || note.getMsgType().equalsIgnoreCase("blocked")) {
                            String message = note.getMessage();
                            String sender = note.getSender();
                            chatListAdapter.removeChatItem(message, sender);
                            if (note.getMsgType().equalsIgnoreCase("blocked") && CommonUtils.isUserLoggedin() && CommonUtils.getUserName(PlayerActivity.this).equalsIgnoreCase(sender)) {
                                playerViewModel.canComment = false;
                            }
                        } else {
                            CommentChat commentChat = new CommentChat("", note.getMessage(), note.getSender(), note.getProfilePic());
                            chatListAdapter.addItem(commentChat);
//                    adjustChatRVHeight();
                            if (chatLayoutManager.findFirstVisibleItemPosition() != 0) {
                                newChatCount++;

                                showNewChatButton();
                            } else {
                                scrollChat();
                            }
                        }
                    }
                }
            }
        });*/

    }

    private Gson gson = new Gson();

    void updateScorecard(String score) {
        this.runOnUiThread(() -> {
            if (!shouldShowScoreCard) {
                shouldShowScoreCard = true;
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    activityPlayerBinding.scoreIndicatorLayout.setVisibility(View.VISIBLE);

                else {
                    activityPlayerBinding.scoreIndicatorGroup.setVisibility(View.VISIBLE);
                    ViewAnimationUtils.expand(activityPlayerBinding.postTitleHolderLayout, titleHolderLayoutHeight);
                }
            }

            ScoreboardResponse scoreboardResponse = gson.fromJson(score, ScoreboardResponse.class);
            playerViewModel.scoreboardResponse = scoreboardResponse;
            scorecardAdapter.addItems(scoreboardResponse.getTeamsList());
        });
    }

    BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtils.isNetworkConnected(context)) {
                onChatConnectionRequestReset();
            }
        }
    };

    boolean isRegisteredReceiverForNetworkChange;

    public void waitAndRequestReconnection() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            if (!isRegisteredReceiverForNetworkChange) {
                this.registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                isRegisteredReceiverForNetworkChange = true;
            }
        } else {
            onChatConnectionRequestReset();
        }
    }

    public void onChatConnectionRequestReset() {
        if (!isDestroyed()) {
            ChatHelper.getInstance(this).setPostChatJoinTask(this, currentPlayingPost);
        }
    }

    @Override
    public void openGamePage(CharSequence game) {
        initializeView();
        HashMap<String, Object> gameProperties = new HashMap<>(baseProperties);
        gameProperties.put("game", game.toString());
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_GAME_TAG_CLICKED, gameProperties);
        Intent intent = new Intent(this, UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, currentPlayingPost.getGame());
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, currentPlayingPost.getGameId());
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        startActivity(intent);
    }

   /* private void showNewChatButton() {
        if (activityPlayerBinding.unreadCommentLayout != null) {
            activityPlayerBinding.unreadCommentLayout.setVisibility(View.VISIBLE);
            activityPlayerBinding.unreadCommentCount.setText(newChatCount <= 50 ? newChatCount + "" : "50+");
            activityPlayerBinding.unreadCommentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollChat();
                    newChatCount = 0;
                    activityPlayerBinding.unreadCommentLayout.setVisibility(View.GONE);
                }
            });
        }
    }*/


    public void updateLivePeople(String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    liveViewersCountText.setText(Math.max(Integer.parseInt(value), currentPlayingPost.getMinViewers()) + " Watching");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(getClass().getName(), "updateLivePeople " + e.getMessage());
                }
            }
        });
    }

    private void paintChatBox() {
        String hint = "Send a nice message to broadcaster...";
        if (!CommonUtils.isUserLoggedin()) {
            hint = "Login to chat with streamer.";
//            loginDialogFragment = new LoginFragmentBottomDialog();
            //todo: login button
        }
        activityPlayerBinding.chatbox.setHint(hint);
    }

    @Override
    public void openLoginFlow() {
        if (loginDialogFragment.isAdded() || loginDialogFragment.isVisible()) {
            return;
        }
        try {
            loginDialogFragment.showNoAddToBackStack(this.getSupportFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addItemsInChat(String postId, List<CommentChat> commentChats) {

    }

    /*private void handleChatSender() {
        paintChatBox();
        activityPlayerBinding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkUtils.isNetworkConnected(PlayerActivity.this)) {
                    showToast("Please check you internet connection");
                    return;
                }

                addOrRemoveSafeChatMessage(true);
                if (!CommonUtils.isUserLoggedin()) {
                    openLoginFlow();
                    return;
                }

                if (playerViewModel.canComment) {

                    String message = activityPlayerBinding.chatbox.getText().toString();
                    if (message == null || message.isEmpty() || message.trim().length() == 0) {
                        return;
                    }
                    properties.remove("message_sticker");
                    SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_CHAT_SEND_CLICKED, properties.putValue("message", message));
                    adjustChatRVHeight();
                    activityPlayerBinding.chatbox.setText("");
                    CommonUtils.hideKeyboard(PlayerActivity.this);
                    scrollChat();
                    Log.d(RheoTvApp.TAG, "sending chattask");
                    ChatHelper.getInstance(PlayerActivity.this).sendMessage(PlayerActivity.this, message, currentPlayingPost.getId());
                } else {
                    CommonUtils.hideKeyboard(PlayerActivity.this);
                    activityPlayerBinding.chatbox.setText("");
                    Toast.makeText(PlayerActivity.this, "You are not allowed to post messages in this live stream.", Toast.LENGTH_SHORT).show();
                }
                *//*new ChatTask(PlayerActivity.this,
                        CommonUtils.getUserName(PlayerActivity.this), message, currentPlayingPost.getId()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*//*
            }
        });
//        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            fillHeart(activityPlayerBinding.heartImageView);
//            activityPlayerBinding.heartImageView.setOnClickListener(this::fadeAndScaleHeart);
//        }
    }*/

    private Runnable heartRunner;
    private Handler heartHandler = new Handler();

    private void fillHeart(View view) {
        ImageView imageView = (ImageView) view;
        Animatable animatable = (Animatable) imageView.getDrawable();
        animatable.start();

        heartRunner = () -> {
            Log.i(AppConstants.TAG, "checking_heart_state");
            animatable.stop();
            isFilling = false;
            scaleHeart(view);
        };
        heartHandler.postDelayed(heartRunner, 9000);
    }

    private void scaleHeart(View view) {
        Log.i(AppConstants.TAG, "scaling_Heart");
        ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 2f),
                PropertyValuesHolder.ofFloat("scaleY", 2f));
        scaleUp.setDuration(250);
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f));
        scaleDown.setDuration(250);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleDown).after(scaleUp);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }

    /*// not in use
    private void fadeAndScaleHeart(View view) {
        if (!NetworkUtils.isNetworkConnected(this)) {
            showToast("Please check you internet connection");
            return;
        }

        if (!CommonUtils.isUserLoggedin()) {
            openLoginFlow();
            return;
        }

        if (isFilling) {
            showToast("Filling Heart for you!");
            return;
        }

        String segmentUrl = null;
        try {
            HlsMediaPlaylist.Segment segment =
                    ((HlsManifest) Objects.requireNonNull(activityPlayerBinding.videoView.getPlayer().getCurrentManifest())).mediaPlaylist.segments.get(0);
            segmentUrl = segment.url;
        } catch (Exception e) {
            e.printStackTrace();
        }

        playerViewModel.addHeart(segmentUrl);
        String message = getString(R.string.give_heart_text).replace("Superb", playerViewModel.slangs.size() > 0 ? playerViewModel.slangs.get(getRandomNumberInRange(0, playerViewModel.slangs.size() - 1)) : heartEmojiText[getRandomNumberInRange(0, heartEmojiText.length - 1)]);
        ChatHelper.getInstance(PlayerActivity.this).sendMessage(PlayerActivity.this, message, currentPlayingPost.getId());

        isFilling = true;

        ObjectAnimator fade = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fade.setDuration(300);
        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 2.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 2.0f));
        scale.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fade, scale);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setAlpha(1.0f);
                view.setScaleX(1.0f);
                view.setScaleY(1.0f);
                fillHeart(view);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }*/

    /*private void addOrRemoveSafeChatMessage(boolean toRemove) {
        this.isFirstTime = false;
        Log.d(RheoTvApp.TAG, "adding safe chat");
        if (toRemove && isSafeChatAdded && chatListAdapter.getList().size() > 4) {
            isSafeChatAdded = false;
            List<CommentChat> chatList = chatListAdapter.getList();
            for (int index = 0; index < chatList.size(); index++) {
                CommentChat commentChat1 = chatList.get(index);
                if (commentChat1.getId().equals(CommonUtils.SAFE_CHAT_ID)) {
                    chatList.remove(index);
                    chatListAdapter.notifyItemRemoved(index);
                }
            }

        } else if (!toRemove) {
            String message = "Hey! \nI am star alien - Big Boss of this house.\uD83D\uDE0E.Be nice while having chat. \nEnjoy streaming and say 'hi' to the streamer. It's free! \n \uD83D\uDE4C";
            CommentChat commentChat = new CommentChat(CommonUtils.SAFE_CHAT_ID, message, "Star Alien", CommonUtils.STAR_ALIEN_PIC);
            isSafeChatAdded = true;
            chatListAdapter.addItem(commentChat);
//            adjustChatRVHeight();
            scrollChat();
        }
    }*/

    private void scrollChat() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activityPlayerBinding.userChatRV != null)
                    activityPlayerBinding.userChatRV.scrollToPosition(0);
            }
        }, 200l);
    }

    /*@Override
    public void addItemsInChat(String postId, List<CommentChat> comments) {
        isLoading = false;
        if (currentPlayingPost != null && currentPlayingPost.getId() != null && currentPlayingPost.getId().equalsIgnoreCase(postId)) {
            chatListAdapter.addItems(comments);
        }
    }*/


    LinearLayoutManager chatLayoutManager;

    @Override
    public void handleChat() {
        if (currentPlayingPost == null)
            return;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setUpTabs();
            if (mVideoRewardCountDownTimer != null)
                mVideoRewardCountDownTimer.cancel();
        } else {
            initializeRewardTimeForLand();
        }

        Log.i(getClass().getName(), "isInitialChatProcessed : " + isInitialChatProcessed + " and " + currentPlayingPost.getIsLive());
        if (!isInitialChatProcessed) {
            isInitialChatProcessed = true;
            if (currentPlayingPost.getIsLive()) {
//                ChatHelper.getInstance(PlayerActivity.this).getTotal(currentPlayingPost.getId(), PlayerActivity.this);
            }
        }

    }

    private static final String CHAT_FRAGMENT_TAG = "CHAT_LIST_FRAGMENT";


    public void setUpViewersRequest() {
        if (!isDestroyed()) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
//                    ChatHelper.getInstance(PlayerActivity.this).getTotal(currentPlayingPost.getId(), PlayerActivity.this);
                }
            }, 5000);
        }
    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
        playerViewModel.loadDailyRewards();
        if (chatListFragment != null) {
            chatListFragment.checkChatBoxState();
        }

        if (chatListFragmentLand != null)
            chatListFragmentLand.checkChatBoxState();

        if (videoRewardFragment != null && videoRewardFragment.isAdded())
            videoRewardFragment.setRewards();

        if (requestPlayFragment != null && requestPlayFragment.isAdded())
            requestPlayFragment.setupViews();

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(AppConstants.UPDATE_STORY_BROADCAST_FILTER));
    }

    @Override
    public void onLoginDialogClose() {

    }

    private class ChatTask extends AsyncTask<String, Void, Boolean> {
        Context context;

        String message;
        String postId;

        public ChatTask(Context context, String name, String message, String postId) {
            this.context = context;
            this.message = message;
            this.postId = postId;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(RheoTvApp.TAG, "sent message chat task");
            return ChatHelper.getInstance(context).sendMessage(PlayerActivity.this, message, postId);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Log.d(RheoTvApp.TAG, "message sent");
            } else {
                //Todo try again to send
                Log.d(RheoTvApp.TAG, "something went wrong");
            }
        }
    }


    private void setDataAndViews() {
        playerViewModel.setPlayerData(postId, this);

        updateDuration();
    }

    private void updateDuration() {
        if (currentPlayingPost != null) {
            if (TimeUtils.hasStreamNotStarted(currentPlayingPost.getStartFrom())) {
                activityPlayerBinding.durationTV.setVisibility(View.GONE);
            } else {
                activityPlayerBinding.durationTV.setVisibility(View.VISIBLE);
                activityPlayerBinding.durationTV.setText(currentPlayingPost.getLeftOutTime());
            }
        }
    }


    private void subscribeToLiveData() {
        playerViewModel.getBlogListLiveData().observe(this, blogs -> playerViewModel.addBlogItemsToList(blogs));
    }


    private void updateList() {
        playerListAdapter.addItems(playerViewModel.getBlogObservableList());
    }

    private void updatePlayingStatus() {
//        playerListAdapter.notifyPlayingItemChange();
        playerListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (activityPlayerBinding.videoView.getPlayer() != null) {
            outState.putLong("current_state", activityPlayerBinding.videoView.getPlayer().getCurrentWindowIndex());
            if (currentPlayingPost != null && currentPlayingPost.getIsLive()) {
                if (isPlayerDraggedBehind)
                    outState.putLong(ARG_PLAYBACK_POSITION, activityPlayerBinding.videoView.getPlayer().getContentPosition());
            } else {
                outState.putLong(ARG_PLAYBACK_POSITION, activityPlayerBinding.videoView.getPlayer().getContentPosition());
            }

        }
        outState.putInt(ARG_SELECTED_TAB, mSelectedTab);
        outState.putString("quality_format", qualityFormat);
        outState.putBoolean(ARG_IS_TWO_SECOND_COUNTED, isTwoSecondViewCounted);
        outState.putLong(ARG_TIME_UNTIL_FINISH, TIME_UNTIL_FINISH);
        super.onSaveInstanceState(outState);
    }


    boolean isPortraitFullScreen;


    private void adjustPlayerHeight(int orientation) {

        ViewGroup.LayoutParams layoutParams = activityPlayerBinding.videoView.getLayoutParams();
        int width = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int oldHeight = layoutParams.height;
        int height;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            height = (width * 9) / 16;
        } else {
            height = screenHeight;
        }

        //activityPlayerBinding.youtubeDoubleTap.setVisibility(View.INVISIBLE);
        /*ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) activityPlayerBinding.youtubeDoubleTap.getLayoutParams();
        lp.height = height;
        lp.width = width;
        lp.topToTop = activityPlayerBinding.videoView.getId();
        lp.bottomToBottom = activityPlayerBinding.videoView.getId();
        lp.leftToLeft = activityPlayerBinding.videoView.getId();
        lp.rightToRight = activityPlayerBinding.videoView.getId();
        activityPlayerBinding.youtubeDoubleTap.setLayoutParams(lp);
        activityPlayerBinding.youtubeDoubleTap.requestLayout();*/

        /*activityPlayerBinding.youtubeDoubleTap.getLayoutParams().width = width;
        activityPlayerBinding.youtubeDoubleTap.getLayoutParams().height = height;
        activityPlayerBinding.youtubeDoubleTap.forceLayout();
        activityPlayerBinding.youtubeDoubleTap.requestLayout();*/

        //layoutParams.height = height;
        //activityPlayerBinding.videoView.setLayoutParams(layoutParams);
        /*activityPlayerBinding.youtubeDoubleTap.getLayoutParams().width = width;
        activityPlayerBinding.youtubeDoubleTap.setVisibility(View.INVISIBLE);
        activityPlayerBinding.youtubeDoubleTap.invalidate();*/
        ValueAnimator va = ValueAnimator.ofInt(oldHeight, height);
        va.setDuration(400);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                activityPlayerBinding.videoView.getLayoutParams().height = value.intValue();
                activityPlayerBinding.videoView.requestLayout();
            }
        });
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isPortraitFullScreen = !isPortraitFullScreen;

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        va.start();
    }

    private void adjustPlayerHeight() {

        ViewGroup.LayoutParams layoutParams = activityPlayerBinding.videoView.getLayoutParams();
        int width = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int oldHeight = layoutParams.height;
        int height;

        if (!isPortraitFullScreen) {
            height = screenHeight;
        } else {
            height = width;
        }
        //layoutParams.height = height;
        //activityPlayerBinding.videoView.setLayoutParams(layoutParams);
        ValueAnimator va = ValueAnimator.ofInt(oldHeight, height);
        va.setDuration(400);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                activityPlayerBinding.videoView.getLayoutParams().height = value.intValue();
                activityPlayerBinding.videoView.requestLayout();
            }
        });
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isPortraitFullScreen = !isPortraitFullScreen;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        va.start();
    }


    public int getBottomBarHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustPlayerHeight(newConfig.orientation);
        handleChatButtonState(newConfig.orientation);
        handleStreamerInfoLayout(newConfig.orientation);
        handleScoreboard(newConfig.orientation);
        activityPlayerBinding.youtubeDoubleTap.invalidate();
        if (chatListFragment != null && chatListFragment.isAdded()) {
            chatListFragment.updateHeartCounter(currentPlayingPost.getHeartCount());
        }

        updateConfig();

/*//        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            ((ImageButton) activityPlayerBinding.videoView.findViewById(R.id.exo_full_screen)).setBackground(ContextCompat.getDrawable(this, R.drawable.collapse));
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            ((ImageButton) activityPlayerBinding.videoView.findViewById(R.id.exo_full_screen)).setBackground(ContextCompat.getDrawable(this, R.drawable.expand));
        }*/
    }

    private void handleScoreboard(int orientation) {
        if (!shouldShowScoreCard) return;
        activityPlayerBinding.scorecardLayout.setVisibility(View.GONE);
        activityPlayerBinding.scorecardLayoutLand.setVisibility(View.GONE);

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            activityPlayerBinding.scoreIndicatorLayout.setVisibility(View.GONE);
        } else {
            if (!isControllerShowing)
                activityPlayerBinding.scoreIndicatorLayout.setVisibility(View.VISIBLE);
            else
                activityPlayerBinding.scoreIndicatorLayout.setVisibility(View.GONE);
        }
    }


    private String updateUrlWithCurrentFormat(String urlWithoutParameters) {
        String[] parts = urlWithoutParameters.split("\\/");
        String oldFormat = parts[parts.length - 2];
        if (qualityFormat.equalsIgnoreCase("auto") && currentPlayingPost != null && !currentPlayingPost.getIsLive()) {
            qualityFormat = "medium";
            setCheckId();
        }
        if (oldFormat.equalsIgnoreCase("auto") || oldFormat.equalsIgnoreCase("medium") || oldFormat.equalsIgnoreCase("low") || oldFormat.equalsIgnoreCase("high")) {

            return urlWithoutParameters.replace(oldFormat, qualityFormat);
        }
        return urlWithoutParameters;
    }

    private void setQualityFormat(String url) {
        String[] parts = url.split("\\/");
        String currentFormat = parts[parts.length - 2];

        if (currentFormat == null) {
            return;
        }
        switch (currentFormat) {
            case "auto":
                qualityFormat = "auto";
                break;
            case "high":
                qualityFormat = "high";
                break;
            case "medium":
                qualityFormat = "medium";
                break;
            case "low":
                qualityFormat = "low";
                break;
        }
        setCheckId();
    }

    private void stopTimer() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
                Log.d("TIMER", "stopped_timer");

                timeSentRecord.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    /*private void startTimer() {
        timer = new MojoTimer();
//        showShimmerAnimation();
        Log.d("TIMER", "started");
        if (timeElapsedOnWidget > 0) {
            timeElapsed = timeElapsedOnWidget;
            timeElapsedOnWidget = 0;
        }
        initTimerTask();
        timer.schedule(timerTask, 0, 1000l);
    }*/


    /*private void initTimerTask() {

        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (player != null) {
                    long currentDuration = player.getCurrentPosition() / 1000;
                    if (timeElapsed == 0 || timeElapsed == 5 || timeElapsed == 30 || timeElapsed == 300 || timeElapsed == 1800) {
                        Log.i("Event watch Stream", timeElapsed + " sec :: " + currentDuration + " ms");
                        SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_WATCH_STREAM, properties
                                .putValue("time_elapsed", timeElapsed).putValue("player_position", currentDuration));
                    }
                    timeElapsed += 5;
                    if (currentDuration > 1 && !timeSentRecord.contains(PlayerActivity.TIME_SENT.TwoSecond)) {
                        timeSentRecord.add(PlayerActivity.TIME_SENT.TwoSecond);
                        makeViewApiCall(2, currentDuration);
                    }
                    if (currentDuration % 10 == 0 || currentDuration < 20) {
                        if (currentDuration > 1 && currentDuration % 10 == 0)
                            makeViewApiCall(10, currentDuration);
                        try {
                            AnalyticsHelper.getInstance(getNonUiContext()).sendVideoPlay(
                                    currentDuration,
                                    currentPlayingPost.getAuthor().getUser().getUsername(),
                                    String.valueOf(currentPlayingPost.getAuthor().getUser().getId()),
                                    currentPlayingPost.getId(),
                                    currentPlayingPost.getTitle(),
                                    currentPlayingPost.getHashtags(),
                                    currentDuration,
                                    player.getDuration() / 1000,
                                    topSource,
                                    topSourceHomeDynamicTab,
                                    topSourceHomeCardType,
                                    extraSharedLinkParam);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (currentDuration % 30 == 0) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateDuration();
                            }
                        }, 1000l);
                    }
                }

            }
        };
    }*/

    private void makeViewApiCall(int duration, long timeElapsed) {
        Log.i(getClass().getName(), "makeViewApiCall at " + System.currentTimeMillis() + " for " + duration + "secs and player time" + timeElapsed);
        Result res = getCurrentPlayingPost();
        if (res != null) {
            String device_id = CommonUtils.getDevId(getNonUiContext());
//            playerViewModel.getDataManager().postVideoView(res, device_id, duration, (int) timeElapsed)
//                    .enqueue(new Callback<ResponseBody>() {
//                        @Override
//                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//
//                        }
//
//                        @Override
//                        public void onFailure(Call<ResponseBody> call, Throwable t) {
//                        }
//                    });
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        String url = uri.toString();
        DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory(RheoTvApp.EXOPLAYER_AGENT);

        HttpDataSource.RequestProperties properties = factory.getDefaultRequestProperties();
        properties.set("app_version", Integer.toString(BuildConfig.VERSION_CODE));

        if (url.toLowerCase().contains(".m3u8"))
            return new HlsMediaSource.Factory(factory).
                    createMediaSource(uri);
        //return new ExtractorMediaSource(uri, new DefaultHttpDataSourceFactory(RheoTvApp.EXOPLAYER_AGENT), , null, null);
        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(RheoTvApp.EXOPLAYER_AGENT)).setExtractorsFactory(new DefaultExtractorsFactory()
                .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_IGNORE_AAC_STREAM)).createMediaSource(uri);

    }

    private boolean isVisible = false;
    public boolean isConnectionRequestMade = false;


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                showImmersive();
            }
//            else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//                hideImmersive();
//            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }

//        hideShowCountTimer(true);
    }

    @Override
    public void onStop() {
        super.onStop();
//        ChatHelper.getInstance(this).closeConnection();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
        isVisible = false;
//        if (mVideoRewardCountDownTimer != null)
//            mVideoRewardCountDownTimer.cancel();
        Log.i(getClass().getName(), "in_onStop: " + streamTimer.isRunning() + " and " + streamTimer.isPaused());
    }

    private void showImmersive() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void hideImmersive() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

//    private void hideShowCountTimer(boolean hideTimer) {
//        if (mVideoRewardCountDownTimer == null) return;
//        Log.i(getClass().getName(), "hideShowCountTimer " + hideTimer);
//        if (hideTimer) {
//            mVideoRewardCountDownTimer.cancel();
//
//        } else {
//            mVideoRewardCountDownTimer.start();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (networkStateReceiver != null) {
                unregisterReceiver(networkStateReceiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (countDownTimer != null)
            countDownTimer.cancel();
//        if (mVideoRewardCountDownTimer != null)
//            mVideoRewardCountDownTimer.cancel();
        if (streamTimer != null)
            streamTimer.stopTimer();

//        ChatHelper.getInstance(this).closeConnection();
    }

    private void releasePlayer() {
        if (player != null) {
            updateResumePosition();
            playWhenReady = player.getPlayWhenReady();
            player.stop();
            player.release();
            player = null;
//            stopTimer();
//            Utils.releaseWakeLock();
            // if (debugTextViewHelper != null)
            //    debugTextViewHelper.stop();
            Log.i(getClass().getName(), "releasePlayer: playbackPosition - " + playbackPosition + " currentWindow " + currentWindow + " playWhenReady " + playWhenReady);
            streamTimer.pauseTimer();
        }
    }

    private void resumePlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }


    @Override
    public void playVideo(String postId) {
        try {
            streamTimer.stopTimer();
            player.release();
            player = null;
            resumePosition = 0;
            restartActivity(postId);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playNext(String postId) {
        player.release();
        player = null;

        /*initializePlayer();
        playerListAdapter.notifyDataSetChanged();
        //chatListAdapter.clearChatNoteList();
        isFirstTime = true;
        isInitialChatProcessed = false;
        handleChat();*/
    }

    public void restartActivity(String postId) {
        Intent intent = getIntent().putExtra(AppConstants.KEY_POST_ID, postId);
        startActivity(intent);
    }

    @Override
    public void openPlayList() {
        Log.d("TAGGER", "playlist opened");
        topSource = "bottom_navigation";
        topSourceHomeCardType = "";
        topSourceHomeDynamicTab = "";
    }

    @Override
    public void onExitClicked() {
        isExitBtnClicked = true;
        onBackPressed();
    }

    @Override
    public HashMap<String, Object> getProperties() {
        return baseProperties;
    }


    @Override
    public void handleExpandCollapse(boolean expand) {
        if (expand) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onAuthorClicked(String authorUserName) {
        /*initializeView();*/
        Intent intent = ProfileActivity.getCallingIntent(this);
        intent.putExtra("author_name", currentPlayingPost.getAuthor().getUser().getUsername());
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        startActivity(intent);
        //getSupportFragmentManager().beginTransaction().add(R.id.frame_container, ProfileContainerFragment.newInstance(authorUserName), "Author").commit();
    }


    @Override
    public void setDeepLinkPost(List<Result> list) {
        /*try {
            if (list != null && list.size() > 0) {
                setCurrentPlayingPost(list.get(0));
            }
            setUp();
            itemId = currentPlayingPost.getId();
            setDataAndViews(list);
            subscribeToLiveData();
            updateList();
            initializePlayer(getCurrentPlayingPost());

        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }*/
    }


    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    public void onItemClick(String id) {
        /*if (playerViewModel.getCurrentPlayingPostId() != null) {
            if (playerViewModel.getCurrentPlayingPostId().equalsIgnoreCase(id)) {
                //do nothing
            } else {
                setCurrentPlayingPost(playerViewModel.playPostWithId(id));
                player.release();
                player = null;
                initializePlayer();
                playerListAdapter.notifyDataSetChanged();
            }
        }*/
    }

    @Override
    public void onBackPressed() {
        if (isExitBtnClicked) {
            finishCheck();
            return;
        }

        if (isStickersShowing) {
            animateStickersRvOut();
            return;
        }
       /* if (isStickersShowing && chatListFragment != null && chatListFragment.isAdded()) {
            chatListFragment.animateStickersRvOut();
            return;
        }*/
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && !isExitBtnClicked) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }
        if (isVideoPlayingStarted && player != null) {
            baseProperties.put("time_elapsed", videoTimeElapsed);
            baseProperties.put("player_position", (player.getCurrentPosition() / 1000));
            SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_WATCH_STREAM_END, baseProperties);
        }

//        if (CommonUtils.isUserLoggedin() && RewardManager.getInstance().isTenMinuteStreamRewardAvailable() && shouldShowTenMinuteAlert) {
//            showRewardAlert();
//            return;
//        }

        if (currentPlayingPost != null) {
            askOverlayPermissionAndReward();
        } else {
            if (isFromDeepLink) {
                startTabContainerActivity();
            } else
                super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(getClass().getName(), "onActivityResult_player_called");
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    initializeView();
                    finishCheck();
                } else {
                    Toast.makeText(this,
                            "Draw over other app permission not available. Closing the application",
                            Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    public void checkForOverlayAndShowOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            showPermissionRequiredDialog();
        } else {
            initializeView();
        }
    }

    private void askOverlayPermissionAndReward() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                long lastShownTimeDiff = (System.currentTimeMillis() - sharedPrefsUtils.getLongPreference(this, SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, 0));
                if (lastShownTimeDiff >= TimeUtils.MILLIS_IN_DAY) {
                    sharedPrefsUtils.setLongPreference(this, SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, System.currentTimeMillis());
                    showPermissionRequiredDialog();
                } else if (CommonUtils.isUserLoggedin() && RewardManager.getInstance().isTenMinuteStreamRewardAvailable() && shouldShowTenMinuteAlert && currentPlayingPost.getIsLive()) {
                    showRewardAlert();
                } else {
                    finishCheck();
                }
            } else {
                initializeView();
                if (isFromPlayerHeadWidget && !((RheoTvApp) getApplication()).isActivityInBackStack(HomeActivity.class)) {
                    startTabContainerActivity();
                }
                finishCheck();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    public void checkPermissionAndInitializeView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            long lastShownTimeDiff = (System.currentTimeMillis() - sharedPrefsUtils.getLongPreference(this, SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, 0));
            if (lastShownTimeDiff >= TimeUtils.MILLIS_IN_DAY) {
                sharedPrefsUtils.setLongPreference(this, SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, System.currentTimeMillis());
                showPermissionRequiredDialog();
            } else {
                super.onBackPressed();
            }
            //If the draw over permission is not available open the settings screen
            //to grant the permission.

        } else {
            initializeView();
            if (isFromPlayerHeadWidget && !((RheoTvApp) getApplication()).isActivityInBackStack(HomeActivity.class)) {
                startTabContainerActivity();
            }
            super.onBackPressed();
        }

    }

    private void startTabContainerActivity() {
        Intent intent = TabContainerActivity.newIntent(this);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    AlertDialog overlayAlertDialog;

    private void showPermissionRequiredDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setCancelable(true);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.overlay_permission_dialog_layout, null);

        dialogView.findViewById(R.id.allow_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);

                    SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_OVERLAY_PERMISSION_GO_TO_SETTINGS_CLICK, baseProperties);

                    if (overlayAlertDialog != null)
                        overlayAlertDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialogView.findViewById(R.id.cancel_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_OVERLAY_PERMISSION_CANCEL_CLICK, baseProperties);
                if (overlayAlertDialog != null)
                    overlayAlertDialog.dismiss();
            }
        });
        builder.setView(dialogView);

        overlayAlertDialog = builder.show();

        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_OVERLAY_PERMISSION_ALERT_DIALOG, baseProperties);

    }

    Intent playerHeadService;

    private void initializeView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                startPlayerService();
            }
        } else {
            startPlayerService();
        }
        finishCheck();
    }

    private void startPlayerService() {
        if (currentPlayingPost == null) return;
        String authorName = "";

        if (currentPlayingPost.getAuthor() != null) {
            authorName = currentPlayingPost.getAuthor().getUser().getUserFullName();
        }

        //        Log.i(getClass().getName(), "initializeView_service: " + (currentPlayingPost == null) + " getVideoUrl " + (currentPlayingPost.getVideoUrl() == null));
        if (currentPlayingPost != null && currentPlayingPost.getVideoUrl() != null && currentPlayingPost.getAuthor() != null && currentPlayingPost.getAuthor().getUser() != null) {
            PlayerHeadServiceHelper.getInstance().startPlayerHeadService(
                    currentPlayingPost.getAuthor().getUser().getUsername(),
                    currentPlayingPost.getGame(),
                    currentPlayingPost.getVideoUrl(),
                    currentPlayingPost.getId(),
                    timeElapsed,
                    TIME_UNTIL_FINISH,
                    currentPlayingPost.getAuthor().getUser().getId().toString(),
                    currentPlayingPost.getIsLive(),
                    currentPlayingPost.getLanguage(),
                    currentPlayingPost.getTitle(),
                    authorName,
                    currentPlayingPost.getGameId(),
                    player != null ? player.getCurrentWindowIndex() : 0,
                    player != null ? player.getCurrentPosition() : 0
            );
        }
    }

    enum TIME_SENT {
        First,
        TwoSecond,
        FiveSecond,
        ThirtySecond,
        SixtySecond
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showToastLong(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void showReportPostSuccessToast() {
        Toast.makeText(this, getString(R.string.post_report_success), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onUserProfileClicked(String username) {
        onAuthorClicked(username);
    }




    /*public void showStickersRV() {
        if (rvHeight == 0) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            rvHeight = metrics.heightPixels / 3;
        }

        ValueAnimator va = ValueAnimator.ofInt(0, rvHeight);
        va.setDuration(400);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                activityPlayerBinding.stickersRv.getLayoutParams().height = value.intValue();
                activityPlayerBinding.stickersRv.requestLayout();
            }
        });
        va.start();
    }


    public void hideStickersRv() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int rvHeight = metrics.heightPixels / 3;

        ValueAnimator va = ValueAnimator.ofInt(rvHeight, 0);
        va.setDuration(400);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                activityPlayerBinding.stickersRv.getLayoutParams().height = value.intValue();
                activityPlayerBinding.stickersRv.requestLayout();
            }
        });
        va.start();
    }*/

    public void animateStickersRvIn() {
        isStickersShowing = true;
        CommonUtils.hideKeyboard(PlayerActivity.this);
        activityPlayerBinding.stickerOverlay.setVisibility(View.VISIBLE);
        activityPlayerBinding.stickerOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateStickersRvOut();
            }
        });
        activityPlayerBinding.closeStickers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateStickersRvOut();
            }
        });
        if (stickersLayoutHeight == 0) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                stickersLayoutHeight = getResources().getDisplayMetrics().heightPixels / 3;
            } else {
                stickersLayoutHeight = getResources().getDisplayMetrics().heightPixels / 2;
            }
        }
        activityPlayerBinding.stickersLayout.setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(activityPlayerBinding.stickersLayout, View.TRANSLATION_Y, stickersLayoutHeight, 0);
        anim.setDuration(300);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setUpStickersRV();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        anim.start();
    }

    public void animateStickersRvOut() {
        isStickersShowing = false;
        activityPlayerBinding.stickerOverlay.setVisibility(View.GONE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(activityPlayerBinding.stickersLayout, View.TRANSLATION_Y, 0, stickersLayoutHeight);
        anim.setDuration(300);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                activityPlayerBinding.stickersLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        anim.start();
    }

    StickersRvAdapter stickersAdapter;

    boolean isStickersLoading;

    boolean isStickersShowing;

    private void setUpStickersRV() {
        if (stickersAdapter == null) {
            int spanCount = 3;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                spanCount = 5;
            }
            GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
            activityPlayerBinding.stickersRv.setLayoutManager(layoutManager);
            stickersAdapter = new StickersRvAdapter(stickersSize);
            activityPlayerBinding.stickersRv.setAdapter(stickersAdapter);
            stickersAdapter.setmListener(this);
            activityPlayerBinding.stickersLoading.setVisibility(View.VISIBLE);
            if (currentPlayingPost != null)
                playerViewModel.loadStickers(currentPlayingPost.getId());
            activityPlayerBinding.stickersRv.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Load more if we have reach the end to the recyclerView
                    if (!PlayerActivity.this.isStickersLoading && playerViewModel.stickersNextUrl != null && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        isStickersLoading = true;
                        stickersAdapter.setShowLoading(true);
                        playerViewModel.loadStickers(currentPlayingPost.getId());
                    }
                }
            });
        }

    }

    @Override
    public void onStickersLoadComplete(List<Sticker> stickers) {
        activityPlayerBinding.stickersLoading.setVisibility(View.GONE);
        isStickersLoading = false;
        stickersAdapter.setShowLoading(false);
        stickersAdapter.setStickers(stickers);
    }

    @Override
    public void onStickerClicked(String stickerUrl, String stickerId) {
        HashMap<String, Object> prp = baseProperties;
        if (!NetworkUtils.isNetworkConnected(PlayerActivity.this)) {
            showToast("Please check you internet connection");
            return;
        }

        animateStickersRvOut();
        if (!CommonUtils.isUserLoggedin()) {
            openLoginFlow();
            return;
        }
        prp.remove("message");
        if (!isInitialStrikerSent) {
            isInitialStrikerSent = true;
            prp.put("message_sticker", stickerId);
            SegmentTracker.getInstance(PlayerActivity.this).trackEvent(SegmentConstants.EVENT_CHAT_STICKER_FIRST_SENT, prp);
        }

        //activityPlayerBinding.chatbox.setText("");

        scrollChat();
        Log.d(RheoTvApp.TAG, "sending chattask");
        ChatHelper.getInstance(PlayerActivity.this).sendMessage(PlayerActivity.this, stickerUrl, currentPlayingPost.getId());
    }


    public void onDeleteCommentClick(int position, String message, String username, String messageType) {
        //chatListAdapter.removeChatItem(position);
        ChatHelper.getInstance(PlayerActivity.this).sendDeletedMessage(PlayerActivity.this, message, username, currentPlayingPost.getId(), messageType);
    }

    @Override
    public void onReportButtonClick(int position, String username, String comment) {
        HashMap<String, Object> properties = new HashMap<>(this.baseProperties);

        if (CommonUtils.isUserLoggedin() && CommonUtils.getUserName(this).equalsIgnoreCase(currentPlayingPost.getAuthor().getUser().getUsername())) {
            properties.put("reported_comment_user", username);
            properties.put("reported_comment", comment);
            SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_REPORT_COMMENT_ON_SELF_STREAM, properties);
            onDeleteCommentClick(position, comment, username, "deleted");
            playerViewModel.reportComment(currentPlayingPost.getId(), username, comment);
        } else {
            properties.put("reported_comment_user", username);
            properties.put("reported_comment", comment);
            SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_REPORT_COMMENT, properties);
            playerViewModel.reportComment(currentPlayingPost.getId(), username, comment);
        }
    }

    @Override
    public void onBlockUserClicked(int position, String username, String comment) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("blocked_user", username);
        properties.put("blocked_msg", comment);
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_BLOCK_USER, properties);
        onDeleteCommentClick(position, comment, username, "blocked");
        playerViewModel.blockUser(currentPlayingPost.getId(), username, comment);
    }

    @Override
    public void onBlockUserSuccess() {
        Toast.makeText(this, "Blocked user successfully", Toast.LENGTH_SHORT).show();
    }

    private void checkAndShowVideoReward() {
        Log.i(getClass().getSimpleName(), "checkAndShowVideoReward");
        if (!isVisible) return;
        if (RewardManager.getInstance().isTenMinuteStreamRewardAvailable())
            if (CommonUtils.isUserLoggedin())
                showScratchCardNotification(REWARD_TYPE_TEN_MINUTE_STREAM);
            else
                openLoginFlow(getString(R.string.new_reward_message));
    }

    private void checkAndShowCommentReward() {
        if (RewardManager.getInstance().isFirstCommentRewardAvailable())
            if (CommonUtils.isUserLoggedin())
                showScratchCardNotification(REWARD_TYPE_FIRST_COMMENT);
            else {
                openLoginFlow(getString(R.string.new_reward_message));
            }
    }

    private void showScratchCardNotification(final String... suggestReward) {
//        try {
//            Reward reward = RewardManager.getInstance().getAvailableReward(suggestReward);
//            if (reward == null || !CommonUtils.isUserLoggedin() || activityPlayerBinding.container == null)
//                return;
//            AnimatedScratchCardView bottomScratchCardView = activityPlayerBinding.container.findViewWithTag(BottomScratchCardView.TAG);
//            activityPlayerBinding.container.removeView(bottomScratchCardView);
//            if (bottomScratchCardView == null) {
//                bottomScratchCardView = new AnimatedScratchCardView(this);
//            }
//            int scratchCardImage = bottomScratchCardView.setRandomScratchCard();
//            bottomScratchCardView.setAction(AnimatedScratchCardView.Companion.getSlideInAnimation(), (view -> {
//                scratchDialogFragment = ScratchDialogFragment.getInstance(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER, reward, scratchCardImage);
//                if ((scratchDialogFragment.isAdded() || scratchDialogFragment.isVisible())) {
//                    return;
//                }
//                scratchDialogFragment.show(this.getSupportFragmentManager(), AppConstants.SCRATCH_FRAGMENT_TAG, PlayerActivity.this, suggestReward);
//            }));
//            bottomScratchCardView.addTo(activityPlayerBinding.container, mScratchCardBottomMargin,
//                    AnimatedScratchCardView.Companion.getSlideOutAnimation(), () -> {
//                        playerViewModel.updateScratchCardStatusShown(reward.getId());
//                        Log.i(getClass().getSimpleName(), "showingScratchCard_now");
//                    });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onScratchRevealed(String rewardId) {
        if (shouldShowTenMinuteAlert) shouldShowTenMinuteAlert = false;
        Log.i(getClass().getName(), "onScratchRevealed " + rewardId);
        playerViewModel.updateScratchCard(rewardId);
    }

    private void showRewardAlert() {
        if (getSupportFragmentManager().findFragmentByTag(AppConstants.ALERT_VIDEO_REWARD_TAG) != null)
            videoAlertDialogFragment = (VideoAlertDialogFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.ALERT_VIDEO_REWARD_TAG);
        else
            videoAlertDialogFragment = VideoAlertDialogFragment.newInstance(TIME_UNTIL_FINISH, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        if (videoAlertDialogFragment != null && (videoAlertDialogFragment.isAdded() || videoAlertDialogFragment.isVisible()))
            return;
        videoAlertDialogFragment.show(this.getSupportFragmentManager(), AppConstants.ALERT_VIDEO_REWARD_TAG, TIME_UNTIL_FINISH);
    }

    @SuppressWarnings("SameParameterValue")
    private void showTooltip(String message, View view, int gravity, long delay, String rewardType) {
        new Handler().postDelayed(() -> {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                new SimpleTooltip.Builder(this)
                        .anchorView(view)
                        .text(message)
                        .gravity(gravity)
                        .animated(true)
                        .textColor(ContextCompat.getColor(this, android.R.color.white))
                        .arrowColor(ContextCompat.getColor(this, R.color.reward_background_color))
                        .backgroundColor(ContextCompat.getColor(this, R.color.reward_background_color))
                        .transparentOverlay(false)
                        .build()
                        .show();
                checkAndDisableTooltip(rewardType);
            }
        }, delay);
    }

    private void checkAndDisableTooltip(String rewardType) {
        switch (rewardType) {
            case REWARD_TYPE_TEN_MINUTE_STREAM:
                if (CommonUtils.showVideoTooltip(PlayerActivity.this))
                    CommonUtils.disableVideoTooltip(PlayerActivity.this);
                break;

            case REWARD_TYPE_FIRST_COMMENT:
                if (CommonUtils.showCommentTooltip(PlayerActivity.this))
                    CommonUtils.disableCommentTooltip(PlayerActivity.this);
                break;
        }
    }

    public void openLoginFlow(String rewardMessage) {
        if (loginDialogFragment.isAdded() || loginDialogFragment.isVisible()) {
            return;
        }
        try {
            loginDialogFragment.setRewardText(rewardMessage);
            loginDialogFragment.showNoAddToBackStack(this.getSupportFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoAlertStayClicked() {
        shouldShowTenMinuteAlert = false;
        // do nothing for now
    }

    @Override
    public void onVideoAlertExitClicked() {
        shouldShowTenMinuteAlert = false;
        onBackPressed();
    }

    @Override
    public void checkRewardAvailable() {
        Log.i(getClass().getSimpleName(), "checkingRewardAvailable");
        if (videoRewardFragment != null && videoRewardFragment.isAdded())
            videoRewardFragment.setRewards();
    }

    private boolean isFirstWatchEventTracked = false;

    long videoTimeElapsed = 0;

    private Hourglass streamTimer = new Hourglass(new Date().getTime(), 1000) {
        @Override
        public void onTimerTick(long timeRemaining, long passedTime) {


            long exoPosition = player != null ? player.getCurrentPosition() / 1000 : -1;
            videoTimeElapsed = passedTime / 1000;
            if (videoTimeElapsed == 30) {
                trackEventWatchStream30Secs(videoTimeElapsed);
            } else if (videoTimeElapsed == 300) {
                trackEventWatchStream5mins(videoTimeElapsed);
            }

            if (videoTimeElapsed == 2) {
                if (!isTwoSecondViewCounted) {
                    Log.i(getClass().getName(), "hit_2nd_api : " + videoTimeElapsed);
                    isTwoSecondViewCounted = true;
                    makeViewApiCall(2, exoPosition);
                }
            } else if (videoTimeElapsed > 2 && videoTimeElapsed % 10 == 2) {
                Log.i(getClass().getName(), "hit_12nd_api : " + videoTimeElapsed);
                makeViewApiCall(10, exoPosition);
                sendVideoAnalytics(exoPosition);
            }

            delayViewUpdate(videoTimeElapsed);
        }

        @Override
        public void onTimerFinish() {

        }
    };


    private void sendVideoAnalytics(long exoPosition) {
        try {
            AnalyticsHelper.getInstance(getNonUiContext()).sendVideoPlay(
                    exoPosition,
                    currentPlayingPost.getAuthor().getUser().getUsername(),
                    String.valueOf(currentPlayingPost.getAuthor().getUser().getId()),
                    currentPlayingPost.getId(),
                    currentPlayingPost.getTitle(),
                    currentPlayingPost.getHashtags(),
                    exoPosition,
                    player.getDuration() / 1000,
                    topSource,
                    topSourceHomeDynamicTab,
                    topSourceHomeCardType,
                    extraSharedLinkParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delayViewUpdate(long timePassed) {
        if (timePassed % 30 == 0) {
            handler.postDelayed(this::updateDuration, 1000);
        }
    }

    private String[] tabName = new String[]{SegmentConstants.SCREEN_NAME_VIDEO_PLAYER_CHAT, SegmentConstants.SCREEN_NAME_REQUEST_TO_PLAY, SegmentConstants.SCREEN_NAME_VIDEO_WATCH_REWARD};
    private RewardsTabAdapter tabAdapter;

    private void setUpTabs() {
//        if (chatListFragment != null && chatListFragment.isAdded()) return;
        Log.i(getClass().getName(), "handling_chat_tabs " + currentPlayingPost + " tab_pos: " + mSelectedTab);

        int oldPos = mSelectedTab;
        tabAdapter = new RewardsTabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(chatListFragment = ChatListFragment.newInstance(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER));
//        tabAdapter.addFragment(requestPlayFragment = RequestPlayFragment.newInstance(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER));
        tabAdapter.addFragment(videoRewardFragment = VideoRewardFragment.newInstance(TIME_UNTIL_FINISH, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER));

        activityPlayerBinding.viewpager.setAdapter(tabAdapter);
        activityPlayerBinding.viewpager.setOffscreenPageLimit(2);
        activityPlayerBinding.tabs.setupWithViewPager(activityPlayerBinding.viewpager);
        activityPlayerBinding.tabs.getTabAt(0).setIcon(R.drawable.avd_chat);
        activityPlayerBinding.tabs.getTabAt(1).setIcon(R.drawable.avd_play_request);
        activityPlayerBinding.tabs.getTabAt(2).setIcon(R.drawable.ic_gift_white_24dp);
        /*activityPlayerBinding.tabs.set(0, R.drawable.avd_chat);
        activityPlayerBinding.tabs.setIcon(1, R.drawable.avd_play_request);
        activityPlayerBinding.tabs.setIcon(2, R.drawable.ic_gift_box);*/
        activityPlayerBinding.tabs.getTabAt(oldPos).select();

        activityPlayerBinding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mSelectedTab = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                try {
                    HashMap<String, Object> prp = baseProperties;
                    prp.put("tab", tabName[tab.getPosition()]);
                    SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_VIDEO_PLAY_TAB_CHANGED, prp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initializeRewardTimeForLand() {
        long ttl = TIME_UNTIL_FINISH;
        mVideoRewardCountDownTimer = new CountDownTimer(ttl, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                TIME_UNTIL_FINISH = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                updateVideoWatchAlert(false);
                checkVideoReward();
                updateVideoTimer(0);
            }
        };

        mVideoRewardCountDownTimer.start();
    }

    // chat tab callbacks
    @Override
    public void checkFirstCommentReward() {
        checkAndShowCommentReward();
    }

    @Override
    public void askForLogin() {
        openLoginFlow();
    }

    @Override
    public void onTotalViewUpdate() {
        //ChatHelper.getInstance(this).getTotal(currentPlayingPost.getId(), this);
    }

    @Override
    public String getSegmentUrl() {
        try {
            HlsMediaPlaylist.Segment segment =
                    ((HlsManifest) Objects.requireNonNull(activityPlayerBinding.videoView.getPlayer().getCurrentManifest())).mediaPlaylist.segments.get(0);
            return segment.url;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public HashMap<String, Object> getBaseProperties() {
        return baseProperties;
    }

    @Override
    public Result getCurrentPost() {
        return playerViewModel.currentPost == null ? getCurrentPlayingPost() : playerViewModel.currentPost;
    }


    @Override
    public void onHeartCountUpdate(int count) {
        if (playerViewModel != null && playerViewModel.currentPost != null)
            playerViewModel.currentPost.setHeartCount(String.valueOf(count));
    }

    @Override
    public void updateStickerFlag() {
        animateStickersRvIn();
//        if (flag)
//            activityPlayerBinding.stickerOverlay.setVisibility(View.VISIBLE);
//        else
//            activityPlayerBinding.stickerOverlay.setVisibility(View.GONE);
//        isStickersShowing = flag;
    }

    @Override
    public void onChatBadgeUpdate(int count) {
        try {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (activityPlayerBinding.tabs == null)
                    return;
                if (count > 0) {
//                    BadgeDrawable badgeDrawable = Objects.requireNonNull(activityPlayerBinding.tabs.getTabAt(0)).showBadge();
//                    badgeDrawable.setBackgroundColor(getResources().getColor(R.color.bottom_bar_selected_item_color));
//                    badgeDrawable.setNumber(count);
                } else {
                    if (activityPlayerBinding.tabs.getTabAt(0) != null)
                        Objects.requireNonNull(activityPlayerBinding.tabs.getTabAt(0)).removeBadge();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // video tab callbacks

    @Override
    public void checkVideoReward() {
        checkAndShowVideoReward();
    }

    @Override
    public String getCurrentPostId() {
        Result result = playerViewModel.currentPost == null ? getCurrentPlayingPost() : playerViewModel.currentPost;
        return result != null ? result.getId() : null;
    }

    @Override
    public void updateVideoWatchAlert(boolean flag) {
        shouldShowTenMinuteAlert = flag;
    }

    @Override
    public void updateVideoTimer(long ttl) {
        TIME_UNTIL_FINISH = ttl;
    }

    @Override
    public boolean isPostLive() {
        Result result = playerViewModel.currentPost == null ? getCurrentPlayingPost() : playerViewModel.currentPost;
        return result != null && result.getIsLive();
    }

    // player request callbacks
    @Override
    public Result getCurrentPostResult() {
        return playerViewModel.currentPost == null ? getCurrentPlayingPost() : playerViewModel.currentPost;
    }

    @Override
    public void onUpdatePlayerCountBadge(int count) {
        try {
            if (count > 0) {
//                BadgeDrawable badgeDrawable = activityPlayerBinding.tabs.getTabAt(1).showBadge();
//                badgeDrawable.setBackgroundColor(getResources().getColor(R.color.bottom_bar_selected_item_color));
//                badgeDrawable.setNumber(count);
            } else {
                activityPlayerBinding.tabs.getTabAt(1).removeBadge();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
