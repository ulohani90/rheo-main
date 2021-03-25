package com.rheotv.android.ui.activities.player.activity.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.player.activity.VideoRewardViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class VideoRewardFragmentModule {
    @Provides
    VideoRewardViewModel requestToPlatViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new VideoRewardViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(VideoRewardViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}

