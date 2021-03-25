package com.rheotv.android.ui.activities.player.activity;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class StickerBottomSheetModule {

    @Provides
    StickerBottomSheetViewModel provideViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new StickerBottomSheetViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideViewModelFactory(ViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
