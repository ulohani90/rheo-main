package com.rheotv.android.ui.fragments;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class LiveStreamProvider {
    @ContributesAndroidInjector(modules = LiveStreamFragmentModule.class)
    abstract LiveStreamingDialogFragment provideLiveStreamingDialogFragment();
}
