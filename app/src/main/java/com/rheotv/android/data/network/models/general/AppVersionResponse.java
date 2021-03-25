package com.rheotv.android.data.network.models.general;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppVersionResponse {
    @SerializedName("supported")
    @Expose
    private Boolean supported;

    @SerializedName("loc_permission_needed")
    @Expose
    private Boolean locationPermissionNeeded;

    @SerializedName("strict")
    @Expose
    private Boolean strict;

    @SerializedName("show_onboarding")
    @Expose
    private Boolean showOnBoarding;

    @SerializedName("direct_video")
    @Expose
    private Boolean hasDirectVideo;

    @SerializedName("splash_thumbnail")
    @Expose
    private String splashAdUrl;

    @SerializedName("splash_valid_ts")
    @Expose
    private String splashValidTS;

    @SerializedName("splash_ad_target_url")
    @Expose
    private String splashAdTargetUrl;

    @SerializedName("ad_name")
    @Expose
    private String splashAdName;

    @SerializedName("for_select_audience")
    @Expose
    private boolean isSelectedUser;

    @SerializedName("display_top_shows")
    @Expose
    private boolean isTopShowUser;

    @SerializedName("preferred_language_bool")
    @Expose
    private boolean isPreferredLanguageBoardingUser;

    public boolean isSelectedUser() {
        return isSelectedUser;
    }

    public boolean isTopShowUser() {
        return isTopShowUser;
    }

    public String getSplashAdName() {
        return splashAdName;
    }

    public void setSplashAdName(String splashAdName) {
        this.splashAdName = splashAdName;
    }

    public Boolean getSupported() {
        return supported;
    }

    public void setSupported(Boolean supported) {
        this.supported = supported;
    }

    public Boolean getLocationPermissionNeeded() {
        return locationPermissionNeeded;
    }

    public void setLocationPermissionNeeded(Boolean locationPermissionNeeded) {
        this.locationPermissionNeeded = locationPermissionNeeded;
    }

    public Boolean getStrict() {
        return strict;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    public Boolean getShowOnBoarding() {
        return showOnBoarding;
    }

    public void setShowOnBoarding(Boolean showOnBoarding) {
        this.showOnBoarding = showOnBoarding;
    }

    public Boolean getHasDirectVideo() {
        return hasDirectVideo;
    }


    public String getSplashValidTS() {
        return splashValidTS;
    }

    public String getSplashAdUrl() {
        return splashAdUrl;
    }

    public void setSplashAdUrl(String splashAdUrl) {
        this.splashAdUrl = splashAdUrl;
    }

    public void setSplashValidTS(String splashValidTS) {
        this.splashValidTS = splashValidTS;
    }

    public String getSplashAdTargetUrl() {
        return splashAdTargetUrl;
    }

    public void setSplashAdTargetUrl(String splashAdTargetUrl) {
        this.splashAdTargetUrl = splashAdTargetUrl;
    }

    public boolean isPreferredLanguageBoardingUser() {
        return isPreferredLanguageBoardingUser;
    }
}
