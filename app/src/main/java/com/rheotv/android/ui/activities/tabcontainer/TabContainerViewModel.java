/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 03/01/19 12:16 AM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer;

import android.util.Log;
import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.google.gson.Gson;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.AnalyticsEventsResponse;
import com.rheotv.android.data.network.models.gamify.DailyRewardsResponse;
import com.rheotv.android.data.network.models.gamify.Reward;
import com.rheotv.android.data.network.models.gamify.RewardTakenResponse;
import com.rheotv.android.data.network.models.general.AppVersionResponse;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.ui.activities.splash.SplashActivity;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

@SuppressWarnings("unused")
public class TabContainerViewModel extends BaseViewModel<TabContainerNavigator> {

    public ObservableField<String> districtHeader = new ObservableField<>("जिला चुनें");

    public ObservableField<String> totalCoins = new ObservableField<>("0");

    public ObservableBoolean districtFragmentCalled = new ObservableBoolean();

    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    public ObservableField<Boolean> shouldShowCoins = new ObservableField<>(false);

    public ObservableBoolean isNavigatorSet = new ObservableBoolean(false);
    public ObservableField<Reward> loginStreak = new ObservableField<>(new Reward(AppConstants.REWARD_TYPE_SEVENTH_DAY, "0", "0", "0"));
    private HashMap<String, Object> properties = new HashMap<>();

    TabContainerViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        properties.put("userName", CommonUtils.getUserName(getNonUiContext()));
    }

    public void goLiveClick(View view) {
        getNavigator().goLiveClicked(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
    }

    public void viewClipsClick(View view) {
        getNavigator().viewClipsScreen();
    }

    public void viewLeaderClick(View view) {
        getNavigator().onLeaderBoardClick();
    }

    public void viewTotalCoins(View view) {
        getNavigator().viewTotalCoins();
        Map<String, Object> map = new HashMap<>(properties);
        map.put("is_first", CommonUtils.isFirstTimeCoinsClicked());
        map.put("total_coins_count", RewardManager.getInstance().getTotalCoins());
        SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_TOTAL_COIN_CLICKED, map);
        CommonUtils.setFirstTimeCoinsClicked();
    }

    public void handleUpdateClicked(View view) {
        getNavigator().openPlayStoreLink();
        SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_UPDATE_AVAILABLE_CLICKED, properties);
    }

    public void districtFragmentButtonClicked(View view) {
        if (districtFragmentCalled.get()) {
            districtFragmentCalled.set(false);
        } else {
            districtFragmentCalled.set(true);
        }
    }

    public void onRewardStreakClick(View view) {
        getNavigator().onRewardStreakClick();
        SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_LOGIN_STREAK_CLICKED, properties);
    }

    public void loadDailyRewards() {
        if (!CommonUtils.isUserLoggedin()) {
            getNavigator().checkRewardAvailable();
            return;
        }

        getDataManager().getDailyRewards().enqueue(new Callback<DailyRewardsResponse>() {
            @Override
            public void onResponse(Call<DailyRewardsResponse> call, Response<DailyRewardsResponse> response) {
                try {
                    if (response.body() != null) {
                        RewardManager.getInstance().setDailyRewards(response.body().getResults());
                        RewardManager.getInstance().setTotalCoins(response.body().getTotalCoins());
                        RewardManager.getInstance().setCodaEnabled(response.body().isCodaEnabled());
                        updateRewardViews();
                        if (getNavigator() != null)
                            getNavigator().checkRewardAvailable();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<DailyRewardsResponse> call, Throwable t) {
                Log.i(getClass().getName(), "loadDailyRewards: " + t.getMessage());
            }
        });
    }

    public void updateScratchCard(String rewardId, String userName) {
        updateRewardViews();
        getDataManager().updateDailyScratchCard(rewardId).enqueue(new Callback<RewardTakenResponse>() {
            @Override
            public void onResponse(Call<RewardTakenResponse> call, Response<RewardTakenResponse> response) {
                if (response.isSuccessful() && response.body().isSuccessful()) {
                    // update reward manager
                    loadDailyRewards();
                }
            }

            @Override
            public void onFailure(Call<RewardTakenResponse> call, Throwable t) {
                Log.i(getClass().getName(), "dummyLoadDailyRewards " + t.getMessage());
            }
        });
        HashMap<String, Object> properties = new HashMap<>(this.properties);
        properties.put("rewardId", rewardId);
        SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_SCRATCH_CARD, properties);
    }

    public void updateScratchCardStatusShown(String rewardId) {
        updateRewardViews();
        getDataManager().updateScratchCardStatusShown(rewardId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<RewardTakenResponse>>() {
                    @Override
                    public void onNext(Response<RewardTakenResponse> rewardTakenResponse) {
                        if (rewardTakenResponse != null && rewardTakenResponse.isSuccessful())
                            loadDailyRewards();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.i(getClass().getName(), "updateScratchCardStatusShown " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    void updateRewardViews() {
        shouldShowCoins.set(CommonUtils.isUserLoggedin());
        if (CommonUtils.isUserLoggedin()) {
            totalCoins.set(RewardManager.getInstance().getTotalCoins());
            loginStreak.set(RewardManager.getInstance().getUserStreakReward());
        } else {
            totalCoins.set("0");
            loginStreak.set(new Reward(AppConstants.REWARD_TYPE_SEVENTH_DAY, "0", "0", "0"));
            RewardManager.getInstance().clear();
        }
    }

    void rateApp(int rating, String feedback) {
        getDataManager().rateApp(rating, feedback).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(getClass().getName(), "rate App " + t.getMessage());
            }
        });

        HashMap<String, Object> properties = new HashMap<>(this.properties);
        properties.put("rating", rating);
        properties.put("feedback", feedback);
        if (rating > 0)
            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_RATING_GIVEN, properties);
        else
            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_RATING_CANCELLED, properties);
    }

    public void fetchProfile(String authorUserName) {
        getDataManager().getProfile(authorUserName).enqueue(new Callback<ProfileResult>() {
            @Override
            public void onResponse(Call<ProfileResult> call, Response<ProfileResult> response) {
                if (response.body() != null && getNavigator() != null) {
                    CommonUtils.setIsUserContentModerator(response.body().getContentModerator());
                    CommonUtils.setPaymentModel(response.body().getPaymentModel());
                    CommonUtils.setLevelType(response.body().getLevelType());
                }
            }

            @Override
            public void onFailure(Call<ProfileResult> call, Throwable t) {
                t.printStackTrace();
                Log.d("mirage", "fetching profile failed. Probably not loggedIn " + t.getMessage());

            }
        });
    }

    public void getAnalyticsEventsList() {
        getDataManager().getAnalyticsEventsList().enqueue(new Callback<AnalyticsEventsResponse>() {
            @Override
            public void onResponse(@NotNull Call<AnalyticsEventsResponse> call, @NotNull Response<AnalyticsEventsResponse> response) {
                if (response.body() != null) {
                    SegmentTracker.getInstance(getNonUiContext()).setAnalyticsEvents(response.body().getEvents(), response.body().getMoengageEvents());
                }
            }

            @Override
            public void onFailure(@NotNull Call<AnalyticsEventsResponse> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void checkVersionSupport() {
        if (CommonUtils.getDeviceId(getNonUiContext()) == null)
            return;
        Log.i(getClass().getSimpleName(), "checkVersionSupport");

        getDataManager().checkVersionSupport(CommonUtils.getBranchExtraInfo(getNonUiContext())).enqueue(new Callback<AppVersionResponse>() {
            @Override
            public void onResponse(@NotNull Call<AppVersionResponse> call, @NotNull Response<AppVersionResponse> response) {
                if (getNavigator() != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!response.body().getSupported()) {
                            if (response.body().getStrict()) {
                                getNavigator().showForceUpdateDialog();
                            } else {
                                getNavigator().showUpdateOptions();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<AppVersionResponse> call, @NotNull Throwable t) {

            }
        });
    }
}
