package com.rheotv.android.ui.activities.player.activity.di;

import com.rheotv.android.ui.activities.player.activity.ChatListFragment;
import com.rheotv.android.ui.activities.player.activity.RequestPlayFragment;
import com.rheotv.android.ui.activities.player.activity.VideoRewardFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class PlayerFragmentProvider {

    @ContributesAndroidInjector(modules = ChatFragmentModule.class)
    abstract ChatListFragment chatListFragment();

    @ContributesAndroidInjector(modules = RequestPlayFragmentModule.class)
    abstract RequestPlayFragment requestPlayFragment();

    @ContributesAndroidInjector(modules = VideoRewardFragmentModule.class)
    abstract VideoRewardFragment videoRewardFragment();

}
