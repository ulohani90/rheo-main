package com.rheotv.android.ui.activities.moments.di

import com.rheotv.android.ui.activities.moments.view.fragments.MomentsContainerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MomentsContainerFragmentProvider {

    @ContributesAndroidInjector(modules = [MomentsContainerFragmentModule::class])
    abstract fun bindPlayerPage(): MomentsContainerFragment
}