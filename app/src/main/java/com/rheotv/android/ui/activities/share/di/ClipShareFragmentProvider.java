package com.rheotv.android.ui.activities.share.di;

import com.rheotv.android.ui.activities.share.ClipShareBottomSheetFragment;
import com.rheotv.android.ui.activities.share.ShareGifFragment;
import com.rheotv.android.ui.activities.share.SharePictureFragment;
import com.rheotv.android.ui.activities.share.ShareVideoFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ClipShareFragmentProvider {

    @ContributesAndroidInjector(modules = ShareActivityModule.class)
    abstract ClipShareBottomSheetFragment shareActivity();

}