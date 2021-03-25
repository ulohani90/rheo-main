package com.rheotv.android.ui.fragments;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class LiveStreamFragmentModule {

    @Provides
    LiveStreamViewModel liveStreamViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new LiveStreamViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideLiveStreamViewModel(LiveStreamViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
