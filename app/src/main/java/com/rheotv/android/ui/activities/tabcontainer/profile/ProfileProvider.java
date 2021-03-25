package com.rheotv.android.ui.activities.tabcontainer.profile;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ProfileProvider {
    @ContributesAndroidInjector(modules = ProfileModule.class)
    abstract ProfileFragment provideProfileFragmentFactory();
}

