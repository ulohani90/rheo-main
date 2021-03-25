package com.rheotv.android.ui.activities.universalActivity;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardActivityVM;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class UniversalActivityModule {
    @Provides
    UniversalActivityVM universalActivityVM(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new UniversalActivityVM(dataManager, schedulerProvider);
    }
}
