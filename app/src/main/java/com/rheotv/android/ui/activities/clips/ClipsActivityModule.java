package com.rheotv.android.ui.activities.clips;


import com.rheotv.android.data.DataManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class ClipsActivityModule {

    @Provides
    ClipsViewModel providesClipsViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new ClipsViewModel(dataManager, schedulerProvider);
    }
}
