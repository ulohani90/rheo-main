package com.rheotv.android.ui.activities.gamify;

import com.rheotv.android.ui.fragments.RecentlyRedeemedFragment;
import com.rheotv.android.ui.fragments.RecentlyRedeemedFragmentModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class RewardsFragmentProvider {
    @ContributesAndroidInjector(modules = RewardHistoryFragmentModule.class)
    abstract RewardHistoryFragment rewardHistoryFragment();

    @ContributesAndroidInjector(modules = RewardRedeemFragmentModule.class)
    abstract RewardRedeemFragment rewardRedeemFragment();

    @ContributesAndroidInjector(modules = RewardVoucherFragmentModule.class)
    abstract RewardVoucherFragment rewardVoucherFragment();

    @ContributesAndroidInjector(modules = RewardGiveawayFragmentModule.class)
    abstract RewardGiveawayFragment rewardGiveAwayFragment();

    @ContributesAndroidInjector(modules = RecentlyRedeemedFragmentModule.class)
    abstract RecentlyRedeemedFragment recentlyRedeemedFragment();
}
