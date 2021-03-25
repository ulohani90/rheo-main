package com.rheotv.android.ui.activities.scoreboard.di;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.scoreboard.ScoreViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class ScoreFragmentModule {
    @Provides
    ScoreViewModel provideScoreViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new ScoreViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideViewModelFactory(ViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
