package com.rheotv.android.ui.activities.profile.editprofile.di.provider

import com.rheotv.android.ui.activities.profile.editprofile.di.module.GameRuleFragmentModule
import com.rheotv.android.ui.activities.profile.editprofile.view.GameRuleFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GameRuleFragmentProvider {

    @ContributesAndroidInjector(modules = [GameRuleFragmentModule::class])
    abstract fun providesGameRuleFragmentFactory(): GameRuleFragment

}
