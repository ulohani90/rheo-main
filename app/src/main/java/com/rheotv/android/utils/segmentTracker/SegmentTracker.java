package com.rheotv.android.utils.segmentTracker;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.amplitude.api.Amplitude;
import com.amplitude.api.Identify;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.moe.pushlibrary.MoEHelper;
import com.rheotv.android.BuildConfig;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.segment.analytics.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class SegmentTracker {

    Context mContext;

    static SegmentTracker mInstance;
    private static FirebaseAnalytics mFirebaseAnalytics;
    AppEventsLogger logger;

//    Analytics analytics;

    List<String> analyticsEvents;
    List<String> moengageEvents;

    public SegmentTracker(Context context) {
        this.mContext = context;
        createEventFile();
    }

    public static SegmentTracker getInstance(Context context) {
        if (mInstance == null)
            mInstance = new SegmentTracker(context);
        if (mFirebaseAnalytics == null)
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        return mInstance;
    }

    public static SegmentTracker getInstance() {
        return getInstance(RheoTvApp.getNonUiContext());
    }

    public void setAnalyticsEvents(List<String> analyticsEvents, List<String> moengageEvents) {
        this.analyticsEvents = analyticsEvents;
        this.moengageEvents = moengageEvents;
    }

    public void init() {
//        this.analytics = new Analytics.Builder(mContext, "wpOnq53ohxnnSaBeENe6Ge5qsJNMqgeD")
//                .trackApplicationLifecycleEvents()
//                /*.use(MoEngageIntegration.FACTORY)*/
//                .build();
//        Analytics.setSingletonInstance(analytics);

    }

    //Tracking Screens
    public void recordScreenName(String screenName, HashMap<String, Object> properties) {
        if (CommonUtils.getBranchExtraInfo(getNonUiContext()) != null)
            properties.put(AppConstants.EXTRA_INFO, CommonUtils.getBranchExtraInfo(getNonUiContext()));
        properties.put(AppConstants.IS_EMULATOR, CommonUtils.isAndroidEmulator());
        properties.put(AppConstants.USER_LANGUAGE, CommonUtils.getUserLanguage(getNonUiContext()));
        properties.put(AppConstants.DIRECT_VIDEO_USER, CommonUtils.isDirectVideoWatchUser());
        properties.put("is_user_offline", !NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext()));

        Properties props = new Properties();
        props.putAll(properties);
//        analytics.screen(screenName, props);

        // since segment doesn't pass screen event to segment
        // so we're calling event for the same
        //trackEvent(screenName, properties);
        if (moengageEvents == null || moengageEvents.isEmpty() || moengageEvents.contains(screenName)) {
            Log.i("Moengag_EVENT_HITScreen", screenName);
            trackEventOnMoengage(screenName, properties);
        }

    }

    public void trackEventOnMoengage(String eventName, Map<String, Object> map) {
        com.moengage.core.Properties properties = new com.moengage.core.Properties();
        for (String key : map.keySet()) {
            properties.addAttribute(key, map.get(key));
        }
        MoEHelper.getInstance(getNonUiContext()).trackEvent(eventName, properties);
    }


    /*public void trackEvent(String eventName, Properties properties) {
        if (CommonUtils.getBranchExtraInfo(getNonUiContext()) != null)
            properties.putValue(AppConstants.EXTRA_INFO, CommonUtils.getBranchExtraInfo(getNonUiContext()));
        properties.putValue(AppConstants.IS_EMULATOR, CommonUtils.isAndroidEmulator());
        properties.putValue(AppConstants.USER_LANGUAGE, CommonUtils.getUserLanguage(getNonUiContext()));
        analytics.track(eventName, properties);
    }*/

    public void trackEvent(String eventName, Map<String, Object> properties) {
        try {
            JSONObject jsonObject = new JSONObject();
            if (CommonUtils.isUserLoggedin())
                jsonObject.put("username", CommonUtils.getUserName());
            jsonObject.put(AppConstants.IS_EMULATOR, CommonUtils.isAndroidEmulator());
            jsonObject.put(AppConstants.USER_LANGUAGE, CommonUtils.getUserLanguage(getNonUiContext()));
            jsonObject.put("is_streamer", CommonUtils.isUserStreamer());
            jsonObject.put("direct_video_user", CommonUtils.isDirectVideoWatchUser());
            properties.put("is_user_offline", !NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext()));
            for (String key : properties.keySet()) {
                jsonObject.put(key, properties.get(key));
            }

            if (CommonUtils.isUserLoggedin())
                properties.put("username", CommonUtils.getUserName());

            properties.put("top_three_fan_icon_user", CommonUtils.isNewTopFanIconUser());
            properties.put("preferred_language_onboarding_user", CommonUtils.isPreferredLanguageBoardingUser());
            properties.put("top_show_user", CommonUtils.isTopShowUser());
            properties.put("for_select_audience", CommonUtils.isSelectedUser());
            properties.put(AppConstants.IS_EMULATOR, CommonUtils.isAndroidEmulator());
            properties.put(AppConstants.USER_LANGUAGE, CommonUtils.getUserLanguage(getNonUiContext()));
            properties.put("is_streamer", CommonUtils.isUserStreamer());
            properties.put("direct_video_user", CommonUtils.isDirectVideoWatchUser());
            properties.put("is_user_offline", !NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext()));

            if (analyticsEvents == null || analyticsEvents.isEmpty() || analyticsEvents.contains(eventName)) {
                //new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(mContext, "Event --> " + eventName + "\nData --> " + jsonObject, Toast.LENGTH_LONG).show());
                Amplitude.getInstance().logEvent(eventName, jsonObject);
                Log.i("AMP_EVENT_HIT", eventName + '\n' + jsonObject);
                //addEventToFile(getDataString(eventName, properties));
            }
            if (moengageEvents == null || moengageEvents.isEmpty() || moengageEvents.contains(eventName)) {
                Log.i("Moengage_EVENT_HIT", eventName);
                properties.put("version", BuildConfig.VERSION_NAME);
                trackEventOnMoengage(eventName, properties);
            }
            Bundle params;
            params = jsonToBundle(jsonObject);

            logger = AppEventsLogger.newLogger(RheoTvApp.getNonUiContext());
            logger.logEvent(eventName, params);

            mFirebaseAnalytics.logEvent(eventName.replaceAll(" ", "_"), params);

            try {
                if (eventName.equals(SegmentConstants.EVENT_CHAT_STICKER_SENT))
                    logger.logPurchase(new BigDecimal(Objects.requireNonNull(properties.get("sticker_value")).toString()), Currency.getInstance("INR"), params);

                if (eventName.equals(SegmentConstants.EVENT_WATCH_STREAM_30_MINS) || eventName.equals(SegmentConstants.EVENT_WATCH_STREAM_11_MINS) ||
                        eventName.equals(SegmentConstants.EVENT_WATCH_STREAM_5_MINS) || eventName.equals(SegmentConstants.EVENT_WATCH_STREAM_45_MINS) ||
                        eventName.equals(SegmentConstants.EVENT_WATCH_STREAM_1_HRS) || eventName.equals(SegmentConstants.EVENT_WATCH_STREAM_2_HRS)) {
                    Double value = new Double(properties.get("time_elapsed").toString()) / 60.0;//in min
                    if (eventName.equals(SegmentConstants.EVENT_WATCH_STREAM_1_HRS))
                        value = 60.0;
                    if (eventName.equals(SegmentConstants.EVENT_WATCH_STREAM_2_HRS))
                        value = 120.0;
                    if (eventName.equals(SegmentConstants.EVENT_WATCH_STREAM_5_MINS))
                        if (!CommonUtils.isAddToCardEventAlreadyHit()) {
                            CommonUtils.setAddToCartEventHit();
                            logAddToCartEvent(properties.get("author").toString(), properties.get("postId").toString(),
                                    properties.get("type").toString(), "INR", value);
                        }
                }
                if (eventName.equals(SegmentConstants.EVENT_WATCH_STREAM_11_MINS))
                    if (!CommonUtils.isAchievementUnlockedEventAlreadyHit()) {
                        CommonUtils.setAchievementUnlockedEventHit();
                        logAchievedLevelEvent("11");
                    }
                if (eventName.equals(SegmentConstants.EVENT_WATCH_STREAM_30_SECS))
                    logCompletedTutorialEvent(properties.get("postId").toString(), true);

                if (eventName.equals(SegmentConstants.EVENT_TEXT_SEARCHED))
                    logSearchEvent("streamer", properties.get("textSearched").toString());

                if (eventName.equals(SegmentConstants.EVENT_FOLLOW_CLICKED))
                    logSubscribeEvent(properties.get("author").toString(), "INR", 10.0);//ask umesh price or remove

                if (eventName.equals(SegmentConstants.EVENT_LOGIN_COMPLETED))
                    logCompleteRegistrationEvent("Google Login");

                if (eventName.equals(SegmentConstants.EVENT_APP_RATED))
                    logRateEvent("Rating", properties.get("feedback").toString(), "10", 5, new Double(properties.get("rating").toString()));


                if (eventName.equals(SegmentConstants.EVENT_PLAY_REQUEST) || eventName.equals(SegmentConstants.EVENT_FIRST_PLAY_REQUEST))
                    logCustomizeProductEvent();

                if (eventName.equals(SegmentConstants.EVENT_UC_REDEEMED))
                    logAddPaymentInfoEvent(true);


            } catch (Exception e) {
                e.printStackTrace();
            }

            //Segment Tracking
//            Properties props = new Properties();
//            props.putAll(properties);
//            analytics.track(eventName, props);

            Log.i(getClass().getSimpleName(), "trackEvent_is: " + properties + "  event: " + eventName);

            //Moengage Tracking

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static Bundle jsonToBundle(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator iter = jsonObject.keys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = jsonObject.getString(key);
            bundle.putString(key, value);
        }
        return bundle;
    }

    public void logAddToCartEvent(String contentData, String contentId, String contentType, String currency, double price) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);
        logger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, price, params);
    }


    public void logSearchEvent(String contentType, String searchString) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
        params.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, searchString);
        params.putInt(AppEventsConstants.EVENT_PARAM_SUCCESS, 1);//1 for success
        logger.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, params);
    }

    public void logSubscribeEvent(String orderId, String currency, double price) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_ORDER_ID, orderId);
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);
        logger.logEvent(AppEventsConstants.EVENT_NAME_SUBSCRIBE, price, params);
    }

    public void logCompleteRegistrationEvent(String registrationMethod) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, registrationMethod);
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params);
    }

    public void logRateEvent(String contentType, String contentData, String contentId, int maxRatingValue, double ratingGiven) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
        params.putInt(AppEventsConstants.EVENT_PARAM_MAX_RATING_VALUE, maxRatingValue);
        logger.logEvent(AppEventsConstants.EVENT_NAME_RATED, ratingGiven, params);
    }

    public void logAddPaymentInfoEvent(boolean success) {
        Bundle params = new Bundle();
        params.putInt(AppEventsConstants.EVENT_PARAM_SUCCESS, success ? 1 : 0);
        logger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_PAYMENT_INFO, params);
    }

    public void logCustomizeProductEvent() {
        logger.logEvent(AppEventsConstants.EVENT_NAME_CUSTOMIZE_PRODUCT);
    }

    public void logAchieveLevelEvent(String level) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_LEVEL, level);
        logger.logEvent(AppEventsConstants.EVENT_NAME_ACHIEVED_LEVEL, params);
    }

    public void logAchievedLevelEvent(String level) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_LEVEL, level);
        logger.logEvent(AppEventsConstants.EVENT_NAME_ACHIEVED_LEVEL, params);
    }

    public void logCompletedTutorialEvent(String contentId, boolean success) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
        params.putInt(AppEventsConstants.EVENT_PARAM_SUCCESS, success ? 1 : 0);
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL, params);
    }

    private String getDataString(String eventName, HashMap<String, Object> properties) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n\n");
        builder.append(eventName);
        for (String key : properties.keySet()) {
            builder.append("\n");
            builder.append(key);
            builder.append(" : ");
            builder.append(properties.get(key));
        }
        return builder.toString();
    }


    File logFile;

    public void createEventFile() {
        int currentFileNum = CommonUtils.getAnalyticsFileCount() + 1;
        logFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/rheo_analytics", "log_" +
                currentFileNum + ".txt");
        CommonUtils.setAnalyticsFileCount(currentFileNum);
        if (!logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }
    }

    private void addEventToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(logFile, true));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public void trackEvent(String eventName) {
        Properties properties = new Properties();
        if (CommonUtils.getBranchExtraInfo(getNonUiContext()) != null)
            properties.putValue(AppConstants.EXTRA_INFO, CommonUtils.getBranchExtraInfo(getNonUiContext()));
        properties.putValue(AppConstants.IS_EMULATOR, CommonUtils.isAndroidEmulator());
        properties.putValue(AppConstants.USER_LANGUAGE, CommonUtils.getUserLanguage(getNonUiContext()));
        trackEvent(eventName, properties);
    }*/

    public void setIdentityUsername(String emailAddress, String name) {
//        analytics.identify(emailAddress, new Traits().putName(name), null);
        Identify identify = new Identify();
        identify.set("email", emailAddress);
        identify.set("name", name);
        Amplitude.getInstance().identify(identify);
        mFirebaseAnalytics.setUserProperty("email", emailAddress);
        mFirebaseAnalytics.setUserProperty("name", name);
    }

    public void setIdentityUsername(String username) {
//        analytics.identify(new Traits().putUsername(username));
        Amplitude.getInstance().setUserId(username);
        mFirebaseAnalytics.setUserId(username);
    }

    public void setIdentityLanguage(String language) {
        Identify identify = new Identify();
        identify.set(AppConstants.USER_LANGUAGE, language);
        Amplitude.getInstance().identify(identify);
        mFirebaseAnalytics.setUserProperty(AppConstants.USER_LANGUAGE, language);
    }

    public void setIdentityPreferredLanguageUser(boolean isPreferredLanguageUser) {
        Identify identify = new Identify();
        identify.set("preferred_language_onboarding_user", isPreferredLanguageUser);
        Amplitude.getInstance().identify(identify);
        mFirebaseAnalytics.setUserProperty("preferred_language_onboarding_user", String.valueOf(isPreferredLanguageUser));
    }

    public void setIdentityTopShowUser(boolean isTopShowUser) {
        Identify identify = new Identify();
        identify.set("top_show_user", isTopShowUser);
        Amplitude.getInstance().identify(identify);
        mFirebaseAnalytics.setUserProperty("top_show_user", String.valueOf(isTopShowUser));
    }

    public void setIdentitySelectedUser(boolean isSelectedUser) {
        Identify identify = new Identify();
        identify.set("for_select_audience", isSelectedUser);
        Amplitude.getInstance().identify(identify);
        mFirebaseAnalytics.setUserProperty("for_select_audience", String.valueOf(isSelectedUser));
    }

    public void setIdentityNewTopFanIconUse(boolean isNewTopFanIconUser) {
        Identify identify = new Identify();
        identify.set("top_three_fan_icon_user", isNewTopFanIconUser);
        Amplitude.getInstance().identify(identify);
        mFirebaseAnalytics.setUserProperty("top_three_fan_icon_user", String.valueOf(isNewTopFanIconUser));
    }

    public void setIdentityFeaturedRoom(boolean flag) {
        Identify identify = new Identify();
        identify.set(SharedPrefsUtils.IS_FEATURED_ROOM_ENABLED, flag);
        Amplitude.getInstance().identify(identify);
        mFirebaseAnalytics.setUserProperty(SharedPrefsUtils.IS_FEATURED_ROOM_ENABLED, String.valueOf(flag));
    }

    public void setIdentityOnboardClipUser(boolean isOnboardClipUser) {
        Identify identify = new Identify();
        identify.set("onboard_background_clip", isOnboardClipUser);
        Amplitude.getInstance().identify(identify);
        mFirebaseAnalytics.setUserProperty("onboard_background_clip", String.valueOf(isOnboardClipUser));
    }

    public Properties getPropertiesFromMap(Map<String, String> data) {
        Properties properties = new Properties();
        try {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                properties.putValue(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties;
    }


}
