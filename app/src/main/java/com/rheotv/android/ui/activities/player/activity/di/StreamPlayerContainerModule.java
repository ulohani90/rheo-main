package com.rheotv.android.ui.activities.player.activity.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class StreamPlayerContainerModule {

    @Provides
    StreamPlayerContainerViewModel provideStreamPlayerContainerViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new StreamPlayerContainerViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideViewModelFactory(StreamPlayerContainerViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
