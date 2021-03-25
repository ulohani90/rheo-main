package com.rheotv.android.ui.activities.tabcontainer.profile.analytics;


import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class AnalyticsFragmentModule {
    @Provides
    AnalyticsFragmentViewModel analyticsFragmentViewModel(DataManager dataManager,
                                                          SchedulerProvider schedulerProvider) {
        return new AnalyticsFragmentViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideAnalyticsFragmentViewModel(AnalyticsFragmentViewModel analyticsFragmentViewModel) {
        return new ViewModelProviderFactory<>(analyticsFragmentViewModel);
    }
}
