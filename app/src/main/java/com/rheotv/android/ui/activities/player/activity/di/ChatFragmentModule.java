package com.rheotv.android.ui.activities.player.activity.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.player.activity.ChatViewModel;
import com.rheotv.android.ui.adapters.ChatListAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatFragmentModule {
    @Provides
    ChatViewModel chatViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new ChatViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideChatViewModel(ChatViewModel chatViewModel) {
        return new ViewModelProviderFactory<>(chatViewModel);
    }
}
