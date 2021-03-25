package com.rheotv.android.ui.activities.profile.viewprofile.di.module

import androidx.lifecycle.ViewModelProvider

import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.objects.FeedObject
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.adapters.PostListAdapter
import com.rheotv.android.utils.rx.SchedulerProvider

import java.util.ArrayList

import dagger.Module
import dagger.Provides

@Module
class UserProfileFragmentModule {
    @Provides
    internal fun profileViewModel(dataManager: DataManager,
                                  schedulerProvider: SchedulerProvider): UserProfileViewModel {
        return UserProfileViewModel(dataManager, schedulerProvider)
    }

    @Provides
    internal fun provideProfileViewModel(profileViewModel: UserProfileViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(profileViewModel)
    }

    @Provides
    internal fun provideProfileFragmentAdapter(): PostListAdapter {
        return PostListAdapter(ArrayList<FeedObject>())
    }
}
