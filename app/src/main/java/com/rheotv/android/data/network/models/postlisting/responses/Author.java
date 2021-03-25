
/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 7:07 PM
 *
 */

package com.rheotv.android.data.network.models.postlisting.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.utils.CommonUtils;


public class Author {

    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("profile_pic")
    @Expose
    private String profilePic;
    @SerializedName("is_journalist")
    @Expose
    private Boolean isJournalist;
    @SerializedName("followers_count")
    @Expose
    private Integer followersCount;
    @SerializedName("is_verified")
    @Expose
    private Boolean isVerified;
    @SerializedName("total_views")
    @Expose
    private Integer totalViews;

    @SerializedName("is_prime")
    @Expose
    private Boolean isPrimeStreamer;

    @SerializedName("total_watch_time")
    @Expose
    private Integer total_watch_time;

    @SerializedName("is_followed")
    @Expose
    private boolean isFollowed;

    @SerializedName("moderators")
    @Expose
    private String moderators;

    @SerializedName("campaign_info")
    @Expose
    private String campaignInfo;

    @SerializedName("display_attribute")
    @Expose
    private Integer displayAttribute;

    @SerializedName("display_unit")
    @Expose
    private String displayUnit;

    public Author(User user, String profilePic, Integer followersCount) {
        this.user = user;
        this.profilePic = profilePic;
        this.followersCount = followersCount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public Boolean getIsJournalist() {
        return isJournalist;
    }

    public void setIsJournalist(Boolean isJournalist) {
        this.isJournalist = isJournalist;
    }

    public Integer getFollowersCount() {
        return followersCount;
    }

    public String getFollowers() {
        return CommonUtils.formatValue(getFollowersCount()) + " Followers";
    }

    public String getMultilineFollowers() {
        return CommonUtils.formatValue(getFollowersCount()) + "\nFollowers";
    }

    public void setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Integer getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Integer totalViews) {
        this.totalViews = totalViews;
    }

    public Boolean getPrimeStreamer() {
        return isPrimeStreamer;
    }

    public void setPrimeStreamer(Boolean primeStreamer) {
        isPrimeStreamer = primeStreamer;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    public String getModerators() {
        return moderators;
    }

    public void setModerators(String moderators) {
        this.moderators = moderators;
    }

    public String getCampaignInfo() {
        return campaignInfo;
    }

    public Integer getDisplayAttribute() {
        return displayAttribute != null ? displayAttribute : 0;
    }

    public String getDisplayUnit() {
        return displayUnit;
    }
}
