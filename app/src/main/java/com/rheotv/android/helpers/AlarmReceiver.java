package com.rheotv.android.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rheotv.android.utils.SharedPrefsUtils;

import java.util.HashMap;
import java.util.Map;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(AlarmReceiver.class.getCanonicalName(), "Alarm Receiver Received");
        MyFirebaseMessagingService service = new MyFirebaseMessagingService();
        Map<String, String> data = new HashMap<>();
        data.put("title", intent.getStringExtra("title"));
        data.put("image_url", intent.getStringExtra("image_url"));
        data.put("body", intent.getStringExtra("body"));
        data.put("target_url", intent.getStringExtra("target_url"));
        String postId = intent.getStringExtra("post_id");
        SharedPrefsUtils prefsUtils = new SharedPrefsUtils();
        String reminderIds = prefsUtils.getStringPreference(context, SharedPrefsUtils.REMINDER_SET_POST_ID);
        if (reminderIds != null) {
            prefsUtils.setStringPreference(context, SharedPrefsUtils.REMINDER_SET_POST_ID, removePostId(reminderIds, postId));
        }

        service.buildNotification(data, context);
    }

    private String removePostId(String reminderIds, String postId) {
        StringBuilder builder = new StringBuilder();
        String[] reminderIdsArray = reminderIds.split(",");
        for (String item : reminderIdsArray) {
            if (!item.equalsIgnoreCase(postId)) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(item);
            }
        }
        return builder.toString();
    }
}
