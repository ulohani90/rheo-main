package com.rheotv.android.ui.activities.profile.di

import com.rheotv.android.ui.activities.profile.view.ProfileContainerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ProfileContainerFragmentProvider {

    @ContributesAndroidInjector(modules = [ProfileContainerFragmentModule::class])
    abstract fun provideProfileContainerFragment(): ProfileContainerFragment?
}