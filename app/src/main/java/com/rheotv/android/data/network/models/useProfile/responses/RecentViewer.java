package com.rheotv.android.data.network.models.useProfile.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RecentViewer {

    @SerializedName("id")
    private int id;

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("profile_pic")
    @Expose
    private String profilePic;

    @SerializedName("is_verified")
    @Expose
    private boolean isVerified;

    @SerializedName("followers_count_str")
    @Expose
    private int followersCount;
    @SerializedName("is_prime")
    @Expose
    private boolean isPrime;


    @SerializedName("share_url")
    @Expose
    private String shareUrl;

    public int getId() {
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

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public boolean isPrime() {
        return isPrime;
    }

    public void setPrime(boolean prime) {
        isPrime = prime;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }
}
