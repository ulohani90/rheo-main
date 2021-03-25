package com.rheotv.android.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.rheotv.android.R;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.activities.tabcontainer.profile.container.ProfileContainerFragment;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

public class LinkHandler {

//    public class LinkHandler {

    final static String RHEOTV_APP = "rheotv://";
    final static String HTTP_RHEOTV = "http://rheotv.com/";
    final static String HTTP_WWW_RHEOTV = "http://www.rheotv.com/";
    final static String HTTPS_RHEOTV = "https://www.rheotv.com/";
    final static String HTTPS_WWW_RHEOTV = "https://www.rheotv.com/";
    final static String TAG = "linkhandler";
    public static String intentOpenUrl = "";

    public static void handleLink(AppCompatActivity activity, String url) {
        Map<String, String> queryParams = getQueryParams(url);
        String targetPath = getMojoTargetPath(url);
        Log.d(TAG, "target path " + targetPath);
        //Todo: handle via regex

        //targetPath = "/user/me/?show_referraltooltip=ue";
        if (targetPath.contains("/post/")) {

            PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
            String postUID = getPostId(targetPath);

            postEvent(postUID);

        } else if (targetPath.contains("user/")) {
            String userName = targetPath.substring(targetPath.indexOf("user/") + 5, targetPath.length() - 1);
            Log.d(TAG, "username : " + userName);

            if (activity instanceof TabContainerActivity) {
                ((TabContainerActivity) activity).loadFragment(ProfileContainerFragment.newInstance(userName, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE), false, false, R.id.frame_container);
            }
        }

        intentOpenUrl = "";

    }

    private static void postEvent(String postId) {
        if (postId == null) return;
        if (eventRetryCount < 10) {
            eventRetryCount++;
            if (EventBus.getDefault().hasSubscriberForEvent(EventBusModel.OpenPostWitId.class)) {
                EventBus.getDefault().post(new EventBusModel.OpenPostWitId(postId, true, false, true));
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> postEvent(postId), 200);
            }
        } else
            eventRetryCount = 0;
    }

    private static int eventRetryCount = 0;

    public static void handleDeepLink(AppCompatActivity activity, String url, String sourceScreen) {
        //targetPath = "/user/me/?show_referraltooltip=ue";
        String targetPath = getMojoTargetPath(url);
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        String postUID = getPostId(targetPath);
        postEvent(postUID);
        intentOpenUrl = "";
    }

    public static void handleDeepLink(AppCompatActivity activity, String url, String sourceScreen, boolean isForCustomRoom) {
        //targetPath = "/user/me/?show_referraltooltip=ue";
        String targetPath = getMojoTargetPath(url);
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        String postUID = getPostId(targetPath);
        postEvent(postUID);
        intentOpenUrl = "";
    }


    private static Map<String, String> getQueryParams(String path) {
        Map<String, String> queryParams = new HashMap<>();
        int index = path.indexOf('?');
        if (index > 0) {
            String queryStr = path.substring(index);
            for (String queryParamPair : queryStr.split("&")) {
                String[] params = queryParamPair.split("=");
                if (params.length > 0) {
                    queryParams.put(params[0], params.length > 1 ? params[1] : "");
                }
            }
        }

        return queryParams;
    }

    public static String getMojoTargetPath(String url) {
        String path = url;
        int index = path.indexOf('?');
        if (index > 0) {
            path = url.substring(0, index);
        }


        index = url.indexOf(RHEOTV_APP);
        if (index >= 0) {
            return path.substring(index + RHEOTV_APP.length());
        }

        index = url.indexOf(HTTP_RHEOTV);
        if (index >= 0) {
            return path.substring(index + HTTP_RHEOTV.length());
        }

        index = url.indexOf(HTTPS_RHEOTV);
        if (index >= 0) {
            return path.substring(index + HTTPS_RHEOTV.length());
        }
        index = url.indexOf(HTTP_WWW_RHEOTV);
        if (index >= 0) {
            return path.substring(index + HTTP_WWW_RHEOTV.length());
        }

        index = url.indexOf(HTTPS_WWW_RHEOTV);
        if (index >= 0) {
            return path.substring(index + HTTPS_WWW_RHEOTV.length());
        }
        return "";
    }

    public static String getPostId(String url) {
        if (url == null || url.isEmpty()) return "";
        int index = url.lastIndexOf("/");
        String subString = url.substring(0, index);
        int secondLastIndex = subString.lastIndexOf("/");
        return subString.substring(secondLastIndex + 1);
    }

    public static String getQueryParamValue(String url, String key) {
        Map<String, String> queryParams = getQueryParams(url);
        if (queryParams.containsKey(key)) {
            return queryParams.get(key);
        }
        return null;
    }

    private static String getExtraParamsStr(String url) {
        String path = "";
        int index = url.indexOf('?');
        if (index > 0) {
            path = url.substring(index);
        }
        return path;
    }


    private static void handlePost(Activity activity, String postUID) {

    }

    public static String getIntentOpenUrl() {
        return intentOpenUrl;
    }

    public static void setIntentOpenUrl(String intentOpenUrl) {
        LinkHandler.intentOpenUrl = intentOpenUrl;
    }

    public static void triggerLink(AppCompatActivity activity) {
        if (!getIntentOpenUrl().isEmpty()) {
            handleLink(activity, getIntentOpenUrl());
        }
    }

}
