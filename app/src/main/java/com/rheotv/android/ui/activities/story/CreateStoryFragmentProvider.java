package com.rheotv.android.ui.activities.story;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class CreateStoryFragmentProvider {

    @ContributesAndroidInjector(modules = StoryImageFragmentModule.class)
    abstract StoryImageFragment storyImageFragment();

    @ContributesAndroidInjector(modules = CreateStoryVideoFragmentModule.class)
    abstract CreateStoryVideoFragment storyVideoragment();

    @ContributesAndroidInjector(modules = StoryViewerModule.class)
    abstract StoryViewerBottomDialog storyViewerBottomDialog();

}
