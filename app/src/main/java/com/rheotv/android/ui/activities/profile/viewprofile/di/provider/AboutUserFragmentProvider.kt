package com.rheotv.android.ui.activities.profile.viewprofile.di.provider

import com.rheotv.android.ui.activities.profile.viewprofile.di.module.AboutUserFragmentModule
import com.rheotv.android.ui.activities.profile.viewprofile.view.AboutUserFragment

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AboutUserFragmentProvider {

    @ContributesAndroidInjector(modules = [AboutUserFragmentModule::class])
    internal abstract fun providesAboutUserFragmentFactory(): AboutUserFragment

}
