package com.rheotv.android.ui.activities.profile.viewprofile.di.provider

import com.rheotv.android.ui.activities.profile.viewprofile.di.module.UserWalletFragmentModule
import com.rheotv.android.ui.activities.profile.viewprofile.view.UserWalletFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UserWalletFragmentProvider {

    @ContributesAndroidInjector(modules = [UserWalletFragmentModule::class])
    internal abstract fun providesUserWalletFragmentFactory(): UserWalletFragment

}
