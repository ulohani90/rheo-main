package com.rheotv.android.ui.activities.crop;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class CropActivityModule {

    @Provides
    CropImageViewModel provideCreateStoryActivityModule(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new CropImageViewModel(dataManager, schedulerProvider);
    }
}
