package com.rheotv.android.ui.activities.tabcontainer.profile.bio;


import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class BioFragmentModule {
    @Provides
    BioFragmentViewModel bioFragmentViewModel(DataManager dataManager,
                                              SchedulerProvider schedulerProvider) {
        return new BioFragmentViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideProfileViewModel(BioFragmentViewModel bioFragmentViewModel) {
        return new ViewModelProviderFactory<>(bioFragmentViewModel);
    }
}