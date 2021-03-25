package com.rheotv.android.ui.fragments;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DownloadVideoFragmentProvider {

    @ContributesAndroidInjector(modules = DownloadVideoFragmentModule.class)
    abstract DownloadVideoFormFragment provideDownloadVideoFragmentFactory();
}
