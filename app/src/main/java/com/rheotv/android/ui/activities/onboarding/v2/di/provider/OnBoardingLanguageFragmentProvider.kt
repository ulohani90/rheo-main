package com.rheotv.android.ui.activities.onboarding.v2.di.provider

import com.rheotv.android.ui.activities.onboarding.v2.di.module.OnBoardingLanguageFragmentModule
import com.rheotv.android.ui.activities.onboarding.v2.view.fragment.OnBoardingContainerFragment
import com.rheotv.android.ui.activities.onboarding.v2.view.fragment.OnBoardingLanguageFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OnBoardingLanguageFragmentProvider {

    @ContributesAndroidInjector(modules = [OnBoardingLanguageFragmentModule::class])
    abstract fun onBoardingLanguageFragment(): OnBoardingLanguageFragment
}