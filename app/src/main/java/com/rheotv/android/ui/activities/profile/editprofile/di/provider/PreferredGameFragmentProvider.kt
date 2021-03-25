package com.rheotv.android.ui.activities.profile.editprofile.di.provider

import com.rheotv.android.ui.activities.profile.editprofile.di.module.PreferredGameFragmentModule
import com.rheotv.android.ui.activities.profile.editprofile.view.PreferredGameFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PreferredGameFragmentProvider {

    @ContributesAndroidInjector(modules = [PreferredGameFragmentModule::class])
    abstract fun providesPreferredGameFragmentFactory(): PreferredGameFragment

}
