package com.rheotv.android.ui.activities.player.activity.newPlayer.di

import com.rheotv.android.ui.activities.player.activity.newPlayer.fragments.RequestToVideoCallBottomSheet
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class RequestToVideoCallBottomSheetProvider {

    @ContributesAndroidInjector(modules = [RequestToVideoCallBottomSheetModule::class])
    abstract fun topFansBottomSheet(): RequestToVideoCallBottomSheet
}