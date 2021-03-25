package com.rheotv.android.ui.activities.profile.editprofile.di.module

import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class GameTimingFragmentModule {
    @Provides
    fun provideViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider) =
            UserProfileViewModel(dataManager, schedulerProvider)

    @Provides
    fun provideViewModelFactory(viewModel: UserProfileViewModel): ViewModelProvider.Factory =
            ViewModelProviderFactory(viewModel)
}
