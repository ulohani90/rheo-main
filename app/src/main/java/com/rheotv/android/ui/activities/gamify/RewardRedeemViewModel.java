package com.rheotv.android.ui.activities.gamify;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.gamify.CodaShopGame;
import com.rheotv.android.data.network.models.gamify.CodaShopGameResponse;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardRedeemViewModel extends BaseViewModel {
    private final MutableLiveData<List<CodaShopGame>> gameListResult = new MutableLiveData<>();

    public RewardRedeemViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public MutableLiveData<List<CodaShopGame>> getGameListResult() {
        return gameListResult;
    }

    public void loadGames() {
        Log.i(getClass().getSimpleName(), "loadGames ");
        getDataManager().getCodaGames().enqueue(new Callback<CodaShopGameResponse>() {
            @Override
            public void onResponse(Call<CodaShopGameResponse> call, Response<CodaShopGameResponse> response) {
                if (response.isSuccessful() && response.body() != null)
                    gameListResult.setValue(response.body().getResults());
            }

            @Override
            public void onFailure(Call<CodaShopGameResponse> call, Throwable t) {
                Log.i(getClass().getSimpleName(), "loadGames error: " + t.getMessage());
            }
        });
    }
}
