package com.rheotv.android.ui.activities.player.activity.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class StreamChatFragmentModule {
    @Provides
    StreamPlayerViewModel chatViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new StreamPlayerViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideChatViewModel(StreamPlayerViewModel chatViewModel) {
        return new ViewModelProviderFactory<>(chatViewModel);
    }
}
