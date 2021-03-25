package com.rheotv.android.ui.activities.tabcontainer.profile.videos;


import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class VideosFragmentProvider {
    @ContributesAndroidInjector(modules = VideosFragmentModule.class)
    abstract VideosFragment provideVideosBioFragment();
}
