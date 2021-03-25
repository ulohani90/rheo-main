package com.rheotv.android.data.network.models.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.postlisting.responses.VideoUrlObj;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class PostObject implements Comparable, Parcelable {

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("video_url")
    @Expose
    String videoUrl;

    @SerializedName("thumbnail")
    @Expose
    String thumbnail;

    @SerializedName("share_url")
    @Expose
    String shareUrl;

    @SerializedName("total_views")
    @Expose
    int totalViews;

    @SerializedName("is_live")
    @Expose
    boolean isLive;

    @SerializedName("author")
    @Expose
    AuthorObject author;

    @SerializedName("game")
    @Expose
    GameObject game;

    @SerializedName("title")
    @Expose
    String title;

    @SerializedName("start_from")
    @Expose
    private int startFrom;

    @SerializedName("language")
    @Expose
    String language;

    @SerializedName("can_download_video")
    @Expose
    private boolean canDownloadVideo;

    @SerializedName("live_watchers_count")
    @Expose
    private Integer watchingCount;

    @SerializedName("is_published")
    @Expose
    private boolean isPublished;

    @SerializedName("video_urls")
    @Expose
    private List<VideoUrlObj> videoUrls;

    private long rewardTimeProgress = 0;
    private int resumeWindow;
    private long resumePosition;
    private boolean shouldShowTagOptions = false;

    private boolean isCardType = false;
    private String reminderTime;

    public PostObject() {
    }

    public PostObject(String id) {
        this.id = id;
    }

    public PostObject(String id, String thumbnail, int totalViews, String title, GameObject game, AuthorObject author, boolean isLive) {
        this.id = id;
        this.thumbnail = thumbnail;
        this.shareUrl = shareUrl;
        this.totalViews = totalViews;
        this.author = author;
        this.game = game;
        this.title = title;
        this.isLive = isLive;
    }

    protected PostObject(Parcel in) {
        id = in.readString();
        videoUrl = in.readString();
        thumbnail = in.readString();
        shareUrl = in.readString();
        totalViews = in.readInt();
        isLive = in.readByte() != 0;
        author = in.readParcelable(AuthorObject.class.getClassLoader());
        game = in.readParcelable(GameObject.class.getClassLoader());
        title = in.readString();
        startFrom = in.readInt();
        language = in.readString();
        canDownloadVideo = in.readByte() != 0;
        rewardTimeProgress = in.readLong();
        resumePosition = in.readLong();
        resumeWindow = in.readInt();
        if (in.readByte() == 0) {
            watchingCount = null;
        } else {
            watchingCount = in.readInt();
        }
        isPublished = in.readByte() != 0;
        videoUrls = new ArrayList<>();
        in.readList(videoUrls, VideoUrlObj.class.getClassLoader());
        shouldShowTagOptions = in.readByte() != 0;
        isCardType = in.readByte() != 0;
        reminderTime = in.readString();
    }

    public static final Creator<PostObject> CREATOR = new Creator<PostObject>() {
        @Override
        public PostObject createFromParcel(Parcel in) {
            return new PostObject(in);
        }

        @Override
        public PostObject[] newArray(int size) {
            return new PostObject[size];
        }
    };

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public int getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(int totalViews) {
        this.totalViews = totalViews;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public AuthorObject getAuthor() {
        return author;
    }

    public void setAuthor(AuthorObject author) {
        this.author = author;
    }

    public GameObject getGame() {
        return game;
    }

    public void setGame(GameObject game) {
        this.game = game;
    }

    public String getTitle() {
        return title != null && !title.isEmpty() ? title : (author != null && author.getUser() != null ? author.getUser().getUsername() + " is playing " + game.getName() : "");
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartFrom() {
        return startFrom * 1000L;
    }

    public void setStartFrom(int startFrom) {
        this.startFrom = startFrom;
    }

    public boolean isCanDownloadVideo() {
        return canDownloadVideo;
    }

    public void setCanDownloadVideo(boolean canDownloadVideo) {
        this.canDownloadVideo = canDownloadVideo;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getRewardTimeProgress() {
        return rewardTimeProgress;
    }

    public void setRewardTimeProgress(long rewardTimeProgress) {
        this.rewardTimeProgress = rewardTimeProgress;
    }

    @Override
    public int compareTo(Object o) {
        if (id.equalsIgnoreCase(((PostObject) o).id)) {
            return 0;
        }
        return 1;
    }

    public Integer getWatchingCount() {
        return watchingCount == null ? 0 : watchingCount;
    }

    public void setWatchingCount(Integer watchingCount) {
        this.watchingCount = watchingCount;
    }

    public int getResumeWindow() {
        return resumeWindow;
    }

    public long getResumePosition() {
        return resumePosition;
    }

    public void setResumeWindow(int resumeWindow) {
        this.resumeWindow = resumeWindow;
    }

    public void setResumePosition(long resumePosition) {
        this.resumePosition = resumePosition;
    }

    public boolean isShowTagOptions() {
        return shouldShowTagOptions;
    }

    public void setShowTagOptions(boolean shouldShowTagOptions) {
        this.shouldShowTagOptions = shouldShowTagOptions;
    }

    public String getLeftOutTime() {
        String publishTime = getStartFrom() + " ";
        int days = TimeUtils.getDaysDifference(publishTime);
        int hours = TimeUtils.getHoursDifference(publishTime);
        hours = hours % 24;

        int minutes = TimeUtils.getMinDifference(publishTime);
        minutes = minutes % 60;

        String leftTime = CommonUtils.getPlural("Day", days);

        if (leftTime.isEmpty()) {
            leftTime = CommonUtils.getPlural("h", hours);
        } else {
            leftTime = leftTime + " " + CommonUtils.getPlural("h", hours);
            leftTime = leftTime.trim();
        }

        if (days == 0) {
            if (leftTime.isEmpty()) {
                leftTime = CommonUtils.getPlural("m", minutes);
            } else {
                leftTime = leftTime + " " + CommonUtils.getPlural("Minute", minutes);
                leftTime = leftTime.trim();
            }
        }

        String leftOutTimeString = "";

        if (isLive) {
            leftOutTimeString = "Since " + leftTime;
        } else {
            leftOutTimeString = "Streamed " + leftTime + " ago.";
        }
        return leftOutTimeString;
    }

    public String getViews() {
        if (getWatchingCount() > 0) {
            return ((getWatchingCount() / 1000 >= 1) ? (getWatchingCount() / 1000) + "." + ((getWatchingCount() % 1000) / 100) + "K" : getWatchingCount()) + " Watching";
        } else {
            int totalNumViews = getTotalViews() > 0 ? getTotalViews() : 1;
            return (getTotalViews() / 1000 >= 1) ? (getTotalViews() / 1000) + "." + ((getTotalViews() % 1000) / 100) + "K" : totalNumViews + " Views";
        }
    }

    public String getStreamingDuration() {
        if (TimeUtils.hasStreamNotStarted(getStartFrom())) {
            return null;
        } else {
            return getLeftOutTime();
        }
    }

    public boolean isCardType() {
        return isCardType;
    }

    public void setCardType(boolean cardType) {
        isCardType = cardType;
    }

    public List<VideoUrlObj> getVideoUrls() {
        return videoUrls;
    }

    public void setVideoUrls(List<VideoUrlObj> videoUrls) {
        this.videoUrls = videoUrls;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public VideoUrlObj getAudioUrl() {
        if (videoUrls == null) return null;
        for (VideoUrlObj object : videoUrls) {
            if (object.getNetworkType().equalsIgnoreCase("audio"))
                return object;
        }
        return null;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(videoUrl);
        parcel.writeString(thumbnail);
        parcel.writeString(shareUrl);
        parcel.writeInt(totalViews);
        parcel.writeByte((byte) (isLive ? 1 : 0));
        parcel.writeParcelable(author, i);
        parcel.writeParcelable(game, i);
        parcel.writeString(title);
        parcel.writeInt(startFrom);
        parcel.writeString(language);
        parcel.writeByte((byte) (canDownloadVideo ? 1 : 0));
        parcel.writeLong(rewardTimeProgress);
        parcel.writeLong(resumePosition);
        parcel.writeInt(resumeWindow);
        if (watchingCount == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(watchingCount);
        }
        parcel.writeByte((byte) (isPublished ? 1 : 0));
        parcel.writeList(videoUrls);
        parcel.writeByte((byte) (shouldShowTagOptions ? 1 : 0));
        parcel.writeByte((byte) (isCardType ? 1 : 0));
        parcel.writeString(reminderTime);
    }
}
