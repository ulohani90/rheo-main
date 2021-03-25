package com.rheotv.android.ui.activities.moments.di

import com.rheotv.android.ui.activities.moments.view.fragments.MomentsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MomentsFragmentProvider {

    @ContributesAndroidInjector(modules = [MomentsFragmentModule::class])
    abstract fun provideMomentsFragment(): MomentsFragment
}