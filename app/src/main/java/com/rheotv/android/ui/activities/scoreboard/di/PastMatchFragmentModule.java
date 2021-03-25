package com.rheotv.android.ui.activities.scoreboard.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.scoreboard.PastMatchViewModel;
import com.rheotv.android.ui.activities.scoreboard.adapter.PastMatchAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class PastMatchFragmentModule {

    @Provides
    PastMatchViewModel viewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new PastMatchViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory providePastMatchViewModel(PastMatchViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }

    @Provides
    PastMatchAdapter provideAdapter() {
        return new PastMatchAdapter(new ArrayList<>());
    }

}
