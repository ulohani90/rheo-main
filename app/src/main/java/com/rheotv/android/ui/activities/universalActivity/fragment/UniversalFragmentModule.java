package com.rheotv.android.ui.activities.universalActivity.fragment;


import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class UniversalFragmentModule {
    @Provides
    UniversalFragmentViewModel universalFragmentViewModel(DataManager dataManager,
                                    SchedulerProvider schedulerProvider) {
        return new UniversalFragmentViewModel(dataManager, schedulerProvider);
    }

    @Provides
    UniversalFragmentListAdapter universalFragmentListAdapter() {
        return new UniversalFragmentListAdapter(new ArrayList<>());
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(UniversalFragmentViewModel universalFragmentViewModel) {
        return new ViewModelProviderFactory<>(universalFragmentViewModel);
    }

    @Provides
    LinearLayoutManager provideLinearLayoutManager(UniversalFragment fragment) {
        return new LinearLayoutManager(fragment.getActivity());
    }
}
