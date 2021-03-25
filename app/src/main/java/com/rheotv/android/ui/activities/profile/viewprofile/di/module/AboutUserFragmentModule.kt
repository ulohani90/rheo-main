package com.rheotv.android.ui.activities.profile.viewprofile.di.module

import androidx.lifecycle.ViewModelProvider

import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.FeaturedPhotoAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.GameRuleAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.GameWiseUserAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.OnlinePresenceAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.viewmodel.AboutUserViewModel
import com.rheotv.android.utils.rx.SchedulerProvider

import dagger.Module
import dagger.Provides

@Module
class AboutUserFragmentModule {

    @Provides
    internal fun profileViewModel(dataManager: DataManager,
                                  schedulerProvider: SchedulerProvider) =
            UserProfileViewModel(dataManager, schedulerProvider)

    @Provides
    internal fun provideProfileViewModel(viewModel: UserProfileViewModel): ViewModelProvider.Factory =
            ViewModelProviderFactory(viewModel)

    @Provides
    internal fun provideFeaturedPhotoAdapter() = FeaturedPhotoAdapter()

    @Provides
    internal fun provideOnlinePresenceAdapter() = OnlinePresenceAdapter()

    @Provides
    internal fun provideGameWiseUserAdapter() = GameWiseUserAdapter()

    @Provides
    internal fun provideGameRuleAdapter() = GameRuleAdapter()

}
