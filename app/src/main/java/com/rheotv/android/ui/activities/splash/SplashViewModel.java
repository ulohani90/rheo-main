/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 2:41 PM
 *
 */

package com.rheotv.android.ui.activities.splash;

import android.os.Handler;
import android.util.Log;

import androidx.databinding.ObservableField;

import com.google.gson.Gson;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.AnalyticsEventsResponse;
import com.rheotv.android.data.network.models.directVideo.VideoResponse;
import com.rheotv.android.data.network.models.gamify.DailyRewardsResponse;
import com.rheotv.android.data.network.models.general.AppVersionResponse;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.helpers.JsonParseHelper;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.TimeUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class SplashViewModel extends BaseViewModel<SplashNavigator> {

    public final ObservableField<AppVersionResponse> appVersionResponse = new ObservableField<>();
    private Integer versionCheckRetries = 0;
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    private JsonParseHelper jsonParseHelper = new JsonParseHelper();

    public ObservableField<Boolean> showOnBoarding = new ObservableField<>();
    public ArrayList<PostObject> directPost = new ArrayList<>();
    private boolean showUpdate = false;

    public SplashViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void startSeeding() {
        // Do the required API calls.
        /*getCompositeDisposable().add(getDataManager()
                .getNewsList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(newsItems -> {
                    setIsLoading(false);
                }, throwable -> {
                    setIsLoading(false);
                    getNavigator().handleError(throwable);
                }));*/
        decideNextActivity();
    }

    private void decideNextActivity() {
        getNavigator().openMainActivity();
    }

    public boolean versionChecked = false;

    public String extraInfo;


    public void getAnalyticsEventsList() {
        getDataManager().getAnalyticsEventsList().enqueue(new Callback<AnalyticsEventsResponse>() {
            @Override
            public void onResponse(Call<AnalyticsEventsResponse> call, Response<AnalyticsEventsResponse> response) {
                if (response != null && response.body() != null) {
                    SegmentTracker.getInstance(getNonUiContext()).setAnalyticsEvents(response.body().getEvents(), response.body().getMoengageEvents());
                }
                if (getNavigator() != null) {
                    getNavigator().setupAnalyticsEvents();
//                    if (CommonUtils.isUserLoggedin()) {
//                        loadDailyRewards();
//                    } else {
//                        checkVersionSupport();
//                    }
                }
            }

            @Override
            public void onFailure(Call<AnalyticsEventsResponse> call, Throwable t) {
                if (getNavigator() != null) {
                    getNavigator().setupAnalyticsEvents();
//                    if (CommonUtils.isUserLoggedin()) {
//                        loadDailyRewards();
//                    } else {
//                        checkVersionSupport();
//                    }
                }
            }
        });
    }

    void loadDailyRewards() {
        getDataManager().getDailyRewards().enqueue(new Callback<DailyRewardsResponse>() {
            @Override
            public void onResponse(Call<DailyRewardsResponse> call, Response<DailyRewardsResponse> response) {
                try {
                    RewardManager.getInstance().setDailyRewards(response.body().getResults());
                    RewardManager.getInstance().setTotalCoins(response.body().getTotalCoins());
                    RewardManager.getInstance().setCodaEnabled(response.body().isCodaEnabled());
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                checkVersionSupport();
            }

            @Override
            public void onFailure(Call<DailyRewardsResponse> call, Throwable t) {
                Log.i(getClass().getName(), "loadDailyRewards: " + t.getMessage());
//                checkVersionSupport();
            }
        });
    }


    public void setVersionChecked(boolean versionChecked) {
        this.versionChecked = versionChecked;
    }

    public void checkVersionSupport() {
//        versionCheckRetries = versionCheckRetries + 1;
//        String latestPosts = sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.HOME_LATEST_POSTS);
//        if(latestPosts != null && !latestPosts.isEmpty()){
        //getNavigator().openMainActivity();
//        }

        if (CommonUtils.getDeviceId(getNonUiContext()) == null)
            return;

        if (versionChecked)
            return;
        versionChecked = true;

//        shouldAskVersion = false;

        Log.i(getClass().getSimpleName(), "checkVersionSupport_called");

        getDataManager().checkVersionSupport(extraInfo).enqueue(new Callback<AppVersionResponse>() {
            @Override
            public void onResponse(Call<AppVersionResponse> call, Response<AppVersionResponse> response) {
                if (getNavigator() != null) {
                    Log.i(getClass().getSimpleName(), "checkVersionSupport: " + new Gson().toJson(response));
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getSplashAdTargetUrl() != null && response.body().getSplashValidTS() != null) {
                            CommonUtils.setSplashBannerValidTillTS(TimeUtils.getTimeInMillis(response.body().getSplashValidTS()));
                            CommonUtils.setSplashBannerUrl(response.body().getSplashAdUrl());
                            CommonUtils.setSplashBannerTarget(response.body().getSplashAdTargetUrl());
                            CommonUtils.setSplashBannerAdName(response.body().getSplashAdName());
                        } else {
                            CommonUtils.setSplashBannerValidTillTS(0);
                            CommonUtils.setSplashBannerUrl(null);
                            CommonUtils.setSplashBannerTarget(null);
                            CommonUtils.setSplashBannerAdName(null);
                        }
                        CommonUtils.setSelectedUser(response.body().isSelectedUser());
                        CommonUtils.setTopShowUser(response.body().isTopShowUser());
                        CommonUtils.setPreferredLanguageBoardingUser(
                               response.body().isPreferredLanguageBoardingUser()
                        );
                        showOnBoarding.set(response.body().getShowOnBoarding());
                        appVersionResponse.set(response.body());
                        if (!response.body().getSupported()) {
                            if (response.body().getStrict()) {
                                if (getNavigator() != null) getNavigator().showForceUpdateDialog();
                            } else {
                                showUpdate = true;
                                if (getNavigator() != null) getNavigator().showUpdateOptions();
                            }
                        } else {
                            if (response.body().getHasDirectVideo() != null && response.body().getHasDirectVideo()) {
                                CommonUtils.setDirectVideoWatchUser();
                                if (CommonUtils.isOnBoarded()) {
                                    getDirectVideo();
                                } else {
                                    if (getNavigator() != null)
                                        getNavigator().onNetworkRequestComplete();
                                }
                            } else if (getNavigator() != null)
                                getNavigator().onNetworkRequestComplete();

                        }
                    } else {
                        if (getNavigator() != null) getNavigator().onNetworkRequestComplete();
                    }
                }

            }

            @Override
            public void onFailure(Call<AppVersionResponse> call, Throwable t) {
                new Handler().postDelayed(() -> {
                    //checkVersionSupport();
                    if (getNavigator() != null)
                        getNavigator().handleError(t);
                }, versionCheckRetries * 1000 + 2);
            }
        });
    }

    public boolean isShowUpdate() {
        return showUpdate;
    }

    public void onUpdateClicked() {
        getNavigator().openPlayStoreLink();
    }

    public void onSkipClicked() {
        getNavigator().openMainActivity();
    }

    /*public void getLatestPosts() {
        getCompositeDisposable().add(getDataManager()
                .getHomePage()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(blogResponse -> {
                    if (blogResponse != null && blogResponse.getResults() != null) {
                        jsonParseHelper.saveLatestResponseJson(blogResponse);
                        getNavigator().isDataLoaded();
                    }

                }, throwable -> {

                }));
    }*/


    public void getCompetitionData(String competitionId) {
        if (CommonUtils.getDeviceId(getNonUiContext()) == null)
            return;

        getDataManager().getCompetitionPage(competitionId).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (getNavigator() != null) getNavigator().showCompetionPage(response.body());
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {

            }
        });
    }

    public void getDirectVideo() {
        getDataManager().loadPost(AppConstants.GET_DIRECT_VIDEO).enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getResults().isEmpty())
                    directPost = new ArrayList<>(response.body().getResults().get(0).getPosts());
                if (getNavigator() != null) getNavigator().onNetworkRequestComplete();
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                if (getNavigator() != null) getNavigator().onNetworkRequestComplete();
            }
        });
    }
}
