package com.rheotv.android.ui.activities.follower.di;

import com.rheotv.android.ui.activities.follower.FollowerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FollowFragmentProvider {
    @ContributesAndroidInjector(modules = FollowerFragmentModule.class)
    abstract FollowerFragment followerFragment();
}
