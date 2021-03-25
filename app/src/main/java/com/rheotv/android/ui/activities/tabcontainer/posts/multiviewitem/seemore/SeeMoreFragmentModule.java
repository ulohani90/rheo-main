package com.rheotv.android.ui.activities.tabcontainer.posts.multiviewitem.seemore;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class SeeMoreFragmentModule {

    @Provides
    SeeMoreViewModel seeMoreViewModel(DataManager dataManager,
                                      SchedulerProvider schedulerProvider) {
        return new SeeMoreViewModel(dataManager, schedulerProvider);
    }
}
