
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
import com.rheotv.android.data.network.models.districtlisting.DistrictResult;
import com.rheotv.android.data.network.models.stickers.LanguagesSlang;

import java.util.List;

public class PostListingResponse {


    @SerializedName("results")
    private List<Result> homeResults;
    @SerializedName("region")
    private List<DistrictResult> regions;
    @SerializedName("user_region_name_id")
    private String userRegionNameId;
    @SerializedName("user_region_name")
    private String userRegionName;
    @SerializedName("user_region_phone")
    private String userRegionPhone;
    @SerializedName("show_feedback_card")
    private boolean showFeedbackCard;
    @SerializedName("show_play_icon")
    private boolean showPlayIcon;
    @SerializedName("wa_group")
    private String waGroup;
    @SerializedName("is_version_supported")
    private boolean isVersionSupported = true;
    @SerializedName("total_coins")
    @Expose
    private int totalCoins;
    @SerializedName("showLeaderBoard")
    @Expose
    private boolean showLeaderBoard = true;
    @SerializedName("count")
    @Expose
    private int count = 0;
    @SerializedName("next")
    @Expose
    private String next;
    @SerializedName("previous")
    @Expose
    private String previous;

    @SerializedName("enable_clips")
    @Expose
    private boolean enableClips;

    @SerializedName("show_go_live")
    @Expose
    private boolean enableGoLive;

    @SerializedName("")
    @Expose
    private List<LanguagesSlang> languagesSlangs;

    public List<Result> getResults() {
        return homeResults;
    }

    public void setResults(List<Result> results) {
        this.homeResults = results;
    }

    public List<DistrictResult> getRegions() {
        return regions;
    }

    public void setRegions(List<DistrictResult> regions) {
        this.regions = regions;
    }

    public String getUserRegionNameId() {
        return userRegionNameId;
    }


    public void setUserRegionNameId(String userRegionNameId) {
        this.userRegionNameId = userRegionNameId;
    }

    public String getUserRegionPhone() {
        return userRegionPhone;
    }

    public void setUserRegionPhone(String userRegionPhone) {
        this.userRegionPhone = userRegionPhone;
    }

    public String getWaGroup() {
        return waGroup;
    }

    public void setWaGroup(String waGroup) {
        this.waGroup = waGroup;
    }

    public String getUserRegionName() {
        return userRegionName;
    }

    public boolean getShowFeedbackCard() {
        return showFeedbackCard;
    }

    public void getShowFeedbackCard(boolean showFeedbackCard) {
        this.showFeedbackCard = showFeedbackCard;
    }

    public boolean getShowPlayIcon() {
        return showPlayIcon;
    }

    public void setShowPlayIcon(boolean showPlayIcon) {
        this.showPlayIcon = showPlayIcon;
    }


    public boolean getIsVersionSupported() {
        return isVersionSupported;
    }

    public void setIsVersionSupported(boolean isVersionSupported) {
        this.isVersionSupported = isVersionSupported;
    }

    public void setUserRegionName(String userRegionName) {
        this.userRegionName = userRegionName;
    }

    public int getTotalCoins() {
        return totalCoins;
    }

    public void setTotalCoins(int coins) {
        this.totalCoins = coins;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isShowLeaderBoard() {
        return showLeaderBoard;
    }

    public void setShowLeaderBoard(boolean showLeaderBoard) {
        this.showLeaderBoard = showLeaderBoard;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public boolean isEnableClips() {
        return enableClips;
    }

    public void setEnableClips(boolean enableClips) {
        this.enableClips = enableClips;
    }

    public boolean isEnableGoLive() {
        return enableGoLive;
    }

    public void setEnableGoLive(boolean enableGoLive) {
        this.enableGoLive = enableGoLive;
    }

    public List<LanguagesSlang> getLanguagesSlangs() {
        return languagesSlangs;
    }
}
