package com.rheotv.android.ui.activities.profile.editprofile.di.provider

import com.rheotv.android.ui.activities.profile.editprofile.di.module.ProfileEditFragmentModule
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ProfileEditFragmentProvider {

    @ContributesAndroidInjector(modules = [ProfileEditFragmentModule::class])
    abstract fun providesProfileDetailFragmentFactory(): ProfileEditFragment

}
