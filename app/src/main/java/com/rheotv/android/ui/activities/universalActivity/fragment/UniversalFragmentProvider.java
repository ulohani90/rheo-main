package com.rheotv.android.ui.activities.universalActivity.fragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class UniversalFragmentProvider {
    @ContributesAndroidInjector(modules = UniversalFragmentModule.class)
    abstract UniversalFragment universalFragment();
}
