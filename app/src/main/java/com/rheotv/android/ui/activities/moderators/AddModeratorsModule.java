package com.rheotv.android.ui.activities.moderators;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class AddModeratorsModule {

    @Provides
    AddModeratorsViewModel providesAddModeratorsViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new AddModeratorsViewModel(dataManager, schedulerProvider);
    }
}
