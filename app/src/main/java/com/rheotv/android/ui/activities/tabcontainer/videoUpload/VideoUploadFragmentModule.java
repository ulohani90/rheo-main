package com.rheotv.android.ui.activities.tabcontainer.videoUpload;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class VideoUploadFragmentModule {

    @Provides
    VideoUploadViewModel providesVideoUploadViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new VideoUploadViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideVideoUploadViewModel(VideoUploadViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
