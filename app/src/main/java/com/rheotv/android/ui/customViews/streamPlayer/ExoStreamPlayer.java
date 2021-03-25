package com.rheotv.android.ui.customViews.streamPlayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
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
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.UnrecognizedInputFormatException;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistTracker;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.VideoUrlObj;
import com.rheotv.android.model.VideoQuality;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.TimeUtils;
import com.rheotv.android.utils.hourglass.Hourglass;

import org.greenrobot.eventbus.EventBus;

import java.net.SocketTimeoutException;
import java.util.Date;

import static com.rheotv.android.ui.customViews.streamPlayer.StreamUtils.buildMediaSource;
import static com.rheotv.android.ui.customViews.streamPlayer.StreamUtils.isBehindLiveWindow;

public class ExoStreamPlayer {
    private final String TAG = getClass().getSimpleName();
    private StreamHolder holder;
    private StreamTapPlayerView tapPlayerView;
    private YouTubeOverlay youtubeOverlay;

    private boolean playWhenReady;
    private ExoPlayer exoPlayer;
    private boolean isPlayerDraggedBehind;
    private long maxPlayerPos;
    private int resumeWindow;
    private long resumePosition;
    private StreamPlayerEventListener playerEventListener;
    //private StreamTapPlayerView previousTapPlayerView;
    private StreamTapPlayerView.PlayerAnalyticsListener mListener;

    private long videoTimeElapsed = 0;
    private boolean isTwoSecondViewCounted;
    private SharedPrefsUtils sharedPrefsUtils;
    private int failedCount = 0;
    private boolean isJumpToNext = false;

    private Handler handler;
    private Runnable updateDurationRunnable = () -> {
        if (mListener != null) {
            mListener.updateDuration();
        }
    };

    private Handler liveWindowHandler;
    private Runnable liveWindowRunnable = new Runnable() {
        @Override
        public void run() {
            if (exoPlayer != null && holder.isLive()) {
                long currentPos = exoPlayer.getContentPosition();
                if (maxPlayerPos - currentPos > 120000) {
                    if (currentPos != 0) {
                        isPlayerDraggedBehind = true;
                        Log.i("Player_State", "Dragged behind");
                        if (holder.isLive() && tapPlayerView != null && tapPlayerView.findViewById(R.id.go_to_live_btn) != null)
                            tapPlayerView.findViewById(R.id.go_to_live_btn).setVisibility(View.VISIBLE);
                        maxPlayerPos = currentPos;
                    } else {
                        if (holder.isLive() && tapPlayerView != null && tapPlayerView.findViewById(R.id.go_to_live_btn) != null)
                            tapPlayerView.findViewById(R.id.go_to_live_btn).setVisibility(View.GONE);
                    }
                } else {
                    maxPlayerPos = currentPos;
                }
                if (liveWindowHandler != null) {
                    liveWindowHandler.postDelayed(this, 200);
                }
            }
        }
    };

    private int loadingTry = 0;
    private Hourglass loadingTimer = new Hourglass(new Date().getTime(), 1000) {

        @Override
        public void onTimerTick(long timeRemaining, long passedTime) {
            Log.e(TAG, "loadingTimer: Time_1 " + passedTime);
            if (exoPlayer == null || passedTime == 0) return;
            if (passedTime % 10000 == 0) {
                if (loadingTry >= 3) {
                    Log.e(TAG, "loadingTimer: Time_2 " + passedTime % 1000);
                    // load next video, after 2 retries
                    loadingTimer.stopTimer();
                    jumpNext();
                } else {
                    Log.e(TAG, "loadingTimer: Time_3 " + passedTime % 1000);
                    if (!exoPlayer.isPlaying()) {
                        Log.e(TAG, "loadingTimer: Time_4 " + passedTime % 1000);
                        //loadingTimer.stopTimer();
                        resumeWindow = exoPlayer != null ? exoPlayer.getCurrentWindowIndex() : 0;
                        resumePosition = Math.max(0, exoPlayer != null ? exoPlayer.getContentPosition() : 0L);
                        resumePosition += 4000;
                        exoPlayer.seekTo(resumeWindow, resumePosition);
                        loadingTry++;
                    }
                }
            }
        }

        @Override
        public void onTimerFinish() {

        }
    };

    private Hourglass streamTimer = new Hourglass(new Date().getTime(), 1000) {
        @Override
        public void onTimerTick(long timeRemaining, long passedTime) {
            long exoPosition = exoPlayer.getCurrentPosition() / 1000;
            videoTimeElapsed = passedTime / 1000;
            if (videoTimeElapsed == 30) {
                if (mListener != null) {
                    Log.i("VideoWatchEvent", "30 sec");
                    mListener.trackEventWatchStream30Secs(videoTimeElapsed);
                }
            } else if (videoTimeElapsed == 300) {
                if (mListener != null) {
                    Log.i("VideoWatchEvent", "5 mins");
                    mListener.trackFirstEventWatchStream5Mins(videoTimeElapsed);
                    mListener.trackEventWatchStream5mins(videoTimeElapsed);
                }
            } else if (videoTimeElapsed == 11 * 60) {
                if (mListener != null) {
                    Log.i("VideoWatchEvent", "11 mins");
                    mListener.trackEventWatchStream11mins(videoTimeElapsed);
                }
            } else if (videoTimeElapsed == 30 * 60) {
                if (mListener != null) {
                    Log.i("VideoWatchEvent", "30 mins");
                    mListener.trackEventWatchStream30mins(videoTimeElapsed);
                }
            } else if (videoTimeElapsed == 45 * 60) {
                if (mListener != null) {
                    Log.i("VideoWatchEvent", "45 mins");
                    mListener.trackEventWatchStream45mins(videoTimeElapsed);
                }
            } else if (videoTimeElapsed == 60 * 60) {
                if (mListener != null) {
                    Log.i("VideoWatchEvent", "1 hr");
                    mListener.trackEventWatchStream1hrs(videoTimeElapsed);
                }
            } else if (videoTimeElapsed == 2 * 60 * 60) {
                if (mListener != null) {
                    Log.i("VideoWatchEvent", "2 hr");
                    mListener.trackEventWatchStream2hrs(videoTimeElapsed);
                }
            }

            if (videoTimeElapsed == 2) {
                if (!isTwoSecondViewCounted) {
                    Log.i(getClass().getName(), "hit_2nd_api : " + videoTimeElapsed);
                    isTwoSecondViewCounted = true;
                    if (mListener != null) {
                        mListener.makeViewApiCall(2, exoPosition);
                    }
                }
            } else if (videoTimeElapsed > 2 && videoTimeElapsed % 10 == 2) {
                Log.i(getClass().getName(), "hit_12nd_api : " + videoTimeElapsed);
                if (mListener != null) {
                    mListener.makeViewApiCall(10, exoPosition);
                    mListener.sendVideoAnalytics(exoPosition, exoPlayer.getDuration());
                }
            }

            long currentTime = exoPlayer.getCurrentPosition();
            if ((!canSeekVideo || !isFirstMomentVideoPaused) && ((momentsStartTime > 0 && currentTime <= momentsStartTime) || (momentEndTime > 0 && currentTime >= momentEndTime))) {
                isFirstMomentVideoPaused = true;
                pausePlayer();
                if (mListener != null) {
                    mListener.onPlayerPausedAfterMomentPlayed();
                }
            }

            delayViewUpdate(videoTimeElapsed);
        }

        @Override
        public void onTimerFinish() {

        }
    };
    private boolean isFirstMomentVideoPaused = true;

    public ExoStreamPlayer(StreamHolder holder) {
        this.holder = holder;
        //previousTapPlayerView = this.tapPlayerView;
        this.tapPlayerView = holder.getDoubleTapPlayerView().get();
        this.youtubeOverlay = holder.getYoutubeDoubleTap().get();
        handler = new Handler();
    }

    public void attachPlayer() {
        /*if (previousTapPlayerView != null) {
            previousTapPlayerView.setPlayer(null);
            previousTapPlayerView = null;
        }*/
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
        if (exoPlayer == null) {
            TrackSelection.Factory factory = new AdaptiveTrackSelection.Factory();
            exoPlayer = getPlayer(new DefaultTrackSelector(factory));
            ((SimpleExoPlayer) exoPlayer).setVolume(holder.getVolume());
            reSetupPlayer();
        } else {
            //((SimpleExoPlayer) exoPlayer).clearVideoSurface();
            reSetupPlayer();
        }

        resumePosition = holder.getResumePosition();
        resumeWindow = holder.getResumeWindow();

        loadStreamQuality();
        buildPlayer();

        Log.i(TAG, "stream_url_is: " + holder.getStreamUrl());
    }

    private void reSetupPlayer() {
        tapPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        tapPlayerView.setPlayer(exoPlayer);
        youtubeOverlay.setPlayer(exoPlayer);
        exoPlayer.setPlayWhenReady(true);
    }

    private void loadStreamQuality() {
        if (holder.getQualityFormat() == null) {
            if (StreamUtils.checkSpecialFormat(tapPlayerView.getContext())) {
                holder.setQualityFormat(getQualityFormat("medium"));
            } else {
                if (sharedPrefsUtils == null) {
                    sharedPrefsUtils = new SharedPrefsUtils();
                }
                String quality = sharedPrefsUtils.getStringPreference(tapPlayerView.getContext(), SharedPrefsUtils.VIDEO_FORMAT_REQUESTED);
                if (quality != null && StreamUtils.containsQuality(holder.getStreamUrl(), quality)) {
                    holder.setQualityFormat(quality);
                } else {
                    holder.setQualityFormat(getQualityFormat("auto"));
                }
            }
        }
    }

    private String getQualityFormat(String quality) {
        if (holder == null || holder.getStreamUrl() == null) return "auto";
        for (VideoUrlObj urlObj : holder.getStreamUrl()) {
            if (urlObj.getNetworkType().equalsIgnoreCase(quality)) {
                return urlObj.getName();
            }
        }
        return getQualityFormat("medium");
    }

    /*private void setQualityFormat(String url) {
        try {
            String[] parts = url.split("\\/");
            String currentFormat = parts[parts.length - 2];
            if (currentFormat == null) return;
            holder.setQualityFormat(QualityFormat.valueOf(currentFormat));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private void buildPlayer() {
        if ((holder.getStreamUrl() != null && !holder.getStreamUrl().isEmpty()) || (holder.getPromoVideoUrl() != null && !holder.getPromoVideoUrl().isEmpty())) {
            String uriString = null;
            if (TimeUtils.hasStreamNotStarted(holder.getStartFrom())) {
                uriString = holder.getPromoVideoUrl();

            } else {
                uriString = StreamUtils.getUrlForCurrentFormat(holder.getStreamUrl(), holder.getQualityFormat());
            }
            Uri uri = Uri.parse(uriString);
            MediaSource mediaSource = buildMediaSource(uri);
            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            exoPlayer.prepare(mediaSource, !haveResumePosition, true);

            if (holder.isLive()) {
                if (resumePosition != 0) {
                    exoPlayer.seekTo(resumeWindow, resumePosition);
                    isPlayerDraggedBehind = true;
                    if (tapPlayerView.findViewById(R.id.go_to_live_btn) != null)
                        tapPlayerView.findViewById(R.id.go_to_live_btn).setVisibility(View.VISIBLE);
                } else {
                    isPlayerDraggedBehind = false;
                    if (tapPlayerView.findViewById(R.id.go_to_live_btn) != null)
                        tapPlayerView.findViewById(R.id.go_to_live_btn).setVisibility(View.GONE);
                    exoPlayer.seekToDefaultPosition();
                }
            } else {
                if (haveResumePosition)
                    exoPlayer.seekTo(resumeWindow, resumePosition);
                else
                    exoPlayer.seekTo(0, 0);
            }

            if (holder.getQualityFormat().equalsIgnoreCase(VideoQuality.Audio.INSTANCE.toString())) {
                holder.getReportIcon().get().setVisibility(View.VISIBLE);
                holder.getSettingIcon().get().setVisibility(View.VISIBLE);
            } else {
                if (holder.getReportIcon().get().getVisibility() == View.VISIBLE) {
                    holder.getReportIcon().get().setVisibility(View.GONE);
                    holder.getSettingIcon().get().setVisibility(View.GONE);
                }
            }

            liveWindowHandler = new Handler(Looper.getMainLooper());
            liveWindowHandler.postDelayed(liveWindowRunnable, 200);
        }

        tapPlayerView.setVisibility(View.VISIBLE);
        tapPlayerView.setUseController(true);
        isJumpToNext = false;
    }

    private void jumpNext() {
        if (!isJumpToNext) {
            Log.i(TAG, "loadingTimer: Jumping next");
            isJumpToNext = true;
            EventBus.getDefault().post(new EventBusModel.End(holder.getPostId()));
        }
    }

    private void buildPromoVideoPlayer() {
        if (holder.getPromoVideoUrl() != null && !holder.getPromoVideoUrl().isEmpty()) {

        }
    }

    private SimpleExoPlayer getPlayer(DefaultTrackSelector selector) {
        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
        /* This is 50000 milliseconds in ExoPlayer 2.9.6 */
        final int loadControlBufferMs = 50000;

        /* Configure the DefaultLoadControl to use the same value for */
        builder.setBufferDurationsMs(loadControlBufferMs,
                loadControlBufferMs,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
        DefaultLoadControl loadControl = builder.createDefaultLoadControl();

        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(holder.getContextRef().get(),
                new DefaultRenderersFactory(holder.getContextRef().get()), selector, loadControl);
        if (playerEventListener == null)
            playerEventListener = new StreamPlayerEventListener();
        player.addListener(playerEventListener);
        return player;
    }

    public void seekToLive() {
        isPlayerDraggedBehind = false;
        exoPlayer.seekToDefaultPosition();
        resumePosition = 0;
    }

    public void pausePlayer() {
        streamTimer.pauseTimer();
        exoPlayer.setPlayWhenReady(false);
    }

    private boolean isMomentsSeek = false;
    private long momentsStartTime = 0;
    private boolean canSeekVideo = true;

    public void momentsSeekStartsFrom(long seekTo) {
        if (exoPlayer != null) {
            isMomentsSeek = true;
            momentsStartTime = seekTo - 4 * 1000;
            isFirstMomentVideoPaused = false;
            exoPlayer.seekTo(momentsStartTime);
        }
    }

    public void canSeek(boolean canSeek) {
        canSeekVideo = canSeek;
    }

    private long momentEndTime = 0;

    public void setMomentsEndTime(long endTime) {
        if (exoPlayer != null) {
            momentEndTime = endTime;
        }
    }

    public void resumePlayer() {
        streamTimer.resumeTimer();
        exoPlayer.setPlayWhenReady(true);
    }

    public void mute() {
        ((SimpleExoPlayer) exoPlayer).setVolume(0);
    }

    public void unMute() {
        ((SimpleExoPlayer) exoPlayer).setVolume(holder.getVolume());
    }

    public void detachPlayer() {
        if (streamTimer != null)
            streamTimer.stopTimer();
        if (loadingTimer != null)
            loadingTimer.stopTimer();
        if (exoPlayer != null)
            exoPlayer.release();
        tapPlayerView = null;
        youtubeOverlay = null;
        if (playerEventListener != null && playerEventListener.endStreamHandler != null)
            playerEventListener.endStreamHandler.removeCallbacks(playerEventListener.streamEndRunnable);
        if (handler != null)
            handler.removeCallbacks(updateDurationRunnable);
        if (liveWindowHandler != null)
            liveWindowHandler.removeCallbacks(liveWindowRunnable);
        if (exoPlayer != null && playerEventListener != null)
            exoPlayer.removeListener(playerEventListener);
    }

    public void updateStreamQuality(String format) {
        holder.setQualityFormat(format);
        attachPlayer();
    }

    public int getResumeWindow() {
        return resumeWindow;
    }

    public long getResumePosition() {
        return resumePosition;
    }

    public long getVideoTimeElapsed() {
        return videoTimeElapsed;
    }

    public void setAnalyticsListener(StreamTapPlayerView.PlayerAnalyticsListener listener) {
        mListener = listener;
    }

    public void removeAnalyticsListener() {
        mListener = null;
    }

    private void delayViewUpdate(long timePassed) {
        if (timePassed % 30 == 0) {
            handler.postDelayed(updateDurationRunnable, 1000);
        }
    }

    public void addStreamRunnableToHandler() {
        if (playerEventListener != null && playerEventListener.endStreamHandler != null)
            playerEventListener.endStreamHandler.postDelayed(playerEventListener.streamEndRunnable, 10000);
    }

    public float getVolume() {

        return ((SimpleExoPlayer) exoPlayer).getVolume();
    }

    public void setVolume(int progress) {
        ((SimpleExoPlayer) exoPlayer).setVolume(((float) progress) / 100);
    }

    class StreamPlayerEventListener implements Player.EventListener {
        private final String TAG = getClass().getSimpleName();
        private boolean inErrorState;
        private String errorString;
        private boolean isInitialLoad = false;

        private Handler endStreamHandler = new Handler(Looper.getMainLooper());
        private Runnable streamEndRunnable = new Runnable() {
            @Override
            public void run() {
                if (tapPlayerView != null)
                    tapPlayerView.onStreamEnd();
            }
        };

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_ENDED) {
                //if (!updateView()) {
                // check if update countdown todo
                // playNextVideo(); todo
                if (tapPlayerView != null)
                    tapPlayerView.onStreamEnd();
                streamTimer.stopTimer();
                loadingTimer.stopTimer();
                Log.i(TAG, "stream_timer_stopping " + streamTimer.isRunning());
                //}
            } else if (playWhenReady && playbackState == Player.STATE_READY) {
                Log.i(getClass().getName(), "stream_timer_starting " + streamTimer.isRunning() + " and " + streamTimer.isPaused());

                endStreamHandler.removeCallbacks(streamEndRunnable);
                if (mListener != null) {
                    mListener.trackFirstEventWatchStream();
                }

                Log.e(TAG, "loadingTimer: STATE_READY ");
                holder.getDoubleTapPlayerView().get().resizeVideoView();
                if (isMomentsSeek && exoPlayer.getCurrentPosition() == momentsStartTime) {
                    momentsStartTime = 0;
                    isMomentsSeek = false;
                }

                // trackFirstEventWatchStream(); todo
                startStreamTime();
                stopLoadingTime();
                finishLoading();
                holder.getProgressView().get().setVisibility(View.GONE);
            } else if (playWhenReady) {
                if (playbackState == Player.STATE_BUFFERING) {
                    holder.getProgressView().get().setVisibility(View.VISIBLE);
                    if (holder.isLive()) endStreamHandler.postDelayed(streamEndRunnable, 10000);
                    streamTimer.pauseTimer();
                    Log.e(TAG, "loadingTimer: Buffering");
                    if (!loadingTimer.isRunning())
                        loadingTimer.startTimer(); // start loading timer so that after every 10 seconds we can jump exo-player to next file
                }
            } else {
                streamTimer.pauseTimer();
                Log.i(getClass().getName(), "stream_timer_pausing " + streamTimer.isRunning());
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            if (inErrorState) {
                updateResumePosition();
            }
        }

        // log error on crashlytics
        @Override
        public void onPlayerError(ExoPlaybackException error) {
            FirebaseCrashlytics.getInstance().recordException(error.getCause());
            stopLoadingTime();
            boolean isExceptionHandled = false;
            if (error.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = error.getRendererException();
                Context context = holder.getContextRef().get();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitializationException.diagnosticInfo == null) {
                        if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                            errorString = context.getString(R.string.error_querying_decoders);
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString = context.getString(R.string.error_no_secure_decoder,
                                    decoderInitializationException.mimeType);
                        } else {
                            errorString = context.getString(R.string.error_no_decoder,
                                    decoderInitializationException.mimeType);
                        }
                    } else {
                        errorString = context.getString(R.string.error_instantiating_decoder,
                                decoderInitializationException.diagnosticInfo);
                    }
                }

                if (errorString != null)
                    showToast(errorString);

                inErrorState = true;
                if (isBehindLiveWindow(error)) {
                    clearResumePosition();
                    holder.getStreamPlayer().attachPlayer();
                } else {
                    updateResumePosition();
                }

                isExceptionHandled = true;

            } else if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                String cause = error.getCause().getLocalizedMessage();
                Log.i(TAG, "Caught Source Exception");

                if (error.getCause() instanceof UnrecognizedInputFormatException) {
                    jumpNext();
                    FirebaseCrashlytics.getInstance().recordException(error);
                    isExceptionHandled = true;
                } else if (error.getCause() instanceof HlsPlaylistTracker.PlaylistStuckException) {
                    buildPlayer();
                    FirebaseCrashlytics.getInstance().recordException(error);
                    isExceptionHandled = true;
                }
                if ((cause != null && cause.equalsIgnoreCase("Response code: 404"))
                        || error.getCause() instanceof SocketTimeoutException) {
                    updateResumePosition();
                    resumePosition += 4000;
                    tapPlayerView.getPlayer().seekTo(resumeWindow, resumePosition);
                    isExceptionHandled = true;
                } else {
                    updateResumePosition();
                    resumePosition += 8000;
                    tapPlayerView.getPlayer().seekTo(resumeWindow, resumePosition);
                }
            } else {
                jumpNext();
//                if (failedCount >= 2) {
//                    EventBus.getDefault().post(ViewPagerMediator.PageChange.NEXT);
//                } else {
//                    updateResumePosition();
//                    resumePosition += 4000;
//                    tapPlayerView.getPlayer().seekTo(resumeWindow, resumePosition);
//                }
//                failedCount++;
            }

//            if (!isExceptionHandled && BuildConfig.DEBUG)
//                Toast.makeText(RheoTvApp.getNonUiContext(), "ExoPlayerException: " + error.getCause().toString(), Toast.LENGTH_SHORT).show();
        }

        private void showToast(String message) {
            Toast.makeText(holder.getContextRef().get(), message, Toast.LENGTH_SHORT).show();
        }

        // todo implement callback
        private boolean updateView() {
            return TimeUtils.hasStreamNotStarted(holder.getStartFrom());
        }

        private void updateResumePosition() {
            resumeWindow = exoPlayer != null ? exoPlayer.getCurrentWindowIndex() : 0;
            resumePosition = Math.max(0, exoPlayer != null ? exoPlayer.getContentPosition() : 0L);
        }

        private void clearResumePosition() {
            resumeWindow = C.INDEX_UNSET;
            resumePosition = C.TIME_UNSET;
        }

        private void startStreamTime() {
            if (!streamTimer.isRunning()) streamTimer.startTimer();
            else if (streamTimer.isPaused()) streamTimer.resumeTimer();
        }

        private void stopLoadingTime() {
            if (loadingTimer.isRunning()) loadingTimer.stopTimer();
            loadingTry = 0;
        }

        private void finishLoading() {
            failedCount = 0;
            if (isInitialLoad) {
                isInitialLoad = false;
                exoPlayer.seekToDefaultPosition();
            }

            if (!holder.getQualityFormat().equalsIgnoreCase(VideoQuality.Audio.INSTANCE.toString())) {
                if (holder.getPlaceholderThumbnail().get().getVisibility() == View.VISIBLE)
                    holder.getPlaceholderThumbnail().get().setVisibility(View.GONE);
            } else {
                holder.getPlaceholderThumbnail().get().setVisibility(View.VISIBLE);
                Glide.with(holder.getContextRef().get())
                        .asGif()
                        .load(R.drawable.audio_gif)
                        .into(holder.getPlaceholderThumbnail().get());
            }
        }
    }
}
