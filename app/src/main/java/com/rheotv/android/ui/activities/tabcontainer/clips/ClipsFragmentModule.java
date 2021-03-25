package com.rheotv.android.ui.activities.tabcontainer.clips;


import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.tabcontainer.trending.TrendingViewModel;
import com.rheotv.android.ui.adapters.ClipsListAdapter;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class ClipsFragmentModule {

    @Provides
    ClipsFragmentViewModel blogViewModel(DataManager dataManager,
                                         SchedulerProvider schedulerProvider) {
        return new ClipsFragmentViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ClipsListAdapter providesClipsListAdapter() {
        return new ClipsListAdapter();
    }

    @Provides
    ViewModelProvider.Factory provideClipsFragmentViewModel(ClipsFragmentViewModel clipsFragmentViewModel) {
        return new ViewModelProviderFactory<>(clipsFragmentViewModel);
    }
}
