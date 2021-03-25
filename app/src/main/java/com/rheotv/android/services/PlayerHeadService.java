package com.rheotv.android.services;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.gson.Gson;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.VideoUrlObj;
import com.rheotv.android.data.network.requestLayer.EventsApiService;
import com.rheotv.android.data.network.requestLayer.MyServiceInterceptor;
import com.rheotv.android.di.module.AppModule;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.splash.SplashActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.hourglass.Hourglass;
import com.rheotv.android.utils.hourglass.HourglassAsync;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON;

public class PlayerHeadService extends Service {
    private WindowManager mWindowManager;


    DisplayMetrics outMetrics;

    int currentOrientation;

    boolean isInitial = true;

    View mediaContainer;

    String mediaUrl;

    String videoUrl;

    PlayerView mPlayerView;

    ImageView maximizePlayer;

    ImageView closePlayer;

    ImageView volumeButton;

    boolean areControlsVisible = true;

    String postId;
    String authorName;
    String game;
    private String authorId;
    private boolean isFromDeeplink;

    Handler handler;

    ProgressBar loadingView;

    List<VideoUrlObj> videoUrls;

    public String HEADER_NOTIFICATION_CHANNEL_ID = "header notification channel";

    HashMap<String, Object> properties;

    int movedThresholdX;

    long clickStartTime;
    long clickEndTime;
    int movedThresholdY;

    long timeElapsedOnPlayerPage;

    long firstTapTime;

    int dyChange;

    int dxChange;

    boolean isLive;

    boolean isPlayerMax;
    private String deviceId;
    private PlayerHeadHolder playerHeadHolder;
    private PostObject post;

    private boolean isLowQuality;

    public PlayerHeadService() {

    }

    private long timeUntillReward = 0;
    private long timeToReward = 0;
    TextView rewardTime;
    int resumeWindow;
    long resumePosition;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(AppConstants.ARG_PLAYER_HOLDER)) {
            playerHeadHolder = intent.getParcelableExtra(AppConstants.ARG_PLAYER_HOLDER);
            if (playerHeadHolder != null) {
                post = playerHeadHolder.getPost();
                videoUrls = playerHeadHolder.getVideoUrls();
            }
        }

        videoUrl = post.getVideoUrl();
        postId = post.getId();
        authorName = post.getAuthor() != null ? post.getAuthor().getUser().getUsername() : "";
        game = post.getGame() != null ? post.getGame().getName() : "";
        timeElapsedOnPlayerPage = playerHeadHolder.getTimeElapsed();
        authorId = post.getAuthor() != null ? post.getAuthor().getUser().getId() + "" : "";
        isLive = post.isLive();
        resumeWindow = playerHeadHolder.getResumeWindow();
        resumePosition = playerHeadHolder.getResumePosition();
        isFromDeeplink = playerHeadHolder.wasFromDeeplink();

        properties = new HashMap<>();
        properties.put("media_url", post.getVideoUrl());
        properties.put("username", authorName);
        properties.put("game", game);
        properties.put("game_id", post.getGame() != null ? post.getGame().getId() : "");
        properties.put("is_live", post.isLive());
        properties.put("type", post.isLive() ? "live" : "fullRecorded");
        properties.put("language", post.getLanguage());
        properties.put("title", post.getTitle());
        properties.put("name", post.getAuthor() != null ? post.getAuthor().getUser().getUserFullName() : "");
        properties.put("author_id", authorId);
        properties.put("isLoggedIn", CommonUtils.isUserLoggedin());
        properties.put("in_window_mode", true);
        properties.put("format", "video");

        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_PLAYER_WIDGET_SHOWN, properties);

        startForeground(12323, showNotification());
        mWindowManager = (WindowManager) getBaseContext().getSystemService(WINDOW_SERVICE);

        outMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(outMetrics);

        //Inflate the chat head layout we created
        currentOrientation = getResources().getConfiguration().orientation;
        //startSettingUpView();

        setUpPlayerView();
        isInitial = false;
        timeToReward = post.getRewardTimeProgress();
        HourglassAsync mVideoRewardCountDownTimer = new HourglassAsync(timeToReward, TimeUnit.SECONDS) {
            @Override
            public void onTimerFinish() {
                if (rewardTime != null) {
                    rewardTime.setVisibility(View.VISIBLE);
                    rewardTime.setText(CommonUtils.convertToRewardFriendly(timeUntillReward * 1000));
                    timeUntillReward = -1;
                }
            }

            @Override
            public void onTimerTick(long remainingTime) {
                post.setRewardTimeProgress(remainingTime);
                timeUntillReward = remainingTime;
            }
        };
        if (timeToReward > 0 && mVideoRewardCountDownTimer != null)
            mVideoRewardCountDownTimer.startTimer();

        deviceId = CommonUtils.getDevId(this);
        buildEventService();

        streamTimer.startTimer();

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(12323, showNotification());
        registerBroadcastReceiver();
    }

    public void setUpPlayerView() {
        mediaContainer = ((LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_player_head, null);
        FrameLayout playerContainer = (FrameLayout) mediaContainer.findViewById(R.id.player_container);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        params.gravity = Gravity.TOP | Gravity.LEFT;
        int viewWidth = (outMetrics.widthPixels * 3) / 5;
        int viewHeight = (viewWidth * 9) / 16;
        params.y = outMetrics.heightPixels - viewHeight - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 104, getResources().getDisplayMetrics());
        params.x = outMetrics.widthPixels - viewWidth - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        params.width = viewWidth;
        params.height = viewHeight;
        dyChange = viewHeight / 2;
        dxChange = viewWidth / 2;
        /*if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            params.x = outMetrics.widthPixels - SIDE_SPACING;
        } else {
            params.x = outMetrics.heightPixels - SIDE_SPACING;
        }

        params.y = TOP_SPACING;*/

        rewardTime = mediaContainer.findViewById(R.id.new_reward_unlocked);
        loadingView = (ProgressBar) mediaContainer.findViewById(R.id.progress_loading);
        closePlayer = (ImageView) mediaContainer.findViewById(R.id.close_player);
        closePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
                properties.put("time_elapsed", timeElapsed);
                SegmentTracker.getInstance(PlayerHeadService.this).trackEvent(SegmentConstants.EVENT_VIDEO_PLAYER_WIDGET_CLOSE_CLICK, properties);
            }
        });
        maximizePlayer = (ImageView) mediaContainer.findViewById(R.id.maximize_player);
        maximizePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndMoveForward();
            }
        });
        volumeButton = (ImageView) mediaContainer.findViewById(R.id.volume_btn);
        volumeButton.setSelected(true);
        volumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (volumeButton.isSelected()) {
                    volumeButton.setSelected(false);
                    if (mPlayerView.getPlayer() != null)
                        ((SimpleExoPlayer) mPlayerView.getPlayer()).setVolume(0.0f);
                    SegmentTracker.getInstance(PlayerHeadService.this).trackEvent(SegmentConstants.EVENT_VIDEO_PLAYER_WIDGET_VOLUME_CLICK_OFF, properties);
                } else {
                    SegmentTracker.getInstance(PlayerHeadService.this).trackEvent(SegmentConstants.EVENT_VIDEO_PLAYER_WIDGET_VOLUME_CLICK_ON, properties);
                    volumeButton.setSelected(true);
                    if (mPlayerView.getPlayer() != null)
                        ((SimpleExoPlayer) mPlayerView.getPlayer()).setVolume(10.0f);
                }
            }
        });

        mPlayerView = new PlayerView(this);
        mPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        mPlayerView.setUseController(false);

        TrackSelection.Factory factory = new AdaptiveTrackSelection.Factory();

        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();

        final int loadControlBufferMs = 50000; /* This is 50000 milliseconds in ExoPlayer 2.9.6 */


        /* Configure the DefaultLoadControl to use the same value for */
        builder.setBufferDurationsMs(loadControlBufferMs,
                loadControlBufferMs,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
        DefaultLoadControl loadControl = builder.createDefaultLoadControl();


        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this,
                new DefaultRenderersFactory(this, EXTENSION_RENDERER_MODE_ON), new DefaultTrackSelector(factory)
                , loadControl);

        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
                } else if (playWhenReady && playbackState == Player.STATE_READY) {
                    loadingView.setVisibility(View.GONE);
                } else if (playWhenReady) {
                    if (playbackState == Player.STATE_BUFFERING) {
                        loadingView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                super.onPlayerError(error);
                if (isLowQuality) {
                    makePlayerWithQuality(player, "medium");
                    isLowQuality = false;
                } else {
                    Toast.makeText(PlayerHeadService.this, "Error while playing live stream", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mPlayerView.setPlayer(player);
        /*try {

            //mediaUrl = updateUrlWithCurrentFormat(CommonUtils.getUrlWithoutParameters(mediaUrl), "low");
        } catch (URISyntaxException | NullPointerException e) {
            e.printStackTrace();
        }*/
        if (videoUrls != null) {
            isLowQuality = true;
            makePlayerWithQuality(player, "low");

        } else {
            try {
                mediaUrl = updateUrlWithCurrentFormat(CommonUtils.getUrlWithoutParameters(videoUrl), "low");
            } catch (URISyntaxException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        playerContainer.addView(mPlayerView, lp);
        if (mediaContainer != null && !mediaContainer.isAttachedToWindow())
            mWindowManager.addView(mediaContainer, params);

        mediaContainer.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        clickStartTime = System.currentTimeMillis();
                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        Log.i(PlayerHeadService.class.getCanonicalName(), "Initial X " + initialX);
                        Log.i(PlayerHeadService.class.getCanonicalName(), "Initial Y " + initialX);
                        Log.i(PlayerHeadService.class.getCanonicalName(), "Initial Touch X " + initialTouchX);
                        Log.i(PlayerHeadService.class.getCanonicalName(), "Initial Touch Y " + initialTouchX);

                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_UP:
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.
                        clickEndTime = System.currentTimeMillis();

                        if (firstTapTime > 0 && clickEndTime - firstTapTime <= 200) {
                            checkAndMoveForward();

                        } else if (clickEndTime - clickStartTime <= 200) {
                            manageControlActionState();
                            firstTapTime = clickEndTime;
                        } else {
                            firstTapTime = 0;
                        }

                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = (initialX + (int) (event.getRawX() - initialTouchX));
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        Log.i("Current Position", "Moving to X::" + params.x + "  Y::" + params.y);
                        if (params.x < 0) {
                            params.x = 0;
                        }
                        if (params.y < 0) {
                            params.y = 0;
                        }
                        if (mediaContainer != null && mediaContainer.isAttachedToWindow())
                            mWindowManager.updateViewLayout(mediaContainer, params);
                        lastAction = event.getAction();
                        return true;
                }
                return false;
            }
        });

        if (handler == null) {
            handler = new Handler();
        }
        handler.postDelayed(controlsHidingRunnable, 3000);
    }

    public void makePlayerWithQuality(SimpleExoPlayer player, String quality) {
        try {
            mediaUrl = getUrlWithQuality(quality);
            if (mediaUrl == null) {
                if (isLowQuality) {
                    mediaUrl = getUrlWithQuality("medium");
                    isLowQuality = false;
                }
            }
            if (mediaUrl == null) {
                mediaUrl = updateUrlWithCurrentFormat(CommonUtils.getUrlWithoutParameters(videoUrl), "low");
            }

            Uri uri = Uri.parse(mediaUrl);
            player.setPlayWhenReady(true);
            MediaSource mediaSource = buildMediaSource(uri);
            player.prepare(mediaSource, false, true);
            if (!isLive) {
                player.seekTo(resumeWindow, resumePosition);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private String getUrlWithQuality(String quality) {
        String videoUrl = null;
        for (VideoUrlObj obj : videoUrls) {
            if (obj.getNetworkType().equalsIgnoreCase(quality)) {
                videoUrl = obj.getUrl();
                break;
            }
        }
        return videoUrl;
    }

    public void upgradeViewSize() {
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) mediaContainer.getLayoutParams();
        lp.height += dyChange;
        lp.width += dxChange;
        mWindowManager.updateViewLayout(mediaContainer, lp);
    }

    public void downgradeViewSize() {
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) mediaContainer.getLayoutParams();
        lp.height -= dyChange;
        lp.width -= dxChange;
        mWindowManager.updateViewLayout(mediaContainer, lp);
    }

    long timeElapsed;
    private boolean isFirstWatchEventTracked = false;

    private String updateUrlWithCurrentFormat(String urlWithoutParameters, String qualityFormat) {
        String[] parts = urlWithoutParameters.split("\\/");
        String oldFormat = parts[parts.length - 2];
        if (oldFormat.equalsIgnoreCase("medium") || oldFormat.equalsIgnoreCase("low") || oldFormat.equalsIgnoreCase("high")) {
            return urlWithoutParameters.replace(oldFormat, qualityFormat);
        }
        return urlWithoutParameters;
    }

    public void checkAndMoveForward() {
        properties.put("time_elapsed", timeElapsed);
        SegmentTracker.getInstance(PlayerHeadService.this).trackEvent(SegmentConstants.EVENT_VIDEO_PLAYER_WIDGET_MAXIMIZE_CLICK, properties);
        if (PlayerHeadServiceHelper.getHelperInstance() != null) {
            startActivityWithIntent();
        } else {
            startApplicationWithIntent();
        }
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
    }

    private void startApplicationWithIntent() {
        Intent intent = new Intent(this, SplashActivity.class);
        if (postId != null && !postId.isEmpty()) {
            Uri uri = Uri.parse(postId);
            intent.setData(uri);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void startActivityWithIntent() {
        if (post != null) {
            post.setResumePosition(post.isLive() ? 0 : (mPlayerView.getPlayer() != null ? mPlayerView.getPlayer().getCurrentPosition() : 0));
            post.setResumeWindow(mPlayerView.getPlayer() != null ? mPlayerView.getPlayer().getCurrentWindowIndex() : 0);
            Bundle bundle = new StreamPlayerContainerFragment.Builder()
                    .addPost(post)
                    .addGameId(AppConstants.LIVE_GAME_ID)
                    .addFromDeepLink(isFromDeeplink)
                    .addSourceScreenName(SegmentConstants.SCREEN_NAME_FEED)
                    .addLoadMore(true)
                    .buildExtras();
            bundle.putBoolean(AppConstants.ARG_SHOW_TAG_OPTIONS, post.isShowTagOptions());
            List<Integer> flagList = new ArrayList<>();
            flagList.add(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!post.isShowTagOptions())
                StreamPlayerActivity.Companion.startActivity(this, bundle, flagList);
            else
                HomeActivity.Companion.startActivity(this, bundle, flagList);

//                    .build();
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
        } else {
            startApplicationWithIntent();
        }
    }

    public void changeAlpha(int visibility, View v1, View v2, View v3, int from, int to) {
        if (visibility == View.VISIBLE) {
            v1.setVisibility(View.VISIBLE);
            v2.setVisibility(View.VISIBLE);
            v3.setVisibility(View.VISIBLE);
        }
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(v1, View.ALPHA, from, to);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(v2, View.ALPHA, from, to);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(v3, View.ALPHA, from, to);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (visibility == View.GONE) {
                    v1.setVisibility(View.GONE);
                    v2.setVisibility(View.GONE);
                    v3.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.playTogether(anim1, anim2, anim3);
        animatorSet.start();
    }

    Runnable controlsHidingRunnable = () -> manageControlActionState();

    private void manageControlActionState() {

        /*if (isPlayerMax) {
            downgradeViewSize();
            isPlayerMax = false;
        } else {
            upgradeViewSize();
            isPlayerMax = true;
        }*/

        if (areControlsVisible) {
            changeAlpha(View.GONE, maximizePlayer, closePlayer, volumeButton, 1, 0);
            areControlsVisible = false;
            if (handler != null)
                handler.removeCallbacks(controlsHidingRunnable);
        } else {
            changeAlpha(View.VISIBLE, maximizePlayer, closePlayer, volumeButton, 0, 1);
            areControlsVisible = true;
            if (handler == null) {
                handler = new Handler();
            }
            handler.postDelayed(controlsHidingRunnable, 3000);
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        String url = uri.toString();
        if (url.toLowerCase().contains(".m3u8"))
            return new HlsMediaSource.Factory(
                    new DefaultHttpDataSourceFactory(RheoTvApp.EXOPLAYER_AGENT)).
                    createMediaSource(uri);

        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(RheoTvApp.EXOPLAYER_AGENT)).setExtractorsFactory(new DefaultExtractorsFactory()
                .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_IGNORE_AAC_STREAM)).createMediaSource(uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenOnOffReceiver);
        streamTimer.stopTimer();

        if (mPlayerView != null) {
            mPlayerView.getPlayer().stop();
            mPlayerView.getPlayer().release();
            mPlayerView.setPlayer(null);
            ViewGroup parent = (ViewGroup) mPlayerView.getParent();
            if (parent == null) {
                return;
            }
            int indexOfPlayerView = parent.indexOfChild(mPlayerView);
            parent.removeViewAt(indexOfPlayerView);
            if (mediaContainer != null && mediaContainer.isAttachedToWindow()) {
                mWindowManager.removeView(mediaContainer);
            }
        }
    }

    public Notification showNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, HEADER_NOTIFICATION_CHANNEL_ID);

        builder.setOngoing(true)
                .setContentTitle(((authorName != null && game != null) ? authorName + " is streaming " + game + " live on Rheo TV" : "You are watching live stream on Rheo TV"))
                .setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
                .setTicker(((authorName != null && game != null) ? authorName + " is streaming " + game + " live on Rheo TV" : "You are watching live stream on Rheo TV"))
                .setSound(null);
        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = notifManager.getNotificationChannel(HEADER_NOTIFICATION_CHANNEL_ID);
            if (mChannel == null) {
                mChannel = new NotificationChannel(HEADER_NOTIFICATION_CHANNEL_ID, " Header Rheo Player", NotificationManager.IMPORTANCE_LOW);
            }
            mChannel.setSound(null, null);
            builder.setChannelId(HEADER_NOTIFICATION_CHANNEL_ID);
            notifManager.createNotificationChannel(mChannel);
        }
        return (builder.build());
    }

    private void registerBroadcastReceiver() {
        final IntentFilter theFilter = new IntentFilter();
        /** System Defined Broadcast */
        theFilter.addAction(Intent.ACTION_SCREEN_ON);
        theFilter.addAction(Intent.ACTION_SCREEN_OFF);
        theFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenOnOffReceiver, theFilter);
    }

    BroadcastReceiver screenOnOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String strAction = intent.getAction();
            if (mPlayerView == null) return;

            KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (strAction.equals(Intent.ACTION_USER_PRESENT) || strAction.equals(Intent.ACTION_SCREEN_OFF) || strAction.equals(Intent.ACTION_SCREEN_ON))
                if (myKM.isKeyguardLocked()) {
                    mPlayerView.getPlayer().setPlayWhenReady(false);
                    System.out.println("Screen off " + "LOCKED");
                    streamTimer.pauseTimer();
                } else {
                    mPlayerView.getPlayer().seekTo(0);
                    mPlayerView.getPlayer().setPlayWhenReady(true);
                    System.out.println("Screen off " + "UNLOCKED");
                    streamTimer.resumeTimer();
                }

        }
    };

    private EventsApiService eventsApiService;

    private void buildEventService() {
        MyServiceInterceptor interceptor = AppModule.getServiceInterceptor(this);
        HttpLoggingInterceptor httpLoggingInterceptor = AppModule.httpLoggingInterceptor();
        Cache cache = AppModule.provideCache(this);
        OkHttpClient client = AppModule.provideOkhttp(interceptor, httpLoggingInterceptor, cache);
        eventsApiService = AppModule.provideEventsService(client, new Gson());
    }

    private Hourglass streamTimer = new Hourglass(new Date().getTime(), 1000) {
        @Override
        public void onTimerTick(long timeRemaining, long passedTime) {
            long ttl = passedTime / 1000;
            if (ttl > 0) {
                long exoPosition = mPlayerView.getPlayer().getCurrentPosition() / 1000;

                if (ttl % 10 == 0) {
                    Log.i(getClass().getName(), "hit_10th_api : " + ttl);
                    makeViewApiCall(exoPosition);
                }
            }

            if (ttl == 0L || ttl == 30L || (ttl > 30 && ttl % ((int) (4.5 * 60)) == 0L)) {
                properties.put("time_elapsed", ttl);
                SegmentTracker.getInstance(PlayerHeadService.this).trackEvent(SegmentConstants.EVENT_VIDEO_PLAYER_WIDGET_WATCH_STREAM, properties);
                properties.put("time_elapsed", (ttl + timeElapsedOnPlayerPage));

                if (!isFirstWatchEventTracked) {
                    isFirstWatchEventTracked = true;
                    if (CommonUtils.isFirstWatchEventNotTracked()) {
                        CommonUtils.setFirstWatchEventTracked();
                        SegmentTracker.getInstance(PlayerHeadService.this).trackEvent(SegmentConstants.EVENT_FIRST_WATCH_STREAM, properties);
                    }
                }

                SegmentTracker.getInstance(PlayerHeadService.this).trackEvent(SegmentConstants.EVENT_WATCH_STREAM, properties);
            }
        }

        @Override
        public void onTimerFinish() {

        }
    };

    private void makeViewApiCall(long exoPosition) {
        try {
            JSONObject otherInfoJson = new JSONObject();
            try {
                otherInfoJson.put("post_id", postId);
                otherInfoJson.put("author_id", authorId);
                otherInfoJson.put("author_username", authorName);
                otherInfoJson.put("viewer_username", deviceId);
                otherInfoJson.put("duration", 10);
                otherInfoJson.put("time_elapsed", exoPosition);
                otherInfoJson.put("format", "video");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(getClass().getName(), "makeViewApiCall at " + System.currentTimeMillis() + " for 10 secs and player time" + timeElapsed);
            String otherInfo = otherInfoJson.toString();
            RequestBody otherInfoReqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), otherInfo);
            eventsApiService.postVideoView(otherInfoReqBody).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


}
