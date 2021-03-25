package com.rheotv.android.ui.activities.tabcontainer.profile.analytics;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AnalyticsFragmentProvider {
    @ContributesAndroidInjector(modules = AnalyticsFragmentModule.class)
    abstract AnalyticsFragment provideAnalyticsFragment();
}
