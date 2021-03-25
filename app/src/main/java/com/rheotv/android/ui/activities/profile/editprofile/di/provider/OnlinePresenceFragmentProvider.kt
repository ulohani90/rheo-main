package com.rheotv.android.ui.activities.profile.editprofile.di.provider

import com.rheotv.android.ui.activities.profile.editprofile.di.module.OnlinePresenceFragmentModule
import com.rheotv.android.ui.activities.profile.editprofile.view.OnlinePresenceFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OnlinePresenceFragmentProvider {

    @ContributesAndroidInjector(modules = [OnlinePresenceFragmentModule::class])
    abstract fun providesOnlinePresenceFragmentFactory(): OnlinePresenceFragment

}
