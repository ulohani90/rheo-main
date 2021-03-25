/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 03/01/19 12:16 AM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class TabContainerActivityModule {

    @Provides
    TabContainerViewModel provideFeedViewModel(DataManager dataManager,
                                               SchedulerProvider schedulerProvider) {
        return new TabContainerViewModel(dataManager, schedulerProvider);
    }

}
