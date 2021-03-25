package com.rheotv.android.ui.activities.tabcontainer.profile.wallet

import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class WalletDetailsInputActivityModule {

    @Provides
    fun provideViewModelFactory(viewModel: WalletDetailsInputActivityViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

    @Provides
    fun provideViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider):
            WalletDetailsInputActivityViewModel {
        return WalletDetailsInputActivityViewModel(dataManager, schedulerProvider)
    }
}