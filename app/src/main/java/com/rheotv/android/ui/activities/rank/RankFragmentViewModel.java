package com.rheotv.android.ui.activities.rank;

import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.useProfile.responses.Achievements;
import com.rheotv.android.data.network.models.useProfile.responses.AchievementsData;
import com.rheotv.android.data.network.models.useProfile.responses.AchievementsResponse;
import com.rheotv.android.ui.adapters.LevelType;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RankFragmentViewModel extends BaseViewModel<RankListNavigator> {

    private int paymentModel = 1;
    private String rewardDefinition;

    public RankFragmentViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    private AchievementsResponse achievementsResponse = null;

    MutableLiveData<String> currentWatchHourLiveData = new MutableLiveData<>();
    MutableLiveData<String> rheoCoinWatchHourLiveData = new MutableLiveData<>();
    MutableLiveData<String> levelAchievementLiveData = new MutableLiveData<>();

    int getPaymentModel() {
        return paymentModel;
    }

    void setPaymentModel(int paymentModel) {
        this.paymentModel = paymentModel;
    }

    public String getRewardDefinition() {
        return rewardDefinition;
    }

    public void setRewardDefinition(String rewardDefinition) {
        this.rewardDefinition = rewardDefinition;
    }

    void getAchievements(LevelType levelType) {
        if (achievementsResponse != null) {
            accumulateDataFromResponse(achievementsResponse.getAchievements(), levelType);
            return;
        }

        getDataManager()
                .getAchievements()
                .enqueue(new Callback<AchievementsResponse>() {
                    @Override
                    public void onResponse(Call<AchievementsResponse> call, Response<AchievementsResponse> response) {
                        if (response != null && response.isSuccessful() && response.body() != null) {
                            achievementsResponse = response.body();
                            accumulateDataFromResponse(achievementsResponse.getAchievements(), levelType);
                        }
                    }

                    @Override
                    public void onFailure(Call<AchievementsResponse> call, Throwable throwable) {
                        try {
                            throwable.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void accumulateDataFromResponse(List<Achievements> achievementsList, LevelType levelType) {
        for (Achievements achievements : achievementsList) {
            if (levelType != null && achievements.getLevel().equalsIgnoreCase(levelType.toString()) && getNavigator() != null) {
                currentWatchHourLiveData.setValue(achievements.getCurrentWatchHour());
                Collections.sort(achievements.getData(), getAscendingOrder());
                AppUtils.filterList(achievements.getData(),
                        achievementsData -> {
                            if (achievementsData.getInfoText() != null && !achievementsData.getInfoText().isEmpty()) {
                                if ("payment_rate".equalsIgnoreCase(achievementsData.getType())) {
                                    rheoCoinWatchHourLiveData.setValue(achievementsData.getInfoText());
                                }
                                if ("level_achievement_bonus".equalsIgnoreCase(achievementsData.getType())) {
                                    levelAchievementLiveData.setValue(achievementsData.getInfoText());
                                }
                            }
                            return !"wh_achievement_bonus".equalsIgnoreCase(achievementsData.getType());
                        });
                getNavigator().setRewardData(achievements.getData());
            }
        }
    }

    private Comparator<AchievementsData> getAscendingOrder() {
        return (achievementsData1, achievementsData2) -> {
            if (achievementsData1.getType() != null && achievementsData1.getType().equalsIgnoreCase("level_achievement_bonus")) {
                return 1;
            }
            if (achievementsData2.getType() != null && achievementsData2.getType().equalsIgnoreCase("level_achievement_bonus")) {
                return 1;
            }
            if (achievementsData1.getTarget() > achievementsData2.getTarget()) {
                return 1;
            } else if (achievementsData1.getTarget() < achievementsData2.getTarget()) {
                return -1;
            }
            return 0;
        };
    }
}
