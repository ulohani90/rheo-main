package com.rheotv.android.ui.activities.gamify;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.gamify.RewardHistoryResponse;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardHistoryViewModel extends BaseViewModel<RewardHistoryNavigator> {
    String rewardNextUrl = null;

    RewardHistoryViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    void loadRewardTransactionHistory() {
        getDataManager().getRewardHistory(rewardNextUrl).enqueue(new Callback<RewardHistoryResponse>() {
            @Override
            public void onResponse(Call<RewardHistoryResponse> call, Response<RewardHistoryResponse> response) {
                if (response == null || response.body() == null) {
                        if (getNavigator() != null)
                            getNavigator().handleError("Something went wrong. Please try again");
                } else {
                    if (getNavigator() != null && response.body().getResults() != null)
                        getNavigator().addItemInRewards(response.body().getResults());
                    rewardNextUrl = response.body().getNext();
                }
            }

            @Override
            public void onFailure(Call<RewardHistoryResponse> call, Throwable t) {
                if (getNavigator() != null)
                    getNavigator().handleError("Something went wrong. Please try again");
            }
        });
    }
}