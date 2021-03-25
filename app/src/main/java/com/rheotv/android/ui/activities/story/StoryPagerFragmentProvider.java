package com.rheotv.android.ui.activities.story;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class StoryPagerFragmentProvider {

    @ContributesAndroidInjector(modules = StoryPagerFragmentModule.class)
    abstract StoryPagerFragment storyPagerFragment();
}
