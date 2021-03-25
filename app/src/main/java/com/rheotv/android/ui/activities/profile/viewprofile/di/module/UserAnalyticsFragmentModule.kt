package com.rheotv.android.ui.activities.profile.viewprofile.di.module

import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.UserAnalyticsAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.viewmodel.UserAnalyticsViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class UserAnalyticsFragmentModule {
    @Provides
    internal fun profileViewModel(dataManager: DataManager,
                                  schedulerProvider: SchedulerProvider): UserAnalyticsViewModel {
        return UserAnalyticsViewModel(dataManager, schedulerProvider)
    }

    @Provides
    internal fun provideProfileViewModel(viewModel: UserAnalyticsViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

    @Provides
    internal fun provideUserAnalyticsAdapter(): UserAnalyticsAdapter {
        return UserAnalyticsAdapter()
    }
}
