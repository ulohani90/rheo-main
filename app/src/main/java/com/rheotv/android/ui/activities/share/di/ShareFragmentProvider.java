package com.rheotv.android.ui.activities.share.di;

import com.rheotv.android.ui.activities.share.ShareGifFragment;
import com.rheotv.android.ui.activities.share.SharePictureFragment;
import com.rheotv.android.ui.activities.share.ShareVideoFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ShareFragmentProvider {

    @ContributesAndroidInjector(modules = ShareFragmentModule.class)
    abstract ShareGifFragment sharedFragment();

    @ContributesAndroidInjector(modules = ShareFragmentModule.class)
    abstract SharePictureFragment sharePictureFragment();

    @ContributesAndroidInjector(modules = ShareFragmentModule.class)
    abstract ShareVideoFragment shareVideoFragment();

}