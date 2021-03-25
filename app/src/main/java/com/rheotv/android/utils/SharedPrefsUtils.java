/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 2:41 PM
 *
 */

package com.rheotv.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.rheotv.android.app.RheoTvApp;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A pack of helpful getter and setter methods for reading/writing to {@link SharedPreferences}.
 */
@Singleton
public class SharedPrefsUtils {
    public static final String HOME_LATEST_POSTS = "homeLatestPosts";
    public static final String TRENDING_POSTS = "trendingPosts";
    public static final String SAVED_DISTRICT_ID = "savedDistrictId";
    public static final String SAVED_DISTRICT_NAME = "savedDistrictName";
    public static final String WA_GROUP = "waGroup";
    public static final String AUTHOR_TOP_POSTS = "authorTopPosts";
    public static final String USER_NAME = "userName";
    public static final String USER_PROFILE_PIC = "userProfilePic";
    public static final String USER_ID = "userid";
    public static final String IS_LOGGED_IN = "isLoggedIn";
    public static final String USER_PHONE_NUMBER = "userPhoneNumber";
    public static final String deviceId = "deviceId";
    public static final String AUTH_TOKEN = "authToken";
    public static final String REGION_CONTACT_NUMBER = "regionContactNumber";
    public static final String SHOW_PLAY_ICON = "showPlayIcon";
    public static final String SHOW_FEEDBACK_CARD = "showFeedbackCard";
    public static final String ITEM_ONE_SELECTED = "itemOneSelected";
    public static final String ITEM_TWO_SELECTED = "itemTwoSelected";
    public static final String TOTAL_SHARE_COUNT = "totalShareCount";
    public static final String FIRST_LAUNCH = "firstLaunch";
    public static final String CONTAINER_IS_SECOND_LAUNCH = "isSecondLaunch";
    public static final String BOTTOM_NAV_TAB_SELECTED = "bottomNavTabSelected";
    public static final String DYNAMIC_TAB_SELECTED = "dynamicTabSelected";
    public static final String HOME_CARD_TYPE = "homeCardType";
    public static final String IS_DISTRICT_SWITCHED_FIRST_TIME = "isDistrictSwitchedFirstTime";
    public static final String VIDEO_FORMAT_REQUESTED = "videoFormatRequested";
    public static final String REMINDER_SET_POST_ID = "ReminderSetPostId";
    public static final String IS_ONBOARDING_DONE = "IsOnboardingDone";
    public static final String IS_RELOGIN = "IsReLogin";
    public static final String LAST_CLIPS_DOT_SHOWN_TS = "LastClipsShownTs";
    public static final String SHOW_VIDEO_TOOL_TIP = "show video tool tip";
    public static final String SHOW_COMMENT_TOOL_TIP = "show comment tool tip";
    public static final String SHOW_RATE_US_DIALOG = "show rate us dialog";
    public static final String APP_RATED = "app rated";
    public static final String LAST_SHOW_VIDEO_HEADER_PERMISSION_TS = "videoPlayerHeaderShownTS";
    public static final String LAST_SHOW_RATING_PERMISSION_TS = "lastShownRatingTime";
    public static final String IS_MOENGAGE_INSTALL_TRACKED = "is_moengage_install_tracked";
    public static final String MOENGAGE_TRACKED_VERSION = "moengage_tracked_version";
    public static final String DEVICE_ID = "device_id";
    public static final String ASK_PHONE_STATE_PERMISSION = "ask_phone_state_permission";
    public static final String ARG_BRANCH_EXTRA_INFO = "arg_branch_extra_info";
    public static final String ARG_USER_LANGUAGE = "user_language";

    public static final String AUTHOR_ID = "author_id";
    public static final String IS_STREAMER = "is_streamer";
    public static final String IS_CONTENT_MODERATOR = "is_content_moderator";
    public static final String IS_FIRST_WATCH_EVENT_TRACKED = "is_first_event_tracked";
    public static final String IS_FIRST_WATCH_EVENT_5_MINS_TRACKED = "is_first_watch_event_5_mins_tracked";
    public static final String IS_FIRST_COMMENT_SEND_EVENT_TRACKED = "is_first_comment_send_event_tracked";
    public static final String IS_FIRST_REQUEST_TO_PLAY_EVENT_TRACKED = "is_first_request_to_play_event_tracked";
    public static final String IS_FIRST_STORY_CLICKED_EVENT_TRACKED = "is_first_story_clicked_event_tracked";
    public static final String IS_FIRST_GO_LIVE_GENERATE_KEY_EVENT_TRACKED = "is_first_go_live_generate_key_event_tracked";
    public static final String IS_FIRST_EVENT_HOMEVIEW_TRACKED = "is_first_event_homeview_tracked";
    public static final String IS_NEW_APP_USER = "is_new_app_user";
    public static final String ANALYTICS_FILE_COUNT = "analytics file count";
    public static final String DIRECT_VIDEO_WATCH_USER = "direct_video_watch_user";
    public static final String IS_SHARE_TUTORIAL_SHOWN = "is_share_tutorial_shown";
    public static final String IS_FIRST_SHARE_DONE = "is_first_share_done";
    public static final String IS_FIRST_SHARE = "is_first_share";
    public static final String PAYMENT_MODEL = "payment_model";
    public static final String LEVEL_TYPE = "level_type";

    public static final String IS_MOENGAGE_IDENTITY_SET = "isMoengageIdentitySet";
    public static final String IS_INSTALLED_FROM_TRACKED = "isInstalledFromBranchTracked";
    public static final String LAST_APP_OPEN_TIME = "last_app_open_time";
    public static final String IS_CLIP_SYNC_INITIALIZED = "is_clip_sync_initialized";
    public static final String IS_RE_LOGIN = "is_re_login";
    public static final String IS_FIRST_PLAY_REQUEST = "is_first_play_request";
    public static final String IS_FIRST_TIME_PAGE_CHANGE = "is_first_time_page_change";
    public static final String IS_FIRST_TIME_COINS_CLICKED = "is_first_time_coins_clicked";
    public static final String IS_FIRST_TIME_LIVE_STREAM_KEY_CREATED = "is_first_time_live_stream_created";
    public static final String IS_FIRST_TIME_STORY_SEEN = "is_first_time_story_seen";
    public static final String IS_FIRST_TIME_STICKER_SENT = "is_first_time_sticker_sent";
    public static final String IS_FIRST_TIME_SELF_PROFILE_VISITED = "is_first_time_self_profile_visited";
    public static final String IS_FIRST_TIME_LIKED = "is_first_time_liked";
    public static final String IS_FIRST_TIME_WATCH_REWARD_SCRATCHED = "is_first_time_watch_reward_scratched";
    public static final String IS_FIRST_TIME_LEADER_BOARD_CLICKED = "is_first_time_leader_board_clicked";
    public static final String IS_FIRST_TIME_FOLLOW_CLICK = "is_first_time_follow_clicked";
    public static final String IS_INSTALL_REFERER_EVENT_TRACKED = "is_install_referer_event_tracked";

    public static final String SPLASH_BANNER_URL = "splash_banner_url";
    public static final String SPLASH_TARGET_URL = "splash_target_url";
    public static final String SPLASH_AD_NAME = "splash_ad_name";
    public static final String CONTACTS_UPLOAD_SUCCESS = "contacts_uplaod_success";
    public static final String SPLASH_VALID_TILL_TIMESTAMP = "valid_till_timestamp";
    public static final String LAST_UPLOAD_CONTACTS_SHOWN_TIME = "upload_contacts_shown_time";
    public static final String HIDE_SYNC_CONTACTS = "hide_sync_contacts";
    public static final String SEARCH_BADGE_APP_UP_COUNT = "search_app_up_count";
    public static final String PLAYER_MODE = "player_mode";
    public static final String AUDIO_MODE_TOAST_COUNT = "audio_mode_toast_count";
    public static final String IS_AUDIO_MODE_ENABLED = "is_audio_mode_enabled";
    public static final String IS_SELECTED_USER = "is_selected_user";
    public static final String IS_USER_WELCOMED = "is_user_welcomed";
    public static final String IS_NEW_USER = "is_new_user";
    public static final String IS_PREFERRED_LANGUAGE_ONBOARDING_USER = "is_preferred_language_onboarding_user";
    public static final String IS_TOP_SHOW_USER = "is_top_show_user";
    public static final String IS_NEW_TOP_FAN_ICON_USER = "three_fan_icon";
    public static final String IS_FEATURED_ROOM_ENABLED = "is_featured_room_enabled";
    public static final String FEATURED_ROOM_ENABLED = "featured_room_enabled";

    public static final String IS_UN_MUTE_AUDIO_ROOM_TOOLTIP_SHOWN = "is_un_mute_tooltip_";
    public static final String IS_VIDEO_CALL_DND_ALERT_SHOWN = "is_video_call_dnd_alert_shown";

    public static final String LAST_CALLED_CHANNEL_ID = "last_call_channel_id";
    public static final String IS_FIRST_AGORA_CALL_DONE = "is_first_agora_call_done";
    public static final String ONBOARD_BACKGROUND_CLIP = "onboard_background_clip";
    public static final String ADD_TO_CART_EVENT_HIT = "add_to_cart_event_hit";
    public static final String ACHIEVEMENT_UNLOCKED_EVENT_HIT = "achievement_unlocked_event_hit";
    public static final String INSTALL_REFERRER_FB_EVENT_TRACKED = "install_referrer_fb_event_tracked";

    @Inject
    public SharedPrefsUtils() {
    }

    int count = 0;

    public void incCount() {
        count++;
    }

    public int getCount() {
        return count;
    }

    /**
     * Helper method to retrieve a String value from {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @return The value from shared preferences, or null if the value could not be read.
     */
    public String getStringPreference(Context context, String key) {
        String value = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getString(key, null);
        }
        return value;
    }

    /**
     * Helper method to write a String value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public boolean setStringPreference(Context context, String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Helper method to retrieve a float value from {@link SharedPreferences}.
     *
     * @param context      a {@link Context} object.
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public float getFloatPreference(Context context, String key, float defaultValue) {
        float value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getFloat(key, defaultValue);
        }
        return value;
    }

    /**
     * Helper method to write a float value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public boolean setFloatPreference(Context context, String key, float value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Helper method to retrieve a long value from {@link SharedPreferences}.
     *
     * @param context      a {@link Context} object.
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public long getLongPreference(Context context, String key, long defaultValue) {
        long value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getLong(key, defaultValue);
        }
        return value;
    }

    /**
     * Helper method to write a long value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public boolean setLongPreference(Context context, String key, long value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Helper method to retrieve an integer value from {@link SharedPreferences}.
     *
     * @param context      a {@link Context} object.
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public int getIntegerPreference(Context context, String key, int defaultValue) {
        int value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getInt(key, defaultValue);
        }
        return value;
    }

    /**
     * Helper method to write an integer value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public boolean setIntegerPreference(Context context, String key, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Helper method to retrieve a boolean value from {@link SharedPreferences}.
     *
     * @param context      a {@link Context} object.
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public boolean getBooleanPreference(Context context, String key, boolean defaultValue) {
        if (context == null)
            context = RheoTvApp.getNonUiContext();
        boolean value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getBoolean(key, defaultValue);
        }
        return value;
    }

    /**
     * Helper method to write a boolean value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public boolean setBooleanPreference(Context context, String key, boolean value) {
        if (context == null)
            context = RheoTvApp.getNonUiContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            return editor.commit();
        }
        return false;
    }

    public void signOut() {
        signOut(null);
    }

    public void signOut(Context context) {
        if (context == null)
            context = RheoTvApp.getNonUiContext();
        setBooleanPreference(context, SharedPrefsUtils.IS_LOGGED_IN, false);
        setStringPreference(context, SharedPrefsUtils.USER_NAME, null);
        setIntegerPreference(context, SharedPrefsUtils.USER_ID, 0);
        setStringPreference(context, SharedPrefsUtils.AUTH_TOKEN, null);
    }

}