package com.rheotv.android.ui.activities.gamify.di;

import com.rheotv.android.ui.activities.gamify.RedeemDetailFragment;
import com.rheotv.android.ui.activities.gamify.RedeemSummaryFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class RedeemFragmentProvider {
    @ContributesAndroidInjector(modules = RedeemDetailFragmentModule.class)
    abstract RedeemDetailFragment redeemDetailFragment();

    @ContributesAndroidInjector(modules = RedeemSummaryFragmentModule.class)
    abstract RedeemSummaryFragment redeemSummaryFragment();
}
