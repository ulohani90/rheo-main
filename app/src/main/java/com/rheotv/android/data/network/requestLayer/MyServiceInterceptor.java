/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 11:45 PM
 *
 */

package com.rheotv.android.data.network.requestLayer;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.rheotv.android.BuildConfig;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.SharedPrefsUtils;

import java.io.IOException;

import javax.inject.Singleton;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/*
  Set the authorization token
  when it is available.
 */

@Singleton
public class MyServiceInterceptor implements Interceptor {
    private String sessionToken;
    private Context context;
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    public MyServiceInterceptor(Context context) {
        this.context = context;
        sessionToken = sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.AUTH_TOKEN);
        if (sessionToken == null) {
            sessionToken = "";
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        HttpUrl originalHttpUrl = original.url();
        HttpUrl url = originalHttpUrl.newBuilder()
                .build();

        Log.i(getClass().getSimpleName(), "intercept_url: " + originalHttpUrl.url().toString() + " and " + CommonUtils.getDevId(context) + " and " + sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.AUTH_TOKEN));
        String userName = CommonUtils.getUserName(context);
        try {
            FirebaseCrashlytics.getInstance().setUserId(userName);
            FirebaseCrashlytics.getInstance().setCustomKey("username", userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Headers headers = new Headers.Builder()
                .add(AppConstants.DEVICE_ID, CommonUtils.getDevId(context))
                .add(AppConstants.CLIENT_ID, BuildConfig.CLIENT_ID)
                .addUnsafeNonAscii(AppConstants.USER_NAME, userName.trim())
                .add(AppConstants.CLIENT_SECRET, BuildConfig.CLIENT_SECRET)
                .add(AppConstants.AUTHORIZATION, sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.AUTH_TOKEN) == null ? "" : "Bearer " + sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.AUTH_TOKEN))
                .add(AppConstants.VERSION_NAME, BuildConfig.VERSION_NAME)
                .add(AppConstants.VERSION_CODE, Integer.toString(BuildConfig.VERSION_CODE))
                .add(AppConstants.CONNECTION_QUALITY, NetworkUtils.getNetworkGeneration(RheoTvApp.getNonUiContext()))
                .add(AppConstants.NETWORK_TYPE, NetworkUtils.getNetworkType(RheoTvApp.getNonUiContext()))
                .add(AppConstants.APP_NAME_KEY, "com.android.rheotv")
                .build();
        Request.Builder requestBuilder = original.newBuilder()
                .headers(headers)
                .url(url);
        Request request = requestBuilder.build();
        Response response = chain.proceed(request);

//        Log.i(getClass().getSimpleName(), "intercept_url: " +
//                " userName: " + userName.trim() + " and " +
//                " Client-id: " + BuildConfig.CLIENT_ID + " and " +
//                " Device-id: " + CommonUtils.getDevId(context) + " and " +
//                originalHttpUrl.url().toString() + " and " +
//                CommonUtils.getDevId(context) + " and " +
//                sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.AUTH_TOKEN));

//        Log.i(getClass().getName(),"NetworkRequest_url " + request.url().toString() + " and body " + (request.body() != null ? new Gson().toJson(request.body()) : "") + " auth: " + sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.AUTH_TOKEN));

        if (response.isSuccessful()) {
            return response;
        }

//        Log.i(getClass().getName(), "intercept_called: " + sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.AUTH_TOKEN) + " and url " + response.request().url() + " body " + response.request().body());
        return chain.proceed(request);
    }
}