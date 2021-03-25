package com.rheotv.android.ui.activities.profile.editprofile.di.provider

import com.rheotv.android.ui.activities.profile.editprofile.di.module.DonationEditFragmentModule
import com.rheotv.android.ui.activities.profile.editprofile.view.DonationEditFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class DonationEditFragmentProvider {

    @ContributesAndroidInjector(modules = [DonationEditFragmentModule::class])
    abstract fun provideDownloadEditFragment(): DonationEditFragment
}