package com.rheotv.android.ui.activities.customroom.di

import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.customroom.adapter.CustomRoomRecyclerAdapter
import com.rheotv.android.ui.activities.customroom.viewmodel.CustomRoomViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class CustomRoomFragmentModule {
    @Provides
    fun provideViewModel(
            dataManager: DataManager,
            schedulerProvider: SchedulerProvider
    ): CustomRoomViewModel {
        return CustomRoomViewModel(dataManager, schedulerProvider)
    }

    @Provides
    fun provideViewModelFactory(viewModel: CustomRoomViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

    @Provides
    fun provideRecyclerAdapter(): CustomRoomRecyclerAdapter = CustomRoomRecyclerAdapter()
}