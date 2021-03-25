
/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 2:44 PM
 *
 */

package com.rheotv.android.di.component;

import android.app.Application;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.di.builder.ActivityBuilder;
import com.rheotv.android.di.module.AppAssistedModule;
import com.rheotv.android.di.module.AppModule;
import com.rheotv.android.di.module.AppWorkerModule;
import com.rheotv.android.factories.AppWorkerFactory;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;


@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        AppModule.class,
        ActivityBuilder.class,
        AppAssistedModule.class,
        AppWorkerModule.class
})
public interface AppComponent extends AndroidInjector<RheoTvApp> {

    void inject(RheoTvApp app);

    AppWorkerFactory factory();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }
}
