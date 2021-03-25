package com.rheotv.android.ui.activities.profile.editprofile.di.provider

import com.rheotv.android.ui.activities.profile.editprofile.di.module.GameWiseUserFragmentModule
import com.rheotv.android.ui.activities.profile.editprofile.view.GameWiseUserFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GameWiseUserFragmentProvider {

    @ContributesAndroidInjector(modules = [GameWiseUserFragmentModule::class])
    abstract fun providesGameWiseUserFragmentFactory(): GameWiseUserFragment

}
