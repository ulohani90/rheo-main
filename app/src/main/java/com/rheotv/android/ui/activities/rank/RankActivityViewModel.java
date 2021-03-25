package com.rheotv.android.ui.activities.rank;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.useProfile.responses.StreamerLevel;
import com.rheotv.android.data.network.models.useProfile.responses.StreamerLevelResponseBody;
import com.rheotv.android.ui.adapters.LevelType;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.io.IOException;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RankActivityViewModel extends BaseViewModel<RankActivityNavigator> {

    private LevelType levelType = LevelType.Unassigned.INSTANCE;
    private int paymentModel = 1;
    private String rewardDefinition;

    public RankActivityViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(LevelType levelType) {
        this.levelType = levelType;
    }

    public int getPaymentModel() {
        return paymentModel;
    }

    public String getRewardDefinition() {
        return rewardDefinition;
    }

    public void setRewardDefinition(String rewardDefinition) {
        this.rewardDefinition = rewardDefinition;
    }

    public void setPaymentModel(int paymentModel) {
        this.paymentModel = paymentModel;
    }

    HashMap<String, StreamerLevel> streamerLevelHashMap = new HashMap<>();

    public void fetchStreamerLevelInfo(int userId) {
        getDataManager().getStreamerLevelInfo(userId).enqueue(new Callback<StreamerLevelResponseBody>() {
            @Override
            public void onResponse(Call<StreamerLevelResponseBody> call, Response<StreamerLevelResponseBody> response) {
                if (response != null) {
                    if (response.body() != null) {
                        if (response.body().getData() != null)
                            setRewardDefinition(response.body().getData().getDefinition());
                        if (getNavigator() != null)
                            getNavigator().setStreamerLevelInfo(response.body());
                    } else if (response.errorBody() != null) {
                        try {
                            if (getNavigator() != null)
                                getNavigator().showErrorToast(response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (getNavigator() != null)
                        getNavigator().showErrorToast("Error occurred. Please try again");
                }
            }

            @Override
            public void onFailure(Call<StreamerLevelResponseBody> call, Throwable t) {
                if (getNavigator() != null)
                    getNavigator().showErrorToast(t.getMessage());
            }
        });
    }
}
