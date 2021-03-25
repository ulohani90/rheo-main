package com.rheotv.android.ui.activities.selectGame.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.selectGame.GameSelectionAdapter;
import com.rheotv.android.ui.activities.selectGame.GameSelectionViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class GameSelectionFragmentModule {
    @Provides
    GameSelectionViewModel rewardHistoryViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new GameSelectionViewModel(dataManager, schedulerProvider);
    }

    @Provides
    GameSelectionAdapter gameSelectionAdapter() {
        return new GameSelectionAdapter(new ArrayList<>());
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(GameSelectionViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
