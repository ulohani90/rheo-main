package com.rheotv.android.ui.activities.player.activity.di;

import com.rheotv.android.ui.activities.player.activity.RequestPlayFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class RequestPlayFragmentProvider {

    @ContributesAndroidInjector(modules = RequestPlayFragmentModule.class)
    abstract RequestPlayFragment requestPlayFragment();
}
