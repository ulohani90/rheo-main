package com.rheotv.android.ui.activities.story;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class StoryPagerFragmentModule {

    @Provides
    StoryViewModel provideStoryFragmentModule(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new StoryViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideStoryViewModel(StoryViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
