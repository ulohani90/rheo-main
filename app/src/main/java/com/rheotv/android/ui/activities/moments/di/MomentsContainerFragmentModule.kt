package com.rheotv.android.ui.activities.moments.di

import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.moments.viewmodel.MomentsContainerViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class MomentsContainerFragmentModule {

    @Provides
    fun provideMomentsContainerViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider) =
            MomentsContainerViewModel(dataManager, schedulerProvider)

    @Provides
    fun provideViewModelFactory(viewModel: MomentsContainerViewModel): ViewModelProvider.Factory =
            ViewModelProviderFactory(viewModel)
}