package com.rheotv.android.ui.activities.leaderboard;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.activities.player.activity.PlayerViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class LeaderBoardActivityModule {
    @Provides
    LeaderBoardActivityVM provideLeaderboardViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new LeaderBoardActivityVM(dataManager, schedulerProvider);
    }
}
