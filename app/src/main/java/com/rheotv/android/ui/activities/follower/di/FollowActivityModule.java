package com.rheotv.android.ui.activities.follower.di;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.activities.follower.FollowViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class FollowActivityModule {
    @Provides
    FollowViewModel provideFollowViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new FollowViewModel(dataManager, schedulerProvider);
    }

}
