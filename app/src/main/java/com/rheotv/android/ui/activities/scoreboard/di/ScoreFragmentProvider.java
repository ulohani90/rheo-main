package com.rheotv.android.ui.activities.scoreboard.di;

import com.rheotv.android.ui.activities.scoreboard.ScoreFragment;
import com.rheotv.android.ui.activities.scoreboard.ScoreBoardDialogFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract public class ScoreFragmentProvider {

    @ContributesAndroidInjector(modules = ScoreFragmentModule.class)
    abstract ScoreFragment scoreActivityModule();

    @ContributesAndroidInjector
    abstract ScoreBoardDialogFragment scoreDialogModule();
}
