package com.rheotv.android.ui.activities.gamify;

import android.content.Context;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class RewardVoucherFragmentModule {

    @Provides
    RewardVoucherViewModel rewardRedeemViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new RewardVoucherViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(RewardVoucherViewModel rewardVoucherViewModel) {
        return new ViewModelProviderFactory<>(rewardVoucherViewModel);
    }

    @Provides
    RewardVoucherAdapter provideRewardVoucherAdapter(Context context) {
        return new RewardVoucherAdapter(context, new RewardVoucherAdapter.VoucherDiffUtil());
    }

}
