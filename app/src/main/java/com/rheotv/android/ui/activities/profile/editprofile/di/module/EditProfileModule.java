package com.rheotv.android.ui.activities.profile.editprofile.di.module;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.activities.profile.editprofile.viewmodel.EditProfileViewModel;
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class EditProfileModule {

    @Provides
    EditProfileViewModel provideViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new EditProfileViewModel(dataManager, schedulerProvider);
    }
}
