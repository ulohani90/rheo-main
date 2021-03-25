package com.rheotv.android.ui.activities.profile.viewprofile.di.module;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.activities.profile.viewprofile.viewmodel.ProfileViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class ProfileActivityModule {

    @Provides
    ProfileViewModel providesProfileViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new ProfileViewModel(dataManager, schedulerProvider);
    }
}
