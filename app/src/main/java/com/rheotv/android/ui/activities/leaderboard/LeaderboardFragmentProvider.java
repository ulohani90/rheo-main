package com.rheotv.android.ui.activities.leaderboard;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class LeaderboardFragmentProvider {
    @ContributesAndroidInjector(modules = LeaderBoardFragmentModule.class)
    abstract LeaderBoardFragment proLeaderBoardFragment();
}

