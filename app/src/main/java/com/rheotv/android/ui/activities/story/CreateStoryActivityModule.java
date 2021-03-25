package com.rheotv.android.ui.activities.story;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class CreateStoryActivityModule {
    @Provides
    CreateStoryViewModel provideCreateStoryActivityModule(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new CreateStoryViewModel(dataManager, schedulerProvider);
    }

}
