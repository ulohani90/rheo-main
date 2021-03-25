package com.rheotv.android.ui.activities.tabcontainer.clips;

import com.rheotv.android.ui.activities.tabcontainer.trending.TrendingListFragment;
import com.rheotv.android.ui.activities.tabcontainer.trending.TrendingListFragmentModule;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ClipsFragmentProvider {

    @ContributesAndroidInjector(modules = ClipsFragmentModule.class)
    abstract ClipsFragment provideClipsFragmentFactory();

}
