package com.rheotv.android.ui.fragments;


import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class DownloadVideoFragmentModule {
    @Provides
    DownloadVideoFragmentViewModel provideDownloadVideoFragmentViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new DownloadVideoFragmentViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideDownloadVideoFragmentFactory(DownloadVideoFragmentViewModel downloadVideoFragmentViewModel) {
        return new ViewModelProviderFactory<>(downloadVideoFragmentViewModel);
    }

}
