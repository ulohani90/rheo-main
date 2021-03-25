package com.rheotv.android.ui.activities.profile.viewprofile.di.provider

import com.rheotv.android.ui.activities.profile.viewprofile.di.module.UserProfileFragmentModule
import com.rheotv.android.ui.activities.profile.viewprofile.view.UserProfileFragment

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UserProfileFragmentProvider {

    @ContributesAndroidInjector(modules = [UserProfileFragmentModule::class])
    internal abstract fun providesUserProfileFragmentFactory(): UserProfileFragment

}