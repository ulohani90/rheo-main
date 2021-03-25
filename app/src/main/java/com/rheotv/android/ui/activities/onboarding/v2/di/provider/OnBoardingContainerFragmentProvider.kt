package com.rheotv.android.ui.activities.onboarding.v2.di.provider

import com.rheotv.android.ui.activities.onboarding.v2.di.module.OnBoardingContainerFragmentModule
import com.rheotv.android.ui.activities.onboarding.v2.view.fragment.OnBoardingContainerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OnBoardingContainerFragmentProvider {

    @ContributesAndroidInjector(modules = [OnBoardingContainerFragmentModule::class])
    abstract fun onBoardingContainerFragment(): OnBoardingContainerFragment
}