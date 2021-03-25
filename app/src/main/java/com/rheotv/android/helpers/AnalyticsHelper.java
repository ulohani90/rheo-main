package com.rheotv.android.helpers;

import android.content.Context;
import android.util.Log;

import com.facebook.appevents.AppEventsConstants;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.rheotv.android.BuildConfig;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.SharedPrefsUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalyticsHelper {

    static final String EVENT_VIDEO_PLAY_PERCENTAGE = "VIDEO_PLAY_PERCENTAGE";
    static String EVENT_APP_OPEN = "APP_OPEN";
    static String EVENT_VIEW_OPEN = "VIEW_OPEN";
    static String EVENT_VIDEO_PLAY = "VIDEO_PLAY";
    static String EVENT_SHARE_CLICK = "SHARE_CLICK";
    static String EVENT_FB_SHARE_CLICK = "FB_SHARE_CLICK";
    static String EVENT_VIEW_CLICK = "VIEW_CLICK";
    static String EVENT_SHARE_SUCCESS = "SHARE_SUCCESS";
    static String EVENT_APP_SHARE = "SHARE_APP";
    static String EVENT_LIKE = "SHARE_LIKE";
    static String EVENT_DOWNLOAD = "EVENT_DOWNLOAD";
    static String EVENT_SUCCESS = "SUCCESS";
    static String EVENT_FAIL = "FAIL";
    static String EVENT_APP_INSTALL = "APP_INSTALL";
    static String EVENT_SIGN_IN = "sign_in";
    static String EVENT_CREATE_LIVE_POST = "create_live_post";
    static String PROPERTY_DISTRICT = "PROPERTY_DISTRICT";
    static String EVENT_VIDEO_PLAY_FIRST_TIME = "VIDEO_PLAY_FIRST_TIME";
    static String EVENT_LEADERBOARD_CLICKED = "EVENT_LEADERBOARD_CLICKED";
    static String PROPERTY_CONNECTION_SPEED = "CONNECTION_SPEED";
    static String RATINGS_POPUP_OPENED = "RATINGS_POPUP_OPENED";
    static String RATINGS_LATER_CLICKED = "RATINGS_LATER_CLICKED";
    static String RATINGS_SUBMIT_CLICKED = "RATINGS_SUBMIT_CLICKED";
    static String JOIN_WHATSAPP_POPUP_OPENED = "JOIN_WHATSAPP_POPUP_OPENED";
    static String WHATSAPP_JOIN_CLICKED = "WHATSAPP_JOIN_CLICKED";
    static String NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED";
    static String NOTIFICATION_DATA = "NOTIFICATION_DATA";
    static String NOTIFICATION_CLICKED = "NOTIFICATION_CLICKED";
    static String NOTIFICATION_CLICKED_DATA = "NOTIFICATION_CLICKED_DATA";
    static String DISTRICT_SWITCHED = "DISTRICT_SWITCHED";
    static String DYNAMIC_TAB_SELECTED = "DYNAMIC_TAB_SELECTED";

    private String DEVICE_ID = "device_id";

    private static AnalyticsHelper analyticsHelper = null;
    private final String TAG = AnalyticsHelper.class.getSimpleName();
    String MIX_PANEL_TOKEN = BuildConfig.MIXPANEL_TOKEN;
    HashMap<String, String> defaultProperties = new HashMap();

    private MixpanelAPI mixpanel;

    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    private AnalyticsHelper(Context context) {
        mixpanel = MixpanelAPI.getInstance(context, MIX_PANEL_TOKEN);
    }

    public static AnalyticsHelper getInstance(Context context) {
        if (analyticsHelper == null) {
            analyticsHelper = new AnalyticsHelper(context);
        }
        analyticsHelper.setDefaultProperty(PROPERTY_CONNECTION_SPEED, NetworkUtils.getNetworkGeneration(RheoTvApp.getNonUiContext()));
        return analyticsHelper;
    }

    public void setDefaultProperty(String key, String value) {
        defaultProperties.put(key, value);
    }

    private JSONObject getPropertiesWithDefaultParams() {
        JSONObject properties = new JSONObject();
        for (String key : defaultProperties.keySet()) {
            try {
                properties.put(key, defaultProperties.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    public void sendAppOpen() {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_APP_OPEN, properties);
        mixpanel.track(AppEventsConstants.EVENT_NAME_ACTIVATED_APP);
        Log.d(RheoTvApp.TAG, "logging activate app event");
    }

    public void sendSignInEvent(boolean isSuccess, String reasonForFailure) {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("result", isSuccess);
            properties.put("reason", reasonForFailure);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_SIGN_IN, properties);
    }

    public void sendCreateLivePostEvent(boolean isSuccess, String reasonForFailure) {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("result", isSuccess);
            properties.put("reason", reasonForFailure);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_CREATE_LIVE_POST, properties);
    }


    public void sendAppInstall() {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_APP_INSTALL, properties);
    }


    public void sendVideoPlayFirstTime() {
        mixpanel.track(EVENT_VIDEO_PLAY_FIRST_TIME);
    }


    public void sendLeaderboardClicked() {
        mixpanel.track(EVENT_LEADERBOARD_CLICKED);
    }

    public void sendVideoPlay(long duration, String authorName, String authorId, String postId, String postName,
                              List<String> hashtags, long playerCurrentPos, long totalDuration, String topSource,
                              String topSourceDynamicTab, String topSourceCardType, Map<String, String> extraParams) {
        try {
            JSONObject properties = getPropertiesWithDefaultParams();
            properties.put("author_name", authorName);
            properties.put("author_id", authorId);
            properties.put("duration", duration);
            properties.put("post_id", postId);
            properties.put("top_source", topSource);
            properties.put("top_source_home_card_type", topSourceCardType);
            properties.put("top_source_home_dynamic_tab", topSourceDynamicTab);
            properties.put("post_name", postName);
            properties.put("hashtags", hashtags);
            properties.put("username", CommonUtils.getUserName(RheoTvApp.getNonUiContext()));
            properties.put("show_video_play_icon", sharedPrefsUtils.getBooleanPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SHOW_PLAY_ICON, false));
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
            if (extraParams != null && extraParams.size() > 0) {
                Set<String> keys = extraParams.keySet();
                for (String key : keys) {
                    properties.put(key, extraParams.get(key));
                }
            }
            mixpanel.track(EVENT_VIDEO_PLAY, properties);

            if (totalDuration > 0) {
                long percentage = (playerCurrentPos * 100) / totalDuration;
                properties.put("percentage", percentage);
                properties.put("total_duration", totalDuration);
                mixpanel.track(EVENT_VIDEO_PLAY_PERCENTAGE, properties);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendPostShareClick(String authorName, String authorId, String postId, String postName, String source) {
        AppConstants.POST_SHARE_COUNT++;

        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("author_name", authorName);
            properties.put("author_id", authorId);
            properties.put("post_id", postId);
            properties.put("post_name", postName);
            properties.put("source", source);
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_SHARE_CLICK, properties);
    }

    public void sendPostFBShareClick(String authorName, String authorId, String postId, String postName, String source) {
        AppConstants.POST_SHARE_COUNT++;
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("author_name", authorName);
            properties.put("author_id", authorId);
            properties.put("post_id", postId);
            properties.put("post_name", postName);
            properties.put("source", source);
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_FB_SHARE_CLICK, properties);
    }


    public void sendPostDownloadClicked(String authorName, String authorId, String postId, String postName, String source) {
        AppConstants.POST_SHARE_COUNT++;
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("author_name", authorName);
            properties.put("author_id", authorId);
            properties.put("post_id", postId);
            properties.put("post_name", postName);
            properties.put("source", source);
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_DOWNLOAD, properties);
    }

    public void sendClick(String viewClicked) {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("view_name", viewClicked);
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_VIEW_CLICK, properties);

    }

    public void ratingsPopupOpened() {
        mixpanel.track(RATINGS_POPUP_OPENED);

    }


    public void notificationReceived() {
        mixpanel.track(NOTIFICATION_RECEIVED);
    }

    public void ratingsLaterClicked() {
        mixpanel.track(RATINGS_LATER_CLICKED);
    }

    public void ratingsSubmitClicked(int rate) {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("star", rate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(RATINGS_SUBMIT_CLICKED, properties);
    }


    public void sendNotificationData(Map<String, String> data) {
        JSONObject messageData = null;
        try {
            messageData = new JSONObject(data);
            messageData.put("device_id", CommonUtils.getDevId(RheoTvApp.getNonUiContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mixpanel.track(NOTIFICATION_DATA, messageData);
    }

    public void sendNotificationEvent(Map<String, Object> data) {
        JSONObject messageData = null;
        try {
            messageData = new JSONObject(data);
            messageData.put("device_id", CommonUtils.getDevId(RheoTvApp.getNonUiContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mixpanel.track(NOTIFICATION_DATA, messageData);
    }

    public void sendNotificationClickedData(Result result, String topSource) {
        JSONObject properties = new JSONObject();
        try {
            properties.put("author_name", result.getAuthor().getUser().getUserFullName());
            properties.put("author_id", result.getAuthor().getUser().getId());
            properties.put("duration", result.getDuration());
            properties.put("post_id", result.getId());
            properties.put("top_source", topSource);
            properties.put("post_name", result.getTitle());
            properties.put("post_body", result.getDescription());
            properties.put("hashtags", result.getHashtags());
            properties.put("category", result.getCategory());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mixpanel.track(NOTIFICATION_CLICKED_DATA, properties);
    }

    public void sendNotificationClicked() {
        mixpanel.track(NOTIFICATION_CLICKED);
    }

    public void sendPostShareSuccess(String authorName, String authorId, String postId, String postName, String type) {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("author_name", authorName);
            properties.put("author_id", authorId);
            properties.put("post_id", postId);
            properties.put("post_name", postName);
            properties.put("type", type);
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_SHARE_SUCCESS, properties);
    }

    public void sendAppShare() {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_APP_SHARE, properties);
    }

    public void sendLike(String authorName, String authorId, String postId, String postName, String source) {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("author_name", authorName);
            properties.put("author_id", authorId);
            properties.put("post_id", postId);
            properties.put("post_name", postName);
            properties.put("source", source);
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_LIKE, properties);
    }

    public void sendViewOpen(String viewName) {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("view_name", viewName);
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_VIEW_OPEN, properties);
    }

    public void sendSuccess(String what) {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("what", what);
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_SUCCESS, properties);
    }

    public void sendFail(String what) {
        JSONObject properties = getPropertiesWithDefaultParams();
        try {
            properties.put("what", what);
            properties.put("PROPERTY_DISTRICT", sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track(EVENT_FAIL, properties);
    }

}
