package com.rheotv.android.ui.activities.player.activity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class StickerBottomSheetProvider {

    @ContributesAndroidInjector(modules = StickerBottomSheetModule.class)
    abstract StickerBottomSheet provideStickerBottomSheet();
}
