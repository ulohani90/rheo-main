package com.rheotv.android.ui.activities.tabcontainer.profile.wallet;

import com.rheotv.android.ui.activities.tabcontainer.profile.container.ProfileContainerModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class WalletFragmentProvider {
    @ContributesAndroidInjector(modules = ProfileContainerModule.class)
    abstract WalletFragment providesWalletFragmentFactory();
}
