package com.rheotv.android.ui.activities.tabcontainer.profile.bio;


import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class BioFragmentProvider {
    @ContributesAndroidInjector(modules = BioFragmentModule.class)
    abstract BioFragment provideBioFragment();
}