package com.rheotv.android.ui.activities.scoreboard.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.scoreboard.ScoreboardViewModel;
import com.rheotv.android.ui.activities.scoreboard.adapter.ScoreboardTeamAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class ScoreboardFragmentModule {

    @Provides
    ScoreboardViewModel viewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new ScoreboardViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideScoreboardViewModel(ScoreboardViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }

    @Provides
    ScoreboardTeamAdapter provideAdapter() {
        return new ScoreboardTeamAdapter(new ArrayList<>());
    }

}
