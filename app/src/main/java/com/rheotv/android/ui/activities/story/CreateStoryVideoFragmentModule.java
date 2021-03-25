package com.rheotv.android.ui.activities.story;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class CreateStoryVideoFragmentModule {
    @Provides
    CreateStoryVideoViewModel createStoryVideoViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new CreateStoryVideoViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideCreateStoryViewModel(CreateStoryVideoViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
