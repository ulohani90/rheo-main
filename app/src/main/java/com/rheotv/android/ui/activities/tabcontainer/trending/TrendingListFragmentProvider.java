package com.rheotv.android.ui.activities.tabcontainer.trending;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class TrendingListFragmentProvider {

    @ContributesAndroidInjector(modules = TrendingListFragmentModule.class)
    abstract TrendingListFragment provideBlogFragmentFactory();
}
