package com.rheotv.android.ui.activities.tabcontainer.profile.container;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ProfileContainerProvider {
    @ContributesAndroidInjector(modules = {ProfileContainerModule.class})
    abstract ProfileContainerFragment provideProfileContainerFragmentFactory();
}

