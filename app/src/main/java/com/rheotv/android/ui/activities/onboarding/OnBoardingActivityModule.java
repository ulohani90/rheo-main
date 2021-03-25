package com.rheotv.android.ui.activities.onboarding;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class OnBoardingActivityModule {

    @Provides
    OnBoardingActivityViewModel providesOnBoardingActivityViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new OnBoardingActivityViewModel(dataManager, schedulerProvider);
    }

}
