package com.rheotv.story.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class StoryCTAData implements Parcelable {

    @SerializedName("profile_id")
    String profileId;

    @SerializedName("user_id")
    String userId;

    @SerializedName("interested_count")
    int interestedCount;

    @SerializedName("iam_interested")
    boolean interested;

    @SerializedName("profile_username")
    String username;

    @SerializedName("profile_pic")
    String profileUrl;

    @SerializedName("is_followed")
    boolean isFollowed;

    public StoryCTAData() {

    }

    public StoryCTAData(String profileId) {
        this.profileId = profileId;
    }

    protected StoryCTAData(Parcel in) {
        profileId = in.readString();
    }

    public String getProfileId() {
        return profileId;
    }

    public int getInterestedCount() {
        return interestedCount;
    }

    public boolean isInterested() {
        return interested;
    }

    public void setInterestedCount(int interestedCount) {
        this.interestedCount = interestedCount;
    }

    public void setInterested(boolean interested) {
        this.interested = interested;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(profileId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StoryCTAData> CREATOR = new Creator<StoryCTAData>() {
        @Override
        public StoryCTAData createFromParcel(Parcel in) {
            return new StoryCTAData(in);
        }

        @Override
        public StoryCTAData[] newArray(int size) {
            return new StoryCTAData[size];
        }
    };
}
