package com.rheotv.android.ui.activities.search;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivityVM;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class SearchActivityModule {
    @Provides
    UniversalActivityVM searchActivityVM(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new UniversalActivityVM(dataManager, schedulerProvider);
    }
}
