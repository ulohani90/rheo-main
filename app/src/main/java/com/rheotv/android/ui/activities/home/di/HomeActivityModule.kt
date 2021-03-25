package com.rheotv.android.ui.activities.home.di

import com.rheotv.android.data.DataManager
import com.rheotv.android.ui.activities.home.viewmodel.HomeActivityViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class HomeActivityModule {

    @Provides
    fun provideViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider) =
            HomeActivityViewModel(dataManager, schedulerProvider)
}