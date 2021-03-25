package com.rheotv.android.ui.activities.player.activity.newPlayer.di

import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.RequestVideoCallViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class VideoChatViewActivityModule {
    @Provides
    fun viewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider): RequestVideoCallViewModel =
            RequestVideoCallViewModel(dataManager, schedulerProvider)

    @Provides
    fun viewModelFactory(viewModel: RequestVideoCallViewModel): ViewModelProvider.Factory? {
        return ViewModelProviderFactory(viewModel)
    }
}