package com.rheotv.android.ui.fragments;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class TopStreamerSelectionFragmentModule {
    @Provides
    TopStreamerSelectionViewModel rewardHistoryViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new TopStreamerSelectionViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(TopStreamerSelectionViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
