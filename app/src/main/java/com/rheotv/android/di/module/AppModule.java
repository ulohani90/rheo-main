
/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 3:44 PM
 *
 */

package com.rheotv.android.di.module;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.rheotv.android.BuildConfig;
import com.rheotv.android.data.AppDataManager;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.requestLayer.ApiService;
import com.rheotv.android.data.network.requestLayer.EventsApiService;
import com.rheotv.android.data.network.requestLayer.MyServiceInterceptor;
import com.rheotv.android.db.AppDatabase;
import com.rheotv.android.db.ClipDao;
import com.rheotv.android.db.PushNotificationDao;
import com.rheotv.android.di.ApiInfo;
import com.rheotv.android.di.DatabaseInfo;
import com.rheotv.android.di.PreferenceInfo;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.rx.AppSchedulerProvider;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.rheotv.android.db.DbUtilsKt.DATABASE_NAME;


@Module
public class AppModule {

    @Singleton
    @Provides
    public static ApiService provideApiService(OkHttpClient okHttpClient, Gson gson) {
        Retrofit.Builder retrofitBuilder;
        retrofitBuilder = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create());
        return retrofitBuilder.build().create(ApiService.class);
    }

    @Singleton
    @Provides
    public static EventsApiService provideEventsService(OkHttpClient okHttpClient, Gson gson) {
        Retrofit.Builder retrofitBuilder;
        retrofitBuilder = new Retrofit.Builder()
                .baseUrl(BuildConfig.EVENTS_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create());
        return retrofitBuilder.build().create(EventsApiService.class);
    }

    @Provides
    @Singleton
    public static OkHttpClient provideOkhttp(MyServiceInterceptor interceptor, HttpLoggingInterceptor httpLoggingInterceptor, Cache cache) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .cache(cache)
                .addInterceptor(interceptor)
                .addInterceptor(httpLoggingInterceptor);
        return builder.build();
    }


    @Provides
    @Singleton
    public static Cache provideCache(Context context) {
        File cacheFile = new File(context.getCacheDir(), "HttpCache");
        cacheFile.mkdirs();

        return new Cache(cacheFile, 10 * 1000 * 1000); //10 MB
    }

    @Provides
    @Singleton
    public static HttpLoggingInterceptor httpLoggingInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        /*HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Timber.d(message);
            }
        });*/
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }

    @Provides
    @Singleton
    public static MyServiceInterceptor getServiceInterceptor(Context context) {
        return new MyServiceInterceptor(context);
    }

    @Provides
    @ApiInfo
    String provideApiKey() {
        return BuildConfig.API_KEY;
    }

    @Provides
    @Singleton
    Context provideContext(Application application) {
        return application;
    }

    @Provides
    @Singleton
    DataManager provideDataManager(AppDataManager appDataManager) {
        return appDataManager;
    }

    @Provides
    @DatabaseInfo
    String provideDatabaseName() {
        return AppConstants.DB_NAME;
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    @Provides
    @PreferenceInfo
    String providePreferenceName() {
        return AppConstants.PREF_NAME;
    }

    @Provides
    @Singleton
    SharedPrefsUtils providePreferencesHelper() {
        return new SharedPrefsUtils();
    }

    @Provides
    SchedulerProvider provideSchedulerProvider() {
        return new AppSchedulerProvider();
    }

    @Provides
    @Singleton
    AppDatabase provideDatabase(Application application) {
        return Room.databaseBuilder(application, AppDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    ClipDao provideClipDao(AppDatabase database) {
        return database.clipDao();
    }

    @Provides
    PushNotificationDao provideNotificationDao(AppDatabase database) {
        return database.notificationDao();
    }
}
