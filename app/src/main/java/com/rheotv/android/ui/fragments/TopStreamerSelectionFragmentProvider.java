package com.rheotv.android.ui.fragments;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class TopStreamerSelectionFragmentProvider {

    @ContributesAndroidInjector(modules = TopStreamerSelectionFragmentModule.class)
    abstract TopStreamerSelectionFragment topStreamerSelectionFragment();
}
