package com.rheotv.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
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
import com.rheotv.android.db.ClipItem;
import com.rheotv.android.ui.adapters.ClipsListAdapter;
import com.rheotv.android.utils.hourglass.Hourglass;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class ExoPlayerRecyclerView extends RecyclerView {

    private static final String TAG = "ExoPlayerRecyclerView";
    private static final String AppName = "Android ExoPlayer";
    /**
     * PlayerViewHolder UI component
     * Watch PlayerViewHolder class
     */
    private ImageView authorImageUrl;
    private TextView authorName;
    private TextView postTitle;
    private TextView followerCount;
    private ProgressBar progressBar;
    private View viewHolderParent;
    private FrameLayout mediaContainer;
    private PlayerView videoSurfaceView;
    private SimpleExoPlayer videoPlayer;
    private boolean isFollowCardShown;


    /**
     * variable declaration
     */
    // Media List
    private List<ClipItem> mediaObjects = new ArrayList<>();
    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;
    private RequestManager requestManager;
    // controlling volume state
    private VolumeState volumeState;

    private boolean isLoading;
    private boolean isLoadMoreAllowed;

    private ExoPlayerClickListener mListener;

    long videoBufferingStartTime = 0;
    long videoBufferingTime = 0;

    private boolean shouldAutoScroll = true;
    //    private HourglassAsync timer;
    boolean isTwoSecondCounted = false;

    public void setListener(ExoPlayerClickListener mListener) {
        this.mListener = mListener;
    }

    private OnClickListener videoViewClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleVolume();
        }
    };

    public ExoPlayerRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ExoPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context.getApplicationContext();
        Display display = ((WindowManager) Objects.requireNonNull(
                getContext().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);


        isFollowCardShown = false;
        videoSurfaceDefaultHeight = point.x;
        screenDefaultHeight = point.y;

        videoSurfaceView = new PlayerView(this.context);
        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);

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
        videoPlayer = ExoPlayerFactory.newSimpleInstance(context,
                new DefaultRenderersFactory(context), trackSelector, loadControl);

        // Disable Player Control
        videoSurfaceView.setUseController(false);
        // Bind the player to the view.
        videoSurfaceView.setPlayer(videoPlayer);
        // Turn on Volume
        setVolumeControl(VolumeState.ON);
//        timer = getTimer();


        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                videoBufferingStartTime = System.currentTimeMillis();

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!CommonUtils.isUserContentModerator())
                        shouldAutoScroll = true;
                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic
                    int currentPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    if (recyclerView.getAdapter().getItemViewType(currentPosition) == ClipsListAdapter.CLIP_VIEW) {
                        if (!recyclerView.canScrollVertically(1)) {
                            playVideo(true);
                        } else {
                            playVideo(false);
                        }

                        try {
                            if (streamTimer != null && streamTimer.isRunning()) {
                                streamTimer.stopTimer();
                            }
                            mListener.onClipChange(mediaObjects.get(currentPosition), currentPosition);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (viewHolderParent != null &&
                        viewHolderParent.equals(view)) {
                    resetVideoView();
                }
            }
        });

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
                        Log.e(TAG, "onPlayerStateChanged: Buffering video.");
                        if (progressBar != null) {
                            progressBar.setVisibility(VISIBLE);
                        }

                        if (playPosition > 0 && playPosition < mediaObjects.size())
                            mListener.onStreamBuffering(mediaObjects.get(playPosition), playPosition);

                        break;
                    case Player.STATE_ENDED:
                        int moveToPos = playPosition + 1;
                        Log.d(TAG, "onPlayerStateChanged: Video ended. Move to " + moveToPos);

                        if (mListener != null && playPosition >= 0 && playPosition < mediaObjects.size()) {
                            mListener.makeViewApiCall((int) videoPlayer.getDuration() / 1000, videoPlayer.getDuration() / 1000, mediaObjects.get(playPosition));
                        }
                        if (shouldAutoScroll && !CommonUtils.isUserContentModerator()) {
                            smoothScrollToPosition(moveToPos);
                        }

                        if (playPosition > 0 && playPosition < mediaObjects.size() && mListener != null)
                            mListener.onStreamEnd(mediaObjects.get(playPosition), playPosition);
                        if (streamTimer != null && streamTimer.isRunning()) {
                            streamTimer.stopTimer();
                        }

                        break;
                    case Player.STATE_IDLE:

                        break;
                    case Player.STATE_READY:
                        Log.e(TAG, "onPlayerStateChanged: Ready to play.");
                        if (progressBar != null) {
                            progressBar.setVisibility(GONE);
                        }
                        Log.d(TAG, "Is Video view added " + (isVideoViewAdded ? "true" : "false"));
                        if (!isVideoViewAdded) {
                            addVideoView();
                        }
                        if (videoBufferingTime == 0) {
                            videoBufferingTime = System.currentTimeMillis() - videoBufferingStartTime;
                        }

                        if (playPosition > 0 && playPosition < mediaObjects.size())
                            mListener.onStreamStart(mediaObjects.get(playPosition), playPosition, videoBufferingTime);
                        if (streamTimer != null) {
                            streamTimer.startTimer();
                        }
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
    }

    private boolean isTwoSecondViewCounted = false;
    private boolean isHalVideoViewViewCounted = false;
    private Hourglass streamTimer = new Hourglass(new Date().getTime(), 1000) {
        @Override
        public void onTimerTick(long timeRemaining, long passedTime) {
            if (videoPlayer == null || playPosition < 0 || mediaObjects == null || playPosition >= mediaObjects.size())
                return;

            long playerDuration = videoPlayer.getCurrentPosition();
            long videoDuration = videoPlayer.getDuration();
            Log.i("ClipsFragment", "" + playerDuration);

            if (playerDuration >= 2000 && !isTwoSecondViewCounted) {
                Log.i(getClass().getName(), "video_durattion = " + videoPlayer.getCurrentPosition());
                if (mListener != null) {
                    mListener.makeViewApiCall((int) videoDuration / 1000, playerDuration / 1000, mediaObjects.get(playPosition));
                }
                isTwoSecondViewCounted = true;
            } else if (playerDuration >= videoDuration / 2 && !isHalVideoViewViewCounted) {
                Log.i(getClass().getName(), "video_durattion = " + videoPlayer.getCurrentPosition());
                if (mListener != null) {
                    mListener.makeViewApiCall((int) ((int) videoDuration / 1000), (int) videoDuration / (2 * 1000), mediaObjects.get(playPosition));
                }
                isHalVideoViewViewCounted = true;
            }
            if (playerDuration >= 6 * 1000 && !isFollowCardShown) {
                ViewHolder viewHolder = findViewHolderForAdapterPosition(playPosition);
                if (viewHolder instanceof ClipsListAdapter.ClipViewHolder) {
                    ClipsListAdapter.ClipViewHolder clipViewHolder = (ClipsListAdapter.ClipViewHolder) viewHolder;
                    clipViewHolder.animateClipCard();
                }
                isFollowCardShown = true;
            }

        }

        @Override
        public void onTimerFinish() {

        }
    };


    public void setShouldAutoScroll(boolean shouldAutoScroll) {
        this.shouldAutoScroll = shouldAutoScroll;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void playVideo(boolean isEndOfList) {

        int targetPosition;

        if (!isEndOfList) {
            int startPosition = ((LinearLayoutManager) Objects.requireNonNull(
                    getLayoutManager())).findFirstVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);

                targetPosition =
                        startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
            } else {
                targetPosition = startPosition;
            }
        } else {
            targetPosition = mediaObjects.size() - 1;
        }

        Log.d(TAG, "playVideo: target position: " + targetPosition);

        // video is already playing so return
        if (targetPosition == playPosition) {
            return;
        }

        // set the position of the list-item that is to be played
        playPosition = targetPosition;
        isTwoSecondViewCounted = false;
        isHalVideoViewViewCounted = false;
        isFollowCardShown = false;
        if (videoSurfaceView == null) {
            return;
        }

        // remove any old surface views from previously playing videos
        videoSurfaceView.setVisibility(INVISIBLE);
        removeVideoView(videoSurfaceView);

        int currentPosition =
                targetPosition - ((LinearLayoutManager) Objects.requireNonNull(
                        getLayoutManager())).findFirstVisibleItemPosition();

        View child = getChildAt(currentPosition);
        if (child == null) {
            return;
        }

        ClipsListAdapter.ClipViewHolder holder = (ClipsListAdapter.ClipViewHolder) child.getTag();
        if (holder == null) {
            playPosition = -1;
            return;
        }
        //mediaCoverImage = holder.mediaCoverImage;
        progressBar = holder.getmBinding().progress;


        //requestManager = holder.requestManager;
        mediaContainer = holder.getmBinding().mediaContainer;
        viewHolderParent = holder.getmBinding().parent;

        videoSurfaceView.setPlayer(videoPlayer);
        //viewHolderParent.setOnClickListener(videoViewClickListener);
        mediaContainer.setOnTouchListener(new OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    mListener.onDoubleTap(mediaObjects.get(playPosition), playPosition);

                    return super.onDoubleTap(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    mListener.onLongPress(mediaObjects.get(playPosition), playPosition);
                    super.onLongPress(e);
                }
            });

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                context, Util.getUserAgent(context, AppName));
        String mediaUrl = mediaObjects.get(targetPosition).getVideoUrl();
        if (mediaUrl != null) {
            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(mediaUrl));
            if (videoPlayer != null) {
                videoPlayer.prepare(videoSource);
                videoPlayer.setPlayWhenReady(true);
            }
        }
    }

    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     */
    private int getVisibleVideoSurfaceHeight(int playPosition) {
        int at = playPosition - ((LinearLayoutManager) Objects.requireNonNull(
                getLayoutManager())).findFirstVisibleItemPosition();
        Log.d(TAG, "getVisibleVideoSurfaceHeight: at: " + at);

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }

        int[] location = new int[2];
        child.getLocationInWindow(location);

        if (location[1] < 0) {
            return location[1] + videoSurfaceDefaultHeight;
        } else {
            return screenDefaultHeight - location[1];
        }
    }

    // Remove the old player
    private void removeVideoView(PlayerView videoView) {
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
            isVideoViewAdded = false;
            viewHolderParent.setOnClickListener(null);
        }
    }

    private void addVideoView() {

        if (playPosition >= 0 && mediaObjects.size() > playPosition) {
            int width = videoSurfaceDefaultHeight;
            int height;
            if (mediaObjects.get(playPosition).getVideoMode() != null && mediaObjects.get(playPosition).getVideoMode().equalsIgnoreCase("landscape")) {
                height = (width * 9) / 16;
            } else {
                height = screenDefaultHeight;
            }

            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) videoSurfaceView.getLayoutParams();
            if (lp == null) {
                lp = new FrameLayout.LayoutParams(width, height);
            } else {
                lp.height = height;
            }
            lp.gravity = Gravity.CENTER;
            videoSurfaceView.setLayoutParams(lp);
            videoSurfaceView.invalidate();

            mediaContainer.addView(videoSurfaceView);
            isVideoViewAdded = true;
            videoSurfaceView.requestFocus();
            videoSurfaceView.setVisibility(VISIBLE);
            videoSurfaceView.setAlpha(1);
        }
        //mediaCoverImage.setVisibility(GONE);
    }

    private void resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView);
            playPosition = -1;
            videoSurfaceView.setVisibility(INVISIBLE);
            //mediaCoverImage.setVisibility(VISIBLE);
        }
    }

    public void releasePlayer() {

        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.release();
            videoPlayer = null;
        }

        viewHolderParent = null;
    }

    public void onPausePlayer() {
        if (videoPlayer != null) {
            videoPlayer.stop(true);
        }
    }

    public void onHoldPlayer() {
        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(false);
        }
    }

    public void resumePlayer() {
        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(true);
        }
    }

    private void toggleVolume() {
        if (videoPlayer != null) {
            if (volumeState == VolumeState.OFF) {
                Log.d(TAG, "togglePlaybackState: enabling volume.");
                setVolumeControl(VolumeState.ON);
            } else if (volumeState == VolumeState.ON) {
                Log.d(TAG, "togglePlaybackState: disabling volume.");
                setVolumeControl(VolumeState.OFF);
            }
        }
    }

    private void setVolumeControl(VolumeState state) {
        volumeState = state;
        if (state == VolumeState.OFF) {
            videoPlayer.setVolume(0f);
            animateVolumeControl();
        } else if (state == VolumeState.ON) {
            videoPlayer.setVolume(1f);
            animateVolumeControl();
        }
    }

    private void animateVolumeControl() {
        /*if (volumeControl != null) {
            volumeControl.bringToFront();
            if (volumeState == VolumeState.OFF) {
                // requestManager.load(R.drawable.ic_volume_off)
                //         .into(volumeControl);
            } else if (volumeState == VolumeState.ON) {
                // requestManager.load(R.drawable.ic_volume_on)
                //        .into(volumeControl);
            }
            volumeControl.animate().cancel();

            volumeControl.setAlpha(1f);

            volumeControl.animate()
                    .alpha(0f)
                    .setDuration(600).setStartDelay(1000);
        }*/
    }

    public void setMediaObjects(List<ClipItem> mediaObjects) {
        this.mediaObjects.addAll(mediaObjects);
    }

    public List<ClipItem> getMediaObjects() {
        return mediaObjects;
    }

    public void clearMediaObjects() {
        if (this.mediaObjects != null)
            this.mediaObjects.clear();
    }


    /**
     * Volume ENUM
     */
    private enum VolumeState {
        ON, OFF
    }

    public interface ExoPlayerClickListener {
        void onDoubleTap(ClipItem result, int playPosition);

        void onLongPress(ClipItem result, int playPosition);

        void onStreamStart(ClipItem result, int playPosition, long bufferingTime);

        void onStreamEnd(ClipItem result, int playPosition);

        void onStreamBuffering(ClipItem result, int playPosition);

        void onClipChange(ClipItem result, int playPosition);

        void makeViewApiCall(int duration, long timeElapsed, ClipItem clipItem);
    }
}