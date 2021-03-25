package com.rheotv.android.ui.activities.inAppBilling.di

import com.rheotv.android.data.DataManager
import com.rheotv.android.ui.activities.inAppBilling.BillingViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
public class BillingActivityModule {
    @Provides
    fun provideViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider) =
            BillingViewModel(dataManager, schedulerProvider)
}