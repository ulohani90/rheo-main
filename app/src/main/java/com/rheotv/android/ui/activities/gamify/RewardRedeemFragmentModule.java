package com.rheotv.android.ui.activities.gamify;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class RewardRedeemFragmentModule {

    @Provides
    RewardRedeemViewModel rewardRedeemViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new RewardRedeemViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(RewardRedeemViewModel rewardRedeemViewModel) {
        return new ViewModelProviderFactory<>(rewardRedeemViewModel);
    }

    @Provides
    RewardRedeemAdapter provideRewardRedeemAdapter() {
        return new RewardRedeemAdapter();
    }

}
