package com.rheotv.android.ui.activities.selectGame.di;

import com.rheotv.android.ui.activities.selectGame.GameSelectionFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class GameSelectionFragmentProvider {
    @ContributesAndroidInjector(modules = GameSelectionFragmentModule.class)
    abstract GameSelectionFragment gameSelectionFragment();
}
