package com.rheotv.android.ui.activities.player.activity;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class PlayerGiftBottomSheetModule {
    @Provides
    VideoRewardViewModel providesVideoUploadViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new VideoRewardViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideVideoUploadViewModel(VideoRewardViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
