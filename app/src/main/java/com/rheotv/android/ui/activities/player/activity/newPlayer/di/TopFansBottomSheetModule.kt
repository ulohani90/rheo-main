package com.rheotv.android.ui.activities.player.activity.newPlayer.di

import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.player.activity.newPlayer.TopFansViewModel
import com.rheotv.android.ui.activities.player.activity.newPlayer.adapter.TopFansRecyclerAdapter
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class TopFansBottomSheetModule {

    @Provides
    fun adapter(): TopFansRecyclerAdapter = TopFansRecyclerAdapter()

    @Provides
    fun viewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider): TopFansViewModel =
            TopFansViewModel(dataManager, schedulerProvider)

    @Provides
    fun viewModelFactory(viewModel: TopFansViewModel): ViewModelProvider.Factory? {
        return ViewModelProviderFactory(viewModel)
    }
}