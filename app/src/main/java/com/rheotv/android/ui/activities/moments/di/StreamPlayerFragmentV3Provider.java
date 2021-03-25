package com.rheotv.android.ui.activities.moments.di;

import com.rheotv.android.ui.activities.moments.view.fragments.StreamPlayerFragmentV3;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class StreamPlayerFragmentV3Provider {
    @ContributesAndroidInjector(modules = StreamPlayerFragmentV3Module.class)
    abstract StreamPlayerFragmentV3 streamPlayerFragmentV3();

}
