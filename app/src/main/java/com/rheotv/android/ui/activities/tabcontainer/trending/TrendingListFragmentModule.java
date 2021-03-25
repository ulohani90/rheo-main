package com.rheotv.android.ui.activities.tabcontainer.trending;


import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class TrendingListFragmentModule {
    @Provides
    TrendingViewModel blogViewModel(DataManager dataManager,
                                    SchedulerProvider schedulerProvider) {
        return new TrendingViewModel(dataManager, schedulerProvider);
    }

    @Provides
    PostListAdapter provideBlogAdapter() {
        return new PostListAdapter(new ArrayList<>());
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(TrendingViewModel blogViewModel) {
        return new ViewModelProviderFactory<>(blogViewModel);
    }

    @Provides
    LinearLayoutManager provideLinearLayoutManager(TrendingListFragment fragment) {
        return new LinearLayoutManager(fragment.getActivity());
    }
}
