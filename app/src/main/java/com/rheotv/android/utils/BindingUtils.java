/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 12:38 PM
 *
 */

package com.rheotv.android.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableBoolean;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager.widget.ViewPager;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.video.VideoListener;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.GraphDataObject;
import com.rheotv.android.data.network.models.objects.FeedObject;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.Author;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.postlisting.responses.SearchResponse;
import com.rheotv.android.data.network.models.postlisting.responses.support.ChatModel;
import com.rheotv.android.ui.activities.chatActivity.ChatFragmentAdapter;
import com.rheotv.android.ui.activities.leaderboard.LeaderboardListAdapter;
import com.rheotv.android.ui.activities.search.fragment.SearchFragmentAdapter;
import com.rheotv.android.ui.activities.tabcontainer.profile.videos.VideoFragmentAdapter;
import com.rheotv.android.ui.adapters.HashTagsAdapter;
import com.rheotv.android.ui.adapters.PlayerListAdapter;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.customViews.AnalyticsMarkerView;
import com.rheotv.android.ui.customViews.streamPlayer.StreamUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.GrayscaleTransformation;


public final class BindingUtils {

    private BindingUtils() {
        // This class is not publicly instantiable
    }

    @BindingAdapter("gifUrl")
    public static void setGifImage(ImageView imageView, String url) {
        try {
            DrawableImageViewTarget target = new DrawableImageViewTarget(imageView);
            CircularProgressDrawable progress = new CircularProgressDrawable(imageView.getContext());
            progress.setStrokeWidth(5f);
            progress.setCenterRadius(30f);
            progress.setColorSchemeColors(R.color.light_pink, R.color.light_bright_green, R.color.light_musturd);
            progress.start();

            Glide.with(imageView.getContext())
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(progress)
                    .into(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter(value = {"loadIosGIF"})
    public static void loadIosGIF(ImageView imageView, Drawable drawable) {
        imageView.postDelayed(() -> {
                    try {
                        Glide.with(imageView.getContext())
                                .asGif()
                                .load(R.drawable.ios_loader)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                , 500);
    }

    @BindingAdapter(value = {"loadGIFFromLocal"})
    public static void loadGIFFromLocal(ImageView imageView, Drawable drawable) {
        try {
            imageView.postDelayed(() ->
                    Glide.with(imageView.getContext())
                            .asGif()
                            .load(R.drawable.ic_live_ripple)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(imageView), 500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter("imageUrl")
    public static void setImageUrl(ImageView imageView, String url) {
//        if (url != null && !url.isEmpty()) {
//            //imageView.setAlpha(0f);
//            Picasso.get().load(url)
//                    .config(Bitmap.Config.RGB_565)
//                    .networkPolicy(NetworkPolicy.OFFLINE, NetworkPolicy.NO_CACHE)//TODO: Comment to show full pixel story and similarly in the method below.
//                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.placeholder))
//                    .into(imageView);
//            //imageView.animate().setDuration(1000).alpha(1f).start();
//
//        } else {
//            Picasso.get().load(R.drawable.placeholder)
//                    .config(Bitmap.Config.RGB_565)
//                    .networkPolicy(NetworkPolicy.OFFLINE, NetworkPolicy.NO_CACHE)//TODO: Comment to show full pixel story and similarly in the method below.
//                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.placeholder))
//                    .into(imageView);
//        }

        setImageUrlUsingCache(imageView, url, true);
    }

    @BindingAdapter(value = {"imageUrl", "bg_color", "gameName"})
    public static void loadGameThumbnail(ImageView imageView, String imageUrl, String
            bg_color, String gameName) {
        try {
            if (imageUrl == null || gameName == null) return;
            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .placeholder(ContextCompat.getDrawable(imageView.getContext(), getPlaceHolderForGame(gameName)))
                    .error(ContextCompat.getDrawable(imageView.getContext(), getPlaceHolderForGame(gameName)))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView);
            imageView.postDelayed(() -> {
                imageView.setImageDrawable(null);
                imageView.setBackgroundColor(Color.parseColor(bg_color));
            }, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getPlaceHolderForGame(String gameName) {
        switch (gameName) {
            case "PUBG Mobile":
                return new Random().nextBoolean() ? R.drawable.pubg_mobile_cover : R.drawable.pubg_cover_2;
            case "Pubg Lite":
                return new Random().nextBoolean() ? R.drawable.pubg_lite_cover : R.drawable.pubg_cover_2;
            case "Free Fire":
                return new Random().nextBoolean() ? R.drawable.free_fire_cover : R.drawable.free_fire_cover_2;
            case "Call of Duty":
                return new Random().nextBoolean() ? R.drawable.call_of_duty_cover : R.drawable.call_of_duty_cover_2;
            case "Others":
                return R.drawable.others_cover;
            case "GTA V":
                return new Random().nextBoolean() ? R.drawable.gta5_cover : R.drawable.gta5_cover_2;
            case "Valorant":
                return R.drawable.valorant_cover;
            case "Minecraft":
                return new Random().nextBoolean() ? R.drawable.minecraft_cover : R.drawable.minecraft_cover_2;
            default:
                return R.drawable.placeholder;
        }
    }

    @BindingAdapter(value = {"imageUrl", "bg_color"})
    public static void setImageUrlWithFallback(ImageView imageView, String url, String bg_color) {
        try {
            setImageUrlUsingCache(imageView, url, true);
            imageView.postDelayed(() -> {
                imageView.setImageDrawable(null);
                imageView.setBackgroundColor(Color.parseColor(bg_color));
            }, 500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setImageUrlUsingCache(ImageView imageView, String url, boolean useCache) {
        try {
            if (url != null && !url.trim().isEmpty()) {
                if (useCache) {
                    Glide.with(imageView.getContext())
                            .load(url)
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                            .error(imageView.getContext().getResources().getDrawable(R.drawable.placeholder))
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(imageView);

                   /* Picasso.get().load(url)
                            .config(Bitmap.Config.RGB_565)
                            .networkPolicy(NetworkPolicy.OFFLINE, NetworkPolicy.NO_CACHE)//TODO: Comment to show full pixel story and similarly in the method below.
                            .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.placeholder))
                            .into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.i("ImageShownFromCache", url);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.i("ImageNotFoundInCache", url);
                                    setImageUrlUsingCache(imageView, url, false);
                                }
                            });*/
                } else {
/*                    if (url.startsWith("https")) {
                        Picasso.get().load(url)
                                .config(Bitmap.Config.RGB_565)
                                .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.placeholder))
                                .into(imageView);
                    } else {
                        Picasso.get().load(Uri.fromFile(new File(url)))
                                .config(Bitmap.Config.RGB_565)
                                .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.placeholder))
                                .into(imageView);
                    }*/
                    Glide.with(imageView.getContext())
                            .load(url)
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .error(imageView.getContext().getResources().getDrawable(R.drawable.placeholder))
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(imageView);
                }
            } else {
                /*Picasso.get().load(R.drawable.placeholder)
                        .config(Bitmap.Config.RGB_565)
                        .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.placeholder))
                        .into(imageView);*/
                Glide.with(imageView.getContext())
                        .load(R.drawable.placeholder)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .into(imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter(value = {"paintText"}, requireAll = false)
    public static void setTextColor(TextView textView, String text) {
        if (text == null) return;
        textView.setTextColor(getTextColor(text));
    }

    @BindingAdapter(value = {"roundImageUri", "textDrawable"}, requireAll = false)
    public static void setRoundImageUri(ImageView imageView, String uri, String text) {
        try {
            Log.i("BindingUtils", "roundImageUri" + uri);
            if (imageView.getVisibility() == View.VISIBLE) {
                if (text != null) {
                    if (uri == null || AppConstants.DEFAULT_AVATAR.equalsIgnoreCase(uri)
                            || AppConstants.DEFAULT_PROFILE_PIC.equalsIgnoreCase(uri)
                            || AppConstants.DEFAULT_AVATAR_V2.equalsIgnoreCase(uri)
                    )
                        uri = "";
                    Glide.with(imageView.getContext())
                            .load(uri)
                            .placeholder(getDefaultTextDrawable(text))
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .error(getDefaultTextDrawable(text))
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(imageView);
                } else {
                    Glide.with(imageView.getContext())
                            .load(uri)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .error(R.drawable.ic_login_white_outline_102dp)
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(imageView);
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static TextDrawable getDefaultTextDrawable(String text) {
        return TextDrawable.builder()
                .beginConfig()
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(getInitials(text), getTextColor(text));
    }

    private static int getTextColor(String text) {
        ColorGenerator generator = ColorGenerator.MATERIAL;
        if (text == null || text.isEmpty()) return generator.getRandomColor();
        return generator.getColor(text);
    }

    private static String getInitials(String text) {
        if (text == null || text.trim().isEmpty()) return "A";
        StringBuilder initials = new StringBuilder();
        String[] parts = text.trim().split(" ");
        char initial;
        for (String part : parts) {
            if (!part.isEmpty()) {
                initial = part.charAt(0);
                initials.append(initial);
            }
        }

        return initials.length() > 0 ? (initials.toString().toUpperCase()) : "A";
    }

    @BindingAdapter("imageUri")
    public static void setImageUri(ImageView imageView, String uri) {
        try {
            Glide.with(imageView.getContext())
                    .load(uri)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter(value = {"videoUri", "startVideo", "shouldSeek", "controllerView", "videoProgress"}, requireAll = false)
    public static void setVideoUri(VideoView videoView, String uri, boolean startVideo,
                                   boolean shouldSeek, View controllerView, View progress) {
        if (uri == null || uri.isEmpty()) return;
        videoView.setVideoURI(Uri.parse(uri));
        videoView.requestFocus();
        if (shouldSeek)
            videoView.seekTo(100);
        if (progress != null)
            videoView.setOnPreparedListener(mp -> mp.setOnInfoListener(videoInfoListener(progress)));
        if (controllerView != null) {
            Context context = videoView.getContext();
            Intent intent = new Intent(AppConstants.FILTER_VIDEO_STATE);
            videoView.setOnClickListener(v -> {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    ((ImageView) controllerView).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_play));
                    intent.putExtra(AppConstants.VIDEO_STATE, false);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                } else {
                    videoView.start();
                    ((ImageView) controllerView).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_pause));
                    intent.putExtra(AppConstants.VIDEO_STATE, true);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            });
            videoView.setOnCompletionListener(mediaPlayer -> ((ImageView) controllerView).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_play)));
        }

        if (startVideo)
            videoView.start();
    }

    private static MediaPlayer.OnInfoListener videoInfoListener(View progress) {
        return (mp, what, extra) -> {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                    progress.setVisibility(View.GONE);
                    return true;
                }
                case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                    progress.setVisibility(View.VISIBLE);
                    return true;
                }
            }
            return false;
        };
    }

    @SuppressLint("CheckResult")
    @BindingAdapter(value = {"mediaUrl", "play", "controlView", "progressView"}, requireAll = false)
    public static void setExoPlayer(PlayerView videoView, String mediaUrl,
                                    boolean playStatus, View controlView, View progress) {
        if (mediaUrl == null) return;
        Context context = videoView.getContext();
        Intent intent = new Intent(AppConstants.FILTER_VIDEO_STATE);
        videoView.setBackgroundColor(Color.TRANSPARENT);

        TrackSelector selector =
                new DefaultTrackSelector(new AdaptiveTrackSelection.Factory());
        videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        //Create the player using ExoPlayerFactory

        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
        /* This is 50000 milliseconds in ExoPlayer 2.9.6 */
        final int loadControlBufferMs = 50000;

        /* Configure the DefaultLoadControl to use the same value for */
        builder.setBufferDurationsMs(loadControlBufferMs,
                loadControlBufferMs,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
        DefaultLoadControl loadControl = builder.createDefaultLoadControl();

        final SimpleExoPlayer videoPlayer = ExoPlayerFactory.newSimpleInstance(context,
                new DefaultRenderersFactory(context), selector, loadControl);

        // Disable Player Control
        videoView.setUseController(false);
        videoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

        videoPlayer.addVideoListener(new VideoListener() {
            @Override
            public void onRenderedFirstFrame() {
                progress.setVisibility(View.GONE);
            }
        });

        // Bind the player to the view.
        videoPlayer.addListener(getParentEventListener(videoView, progress, controlView, intent));
        videoView.setShutterBackgroundColor(Color.parseColor("#1d2e44"));
        videoView.setPlayer(videoPlayer);

        videoView.setVisibility(View.INVISIBLE);
        MediaSource videoSource = StreamUtils.buildMediaSource(Uri.parse(mediaUrl));


        if (videoView.getPlayer() != null) {
            videoPlayer.prepare(videoSource);
            videoPlayer.setPlayWhenReady(playStatus);
        }

        controlView.setOnClickListener(v -> {
            if (videoPlayer.isPlaying()) {
                videoPlayer.setPlayWhenReady(false);
                ((ImageView) controlView).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_play));
            } else {
                videoPlayer.setPlayWhenReady(true);
                ((ImageView) controlView).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_pause));
            }
        });
    }

    public static Player.EventListener getParentEventListener(PlayerView playerView, View
            progress, View controlView, Intent broadcastIntent) {
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
                if (playbackState == Player.STATE_READY) {
                    if (playWhenReady) {
                        if (broadcastIntent != null) {
                            broadcastIntent.putExtra(AppConstants.VIDEO_STATE, true);
                            LocalBroadcastManager.getInstance(progress.getContext()).sendBroadcast(broadcastIntent);
                        }
                        if (playerView.getVisibility() == View.INVISIBLE)
                            playerView.setVisibility(View.VISIBLE);
//                        if (artwork.getVisibility() == View.VISIBLE)
//                            artwork.setVisibility(View.GONE);

                    } else {
                        if (broadcastIntent != null) {
                            broadcastIntent.putExtra(AppConstants.VIDEO_STATE, false);
                            LocalBroadcastManager.getInstance(progress.getContext()).sendBroadcast(broadcastIntent);
                        }
                    }
                } else if (playWhenReady && playbackState == Player.STATE_BUFFERING) {
                    progress.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_ENDED) {
                    if (broadcastIntent != null) {
                        broadcastIntent.putExtra(AppConstants.VIDEO_STATE, false);
                        LocalBroadcastManager.getInstance(progress.getContext()).sendBroadcast(broadcastIntent);
                    }

                    if (controlView != null)
                        ((ImageView) controlView).setImageDrawable(
                                ContextCompat.getDrawable(controlView.getContext(), R.drawable.avd_play)
                        );
                }

            }
        };
    }

    @BindingAdapter(value = {"playVideo"}, requireAll = false)
    public static void videoPlayback(VideoView view, Boolean play) {
        if (play == null) return;
        if (play)
            view.start();
        else if (view.isPlaying())
            view.pause();
        else
            view.stopPlayback();
    }


    @BindingAdapter(value = {"blurImageUri", "blurFactor"}, requireAll = false)
    public static void setBlurImageUri(ImageView imageView, String url, Integer blurFactor) {
        if (url == null || url.isEmpty()) return;
        if (blurFactor == null) blurFactor = 1;
        Context context = imageView.getContext();
        Picasso.get()
                .load(url)
                .transform(new BlurTransformation(context, 5, blurFactor))
                .into(imageView);
    }

    /*@BindingAdapter("imageUrlUsingGlide")
    public static void setImageUrlUsingGlide(ImageView imageView, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(imageView.getContext()).load(url).placeholder(R.drawable.placeholder).format(DecodeFormat.PREFER_RGB_565).into(imageView);

            //imageView.setAlpha(0f);
           *//* Picasso.get().load(url)
                    .config(Bitmap.Config.RGB_565) //TODO: Comment to show full pixel story and similarly in the method below.
                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.home_item_placeholder_image))
                    .into(imageView);*//*
            //imageView.animate().setDuration(1000).alpha(1f).start();

        } else {
            Glide.with(imageView.getContext()).load(R.drawable.placeholder).format(DecodeFormat.PREFER_RGB_565).into(imageView);
        }
    }*/

    @BindingAdapter(value = {"scaledImageUrl", "scaleWidth", "scaleHeight"}, requireAll = false)
    public static void setProfileImageUrlRounded(ImageView imageView, String url, int width,
                                                 int height) {
        if (url != null && !url.isEmpty()) {
            Picasso.get().load(url)
                    .config(Bitmap.Config.RGB_565)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .resize(ViewUtils.dpToPx(width), ViewUtils.dpToPx(height))
                    .onlyScaleDown()
                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar))
                    .transform(new CropCircleTransformation())
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(url)
                                    .config(Bitmap.Config.RGB_565)
                                    .resize(width, height)
                                    .onlyScaleDown()
                                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                                    .into(imageView);
                        }
                    });
        } else {
            Picasso.get().load(R.drawable.avd_avatar)
                    .config(Bitmap.Config.RGB_565)
                    .resize(width, height)
                    .onlyScaleDown()
                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                    .into(imageView);
        }
        /*if (url != null && !url.isEmpty()) {
            //imageView.setAlpha(0f);
            Picasso.get().load(url)
                    .config(Bitmap.Config.RGB_565)
                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                    .into(imageView);
            //imageView.animate().setDuration(200).alpha(1f).start();
        } else {
            Picasso.get().load(R.drawable.avd_avatar)
                    .config(Bitmap.Config.RGB_565)
                    .networkPolicy(NetworkPolicy.OFFLINE, NetworkPolicy.NO_CACHE)//TODO: Comment to show full pixel story and similarly in the method below.
                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                    .into(imageView);
        }*/
    }

    @BindingAdapter(value = {"scaledImageUrl", "scaleWidth", "scaleHeight", "placeHolder"}, requireAll = false)
    public static void setProfileImageUrlRounded(ImageView imageView, String url, int width,
                                                 int height, Drawable placeHolder) {
        if (url != null && !url.isEmpty()) {
            Picasso.get().load(url)
                    .config(Bitmap.Config.RGB_565)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .resize(ViewUtils.dpToPx(width), ViewUtils.dpToPx(height))
                    .onlyScaleDown()
                    .placeholder(placeHolder)
                    .transform(new CropCircleTransformation())
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(url)
                                    .config(Bitmap.Config.RGB_565)
                                    .resize(width, height)
                                    .onlyScaleDown()
                                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                                    .into(imageView);
                        }
                    });
        } else {
            Picasso.get().load(R.drawable.avd_avatar)
                    .config(Bitmap.Config.RGB_565)
                    .resize(width, height)
                    .onlyScaleDown()
                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                    .into(imageView);
        }
    }

    public static void setProfileImageUrlFromCache(ImageView imageView, String url,
                                                   boolean useCached) {
        try {
            if (useCached) {
                Picasso.get().load(url)
                        .config(Bitmap.Config.RGB_565)
                        .networkPolicy(NetworkPolicy.OFFLINE)//TODO: Comment to show full pixel story and similarly in the method below.
                        .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                setProfileImageUrlFromCache(imageView, url, false);
                            }
                        });
            } else {
                Picasso.get().load(url)
                        .config(Bitmap.Config.RGB_565)
                        .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                        .into(imageView);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    /*@BindingAdapter("profileImageUrl")
    public static void setProfileImageUrl(ImageView imageView, String url) {
        if (url != null && !url.isEmpty()) {
            imageView.setAlpha(0f);
            Picasso.get().load(url)
                    .config(Bitmap.Config.RGB_565)//TODO: Comment to show full pixel story and similarly in the method below.
                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar))
                    .into(imageView);
            imageView.animate().setDuration(200).alpha(1f).start();
        } else {
            Picasso.get().load(R.drawable.avd_avatar)
                    .config(Bitmap.Config.RGB_565) //TODO: Comment to show full pixel story and similarly in the method below.
                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar))
                    .into(imageView);
        }
    }

    @BindingAdapter("gameThumbUrl")
    public static void setGameThumbUrl(ImageView imageView, String url) {
        Picasso.get().load(url).config(Bitmap.Config.RGB_565).transform(new RoundedCornersTransformation((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, imageView.getResources().getDisplayMetrics())
                , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, imageView.getResources().getDisplayMetrics())))
                .into(imageView);
    }*/

    public static void loadBitmap(ImageView imageView, String url) {

        Picasso.get()
                .load(url)
                .transform(new BlurTransformation(imageView.getContext(), 17, 1))
                .into(imageView);
        /*Picasso.get().load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                Bitmap blurredBitmap = blur(imageView.getContext(), bitmap);
                imageView.setImageBitmap(blurredBitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });*/
    }

    public static Bitmap blur(Context context, Bitmap image) {
        float BITMAP_SCALE = 0.4f;
        float BLUR_RADIUS = 17.5f;
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, tmpIn.getElement());

        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }


    public static void setImageUrlWithoutAnimation(ImageView imageView, String url) {
        if (url != null && !url.isEmpty()) {
            Picasso.get().load(url)
                    .config(Bitmap.Config.RGB_565) //TODO: Comment to show full pixel story and similarly in the method below.
//                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.placeholder))
                    .noPlaceholder()
                    .into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"imageUrlCircular", "height", "width"})
    public static void setImageUrlCircular(ImageView imageView, String url, int height, int width) {
        try {
            if (height <= 0) {
                height = 40;
            }
            if (width <= 0) {
                width = 40;
            }
            if (url != null && !url.isEmpty()) {
                if (url.startsWith("https")) {
                    Picasso.get().load(url)
                            .config(Bitmap.Config.RGB_565)
                            .resize(ViewUtils.dpToPx(width), ViewUtils.dpToPx(height))
                            .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                            .into(imageView);
                } else {
                    Picasso.get().load(Uri.fromFile(new File(url)))
                            .config(Bitmap.Config.RGB_565)
                            .resize(ViewUtils.dpToPx(width), ViewUtils.dpToPx(height))
                            .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                            .into(imageView);
                }
            } else {
                Picasso.get().load(R.drawable.avd_avatar)
                        .config(Bitmap.Config.RGB_565)
                        .resize(ViewUtils.dpToPx(width), ViewUtils.dpToPx(height))
                        .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                        .into(imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"imageUrlCircular", "grayFilter", "height", "width"})
    public static void setImageUrlCircular(ImageView imageView, String url,
                                           boolean showGrayFilter, int height, int width) {
        if (url != null && !url.isEmpty()) {
            ArrayList<Transformation> transformations = new ArrayList<>();
            transformations.add(new CropCircleTransformation());
            if (showGrayFilter) {
                transformations.add(new GrayscaleTransformation());
            }

            Picasso.get().load(url)
                    .config(Bitmap.Config.RGB_565)
                    .transform(transformations)
                    .resize(ViewUtils.dpToPx(width), ViewUtils.dpToPx(height))
                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar))
                    .into(imageView);
        } else {
            Picasso.get().load(R.drawable.avd_avatar)
                    .config(Bitmap.Config.RGB_565)
                    .resize(ViewUtils.dpToPx(width), ViewUtils.dpToPx(height))
                    .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.avd_avatar)).transform(new CropCircleTransformation())
                    .into(imageView);
        }

    }

    @BindingAdapter({"postAdapter"})
    public static void addPostItems(RecyclerView recyclerView, List<FeedObject> blogs) {
        PostListAdapter adapter = (PostListAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            int oldPostListSize = adapter.getPostListSize();
            adapter.clearItems();
            Log.d("TAGGER", blogs.size() + " is the blogs size in adapter");
            adapter.addItems(blogs, oldPostListSize);
//            recyclerView.getRecycledViewPool().clear();
            adapter.notifyDataSetChanged();
//            adapter.notifyItemRangeInserted(oldPostListSize, blogs.size());
        }
    }

    @BindingAdapter({"videosAdapter"})
    public static void addVideosItems(RecyclerView recyclerView, List<PostObject> blogs) {
        VideoFragmentAdapter adapter = (VideoFragmentAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.clearItems();
            Log.d("TAGGER", blogs.size() + " is the blogs size in adapter");
            adapter.addItems(blogs);
        }
    }

    /*@BindingAdapter({"universalAdapter"})
    public static void addUniversalItems(RecyclerView recyclerView, List<Result> blogs) {
        UniversalFragmentListAdapter adapter = (UniversalFragmentListAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.clearItems();
            Log.d("TAGGER", blogs.size() + " is the blogs size in adapter");
            adapter.addItems(blogs);
        }
    }*/


    @BindingAdapter({"chatAdapter"})
    public static void addChatItems(RecyclerView recyclerView, List<ChatModel> blogs) {
        ChatFragmentAdapter adapter = (ChatFragmentAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.clearItems();
            Log.d("TAGGER", blogs.size() + " is the blogs size in adapter");
            adapter.addItems(blogs);
        }
    }

    @BindingAdapter({"searchAdapter"})
    public static void addSearchItems(RecyclerView
                                              recyclerView, List<SearchResponse> searchResponses) {
        SearchFragmentAdapter adapter = (SearchFragmentAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.clearItems();
            Log.d("TAGGER", searchResponses.size() + " is the blogs size in adapter");
            adapter.addItems(searchResponses);
        }
    }

    @BindingAdapter(value = {"isVisible"}, requireAll = false)
    public static void showVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter({"leaderBoardAdapter"})
    public static void addLbItems(RecyclerView recyclerView, List<Author> leaderBoardItems) {
        LeaderboardListAdapter adapter = (LeaderboardListAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            //adapter.clearItems();
            Log.d("TAGGER", leaderBoardItems.size() + " is the blogs size in adapter");
            adapter.addItems(leaderBoardItems);
        }
    }


    @BindingAdapter({"carouselAdapter"})
    public static void addCarouselItems(ViewPager viewPager, List<PostObject> blogs) {
        /*CarouselAdapter adapter = (CarouselAdapter) viewPager.getAdapter();
        if (adapter != null) {
            Log.d("TAGGER", blogs.size() + " is the blogs size in adapter");
            adapter.setItems(blogs);
            adapter.notifyDataSetChanged();
        }*/
    }

    @BindingAdapter({"tagsAdapter"})
    public static void addTagItems(RecyclerView recyclerView, List<String> tagsList) {
        HashTagsAdapter adapter = (HashTagsAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.clearItems();
            Log.d("TAGGER", tagsList.size() + " is the tags size in adapter");
            adapter.addTagItems(tagsList);
        }
    }

    @BindingAdapter({"playerAdapter"})
    public static void addPostItemsToPlayerBottomSheet(RecyclerView
                                                               recyclerView, List<Result> blogs) {
        PlayerListAdapter adapter = (PlayerListAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.clearItems();
            Log.d("TAGGER", blogs.size() + " is the blogs size in player adapter");
            adapter.addItems(blogs);
        }
    }

    @BindingAdapter("setImageState")
    public static void setLikeDrawable(ImageView imageView, Boolean isLiked) {
        Drawable drawable;
        if (isLiked) {
            drawable = imageView.getContext().getResources().getDrawable(R.drawable.ic_like_active);
        } else {
            drawable = imageView.getContext().getResources().getDrawable(R.drawable.ic_like_inactive);
        }
        imageView.setImageDrawable(drawable);
    }

    @BindingAdapter("setPlayingBackground")
    public static void setPlayingBackground(View view, ObservableBoolean isLiked) {
        if (isLiked.get()) {
            view.setBackgroundColor(view.getContext().getResources().getColor(R.color.grey));
        }
    }

    @BindingAdapter("setFollowBackground")
    public static void setFollowBackground(View view, ObservableBoolean followed) {
        if (!followed.get()) {
            view.setAlpha((float) 0.5);
        } else {
            view.setAlpha((float) 1);
        }
    }

    @BindingAdapter("viewpagerHeight")
    public static void setViewPagerHeight(View view, ObservableBoolean isSingleItem) {
        if (isSingleItem.get()) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            final float scale = view.getResources().getDisplayMetrics().density;
            int pixels = (int) (220 * scale + 0.5f);
            params.height = pixels;
            view.requestLayout();
        }
    }

    @BindingAdapter(value = {"spanDrawable", "spanText"}, requireAll = false)
    public static void appendIconAtEnd(TextView textView, Drawable drawable, String text) {
        SpannableString ss = new SpannableString(text + " ");
        ImageSpan span = new ImageSpan(textView.getContext(), R.drawable.avd_down_arrow, ImageSpan.ALIGN_BOTTOM) {
            public void draw(Canvas canvas, CharSequence text, int start,
                             int end, float x, int top, int y, int bottom,
                             @NonNull Paint paint) {
                Drawable d = getDrawable();
                canvas.save();
                // If we set transY = 0, then the drawable will be drawn at the top of the text.
                // y is the the distance from the baseline to the top of the text, so
                // transY = y will draw the top of the drawable on the baseline. We want the
                // bottom of the drawable on the baseline, so we subtract the height
                // of the drawable.
                int transY = bottom - d.getBounds().bottom + (d.getBounds().height() / 12);
                canvas.translate(x, transY);
                d.draw(canvas);

                canvas.restore();
            }
        };
        ss.setSpan(span, ss.length() - 1, ss.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        textView.setText(ss);
    }

    @BindingAdapter(value = {"viewVisible", "fadeWhen"}, requireAll = false)
    public static void fadeVisibility(View view, boolean isVisible, Boolean fadeWhen) {
        if (fadeWhen == null) fadeWhen = true;
        if (!fadeWhen) return;
        if (isVisible)
            fadeShow(view);
        else
            fadeHide(view);
    }

    @BindingAdapter(value = {"fadeAnimateIf", "canFade", "visibleGone"}, requireAll = false)
    public static void showHideViewWithFade(View view, boolean fadeAnimateIf, boolean showFade,
                                            boolean isVisible) {
        if (fadeAnimateIf) {
            if (showFade)
                fadeShow(view);
            else
                fadeHide(view);
        } else {
            view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    private static void fadeShow(View view) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f);
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                view.setVisibility(View.VISIBLE);
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
        animation.setDuration(500).start();
    }

    private static void fadeHide(View view) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f);
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animation.setDuration(500).start();
    }

    @BindingAdapter(value = {"formatNumber"}, requireAll = false)
    public static void setNumberFormat(TextView textView, long number) {
        textView.setText(CommonUtils.formatValue(number));
    }

    @BindingAdapter(value = {"drawable"}, requireAll = false)
    public static void setDrawable(ImageView imageView, Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    @BindingAdapter(value = {"indicatorVisibility"})
    public static void setIndicatorEnable(ImageView imageView, boolean isVisible) {
        Drawable drawable = imageView.getDrawable();
        if (!(drawable instanceof LayerDrawable)) {
            return;
        }
        LayerDrawable layerDrawable = (LayerDrawable) drawable;
        Drawable indicatorDrawable = layerDrawable.findDrawableByLayerId(R.id.indicator);
        if (indicatorDrawable == null) {
            return;
        }
        indicatorDrawable.setAlpha(isVisible ? 200 : 0);
    }

    @BindingAdapter(value = {"visibleWhen"}, requireAll = false)
    public static void showHideView(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter(value = {"shouldFormatChatDate"}, requireAll = false)
    public static void setUpMap(LineChart chart, boolean shouldFormatValue) {
        chart.getDescription().setEnabled(false);
        Context context = chart.getContext();
        SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM");

        // enable touch gestures
        chart.setTouchEnabled(true);

        chart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setPinchZoom(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);

        AnalyticsMarkerView mv = new AnalyticsMarkerView(context, R.layout.chart_marker_layout, shouldFormatValue);
        // Set the marker to the chart
        mv.setChartView(chart);
        chart.setMarker(mv);

        // set an alternative background color
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setViewPortOffsets(60f, 0f, 60f, 60f);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(context.getResources().getColor(R.color.white_text_color));
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(context.getResources().getColor(R.color.map_axis_line_color));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float timeInMillis) {
                return mFormat.format(new Date((long) timeInMillis));
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setAxisLineColor(context.getResources().getColor(R.color.map_axis_line_color));
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setTextColor(context.getResources().getColor(R.color.white_text_color));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    @BindingAdapter(value = {"chatGraphObject", "chartLabel"}, requireAll = false)
    public static void populateChartView(LineChart
                                                 chart, List<GraphDataObject> objects, String label) {
        if (objects == null || label == null) return;
        Log.i("BindingUtils", "setData objs: " + objects.size());
        Context context = chart.getContext();

        // set data
        ArrayList<Entry> values = new ArrayList<>();
        for (GraphDataObject obj : objects) {
            if (label.equals("DataSet1"))
                values.add(new Entry(obj.getDate(), obj.getViews()));
            else
                values.add(new Entry(obj.getDate(), obj.getDuration()));
            Log.i("BindingUtils", "setData objs: " + obj.getDate() + " and " + obj.getDuration());
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, label);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(context.getResources().getColor(R.color.bottom_bar_selected_item_color));
        set1.setValueTextColor(context.getResources().getColor(R.color.bottom_bar_selected_item_color));
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);
        set1.setFillAlpha(65);
        set1.setFillColor(context.getResources().getColor(R.color.bottom_bar_selected_item_color));
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);

        // create a data object with the data sets
        LineData data = new LineData(set1);
        data.setValueTextColor(context.getResources().getColor(R.color.white_text_color));
        data.setValueTextSize(9f);
        chart.setData(data);
        chart.invalidate();
    }

    @BindingAdapter("onOkInSoftKeyboard") // I like it to match the listener method name
    public static void setOnOkInSoftKeyboardListener(TextView view,
                                                     final OnOkInSoftKeyboardListener listener) {
        if (listener == null) {
            view.setOnEditorActionListener(null);
        } else {
            view.setOnEditorActionListener((v, actionId, event) -> {
                // ... solution to receiving event
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    listener.onOkInSoftKeyboard();
                    return true;
                }
                return false;
            });
        }
    }

    interface OnOkInSoftKeyboardListener {
        void onOkInSoftKeyboard();
    }

    @BindingAdapter(value = {"audioUrl"}, requireAll = false)
    public static void playAudio(ImageView indicatorView, String audioUrl) {
        if (audioUrl == null) return;
        try {
            Uri uri = Uri.parse(audioUrl);
            MediaPlayer player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(indicatorView.getContext(), uri);
            player.prepare();

            indicatorView.setOnClickListener(view -> {
                if (audioUrl == null || audioUrl.isEmpty()) return;
                if (player.isPlaying()) {
                    player.pause();
                    ContextCompat.getDrawable(indicatorView.getContext(), R.drawable.avd_play);
                } else {
                    player.start();
                    ContextCompat.getDrawable(indicatorView.getContext(), R.drawable.avd_pause);
                }
            });
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @BindingAdapter(value = {"toolbarUrl"}, requireAll = false)
    public static void setToolbarBackground(Toolbar toolbar, String url) {
        try {
            Glide.with(toolbar.getContext())
                    .load(url)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .error(R.drawable.ic_login_white_outline_102dp)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            toolbar.setBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter(value = {"buttonKey", "buttonValue", "buttonFirst", "buttonSecond"}, requireAll = false)
    public static void toggleStartAmPmButton(MaterialButtonToggleGroup group, String
            key, String value, int firstButtonId, int secondButtonId) {
        if (key.equalsIgnoreCase(value))
            group.check(firstButtonId);
        else
            group.check(secondButtonId);
    }

    @BindingAdapter(value = {"timestamp", "format"}, requireAll = false)
    public static void setFormattedTime(TextView textView, long timestamp, String format) {
        try {
            textView.setText(TimeUtils.getFormattedDate(format, new Date(timestamp)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter(value = {"spannableText", "spannableStart", "spannableEnd"}, requireAll = false)
    public static void setSpannableText(TextView textView, String text, int startIndex,
                                        int endIndex) {
        SpannableString string = new SpannableString(text);
        string.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        textView.setText(string);
    }

    @BindingAdapter(value = {"textToSpan", "textSpan"}, requireAll = false)
    public static void setUnderlineSpannableText(TextView textView, String text, String
            spanText) {
        SpannableString spannable = new SpannableString(text);
        int startIndexOfPath = spannable.toString().indexOf(spanText);
        spannable.setSpan(new UnderlineSpan(), startIndexOfPath, startIndexOfPath + spanText.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        textView.setText(spannable);
    }

    @BindingAdapter(value = {"greyDrawable"}, requireAll = false)
    public static void greyGradientPicture(ImageView imageView, Drawable drawable) {
        Picasso.get().load(R.drawable.login_bg)
                .config(Bitmap.Config.RGB_565)
                .transform(new GrayscaleTransformation())
                .into(imageView);
    }

    @BindingAdapter(value = {"chipSuggestion"}, requireAll = false)
    public static void addChips(ChipGroup chipGroup, List<String> items) {
        if (items == null) return;
        chipGroup.removeAllViews();
        AppUtilsKt.INSTANCE.addJavaClips(chipGroup, items);
    }

    @BindingAdapter(value = {"srcBitmap"}, requireAll = false)
    public static void addBitmap(ImageView imageView, String url) {
        ArrayList<String> list = new ArrayList<>();
        list.add(url);
        imageView.setImageBitmap(ViewUtils.getThumbnailFromList(list));
    }

    @BindingAdapter(value = {"imageUrl", "blurWhen"}, requireAll = false)
    public static void blurUnBlur(ImageView imageView, String url, boolean isBlur) {
        if (isBlur) {
            setBlurImageUri(imageView, url, 5);
        } else {
            setImageUrl(imageView, url);
        }
    }

    @BindingAdapter(value = {"imageSrc"}, requireAll = false)
    public static void loadImage(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .placeholder(imageView.getContext().getResources().getDrawable(R.drawable.placeholder))
                .error(imageView.getContext().getResources().getDrawable(R.drawable.placeholder))
                .into(imageView);
    }

}
