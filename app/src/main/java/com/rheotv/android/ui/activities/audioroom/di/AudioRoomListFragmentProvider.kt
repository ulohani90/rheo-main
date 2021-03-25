package com.rheotv.android.ui.activities.audioroom.di

import com.rheotv.android.ui.activities.audioroom.view.AudioRoomListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
public abstract class AudioRoomListFragmentProvider {

    @ContributesAndroidInjector(modules = [AudioRoomListFragmentModule::class])
    abstract fun audioRoomListFragmentFragment(): AudioRoomListFragment

}