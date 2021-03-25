package com.rheotv.android.ui.activities.leaderboard;


import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class LeaderBoardFragmentModule {
    @Provides
    LeaderboardViewModel profileViewModel(DataManager dataManager,
                                          SchedulerProvider schedulerProvider) {
        return new LeaderboardViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideProfileViewModel(LeaderboardViewModel leaderboardViewModel) {
        return new ViewModelProviderFactory<>(leaderboardViewModel);
    }

    @Provides
    LeaderboardListAdapter provideBlogAdapter() {
        return new LeaderboardListAdapter(new ArrayList<>());
    }
}
