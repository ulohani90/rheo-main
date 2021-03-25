package com.rheotv.android.data.network.models.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StreamerObject {

    @SerializedName("id")
    @Expose
    int id;

    @SerializedName("username")
    @Expose
    String username;

    @SerializedName("profile_pic")
    @Expose
    String profilePic;

    @SerializedName("followers_count_str")
    @Expose
    String followersCountStr;

    @SerializedName("is_verified")
    @Expose
    String isVerified;

    @SerializedName("share_url")
    @Expose
    String shareUrl;

    boolean isFollowed;

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getFollowersCountStr() {
        return followersCountStr;
    }

    public void setFollowersCountStr(String followersCountStr) {
        this.followersCountStr = followersCountStr;
    }

    public String getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(String isVerified) {
        this.isVerified = isVerified;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    public boolean isFollowed() {
        return isFollowed;
    }
}
