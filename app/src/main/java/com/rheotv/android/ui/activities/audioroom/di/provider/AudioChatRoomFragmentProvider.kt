package com.rheotv.android.ui.activities.audioroom.di.provider

import com.rheotv.android.ui.activities.audioroom.di.AudioChatRoomFragmentModule
import com.rheotv.android.ui.activities.audioroom.view.AudioChatRoomFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AudioChatRoomFragmentProvider {

    @ContributesAndroidInjector(modules = [AudioChatRoomFragmentModule::class])
    abstract fun provideAudioChatRoomFragment(): AudioChatRoomFragment
}