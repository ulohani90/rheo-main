package com.rheotv.android.ui.activities.player.activity;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

public class VideoRewardViewModel extends BaseViewModel {

    public ObservableBoolean showWatchVideoReward = new ObservableBoolean(true);
    public ObservableBoolean isLoggedIn = new ObservableBoolean(true);
    public ObservableField<String> rewardTimeRemaining = new ObservableField<>();
    public ObservableField<String> rewardSubTitle = new ObservableField<>();
    public MutableLiveData<PlayerGiftBottomSheet.TimerViewState> viewState = new MutableLiveData<>();

    public VideoRewardViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void updateView(PlayerGiftBottomSheet.TimerViewState timerViewState) {
        viewState.setValue(timerViewState);
    }
}
