package com.rheotv.android.ui.activities.profile.editprofile.di.module;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.activities.profile.editprofile.viewmodel.ProfileEditViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class ProfileEditActivityModule {

    @Provides
    ProfileEditViewModel providesProfileEditViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new ProfileEditViewModel(dataManager, schedulerProvider);
    }
}
