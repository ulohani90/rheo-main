package com.rheotv.android.ui.activities.profile.viewprofile.di.provider

import com.rheotv.android.ui.activities.profile.viewprofile.di.module.UserChatFragmentModule
import com.rheotv.android.ui.activities.profile.viewprofile.view.UserChatFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UserChatFragmentProvider {

    @ContributesAndroidInjector(modules = [UserChatFragmentModule::class])
    internal abstract fun providesUserChatFragmentFactory(): UserChatFragment

}
