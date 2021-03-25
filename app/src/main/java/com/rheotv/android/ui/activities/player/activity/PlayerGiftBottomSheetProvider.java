package com.rheotv.android.ui.activities.player.activity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class PlayerGiftBottomSheetProvider {

    @ContributesAndroidInjector(modules = PlayerGiftBottomSheetModule.class)
    abstract PlayerGiftBottomSheet providePlayerGiftBottomSheet();
}
