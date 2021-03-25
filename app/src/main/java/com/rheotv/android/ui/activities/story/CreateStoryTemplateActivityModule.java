package com.rheotv.android.ui.activities.story;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class CreateStoryTemplateActivityModule {
    @Provides
    CreateTemplateViewModel provideCreateTemplateViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new CreateTemplateViewModel(dataManager, schedulerProvider);
    }

}
