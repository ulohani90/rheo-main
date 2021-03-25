package com.rheotv.android.ui.activities.player.activity.newPlayer.di

import com.rheotv.android.data.DataManager
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.RequestVideoCallViewModel
import com.rheotv.android.utils.AgoraConnectionUtils
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class FullScreenVideoActivityModule {

    @Provides
    fun provideViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider) =
            RequestVideoCallViewModel(dataManager, schedulerProvider)

    @Provides
    fun provideAgoraConnectUtils() = AgoraConnectionUtils()


}