package com.rheotv.android.ui.activities.player.activity.newPlayer.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.player.activity.newPlayer.StreamPlayerViewModelV2;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class StreamChatFragmentV2Module {
    @Provides
    StreamPlayerViewModelV2 chatViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new StreamPlayerViewModelV2(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideChatViewModel(StreamPlayerViewModelV2 chatViewModel) {
        return new ViewModelProviderFactory<>(chatViewModel);
    }
}
