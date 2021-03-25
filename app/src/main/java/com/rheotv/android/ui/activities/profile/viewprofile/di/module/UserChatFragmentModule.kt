package com.rheotv.android.ui.activities.profile.viewprofile.di.module

import androidx.lifecycle.ViewModelProvider

import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ViewModelProviderFactory
import com.rheotv.android.ui.activities.profile.viewprofile.viewmodel.UserChatViewModel
import com.rheotv.android.ui.adapters.ChatListAdapter
import com.rheotv.android.utils.rx.SchedulerProvider

import dagger.Module
import dagger.Provides

@Module
class UserChatFragmentModule {
    @Provides
    internal fun profileViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider) =
            UserChatViewModel(dataManager, schedulerProvider)

    @Provides
    internal fun provideProfileViewModel(viewModel: UserChatViewModel): ViewModelProvider.Factory =
            ViewModelProviderFactory(viewModel)

    @Provides
    internal fun provideChatListAdapter() = ChatListAdapter()
}
