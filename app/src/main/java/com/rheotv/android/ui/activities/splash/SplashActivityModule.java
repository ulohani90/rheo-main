/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 2:41 PM
 *
 */

package com.rheotv.android.ui.activities.splash;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.fragments.LoginViewModel;

import dagger.Module;
import dagger.Provides;
import com.rheotv.android.utils.rx.SchedulerProvider;

@Module
public class SplashActivityModule {

    @Provides
    SplashViewModel provideSplashViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new SplashViewModel(dataManager, schedulerProvider);
    }

    @Provides
    LoginViewModel provideLoginViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new LoginViewModel(dataManager, schedulerProvider);
    }

}
