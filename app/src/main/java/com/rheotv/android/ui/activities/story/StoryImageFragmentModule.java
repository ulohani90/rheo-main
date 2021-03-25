package com.rheotv.android.ui.activities.story;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class StoryImageFragmentModule {
    @Provides
    StoryImageViewModel storyImageViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new StoryImageViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideStoryImageViewModel(StoryImageViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
