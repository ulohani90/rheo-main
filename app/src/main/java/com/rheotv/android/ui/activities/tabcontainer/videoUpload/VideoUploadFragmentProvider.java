package com.rheotv.android.ui.activities.tabcontainer.videoUpload;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class VideoUploadFragmentProvider {
    @ContributesAndroidInjector(modules = VideoUploadFragmentModule.class)
    abstract VideoUploadFragment provideVideoUploadFragment();
}
