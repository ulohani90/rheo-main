package com.rheotv.android.ui.activities.player.activity.di

import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class StreamPlayerActivityProvider {

    @ContributesAndroidInjector(modules = [StreamPlayerContainerModule::class])
    abstract fun bindPlayerPage(): StreamPlayerContainerFragment
}