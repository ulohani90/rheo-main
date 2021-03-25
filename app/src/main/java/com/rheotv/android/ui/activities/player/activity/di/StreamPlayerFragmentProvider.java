package com.rheotv.android.ui.activities.player.activity.di;

import com.rheotv.android.ui.activities.player.activity.StreamChatFragment;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerFragment;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerFragmentModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class StreamPlayerFragmentProvider {

    @ContributesAndroidInjector(modules = StreamPlayerFragmentModule.class)
    abstract StreamPlayerFragment streamPlayerFragment();

    @ContributesAndroidInjector(modules = StreamChatFragmentModule.class)
    abstract StreamChatFragment chatFragment();
}