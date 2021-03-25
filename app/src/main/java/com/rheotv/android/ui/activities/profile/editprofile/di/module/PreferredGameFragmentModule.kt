package com.rheotv.android.ui.activities.profile.editprofile.di.module

import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.GameSelectionAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.PictureAdapter
import com.rheotv.android.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class PreferredGameFragmentModule {
    @Provides
    fun provideViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider) =
            UserProfileViewModel(dataManager, schedulerProvider)

    @Provides
    fun provideViewModelFactory(viewModel: UserProfileViewModel): ViewModelProvider.Factory =
            ViewModelProviderFactory(viewModel)


    @Provides
    fun provideGameSelectionAdapter(): GameSelectionAdapter {
        return GameSelectionAdapter()
    }
}
