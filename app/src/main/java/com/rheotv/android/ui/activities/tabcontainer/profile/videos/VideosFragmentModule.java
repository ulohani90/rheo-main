package com.rheotv.android.ui.activities.tabcontainer.profile.videos;


import android.content.Context;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.universalActivity.fragment.UniversalFragment;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class VideosFragmentModule {
    @Provides
    VideosFragmentViewModel videosFragmentViewModel(DataManager dataManager,
                                                    SchedulerProvider schedulerProvider) {
        return new VideosFragmentViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideVideosFragmentViewModel(VideosFragmentViewModel videosFragmentViewModel) {
        return new ViewModelProviderFactory<>(videosFragmentViewModel);
    }


    @Provides
    VideoFragmentAdapter videoFragmentAdapter(Context context) {
        return new VideoFragmentAdapter(context, new ArrayList<>());
    }

    @Provides
    LinearLayoutManager provideLinearLayoutManager(UniversalFragment fragment) {
        return new LinearLayoutManager(fragment.getActivity());
    }
}
