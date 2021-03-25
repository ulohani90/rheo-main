package com.rheotv.android.ui.activities.profile.viewprofile.di.provider

import com.rheotv.android.ui.activities.profile.viewprofile.di.module.UserAnalyticsFragmentModule
import com.rheotv.android.ui.activities.profile.viewprofile.view.UserAnalyticsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UserAnalyticsFragmentProvider {

    @ContributesAndroidInjector(modules = [UserAnalyticsFragmentModule::class])
    internal abstract fun providesUserAnalyticsFragmentFactory(): UserAnalyticsFragment

}
