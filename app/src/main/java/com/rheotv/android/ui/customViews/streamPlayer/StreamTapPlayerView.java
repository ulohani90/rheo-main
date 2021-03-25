package com.rheotv.android.ui.customViews.streamPlayer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.github.vkay94.dtpv.DoubleTapPlayerView;
import com.github.vkay94.dtpv.SeekListener;
import com.github.vkay94.dtpv.youtube.YouTubeOverlay;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.rheotv.android.R;
import com.rheotv.android.ui.customViews.HeartAnimator;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class StreamTapPlayerView extends DoubleTapPlayerView {
    private StreamHolder holder;

    private int fastForwardRewindDuration = 10000;
    private int animationDuration = 800;
    private boolean isControllerShowing = isControllerVisible();
    private boolean isChatEnabled = false;
    private ExoStreamPlayer exoStreamPlayer;
    private Handler hideOverlayHandler = new Handler(Looper.getMainLooper());
    private WeakReference<HeartAnimator.HeartAnimatorInteractionListener> heartAnimatorListener;
    private PlayerAnalyticsListener mListener;
    private WeakReference<StreamPlayerCallbackListener> viewCallbackListener;
    private HeartAnimator heartAnimator;
    private boolean shouldUpdateRotateButtonPosition = false;
    private AudioManager audioManager;
    private Runnable controlOverlayRunnable = () -> {
        Log.i(TAG, "controllers_onControllerVisibilityChange: runner: " + isControllerShowing);
        if (isControllerShowing) {
            hideControls();
        }
    };

    public StreamTapPlayerView(Context context) {
        super(context);
    }

    public StreamTapPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StreamTapPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void attach(StreamHolder holder) {
        this.holder = holder;
        initTapView();
    }

    public void setAnalyticsListener(PlayerAnalyticsListener listener) {
        mListener = listener;
    }

    public void setViewCallbackListener(StreamPlayerCallbackListener viewCallbackListener) {
        this.viewCallbackListener = new WeakReference<>(viewCallbackListener);
    }

    public void setHeartAnimatorListener(HeartAnimator.HeartAnimatorInteractionListener heartAnimatorListener) {
        this.heartAnimatorListener = new WeakReference<>(heartAnimatorListener);
    }

    private void initTapView() {
        holder.setDoubleTapPlayerView(this);
        YouTubeOverlay youTubeOverlay = holder.getYoutubeDoubleTap().get();
        heartAnimator = new HeartAnimator(findViewById(R.id.heart_animation_container), heartAnimatorListener.get());
        heartAnimator.start(new WeakReference<>(findViewById(R.id.heart_image_view)));

        youTubeOverlay.setPlayerView(this);
        youTubeOverlay.setAnimationDuration(animationDuration);
        youTubeOverlay.setFastForwardRewindDuration(fastForwardRewindDuration);
        if (holder.isDoubleTapSendEventEnabled())
            youTubeOverlay.setSeekListener(seekListener);
        youTubeOverlay.setPerformListener(new OverlayPerformListener(holder));
        activateDoubleTap(true)
                .setDoubleTapDelay(650)
                .setDoubleTapListener(youTubeOverlay);

        hideOverlayHandler.postDelayed(controlOverlayRunnable, 2000);
        setupViews();
    }

    private void setupViews() {
        setOnClickListener(v -> onViewClick());
        updateViewForLive();
        controllerViewClick();
        bindViews();
    }

    public void attachPlayer() {
        if (this.holder == null) return;
        if (exoStreamPlayer == null)
            exoStreamPlayer = new ExoStreamPlayer(holder);

        exoStreamPlayer.attachPlayer();
        if (mListener != null) {
            exoStreamPlayer.setAnalyticsListener(mListener);
        }
    }

    public void resumePlayer() {
        if (exoStreamPlayer != null)
            exoStreamPlayer.resumePlayer();
    }

    public void pausePlayer() {
        if (exoStreamPlayer != null)
            exoStreamPlayer.pausePlayer();
    }

    public void momentsSeekStartsFrom(long seekTo) {
        if (exoStreamPlayer != null)
            exoStreamPlayer.momentsSeekStartsFrom(seekTo);
    }

    public void canSeekVideo(boolean canSeek) {
        if (exoStreamPlayer != null)
            exoStreamPlayer.canSeek(canSeek);
    }

    public void setMomentsEndTime(long endTime) {
        if (exoStreamPlayer != null) {
            exoStreamPlayer.setMomentsEndTime(endTime);
        }
    }

    public void mutePlayer() {
        if (exoStreamPlayer != null)
            exoStreamPlayer.mute();
    }

    public void unMutePlayer() {
        if (exoStreamPlayer != null)
            exoStreamPlayer.unMute();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(boolean shouldAddRunnable) {
        if (shouldAddRunnable && exoStreamPlayer != null)
            exoStreamPlayer.addStreamRunnableToHandler();
    }

    public void detachPlayer() {
        if (exoStreamPlayer == null) {
            return;
        }
        exoStreamPlayer.detachPlayer();
        exoStreamPlayer.removeAnalyticsListener();
        setPlayer(null);
        exoStreamPlayer = null;
        hideOverlayHandler.removeCallbacks(controlOverlayRunnable);
    }

    public void updateStreamQuality(String format) {
        if (exoStreamPlayer != null)
            exoStreamPlayer.updateStreamQuality(format);
    }

    public int getResumeWindow() {
        return exoStreamPlayer != null ? exoStreamPlayer.getResumeWindow() : 0;
    }

    public long getResumePosition() {
        return exoStreamPlayer != null ? exoStreamPlayer.getResumePosition() : 0;
    }

    public long getTimeEllipse() {
        return exoStreamPlayer != null ? exoStreamPlayer.getVideoTimeElapsed() : 0;
    }

    public void rotateLayout() {
        Map<String, Object> map = new HashMap<>();
        map.put("Landscape", Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? "true" : "false");
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_LANDSCAPE_BUTTON_CLICK, map);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (holder.getVideoMode().equalsIgnoreCase("landscape")) {
                ((Activity) holder.getContextRef().get()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                resizeVideoView();
            }
        } else {
            ((Activity) holder.getContextRef().get()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        }
    }

    public void onOrientationChange(int orientation) {
        resizeVideoView();
        viewStateOnOrientationChange(orientation);
        updateRotateButtonPosition();
    }

    public void adjustPlayerHeight(int orientation) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int width = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int oldHeight = layoutParams.height;
        int height;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            height = (width * 9) / 16;
        } else {
            height = screenHeight;
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        }


        ValueAnimator va = ValueAnimator.ofInt(oldHeight, height);
        va.setDuration(400);
        va.addUpdateListener(animation -> {
            Integer value = (Integer) animation.getAnimatedValue();
            getLayoutParams().height = value.intValue();
            requestLayout();
            if (mListener != null)
                mListener.setPlayerObserver();
        });
        va.start();


    }

    public void resizeVideoView() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int orientation = Resources.getSystem().getConfiguration().orientation;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        int currentHeight = layoutParams.height;
        int newHeight;
        layoutParams.width = width;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            newHeight = screenHeight;
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        } else {
            if (holder != null && holder.getVideoMode().equalsIgnoreCase("landscape")) {
                newHeight = (width * 9) / 16;
                setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            } else {
                newHeight = screenHeight;
                setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            }
//
        }
        layoutParams.height = newHeight;
        setLayoutParams(layoutParams);
//        animateViewResize(currentHeight, newHeight);
        viewStateOnOrientationChange(orientation);
    }

//    public void resizeViewOnSetup() {
//        ViewGroup.LayoutParams layoutParams = getLayoutParams();
//        int width = getContext().getResources().getDisplayMetrics().widthPixels;
//        int currentHeight = layoutParams.height;
//        int newHeight;
//        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            newHeight = getContext().getResources().getDisplayMetrics().heightPixels;
//        } else {
//            newHeight = (width * 9) / 16;
//        }
////        animateViewResize(currentHeight, newHeight);
//        setLayoutParams(layoutParams);
//    }

    public void resizeViewOnSetup() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int width = getContext().getResources().getDisplayMetrics().widthPixels;

        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.height = getContext().getResources().getDisplayMetrics().heightPixels;
        } else {
            layoutParams.height = (width * 9) / 16;
        }
        setLayoutParams(layoutParams);
    }

    public void animateViewResize(int oldHeight, int newHeight) {
        ValueAnimator va = ValueAnimator.ofInt(oldHeight, newHeight);
        va.setDuration(400);
        va.addUpdateListener(animation -> {
            getLayoutParams().height = (Integer) animation.getAnimatedValue();
            requestLayout();
        });
        va.start();
    }

    private void onViewClick() {
        Log.i(TAG, "onControllerVisibilityChange_onViewClick: " + isControllerShowing);
        if (isControllerShowing) {
            hideControls();
            hideOverlayHandler.removeCallbacks(controlOverlayRunnable);
        } else {
            showControls();
            hideOverlayHandler.postDelayed(controlOverlayRunnable, 5000);
        }
    }

    private void updateViewForLive() {
        if (holder.isLive()) {
            findViewById(R.id.live_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.total_views).setVisibility(View.GONE);
            findViewById(R.id.recorded_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.exo_duration).setVisibility(View.GONE);
            findViewById(R.id.exo_position).setVisibility(View.GONE);
            findViewById(R.id.exo_play).setVisibility(View.GONE);
            ((ImageView) findViewById(R.id.exo_play)).setImageResource(0);
            ((ImageView) findViewById(R.id.exo_pause)).setImageResource(0);
            ((ImageView) findViewById(R.id.exo_rew)).setImageResource(0);
            ((ImageView) findViewById(R.id.exo_ffwd)).setImageResource(0);
            findViewById(R.id.exo_rew).setVisibility(View.GONE);
            findViewById(R.id.exo_ffwd).setVisibility(View.GONE);
            setUseController(false);
        } else {
            findViewById(R.id.exo_duration).setVisibility(View.VISIBLE);
            findViewById(R.id.exo_position).setVisibility(View.VISIBLE);
            findViewById(R.id.live_layout).setVisibility(View.GONE);
            findViewById(R.id.recorded_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.exo_play).setVisibility(View.VISIBLE);
            findViewById(R.id.exo_pause).setVisibility(View.VISIBLE);
            findViewById(R.id.exo_rew).setVisibility(View.VISIBLE);
            findViewById(R.id.exo_ffwd).setVisibility(View.VISIBLE);
            ((ImageView) findViewById(R.id.exo_rew)).setImageResource(R.drawable.ic_rewind_white_24dp);
            ((ImageView) findViewById(R.id.exo_ffwd)).setImageResource(R.drawable.ic_fast_forward_white_24dp);
            setUseController(true);
        }
    }

    public void setStreamPlayerCallbackListener(StreamPlayerCallbackListener listener) {
        this.listener = listener;
    }

    StreamPlayerCallbackListener listener;

    private void controllerViewClick() {
        if (findViewById(R.id.heart_image_view) != null && heartAnimatorListener != null) {
            findViewById(R.id.heart_image_view).setOnClickListener(view -> {
                if (heartAnimator != null)
                    heartAnimator.fadeAndScaleHeart(new WeakReference<>(view));
                if (viewCallbackListener != null && viewCallbackListener.get() != null)
                    viewCallbackListener.get().onHeartViewClick();
            });
        }

        findViewById(R.id.rotate_btn).setOnClickListener(view -> rotateLayout());
        findViewById(R.id.volume_btn).setOnClickListener(view -> adjustVolume());
        if (viewCallbackListener == null) return;
        if (findViewById(R.id.settings_btn) != null)
            findViewById(R.id.settings_btn).setOnClickListener(view -> viewCallbackListener.get().onSettingViewClick());
//        if (findViewById(R.id.close_btn) != null) {
//            rotateLayout();
        findViewById(R.id.close_btn).setOnClickListener(view -> viewCallbackListener.get().onCloseViewClick());
//        }
        if (findViewById(R.id.share_btn) != null)
            findViewById(R.id.share_btn).setOnClickListener(view -> viewCallbackListener.get().onShareViewClick());
        if (findViewById(R.id.streamer_info_layout) != null)
            findViewById(R.id.streamer_info_layout).setOnClickListener(view -> viewCallbackListener.get().onStreamProfileClick());
        if (findViewById(R.id.chat_state_btn) != null) {
            findViewById(R.id.chat_state_btn).setOnClickListener(view -> {
                if (findViewById(R.id.chat_state_btn) != null) {
                    if (isChatEnabled) {
                        ((ImageView) findViewById(R.id.chat_state_btn)).setImageResource(R.drawable.ic_hide_chat_white_24dp);
                    } else {
                        hideControls();
                        hideController();
                        ((ImageView) findViewById(R.id.chat_state_btn)).setImageResource(R.drawable.ic_show_chat_white_24dp);
                    }
                    isChatEnabled = !isChatEnabled;
                    if (viewCallbackListener != null && viewCallbackListener.get() != null)
                        viewCallbackListener.get().onChatViewClick();
                }
            });
        }
        if (findViewById(R.id.flag_btn) != null) {
            findViewById(R.id.flag_btn).setOnClickListener(view -> viewCallbackListener.get().onFlagBtnClick());
        }
        if (findViewById(R.id.gift_button) != null)
            findViewById(R.id.gift_button).setOnClickListener(view -> viewCallbackListener.get().onGiftViewClick());
        if (findViewById(R.id.sticker_button) != null)
            findViewById(R.id.sticker_button).setOnClickListener(view -> viewCallbackListener.get().onStickerViewClick());
        if (findViewById(R.id.follow_button) != null)
            findViewById(R.id.follow_button).setOnClickListener(view -> viewCallbackListener.get().onFollowStreamViewClick());
        if (findViewById(R.id.go_to_live_btn) != null) {
            findViewById(R.id.go_to_live_btn).setOnClickListener(v -> {
                v.setVisibility(View.GONE);
                exoStreamPlayer.seekToLive();
            });
        }
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        ((SeekBar) findViewById(R.id.volume_seekbar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (15 * progress) / 100, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void adjustVolume() {
        if (findViewById(R.id.volume_seekbar).getVisibility() == View.VISIBLE) {
            findViewById(R.id.volume_seekbar).setVisibility(View.GONE);
        } else {
            findViewById(R.id.volume_seekbar).setVisibility(View.VISIBLE);

            ((SeekBar) findViewById(R.id.volume_seekbar)).setProgress((100 * audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) / 15);
        }

    }

    private void bindViews() {
        if (findViewById(R.id.stream_title_text_view) != null) {
            BindingUtils.appendIconAtEnd(findViewById(R.id.stream_title_text_view),
                    ContextCompat.getDrawable(getContext(), R.drawable.avd_down_arrow),
                    holder.getAuthorHolder().getStreamTitle());
        }

        if (findViewById(R.id.game_name_text_view) != null)
            ((TextView) findViewById(R.id.game_name_text_view)).setText(holder.getAuthorHolder().getGameName());

        if (findViewById(R.id.stream_viewer_count_text_View) != null)
            ((TextView) findViewById(R.id.stream_viewer_count_text_View)).setText(holder.getAuthorHolder().getViewCount());

        if (findViewById(R.id.user_name_tv) != null)
            ((TextView) findViewById(R.id.user_name_tv)).setText(holder.getAuthorHolder().getUsername());

        Log.i(TAG, "user_profile: " + holder.getAuthorHolder().getProfileUrl());
        if (findViewById(R.id.user_profile_image) != null)
            BindingUtils.setImageUrlUsingCache(findViewById(R.id.user_profile_image), holder.getAuthorHolder().getProfileUrl(), false);

        ImageButton imageButton;
        if ((imageButton = findViewById(R.id.chat_state_btn)) != null) {
            imageButton.setImageResource(isChatEnabled ? R.drawable.ic_show_chat_white_24dp : R.drawable.ic_hide_chat_white_24dp);
        }
        setViewCount();
        markFollowing();
    }

    public void setShouldUpdateRotateButtonPosition(boolean shouldUpdateRotateButtonPosition) {
        this.shouldUpdateRotateButtonPosition = shouldUpdateRotateButtonPosition;
    }

    public void updateRotateButtonPosition() {
        if (!shouldUpdateRotateButtonPosition) return;
        View rotateButton = findViewById(R.id.rotate_btn);
        if (rotateButton != null) {
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) rotateButton.getLayoutParams();
            if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                lp.setMarginEnd(ViewUtils.dpToPx(72));
            } else {
                lp.setMarginEnd(0);
            }
            rotateButton.setLayoutParams(lp);
        }
    }

    public void updateViewCount(String count) {
        if (holder == null || holder.getAuthorHolder() == null) return;
        holder.getAuthorHolder().setViewCount(count);
        setViewCount();
    }

    public void setViewCount() {
        if (findViewById(R.id.stream_viewer_count_text_View) != null)
            ((TextView) findViewById(R.id.stream_viewer_count_text_View)).setText(holder.getAuthorHolder().getViewCount());
    }

    public void updateFollowing(boolean isFollowing) {
        if (holder == null) return;
        holder.getAuthorHolder().setFollowing(isFollowing);
        int followCount = holder.getAuthorHolder().getFollowCount();
        holder.getAuthorHolder().setFollowCount(isFollowing ? followCount + 1 : followCount - 1);
        markFollowing();
    }

    private void markFollowing() {
        if (findViewById(R.id.follow_button) != null) {
            ((TextView) findViewById(R.id.follow_button)).setText(holder.getAuthorHolder().isFollowing() ? getContext().getString(R.string.following) : getContext().getString(R.string.follow));
            findViewById(R.id.follow_button).setBackgroundTintList(ColorStateList.valueOf(holder.getAuthorHolder().isFollowing() ? ContextCompat.getColor(getContext(), R.color.light_grey) : ContextCompat.getColor(getContext(), R.color.color_accent)));
        }

        if (findViewById(R.id.user_followers_count) != null)
            ((TextView) findViewById(R.id.user_followers_count)).setText(CommonUtils.formatValue(holder.getAuthorHolder().getFollowCount()) + " Followers");
    }

    public void animateHeartUp(long count) {
        int orientation = getContext().getResources().getConfiguration().orientation;
        if (findViewById(R.id.heart_image_view) != null && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (heartAnimator != null)
                heartAnimator.animateHeartUp();
        }

        BindingUtils.setNumberFormat(findViewById(R.id.heart_count_text), count);
    }

    public void onStreamEnd() {
        if (viewCallbackListener != null && viewCallbackListener.get() != null)
            viewCallbackListener.get().streamEnded();
    }

    public void viewStateOnOrientationChange(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.close_btn).setVisibility(View.VISIBLE);
            findViewById(R.id.share_btn).setVisibility(View.VISIBLE);
            findViewById(R.id.streamer_info_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.gift_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sticker_button).setVisibility(View.VISIBLE);
            findViewById(R.id.game_name_text_view).setVisibility(View.VISIBLE);
            findViewById(R.id.stream_title_text_view).setVisibility(View.VISIBLE);
            findViewById(R.id.heart_animation_container).setVisibility(View.VISIBLE);
            findViewById(R.id.heart_image_view).setVisibility(View.VISIBLE);
            findViewById(R.id.chat_state_btn).setVisibility(View.VISIBLE);
            findViewById(R.id.stream_viewer_count_text_View).setVisibility(View.VISIBLE);
            findViewById(R.id.heart_count_text).setVisibility(View.VISIBLE);
            if (holder != null) {
                if (holder.isGiftEnabled())
                    findViewById(R.id.gift_button).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.gift_button).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.close_btn).setVisibility(View.GONE);
            findViewById(R.id.share_btn).setVisibility(View.GONE);
            findViewById(R.id.streamer_info_layout).setVisibility(View.GONE);
            findViewById(R.id.gift_button).setVisibility(View.GONE);
            findViewById(R.id.sticker_button).setVisibility(View.GONE);
            findViewById(R.id.game_name_text_view).setVisibility(View.GONE);
            findViewById(R.id.stream_title_text_view).setVisibility(View.GONE);
            findViewById(R.id.heart_animation_container).setVisibility(View.GONE);
            findViewById(R.id.heart_image_view).setVisibility(View.GONE);
            findViewById(R.id.chat_state_btn).setVisibility(View.GONE);
            findViewById(R.id.stream_viewer_count_text_View).setVisibility(View.GONE);
            findViewById(R.id.heart_count_text).setVisibility(View.GONE);
        }
    }

    public void hideControls() {
        Log.i(TAG, "controllers_hideControls: " + isControllerShowing + " / " + isControllerVisible());
        isControllerShowing = false;
        findViewById(R.id.volume_seekbar).setVisibility(View.GONE);
        //hideController();
        if (viewCallbackListener != null && viewCallbackListener.get() != null)
            viewCallbackListener.get().onControllerVisibilityChange(isControllerShowing);
    }

    public void showControls() {
        Log.i(TAG, "controllers_showControls: " + isControllerShowing + " / " + isControllerVisible());
        isControllerShowing = true;
        findViewById(R.id.volume_seekbar).setVisibility(View.GONE);
        //showController();
        if (viewCallbackListener != null && viewCallbackListener.get() != null)
            viewCallbackListener.get().onControllerVisibilityChange(isControllerShowing);
    }

    private class OverlayPerformListener implements YouTubeOverlay.PerformListener {
        private StreamHolder holder;

        OverlayPerformListener(StreamHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onAnimationStart() {
            // Do UI changes when double tapping / animation starts including showing the overlay
            setUseController(false);
            holder.getYoutubeDoubleTap().get().setVisibility(View.VISIBLE);
            Log.i(TAG, "OverlayPerformListener: onAnimationStart");
        }

        @Override
        public void onAnimationEnd() {
            Log.i(TAG, "OverlayPerformListener: onAnimationEnd");
            holder.getYoutubeDoubleTap().get().setVisibility(View.GONE);
            setUseController(true);
            isControllerShowing = false;
            hideOverlayHandler.removeCallbacks(controlOverlayRunnable);
        }
    }

    private SeekListener seekListener = new SeekListener() {
        @Override
        public void seekBackward(long duration) {
            Log.i(TAG, "Video seek backward: " + duration);
            mListener.trackEventBackwardSeek(duration);
        }

        @Override
        public void seekForward(long duration) {
            Log.i(TAG, "Video seek forward: " + duration);
            mListener.trackEventForwardSeek(duration);
        }

        @Override
        public void onVideoEndReached() {
            Log.i(TAG, "Video end reached");
        }

        @Override
        public void onVideoStartReached() {
//            playWhenReady = false;
//            if (player != null)
//                player.setPlayWhenReady(false);
            Log.i(TAG, "Video start reached");
        }
    };

    public interface PlayerAnalyticsListener {
        void trackFirstEventWatchStream();

        void trackEventWatchStream30Secs(long ttl);

        void trackEventWatchStream5mins(long ttl);

        void trackEventWatchStream11mins(long ttl);

        void trackEventWatchStream30mins(long ttl);

        void trackEventWatchStream45mins(long ttl);

        void trackEventWatchStream1hrs(long ttl);

        void trackEventWatchStream2hrs(long ttl);

        void trackFirstEventWatchStream5Mins(long ttl);

        void makeViewApiCall(int duration, long timeElapsed);

        void sendVideoAnalytics(long exoPosition, long duration);

        void updateDuration();

        void setPlayerObserver();

        void trackEventForwardSeek(long duration);

        void trackEventBackwardSeek(long duration);
        void onPlayerPausedAfterMomentPlayed();
    }
}
