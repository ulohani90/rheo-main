package com.rheotv.android.ui.activities.selectGame.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.selectGame.LanguageSelectionViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class LanguageSelectionFragmentModule {
    @Provides
    LanguageSelectionViewModel rewardHistoryViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new LanguageSelectionViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(LanguageSelectionViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
