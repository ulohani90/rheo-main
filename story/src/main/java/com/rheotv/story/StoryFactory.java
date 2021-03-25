package com.rheotv.story;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.ExoPlayerFactory;
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
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rheotv.story.model.Story;

import java.text.DecimalFormat;

public class StoryFactory {

    PlayerView videoView;

    Player.EventListener listener;

    private boolean isVideoLengthEdited = false;

    @SuppressLint("NewApi")
    public View getView(final Story story, final StoryLayout storyLayout, final int index) {
        storyLayout.pauseProgress();
        switch (story.getType()) {
            case Constants.TEXT:
                try {
                    if (story.getMetaData() == null) {
                        return null;
                    }
                    Story.TextStory textStory = new Gson().fromJson(story.getMetaData(), Story.TextStory.class);
                    if (textStory == null) return null;
                    FrameLayout frameLayout = new FrameLayout(storyLayout.getContext());
                    frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    frameLayout.setBackgroundColor(Color.parseColor(textStory.getBackgroundColor()));
                    TextView textView = new TextView(frameLayout.getContext());
                    textView.setText(textStory.getText());
                    textView.setTextSize(25f);
                    textView.setTextColor(Color.rgb(251, 251, 251));
                    textView.setLineSpacing(StoryUtils.INSTANCE.dpToPic(5), 1f);
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                    frameLayout.addView(textView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                    recordWatched(storyLayout, story);
                    storyLayout.resumeProgress();
                    return frameLayout;
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    if (story.getUrl() != null && !story.getUrl().isEmpty()) {
                        story.setType(Constants.IMAGE);
                        return getView(story, storyLayout, index);
                    }
                    return null;
                }
            case Constants.IMAGE:
                final ImageView imageView = new ImageView(storyLayout.getContext());
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                inflateImageView(imageView, story, storyLayout);
                return imageView;

            case Constants.VIDEO:
                if (videoView == null) {
                    videoView = new PlayerView(storyLayout.getContext());
                    videoView.setBackgroundColor(Color.TRANSPARENT);
                    TrackSelection.Factory videoTrackSelectionFactory =
                            new AdaptiveTrackSelection.Factory();
                    TrackSelector trackSelector =
                            new DefaultTrackSelector(videoTrackSelectionFactory);
                    videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                    //videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                    //Create the player using ExoPlayerFactory
                    final SimpleExoPlayer videoPlayer = ExoPlayerFactory.newSimpleInstance(videoView.getContext(), trackSelector);

                    // Disable Player Control
                    videoView.setUseController(false);
                    // Bind the player to the view.
                    videoView.setPlayer(videoPlayer);
                }

                if (listener != null) {
                    videoView.getPlayer().removeListener(listener);
                }

                if (isVideoLengthEdited)
                    isVideoLengthEdited = false;

                listener = getParentEventListener(storyLayout, index, story);
                videoView.getPlayer().addListener(listener);
                videoView.setVisibility(View.INVISIBLE);

                com.google.android.exoplayer2.upstream.DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                        videoView.getContext(), Util.getUserAgent(videoView.getContext(), "Android ExoPlayer"));

                String mediaUrl = story.getUrl();
                if (mediaUrl != null) {
                    MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(Uri.parse(mediaUrl));

                    if (videoView.getPlayer() != null) {
                        ((SimpleExoPlayer) videoView.getPlayer()).prepare(videoSource);
                        videoView.getPlayer().setPlayWhenReady(true);
                    }
                }

                if (videoView.getParent() != null) {
                    ViewGroup parent = (ViewGroup) videoView.getParent();
                    parent.removeAllViews();
                }
                return videoView;

            default:
                return null;
        }
    }

    public Player.EventListener getParentEventListener(final StoryLayout storyLayout, final int index, final Story story) {
        return new Player.EventListener() {
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
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    if (!isVideoLengthEdited) {
                        isVideoLengthEdited = true;
                        storyLayout.editDurationAndResume(index, (int) ((videoView.getPlayer().getDuration()) / 1000));
                    } else {
                        if (!storyLayout.isExplicitPause()) {
                            storyLayout.resumeProgress();
                        }
                    }
                    videoView.setVisibility(View.VISIBLE);
                    recordWatched(storyLayout, story);
                } else if (playWhenReady && playbackState == Player.STATE_BUFFERING) {
                    storyLayout.pauseProgress();
                }
            }
        };
    }

    private void inflateImageView(final ImageView imageView, final Story story, final StoryLayout storyLayout) {
        if (((Activity) imageView.getContext()).isDestroyed()) return;
        Glide.with(imageView.getContext())
                .load(story.getUrl())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        storyLayout.next();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null && !storyLayout.isExplicitPause()) {
                            storyLayout.resumeProgress();
                            recordWatched(storyLayout, story);
                        } else {
                            storyLayout.showLoader(false);
                        }
                        return false;
                    }
                })
                .into(imageView);
    }

    private void recordWatched(StoryLayout storyLayout, Story story) {
        storyLayout.getCallback().watched(story);
    }

    private final int SECOND_MILLIS = 1000;
    private final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private final int DAY_MILLIS = 24 * HOUR_MILLIS;

    String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    String formatValue(double value) {
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = (long) value;
        int value1 = (int) Math.floor(Math.log10(numValue));
        int base = value1 / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.#").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }

    PlayerView removeListener(Player player) {
        if (player == null) return null;
        player.removeListener(listener);
        player.stop();
        player.release();
        listener = null;
        return null;
    }

    public void addListener(Player player, StoryLayout storyLayout, int index, Story story) {
        if (player == null) return;
        listener = getParentEventListener(storyLayout, index, story);
        player.addListener(listener);
    }
}
