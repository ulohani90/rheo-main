package com.rheotv.android.ui.activities.gamify;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.gamify.DailyRewardsResponse;
import com.rheotv.android.data.network.models.gamify.Reward;
import com.rheotv.android.data.network.models.gamify.RewardTakenResponse;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardVoucherViewModel extends BaseViewModel {

    public RewardVoucherViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    private MutableLiveData<List<Reward>> mAvailableScratchCardList = new MutableLiveData<>();

    void loadAvailableScratchCard() {
        if (!CommonUtils.isUserLoggedin())
            return;
        Log.i(getClass().getSimpleName(), "loadAvailableScratchCard ");
        getDataManager().loadAvailableScratchCards()
                .flatMap(dailyRewardsResponse -> {
                    RewardManager.getInstance().updateData(dailyRewardsResponse);
                    return Observable
                            .fromIterable(dailyRewardsResponse.getResults())
                            .filter(item -> item != null && AppConstants.STATUS_SHOWN.equalsIgnoreCase(item.getRewardStatus()))
                            .toList()
                            .toObservable();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<List<Reward>>() {
                    @Override
                    public void onNext(List<Reward> rewards) {
                        if (rewards != null) {
                            mAvailableScratchCardList.postValue(rewards);
                            updateRewardViews();
                        }
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        if (throwable != null) {
                            Log.i(getClass().getSimpleName(), "loadGames error: " + throwable.getMessage());
                        }
                    }
                    @Override
                    public void onComplete() {
                        if (!getCompositeDisposable().isDisposed()) {
                            getCompositeDisposable().clear();
                        }
                    }
                });
    }

    MutableLiveData<List<Reward>> getAvailableScratchCardList() {
        return mAvailableScratchCardList;
    }

    public void updateScratchCard(String rewardId) {
        getDataManager().updateDailyScratchCard(rewardId).enqueue(new Callback<RewardTakenResponse>() {
            @Override
            public void onResponse(Call<RewardTakenResponse> call, Response<RewardTakenResponse> response) {
                if (response.isSuccessful() && response.body().isSuccessful()) {
                    // update reward manager
                    loadAvailableScratchCard();
                }
            }

            @Override
            public void onFailure(Call<RewardTakenResponse> call, Throwable t) {
                Log.i(getClass().getName(), "dummyLoadDailyRewards " + t.getMessage());
            }
        });
    }

    private void updateRewardViews() {

    }
}