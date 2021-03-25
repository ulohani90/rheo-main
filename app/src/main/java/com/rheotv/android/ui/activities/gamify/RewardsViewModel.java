package com.rheotv.android.ui.activities.gamify;

import android.content.Context;

import androidx.databinding.ObservableField;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.RewardCustomWebviewApiResponse;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardsViewModel extends BaseViewModel<RewardsNavigator> {

    public ObservableField<String> totalCoins = new ObservableField<>();

    public ObservableField<String> webViewUrl = new ObservableField<>();

    public ObservableField<Boolean> showLoading = new ObservableField<>();

    public RewardsViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void trackRuleInfo(Context context) {
        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_REWARD_RULE_INFO_CLICKED, new HashMap<>());
    }

    public void loadRewardsPage() {
        getDataManager().getRewardsWebviewUrl().enqueue(new Callback<RewardCustomWebviewApiResponse>() {
            @Override
            public void onResponse(Call<RewardCustomWebviewApiResponse> call, Response<RewardCustomWebviewApiResponse> response) {
                if (response != null && response.body() != null) {
                    if (getNavigator() != null)
                        getNavigator().loadRewardsWebviewSuccess(response.body().getResult().getTitle(), response.body().getResult().getUrl());
                } else {
                    if (getNavigator() != null)
                        getNavigator().loadRewardsWebviewFailure();
                }
            }

            @Override
            public void onFailure(Call<RewardCustomWebviewApiResponse> call, Throwable t) {
                if (getNavigator() != null)
                    getNavigator().loadRewardsWebviewFailure();
            }
        });
    }
}
