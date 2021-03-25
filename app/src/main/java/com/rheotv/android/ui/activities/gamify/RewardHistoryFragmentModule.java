package com.rheotv.android.ui.activities.gamify;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class RewardHistoryFragmentModule {
    @Provides
    RewardHistoryViewModel rewardHistoryViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new RewardHistoryViewModel(dataManager, schedulerProvider);
    }

    @Provides
    RewardHistoryAdapter rewardHistoryAdapter() {
        return new RewardHistoryAdapter(new ArrayList<>());
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(RewardHistoryViewModel rewardHistoryViewModel) {
        return new ViewModelProviderFactory<>(rewardHistoryViewModel);
    }
}
