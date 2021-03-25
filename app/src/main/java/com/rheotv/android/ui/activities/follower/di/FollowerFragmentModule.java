package com.rheotv.android.ui.activities.follower.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.follower.FollowAdapter;
import com.rheotv.android.ui.activities.follower.FollowerViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class FollowerFragmentModule {

    @Provides
    FollowerViewModel viewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new FollowerViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(FollowerViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }

    @Provides
    FollowAdapter prvodeFollowAdapter() {
        return new FollowAdapter();
    }

}
