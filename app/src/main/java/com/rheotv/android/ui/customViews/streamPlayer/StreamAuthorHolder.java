package com.rheotv.android.ui.customViews.streamPlayer;

import android.os.Parcel;
import android.os.Parcelable;

public class StreamAuthorHolder implements Parcelable {
    private String streamTitle;
    private String gameName;
    private String profileUrl;
    private String username;
    private Integer followCount;
    private boolean isFollowing;
    private String viewCount;

    public StreamAuthorHolder(String streamTitle,
                              String gameName,
                              String profileUrl,
                              String username,
                              Integer followCount,
                              boolean isFollowing,
                              String viewCount
    ) {
        this.streamTitle = streamTitle;
        this.gameName = gameName;
        this.profileUrl = profileUrl;
        this.username = username;
        this.followCount = followCount;
        this.isFollowing = isFollowing;
        this.viewCount = viewCount;
    }

    protected StreamAuthorHolder(Parcel in) {
        streamTitle = in.readString();
        gameName = in.readString();
        profileUrl = in.readString();
        username = in.readString();
        followCount = in.readInt();
        isFollowing = in.readByte() != 0;
        viewCount = in.readString();
    }

    public static final Creator<StreamAuthorHolder> CREATOR = new Creator<StreamAuthorHolder>() {
        @Override
        public StreamAuthorHolder createFromParcel(Parcel in) {
            return new StreamAuthorHolder(in);
        }

        @Override
        public StreamAuthorHolder[] newArray(int size) {
            return new StreamAuthorHolder[size];
        }
    };

    public String getStreamTitle() {
        return streamTitle;
    }

    public String getGameName() {
        return gameName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getUsername() {
        return username;
    }

    public Integer getFollowCount() {
        return followCount == null ? 0 : followCount;
    }

    public void setFollowCount(Integer followCount) {
        this.followCount = followCount;
    }

    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(streamTitle);
        parcel.writeString(gameName);
        parcel.writeString(profileUrl);
        parcel.writeString(username);
        parcel.writeInt(followCount);
        parcel.writeByte((byte) (isFollowing ? 1 : 0));
        parcel.writeString(viewCount);
    }

    public static class Builder {
        private String streamTitle;
        private String gameName;
        private String profileUrl;
        private String username;
        private Integer followCount;
        private boolean isFollowing;
        private String viewCount;

        public Builder setStreamTitle(String streamTitle) {
            this.streamTitle = streamTitle;
            return this;
        }

        public Builder setGameName(String gameName) {
            this.gameName = gameName;
            return this;
        }

        public Builder setProfileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setFollowCount(Integer followCount) {
            this.followCount = followCount;
            return this;
        }

        public Builder setFollowing(boolean isFollowing) {
            this.isFollowing = isFollowing;
            return this;
        }

        public Builder setViewCount(String viewCount) {
            this.viewCount = viewCount;
            return this;
        }

        public StreamAuthorHolder build() {
            return new StreamAuthorHolder(
                    streamTitle,
                    gameName,
                    profileUrl,
                    username,
                    followCount,
                    isFollowing,
                    viewCount
            );
        }
    }
}
