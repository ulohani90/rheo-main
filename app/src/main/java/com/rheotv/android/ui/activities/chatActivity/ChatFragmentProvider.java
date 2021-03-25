package com.rheotv.android.ui.activities.chatActivity;

import com.rheotv.android.ui.activities.universalActivity.fragment.UniversalFragment;
import com.rheotv.android.ui.activities.universalActivity.fragment.UniversalFragmentModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ChatFragmentProvider {
    @ContributesAndroidInjector(modules = ChatFragmentModule.class)
    abstract ChatFragment chatFragment();
}