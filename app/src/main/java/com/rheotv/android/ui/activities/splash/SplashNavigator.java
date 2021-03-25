/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 2:41 PM
 *
 */

package com.rheotv.android.ui.activities.splash;

import android.app.Activity;

import com.rheotv.android.data.network.models.postlisting.responses.Result;

/**
 * Created by amitshekhar on 08/07/17.
 */

public interface SplashNavigator {

    void openPlayStoreLink();

    void openMainActivity();

    void handleError(Throwable throwable);

    void renderHomePage(boolean showUpdateMsg);

    void showUpdateOptions();

    void isDataLoaded();

    void showForceUpdateDialog();

    void showCompetionPage(Result result);

    void onNetworkRequestComplete();

    Activity getCallingActivityInstance();

    void setupAnalyticsEvents();
}
