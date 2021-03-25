package com.rheotv.android.ui.activities.search.fragment;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class SearchFragmentModule {
    @Provides
    SearchFragmentViewModel searchFragmentViewModel(DataManager dataManager,
                                                    SchedulerProvider schedulerProvider) {
        return new SearchFragmentViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideSearchViewModel(SearchFragmentViewModel searchFragmentViewModel) {
        return new ViewModelProviderFactory<>(searchFragmentViewModel);
    }

    @Provides
    LinearLayoutManager provideLinearLayoutManager(SearchFragment fragment) {
        return new LinearLayoutManager(fragment.getActivity());
    }
}
