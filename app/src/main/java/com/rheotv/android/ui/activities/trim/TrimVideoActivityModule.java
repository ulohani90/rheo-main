package com.rheotv.android.ui.activities.trim;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class TrimVideoActivityModule {

    @Provides
    TrimVideoViewModel provideCreateStoryActivityModule(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new TrimVideoViewModel(dataManager, schedulerProvider);
    }
}
