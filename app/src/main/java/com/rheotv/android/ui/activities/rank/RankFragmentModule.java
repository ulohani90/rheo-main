package com.rheotv.android.ui.activities.rank;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class RankFragmentModule {
    @Provides
    RankFragmentViewModel provideViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new RankFragmentViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideViewModelFactory(RankFragmentViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
