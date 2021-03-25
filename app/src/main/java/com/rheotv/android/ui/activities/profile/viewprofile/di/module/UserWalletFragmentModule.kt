package com.rheotv.android.ui.activities.profile.viewprofile.di.module

import androidx.lifecycle.ViewModelProvider

import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.profile.viewprofile.viewmodel.UserWalletViewModel
import com.rheotv.android.utils.rx.SchedulerProvider

import dagger.Module
import dagger.Provides

@Module
class UserWalletFragmentModule {

    @Provides
    internal fun profileViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider) = UserWalletViewModel(dataManager, schedulerProvider)

    @Provides
    internal fun provideProfileViewModel(viewModel: UserWalletViewModel): ViewModelProvider.Factory =
            ViewModelProviderFactory(viewModel)
}
