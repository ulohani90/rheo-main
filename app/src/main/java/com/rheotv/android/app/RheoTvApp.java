/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 3:15 PM
 *
 */

package com.rheotv.android.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.provider.FontRequest;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.FontRequestEmojiCompatConfig;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.work.Configuration;

import com.amplitude.api.Amplitude;
import com.facebook.appevents.AppEventsLogger;
import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatConfig;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.moengage.core.MoEngage;
import com.rheotv.android.BuildConfig;
import com.rheotv.android.R;
import com.rheotv.android.di.component.AppComponent;
import com.rheotv.android.di.component.DaggerAppComponent;
import com.rheotv.android.factories.AppWorkerFactory;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.FreshchatImageLoader;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.rheotv.android.utils.worker.ClearNotificationWorker;
import com.rheotv.android.utils.worker.ClipSyncWorker;
import com.rheotv.android.utils.worker.OfflineNotificationWorker;
import com.rheotv.android.utils.worker.SyncFcmTokenWorker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.branch.referral.Branch;

public class RheoTvApp extends MultiDexApplication implements HasAndroidInjector, Configuration.Provider,
        LifecycleObserver {

    static Application application;
    @Inject
    DispatchingAndroidInjector<Object> activityDispatchingAndroidInjector;

    //    @Inject
//    private SampleComponent sampleComponent;
    private AppComponent appComponent;
    private Activity currentActivity;

    private boolean downloadInProgress = false;
    public static final String EXOPLAYER_AGENT = "rheotv";
    public static final String TAG = "rheotv";

    SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    private ArrayList<Class> runningActivities = new ArrayList<>();
    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;

    private Handler actionHandler = new Handler();
    private static FirebaseRemoteConfig mFirebaseRemoteConfig;
    private final long REMOTE_CONFIG_FETCH_INTERVAL = 0; // 3600

    public static FirebaseRemoteConfig getFirebaseRemoteConfig() {
        return mFirebaseRemoteConfig;
    }

//    private AppWorkComponent workComponent;

    //    @Inject
//    CalligraphyConfig mCalligraphyConfig;
    public boolean getDownloadStatus() {
        return downloadInProgress;
    }

    public void setDownloadStatus(boolean status) {
        this.downloadInProgress = status;
    }

    public static Context getNonUiContext() {
        return application.getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return activityDispatchingAndroidInjector;
    }

//    @NonNull
//    @Override
//    public Configuration getWorkManagerConfiguration() {
//        SampleWorkerFactory factory = sampleComponent.factory();
//        return new Configuration.Builder().setWorkerFactory(factory).build();
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        // Initialize the Branch object
        Branch.getAutoInstance(this);
        AppEventsLogger.activateApp(this);

        AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext());
        appComponent = DaggerAppComponent.builder()
                .application(this)
                .build();
        appComponent.inject(this);

        configureCrashReporting();

//        postFCMToken();

        sAnalytics = GoogleAnalytics.getInstance(this);
        FirebaseAnalytics.getInstance(this);

        sharedPrefsUtils.setIntegerPreference(getNonUiContext()
                , AppConstants.APP_OPEN_COUNT
                , sharedPrefsUtils.getIntegerPreference(getNonUiContext(), AppConstants.APP_OPEN_COUNT, 0) + 1);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        try {
            Date currentTime = Calendar.getInstance().getTime();
            Date todaysDate = sdf.parse(sdf.format(currentTime));
            boolean shouldSetLastAppOpened = false;
            String lastOpenedDateString = sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), AppConstants.LAST_APP_OPENED);
            if (lastOpenedDateString == null) {
                shouldSetLastAppOpened = true;
            } else if (sdf.parse(lastOpenedDateString).compareTo(todaysDate) < 0) {
                shouldSetLastAppOpened = true;
            }
            if (shouldSetLastAppOpened) {
                sharedPrefsUtils.setStringPreference(getNonUiContext()
                        , AppConstants.LAST_APP_OPENED
                        , sdf.format(todaysDate));
                sharedPrefsUtils.setBooleanPreference(getNonUiContext()
                        , AppConstants.IS_OPENED_FIRST_TIME_TODAY
                        , true);
            }
        } catch (ParseException e) {

        }
        initEmojiFontSupport();
        initSegment();

        //Freshdesk
        FreshchatConfig freshchatConfig = new FreshchatConfig("a90d4fd9-4139-4ea5-844f-ed8c581b921c", "6cf49694-9080-4592-81d2-4b8a9ec411dd");
        Freshchat.setImageLoader(new FreshchatImageLoader());

        freshchatConfig.setCameraCaptureEnabled(true);
        freshchatConfig.setGallerySelectionEnabled(true);

        Freshchat.getInstance(getApplicationContext()).init(freshchatConfig);
//        new Handler().postDelayed(() -> RewardManager.getInstance().setActionTaken(true), 900000);
        initMoEngage();
        workerTask();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        registerLifecycleCallbacks();
        initFirebaseRemoteConfig();
    }

    public void initFirebaseRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        Log.i(TAG, "firebase_token: " + FirebaseInstanceId.getInstance().getToken());
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(REMOTE_CONFIG_FETCH_INTERVAL)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        /** making language a/b test to be backend driven
                         * check version check api in SplashViewModel's checkVersionSupport() function
                         */
//                        CommonUtils.setPreferredLanguageBoardingUser(
//                                mFirebaseRemoteConfig.getBoolean(AppConstants.ONBOARD_PREFERRED_LANGUAGE)
//                        );
                        CommonUtils.setOnBoardClipEnabled(
                                mFirebaseRemoteConfig.getBoolean(SharedPrefsUtils.ONBOARD_BACKGROUND_CLIP)
                        );
                        CommonUtils.setNewTopFanIconUser(
                                mFirebaseRemoteConfig.getBoolean(SharedPrefsUtils.IS_NEW_TOP_FAN_ICON_USER)
                        );
                        CommonUtils.setFeaturedRoomEnabled(
                                mFirebaseRemoteConfig.getBoolean(SharedPrefsUtils.FEATURED_ROOM_ENABLED)
                        );
                        Log.d(TAG, "Config params updated: " + task.getResult() + " and " + CommonUtils.isPreferredLanguageBoardingUser() + " and " + CommonUtils.isNewTopFanIconUser());
//                        Toast.makeText(this, "Fetch and activate succeeded", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "fetch failed");
                    }
                });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
//        Log.i(TAG, "APP_ON_BACKGROUNDED " + currentActivity.getClass().getSimpleName());
        Log.i(TAG, "play_video_0: " + (currentActivity != null));
        if (currentActivity != null) {
            Log.i(TAG, "play_video_01: " + (currentActivity.getClass().getSimpleName()));
            if (currentActivity instanceof HomeActivity) {
                ((HomeActivity) currentActivity).startAudioService();
            } else if (currentActivity instanceof StreamPlayerActivity) {
                ((StreamPlayerActivity) currentActivity).startAudioService();
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        if (currentActivity != null) {
//            Log.i(TAG, "APP_ON_FOREGROUNDED " + currentActivity.getClass().getSimpleName());
            if (currentActivity instanceof HomeActivity)
                ((HomeActivity) currentActivity).stopAudioService();
            else if (currentActivity instanceof StreamPlayerActivity)
                ((StreamPlayerActivity) currentActivity).stopAudioService();
        }
    }

    private void workerTask() {
        ClipSyncWorker.Companion.schedulePeriodicSync(this);
        OfflineNotificationWorker.Companion.scheduleNotification(this);
        ClearNotificationWorker.Companion.schedulePeriodicClearTask(this);
        SyncFcmTokenWorker.Companion.syncToken(this);
    }

    private void initSegment() {
        SegmentTracker.getInstance(this);
        Amplitude.getInstance()
                .initialize(this, SegmentConstants.AMPLITUDE_API_KEY)
                .enableForegroundTracking(this);
    }

    private void initEmojiFontSupport() {
        FontRequest fontRequest = new FontRequest(
                "com.example.fontprovider",
                "com.example",
                "emoji compat Font Query",
                R.array.com_google_android_gms_fonts_certs);
        EmojiCompat.Config config = new FontRequestEmojiCompatConfig(this, fontRequest);
        EmojiCompat.init(config);
    }

    private void configureCrashReporting() {

    }

    public void addThisActivityToRunningActivityies(Class cls) {
        if (!runningActivities.contains(cls)) runningActivities.add(cls);
    }

    public void removeThisActivityFromRunningActivities(Class cls) {
        if (runningActivities.contains(cls)) runningActivities.remove(cls);
    }

    public boolean isActivityInBackStack(Class cls) {
        return runningActivities.contains(cls);
    }

    synchronized public Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }

        return sTracker;
    }

    private void initMoEngage() {
        MoEngage moEngage = new MoEngage.Builder(this, "GWWR60RTQEQH3Q54V2JFN7F6")
                .optOutTokenRegistration()
                .setNotificationLargeIcon(R.drawable.ic_app_logo_standard)
                .setNotificationSmallIcon(R.drawable.ic_app_logo_standard)
                .build();
        MoEngage.initialise(moEngage);
        /*CommonUtils.trackMoEngageInstallAndUpdateEvent(this);*/
        CommonUtils.setMoenageUniqueId();
    }


    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        AppWorkerFactory factory = appComponent.factory();
        return new Configuration.Builder().setWorkerFactory(factory).build();
    }

    /**
     * lifecycle callbacks
     */
    private void registerLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.i(TAG, "play_video_onActivityCreated: " + activity.getClass().getSimpleName());
                RheoTvApp.this.currentActivity = activity;
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.i(TAG, "play_video_onActivityStarted: " + activity.getClass().getSimpleName());
                RheoTvApp.this.currentActivity = activity;
//                Log.i(TAG, "APP_BACKGROUNDED " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.i(TAG, "play_video_onActivityResumed: " + activity.getClass().getSimpleName());
                RheoTvApp.this.currentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                // don't clear current activity because activity may get stopped after
                // the new activity is resumed
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                // don't clear current activity because activity may get destroyed after
                // the new activity is resumed
//                RheoTvApp.this.currentActivity = null;
                Log.i(TAG, "play_video_onActivityDestroyed: " + (activity.getClass().getSimpleName()));
            }
        });
    }
}
