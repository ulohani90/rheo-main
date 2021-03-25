package com.rheotv.android.services;

import android.os.Parcel;
import android.os.Parcelable;

import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.VideoUrlObj;

import java.util.ArrayList;
import java.util.List;

public class PlayerHeadHolder implements Parcelable {

    private PostObject post;
    private long timeElapsed;
    private int resumeWindow;
    private long resumePosition;
    private boolean wasFromDeeplink;

    private List<VideoUrlObj> videoUrls;

    public PlayerHeadHolder(PostObject post, long timeElapsed, int resumeWindow, long resumePosition, List<VideoUrlObj> videoUrls) {
        this(post, timeElapsed, resumeWindow, resumePosition, videoUrls, false);
    }

    public PlayerHeadHolder(PostObject post, long timeElapsed, int resumeWindow, long resumePosition, List<VideoUrlObj> videoUrls, boolean wasFromDeeplink) {
        this.post = post;
        this.timeElapsed = timeElapsed;
        this.resumeWindow = resumeWindow;
        this.resumePosition = resumePosition;
        this.videoUrls = videoUrls;
        this.wasFromDeeplink = wasFromDeeplink;
    }

    protected PlayerHeadHolder(Parcel in) {
        post = in.readParcelable(PostObject.class.getClassLoader());
        timeElapsed = in.readLong();
        resumeWindow = in.readInt();
        resumePosition = in.readLong();
        videoUrls = new ArrayList<>();
        in.readTypedList(videoUrls, VideoUrlObj.CREATOR);

    }

    public static final Creator<PlayerHeadHolder> CREATOR = new Creator<PlayerHeadHolder>() {
        @Override
        public PlayerHeadHolder createFromParcel(Parcel in) {
            return new PlayerHeadHolder(in);
        }

        @Override
        public PlayerHeadHolder[] newArray(int size) {
            return new PlayerHeadHolder[size];
        }
    };

    public PostObject getPost() {
        return post;
    }

    public void setPost(PostObject post) {
        this.post = post;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public int getResumeWindow() {
        return resumeWindow;
    }

    public void setResumeWindow(int resumeWindow) {
        this.resumeWindow = resumeWindow;
    }

    public long getResumePosition() {
        return resumePosition;
    }

    public void setResumePosition(long resumePosition) {
        this.resumePosition = resumePosition;
    }

    public List<VideoUrlObj> getVideoUrls() {
        return videoUrls;
    }

    public boolean wasFromDeeplink() {
        return wasFromDeeplink;
    }

    public void setFromDeeplink(boolean wasFromDeeplink) {
        this.wasFromDeeplink = wasFromDeeplink;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(post, i);
        parcel.writeLong(timeElapsed);
        parcel.writeInt(resumeWindow);
        parcel.writeLong(resumePosition);
        parcel.writeTypedList(videoUrls);
    }
}
