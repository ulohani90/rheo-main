package com.rheotv.android.ui.activities.profile.viewprofile.viewmodel;

import android.view.View;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.ui.activities.profile.viewprofile.utils.ProfileNavigator;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

public class ProfileViewModel extends BaseViewModel<ProfileNavigator> {

    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    public ProfileResult authorProfileData;
    public HashMap<String, Object> baseProperties = new HashMap<>();

    public ProfileViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }


    public void setAuthorProfileData(ProfileResult authorProfileData) {
        this.authorProfileData = authorProfileData;
    }

    public void onBackPressed() {
        getNavigator().onToolbarBackPressed();
    }

    public void onShareClicked(View view) {
        if (!CommonUtils.isUserLoggedin()) {
            getNavigator().openLoginFlow();
        } else {
            //share profile
            if (authorProfileData != null && authorProfileData.getShareUrl() != null) {
                baseProperties.put("username", authorProfileData.getUser().getUsername());
                SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_USER_PROFILE_SHARE_CLICK, baseProperties);
                HashMap<String, String> map = new HashMap<>();
                map.put(AppConstants.BRANCH_PROFILE_URL_SHARE, authorProfileData.getShareUrl());
                map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_PROFILE);
                FirebaseDynamicLinkUtils.share(view.getContext(),
                        authorProfileData.getCampaignInfo(),
                        "share_user_profile",
                        "Watch " + ((authorProfileData != null && authorProfileData.getUser() != null) ? authorProfileData.getUser().getFullName() : "player") + " streaming live on Rheo TV!\n",
                        "Hit follow button to get notified when " + ((authorProfileData != null && authorProfileData.getUser() != null) ? authorProfileData.getUser().getFullName() : "player") + " comes live next.",
                        authorProfileData.getProfilePic(), map, authorProfileData.getShareUrl(), true);
                AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext()).sendClick("journalist_share");
            }
        }
    }
}
