package com.rheotv.android.ui.activities.onboarding.v2.di.provider

import com.rheotv.android.ui.activities.onboarding.v2.di.module.OnBoardingContainerFragmentModule
import com.rheotv.android.ui.activities.onboarding.v2.di.module.OnBoardingLanguageFragmentModule
import com.rheotv.android.ui.activities.onboarding.v2.view.fragment.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OnBoardingFragmentsProvider {

    @ContributesAndroidInjector(modules = [OnBoardingContainerFragmentModule::class])
    abstract fun onBoardingContainerFragment(): OnBoardingContainerFragment

    @ContributesAndroidInjector(modules = [OnBoardingLanguageFragmentModule::class])
    abstract fun onBoardingLanguageFragment(): OnBoardingLanguageFragment

    @ContributesAndroidInjector(modules = [OnBoardingLanguageFragmentModule::class])
    abstract fun onBoardingLoginFragment(): OnBoardingLoginFragment

    @ContributesAndroidInjector(modules = [OnBoardingLanguageFragmentModule::class])
    abstract fun onBoardingUsernameInputFragment(): OnBoardingUsernameInputFragment

    @ContributesAndroidInjector(modules = [OnBoardingLanguageFragmentModule::class])
    abstract fun onBoardingStreamerSelectionFragment(): OnBoardingStreamerSelectionFragment
}