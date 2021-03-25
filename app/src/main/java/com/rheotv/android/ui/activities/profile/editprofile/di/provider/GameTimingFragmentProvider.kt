package com.rheotv.android.ui.activities.profile.editprofile.di.provider

import com.rheotv.android.ui.activities.profile.editprofile.di.module.GameTimingFragmentModule
import com.rheotv.android.ui.activities.profile.editprofile.view.GameTimingFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GameTimingFragmentProvider {

    @ContributesAndroidInjector(modules = [GameTimingFragmentModule::class])
    abstract fun providesGameTimingFragmentFragmentFactory(): GameTimingFragment

}
