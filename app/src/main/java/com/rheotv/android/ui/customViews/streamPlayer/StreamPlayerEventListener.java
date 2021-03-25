package com.rheotv.android.ui.customViews.streamPlayer;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.UnrecognizedInputFormatException;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.rheotv.android.R;
import com.rheotv.android.ui.activities.player.activity.ViewPagerMediator;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.utils.hourglass.Hourglass;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

import static com.rheotv.android.ui.customViews.streamPlayer.StreamUtils.isBehindLiveWindow;

public class StreamPlayerEventListener implements Player.EventListener {
    private final String TAG = getClass().getSimpleName();
    private StreamHolder holder;
    private boolean isLive = false;
    private boolean inErrorState;
    private String errorString;
    private long resumePosition;
    private int resumeWindow;
    private boolean isInitialLoad;
    private String videoMode = "portrait";

    private ExoPlayer exoPlayer;
    private StreamTapPlayerView playerView;

    private Handler endStreamHandler = new Handler();
    private Runnable streamEndRunnable = new Runnable() {
        @Override
        public void run() {
            // check end stream
        }
    };

    public StreamPlayerEventListener(StreamHolder holder) {
        this.holder = holder;
    }

    private Hourglass streamTimer = new Hourglass(new Date().getTime(), 1000) {
        @Override
        public void onTimerTick(long timeRemaining, long passedTime) {

        }

        @Override
        public void onTimerFinish() {

        }
    };

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        View progressView = holder.getProgressView().get();
        if (playbackState == Player.STATE_ENDED) {
            if (!updateView()) {
                // check if update countdown todo
                // playNextVideo(); todo
                streamTimer.stopTimer();
                Log.i(TAG, "stream_timer_stopping " + streamTimer.isRunning());
            }
        } else if (playWhenReady && playbackState == Player.STATE_READY) {
            Log.i(getClass().getName(), "stream_timer_starting " + streamTimer.isRunning() + " and " + streamTimer.isPaused());

            endStreamHandler.removeCallbacks(streamEndRunnable);

            // trackFirstEventWatchStream(); todo
            resizeVideoView();
            startStreamTime();
            finishLoading();
            progressView.setVisibility(View.GONE);
        } else if (playWhenReady) {
            if (playbackState == Player.STATE_BUFFERING) {
                progressView.setVisibility(View.VISIBLE);
                if (isLive) endStreamHandler.postDelayed(streamEndRunnable, 10000);

                streamTimer.pauseTimer();
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

    @Override
    public void onPlayerError(ExoPlaybackException error) {
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
                // initialize(); todo re-initialize
            } else {
                updateResumePosition();
            }

        } else if (error.type == ExoPlaybackException.TYPE_SOURCE) {
            String cause = error.getCause().getLocalizedMessage();
            Log.i(TAG, "Caught Source Exception");

            if (error.getCause() instanceof UnrecognizedInputFormatException) {
                EventBus.getDefault().post(new EventBusModel.End(holder.getPostId()));
                FirebaseCrashlytics.getInstance().recordException(error);
            } else if (cause != null && cause.equalsIgnoreCase("Response code: 404")) {
                updateResumePosition();
                resumePosition += 4000;
                playerView.getPlayer().seekTo(resumeWindow, resumePosition);
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(holder.getContextRef().get(), message, Toast.LENGTH_SHORT).show();
    }

    // todo implement callback
    private boolean updateView() {
        return true;
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

    private void finishLoading() {
        if (isInitialLoad) {
            isInitialLoad = false;
            exoPlayer.seekToDefaultPosition();
            holder.getPlaceholderThumbnail().get().setVisibility(View.GONE);
        }
    }

    private void resizeVideoView() {
        ViewGroup.LayoutParams layoutParams = playerView.getLayoutParams();
        int width = playerView.getContext().getResources().getDisplayMetrics().widthPixels;
        int orientation = playerView.getContext().getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
        } else {
            int height;
            if (videoMode.equalsIgnoreCase("landscape"))
                height = (width * 9) / 16;
            else
                height = width;

            layoutParams.height = height;
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        }

        playerView.setLayoutParams(layoutParams);
    }
}
