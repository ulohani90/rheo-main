package com.rheotv.android.ui.customViews.streamPlayer;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;

import com.github.vkay94.dtpv.youtube.YouTubeOverlay;
import com.rheotv.android.data.network.models.postlisting.responses.VideoUrlObj;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class StreamHolder implements Parcelable {
    private WeakReference<Context> contextRef;
    private WeakReference<YouTubeOverlay> youtubeDoubleTap;
    private WeakReference<StreamTapPlayerView> doubleTapPlayerView;
    private ExoStreamPlayer streamPlayer;
    private WeakReference<View> progressView;
    private WeakReference<ImageView> placeholderThumbnail;
    private WeakReference<ImageView> settingIcon;
    private WeakReference<ImageView> reportIcon;

    private String postId;
    private boolean isLive;
    private int volume;
    private List<VideoUrlObj> streamUrl;
    private String introVideoUrl;
    private String promoVideoUrl;
    private String videoMode;
    private long startFrom;
    private int resumeWindow;
    private long resumePosition;
    private StreamAuthorHolder authorHolder;
    private String qualityFormat;
    private boolean isGiftEnabled;
    private boolean isDoubleTapSendEventEnabled;
//    private StreamPlayerCallbackListener streamPlayerCallbackListener;
//    private HeartAnimator.HeartAnimatorInteractionListener heartAnimatorListener;

    public StreamHolder(WeakReference<Context> contextRef,
                        YouTubeOverlay youtubeDoubleTap,
                        StreamTapPlayerView doubleTapPlayerView,
                        ExoStreamPlayer streamPlayer,
                        View progressView,
                        WeakReference<ImageView> placeholderThumbnail,
                        WeakReference<ImageView> settingIcon,
                        WeakReference<ImageView> reportIcon,
                        String postId,
                        long startFrom,
                        int resumeWindow,
                        long resumePosition,
                        boolean isLive,
                        int volume,
                        List<VideoUrlObj> streamUrl,
                        String promoVideoUrl,
                        String videoMode,
                        StreamAuthorHolder authorHolder,
                        String qualityFormat,
                        boolean isGiftEnabled,
                        boolean isDoubleTapSendEventEnabled

//                        StreamPlayerCallbackListener streamPlayerCallbackListener,
//                        HeartAnimator.HeartAnimatorInteractionListener heartAnimatorListener
    ) {
        this.contextRef = contextRef;
        this.youtubeDoubleTap = new WeakReference<>(youtubeDoubleTap);
        this.doubleTapPlayerView = new WeakReference<>(doubleTapPlayerView);
        this.streamPlayer = streamPlayer;
        this.progressView = new WeakReference<>(progressView);
        this.placeholderThumbnail = placeholderThumbnail;
        this.settingIcon = settingIcon;
        this.reportIcon = reportIcon;
        this.startFrom = startFrom;
        this.resumePosition = resumePosition;
        this.resumeWindow = resumeWindow;
        this.postId = postId;
        this.isLive = isLive;
        this.volume = volume;
        this.authorHolder = authorHolder;
        this.streamUrl = streamUrl;
        this.introVideoUrl = introVideoUrl;
        this.promoVideoUrl = promoVideoUrl;
        this.videoMode = videoMode;
        this.qualityFormat = qualityFormat;
        this.isGiftEnabled = isGiftEnabled;
        this.isDoubleTapSendEventEnabled = isDoubleTapSendEventEnabled;
//        this.streamPlayerCallbackListener = streamPlayerCallbackListener;
//        this.heartAnimatorListener = heartAnimatorListener;
    }

    protected StreamHolder(Parcel in) {
        isLive = in.readByte() != 0;
        volume = in.readInt();
        streamUrl = new ArrayList<>();
        in.readTypedList(streamUrl, VideoUrlObj.CREATOR);
        promoVideoUrl = in.readString();
        videoMode = in.readString();
        startFrom = in.readLong();
        isGiftEnabled = in.readByte() != 0;
        postId = in.readString();
        isDoubleTapSendEventEnabled = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isLive ? 1 : 0));
        dest.writeInt(volume);
        dest.writeTypedList(streamUrl);
        dest.writeString(promoVideoUrl);
        dest.writeString(videoMode);
        dest.writeLong(startFrom);
        dest.writeByte((byte) (isGiftEnabled ? 1 : 0));
        dest.writeString(postId);
        dest.writeByte((byte) (isDoubleTapSendEventEnabled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StreamHolder> CREATOR = new Creator<StreamHolder>() {
        @Override
        public StreamHolder createFromParcel(Parcel in) {
            return new StreamHolder(in);
        }

        @Override
        public StreamHolder[] newArray(int size) {
            return new StreamHolder[size];
        }
    };

    public WeakReference<Context> getContextRef() {
        return contextRef;
    }

    public WeakReference<YouTubeOverlay> getYoutubeDoubleTap() {
        return youtubeDoubleTap;
    }

    public WeakReference<StreamTapPlayerView> getDoubleTapPlayerView() {
        return doubleTapPlayerView;
    }

    public ExoStreamPlayer getStreamPlayer() {
        return streamPlayer;
    }

    public WeakReference<View> getProgressView() {
        return progressView;
    }

    public WeakReference<ImageView> getPlaceholderThumbnail() {
        return placeholderThumbnail;
    }

    public WeakReference<ImageView> getSettingIcon() {
        return settingIcon;
    }

    public WeakReference<ImageView> getReportIcon() {
        return reportIcon;
    }

    public boolean isLive() {
        return isLive;
    }

    public int getVolume() {
        return volume;
    }

    public List<VideoUrlObj> getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(List<VideoUrlObj> streamUrl) {
        this.streamUrl = streamUrl;
    }

    public void setDoubleTapPlayerView(StreamTapPlayerView doubleTapPlayerView) {
        this.doubleTapPlayerView = new WeakReference<>(doubleTapPlayerView);
    }

    public String getVideoMode() {
        return videoMode;
    }

    public long getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(long startFrom) {
        this.startFrom = startFrom;
    }

    public String getPromoVideoUrl() {
        return promoVideoUrl;
    }


    public void setVideoMode(String videoMode) {
        this.videoMode = videoMode;
    }

    public String getQualityFormat() {
        return qualityFormat;
    }

    public void setQualityFormat(String qualityFormat) {
        this.qualityFormat = qualityFormat;
    }

//    public StreamPlayerCallbackListener getStreamPlayerCallbackListener() {
//        return streamPlayerCallbackListener;
//    }

    public StreamAuthorHolder getAuthorHolder() {
        return authorHolder;
    }

//    public HeartAnimator.HeartAnimatorInteractionListener getHeartAnimatorListener() {
//        return heartAnimatorListener;
//    }

    public int getResumeWindow() {
        return resumeWindow;
    }

    public long getResumePosition() {
        return resumePosition;
    }

    public boolean isGiftEnabled() {
        return isGiftEnabled;
    }

    public boolean isDoubleTapSendEventEnabled() {
        return isDoubleTapSendEventEnabled;
    }

    public String getPostId() {
        return postId;
    }

    public static class Builder {
        private WeakReference<Context> contextRef;
        private YouTubeOverlay youtubeDoubleTap;
        private StreamTapPlayerView doubleTapPlayerView;
        private ExoStreamPlayer streamPlayer;
        private View progressView;
        private WeakReference<ImageView> placeholderThumbnail;
        private WeakReference<ImageView> settingIcon;
        private WeakReference<ImageView> reportIcon;
        //        private StreamPlayerCallbackListener streamPlayerCallbackListener;
//        private HeartAnimator.HeartAnimatorInteractionListener heartAnimatorListener;
        private StreamAuthorHolder authorHolder;

        private String postId;
        private boolean isLive;
        private int volume = 10;
        private List<VideoUrlObj> streamUrl;

        private String promoVideoUrl;
        private long startFrom;
        private int resumeWindow = 0;
        private long resumePosition = 0;
        private String videoMode = "portrait";
        private String qualityFormat;
        private boolean isGiftEnable;
        private boolean isDoubleTapSendEventEnabled = true;

        public Builder setContext(Context context) {
            this.contextRef = new WeakReference<>(context);
            return this;
        }

        public Builder setYoutubeDoubleTap(YouTubeOverlay youtubeDoubleTap) {
            this.youtubeDoubleTap = youtubeDoubleTap;
            return this;
        }

        public Builder setStreamTapPlayerView(StreamTapPlayerView doubleTapPlayerView) {
            this.doubleTapPlayerView = doubleTapPlayerView;
            return this;
        }

        public Builder setProgressView(View progressView) {
            this.progressView = progressView;
            return this;
        }

        public Builder setPlaceholderThumbnail(WeakReference<ImageView> placeholderThumbnail) {
            this.placeholderThumbnail = placeholderThumbnail;
            return this;
        }

        public Builder setDoubleTapSendEventEnabled(boolean isDoubleTapSendEventEnabled) {
            this.isDoubleTapSendEventEnabled = isDoubleTapSendEventEnabled;
            return this;
        }

        public Builder setSettingIcon(WeakReference<ImageView> settingIcon) {
            this.settingIcon = settingIcon;
            return this;
        }

        public Builder setReportIcon(WeakReference<ImageView> reportIcon) {
            this.reportIcon = reportIcon;
            return this;
        }

        public Builder setPostId(String id) {
            postId = id;
            return this;
        }

        public Builder setLive(boolean live) {
            isLive = live;
            return this;
        }

        public Builder setStartFrom(long startFrom) {
            this.startFrom = startFrom;
            return this;
        }

        public Builder setVolume(int volume) {
            this.volume = volume;
            return this;
        }

        public Builder setStreamUrl(List<VideoUrlObj> streamUrl) {
            this.streamUrl = streamUrl;
            return this;
        }

        public Builder setPromoVideoUrl(String promoVideoUrl) {
            this.promoVideoUrl = promoVideoUrl;
            return this;
        }

        public Builder setStreamPlayer(ExoStreamPlayer streamPlayer) {
            this.streamPlayer = streamPlayer;
            return this;
        }

        public Builder setVideoMode(String videoMode) {
            this.videoMode = videoMode;
            return this;
        }

        public Builder setStreamAuthorHolder(StreamAuthorHolder authorHolder) {
            this.authorHolder = authorHolder;
            return this;
        }

        public Builder setQualityFormat(String qualityFormat) {
            this.qualityFormat = qualityFormat;
            return this;
        }

//        public Builder setStreamPlayerCallbackListener(StreamPlayerCallbackListener streamPlayerCallbackListener) {
//            this.streamPlayerCallbackListener = streamPlayerCallbackListener;
//            return this;
//        }

//        public Builder setHeartAnimatorListener(HeartAnimator.HeartAnimatorInteractionListener heartAnimatorListener) {
//            this.heartAnimatorListener = heartAnimatorListener;
//            return this;
//        }

        public Builder setResumeWindow(int resumeWindow) {
            this.resumeWindow = resumeWindow;
            return this;
        }

        public Builder setResumePosition(long resumePosition) {
            this.resumePosition = resumePosition;
            return this;
        }

        public Builder setGiftEnabled(boolean isGiftEnable) {
            this.isGiftEnable = isGiftEnable;
            return this;
        }

        public StreamHolder build() {
            return new StreamHolder(contextRef,
                    youtubeDoubleTap,
                    doubleTapPlayerView,
                    streamPlayer,
                    progressView,
                    placeholderThumbnail,
                    settingIcon,
                    reportIcon,
                    postId,
                    startFrom,
                    resumeWindow,
                    resumePosition,
                    isLive,
                    volume,
                    streamUrl,
                    promoVideoUrl,
                    videoMode,
                    authorHolder,
                    qualityFormat,
                    isGiftEnable,
                    isDoubleTapSendEventEnabled
//                    streamPlayerCallbackListener,
//                    heartAnimatorListener
            );
        }
    }
}
