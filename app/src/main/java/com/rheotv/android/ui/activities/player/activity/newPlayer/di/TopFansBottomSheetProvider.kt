package com.rheotv.android.ui.activities.player.activity.newPlayer.di

import com.rheotv.android.ui.activities.player.activity.newPlayer.fragments.TopFansBottomSheet
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TopFansBottomSheetProvider {

    @ContributesAndroidInjector(modules = [TopFansBottomSheetModule::class])
    abstract fun topFansBottomSheet(): TopFansBottomSheet
}