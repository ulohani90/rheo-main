package com.rheotv.android.ui.activities.onboarding.v2.di.module

import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.onboarding.v2.viewmodel.OnBoardingViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class OnBoardingLanguageFragmentModule {

    @Provides
    fun provideViewModel(
            dataManager: DataManager,
            schedulerProvider: SchedulerProvider
    ): OnBoardingViewModel = OnBoardingViewModel(dataManager, schedulerProvider)

    @Provides
    fun provideViewModelFactory(viewModel: OnBoardingViewModel): ViewModelProvider.Factory =
            ViewModelProviderFactory(viewModel)

}