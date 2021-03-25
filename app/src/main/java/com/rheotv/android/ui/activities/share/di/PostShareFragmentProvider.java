package com.rheotv.android.ui.activities.share.di;

import com.rheotv.android.ui.activities.share.PostShareBottomSheetFragment;
import com.rheotv.android.ui.activities.share.ShareGifFragment;
import com.rheotv.android.ui.activities.share.SharePictureFragment;
import com.rheotv.android.ui.activities.share.ShareVideoFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class PostShareFragmentProvider {

    @ContributesAndroidInjector(modules = ShareActivityModule.class)
    abstract PostShareBottomSheetFragment shareActivity();

}