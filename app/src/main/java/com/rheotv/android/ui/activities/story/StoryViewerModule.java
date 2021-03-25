package com.rheotv.android.ui.activities.story;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class StoryViewerModule {

    @Provides
    StoryViewerViewModel loginViewModel(DataManager dataManager,
                                  SchedulerProvider schedulerProvider) {
        return new StoryViewerViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideLoginViewModel(StoryViewerViewModel viewerViewModel) {
        return new ViewModelProviderFactory<>(viewerViewModel);
    }

}
