package com.rheotv.android.ui.activities.streamEnd.di;

import android.content.Context;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.activities.streamEnd.StreamEndViewModel;
import com.rheotv.android.ui.activities.tabcontainer.profile.videos.VideoFragmentAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class StreamEndActivityModule {

    @Provides
    StreamEndViewModel provideStreamEndViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new StreamEndViewModel(dataManager, schedulerProvider);
    }

    @Provides
    VideoFragmentAdapter videoFragmentAdapter(Context context) {
        return new VideoFragmentAdapter(context, new ArrayList<>());
    }
}
