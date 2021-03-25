package com.rheotv.android.ui.activities.player.activity.newPlayer.di;

import com.rheotv.android.ui.activities.player.activity.newPlayer.StreamChatFragmentV2;
import com.rheotv.android.ui.activities.player.activity.newPlayer.StreamPlayerFragmentV2;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class StreamPlayerFragmentV2Provider {
    @ContributesAndroidInjector(modules = StreamPlayerFragmentV2Module.class)
    abstract StreamPlayerFragmentV2 streamPlayerFragmentV2();

    @ContributesAndroidInjector(modules = StreamChatFragmentV2Module.class)
    abstract StreamChatFragmentV2 chatFragment();
}
