package com.rheotv.android.ui.activities.rank;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class RankFragmentProvider {
    @ContributesAndroidInjector(modules = RankFragmentModule.class)
    abstract RankListFragment rankListFragment();
}
