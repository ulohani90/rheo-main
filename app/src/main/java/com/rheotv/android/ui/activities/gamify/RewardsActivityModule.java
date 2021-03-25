package com.rheotv.android.ui.activities.gamify;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class RewardsActivityModule {

    @Provides
    RewardsViewModel provideRewardsViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new RewardsViewModel(dataManager, schedulerProvider);
    }


}
