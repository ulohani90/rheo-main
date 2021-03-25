package com.rheotv.android.ui.activities.tabcontainer.profile.wallet;

import com.rheotv.android.ui.activities.tabcontainer.profile.container.ProfileContainerModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class WalletFragmentV2Provider {
    @ContributesAndroidInjector(modules = ProfileContainerModule.class)
    abstract WalletFragmentV2 providesWalletFragmentV2Factory();
}
