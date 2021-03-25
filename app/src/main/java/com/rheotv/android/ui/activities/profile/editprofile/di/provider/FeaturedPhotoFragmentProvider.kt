package com.rheotv.android.ui.activities.profile.editprofile.di.provider

import com.rheotv.android.ui.activities.profile.editprofile.di.module.FeaturedPhotoFragmentModule
import com.rheotv.android.ui.activities.profile.editprofile.di.module.GameRuleFragmentModule
import com.rheotv.android.ui.activities.profile.editprofile.view.FeaturedPhotoFragment
import com.rheotv.android.ui.activities.profile.editprofile.view.GameRuleFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FeaturedPhotoFragmentProvider {

    @ContributesAndroidInjector(modules = [FeaturedPhotoFragmentModule::class])
    abstract fun providesFeaturedPhotoFragmentFactory(): FeaturedPhotoFragment

}
