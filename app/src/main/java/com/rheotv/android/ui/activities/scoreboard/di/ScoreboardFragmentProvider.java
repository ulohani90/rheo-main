package com.rheotv.android.ui.activities.scoreboard.di;

import com.rheotv.android.ui.activities.scoreboard.PastMatchFragment;
import com.rheotv.android.ui.activities.scoreboard.ScoreboardFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ScoreboardFragmentProvider {
    @ContributesAndroidInjector(modules = ScoreboardFragmentModule.class)
    abstract ScoreboardFragment scoreboardFragment();

    @ContributesAndroidInjector(modules = PastMatchFragmentModule.class)
    abstract PastMatchFragment pastMatchFragment();
}
