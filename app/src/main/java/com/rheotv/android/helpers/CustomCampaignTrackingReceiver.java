package com.rheotv.android.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.rheotv.android.utils.CommonUtils.getHashMapFromQuery;

public class CustomCampaignTrackingReceiver extends BroadcastReceiver {
    public static final String INSTALL_REFERRER = "INSTALL REFERRER";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Pass the intent to other receivers.
        if (CommonUtils.isInstallRefererEventTracked()) return;
        Bundle extras = intent.getExtras();
        HashMap<String, Object> properties = new HashMap<>();
        try {
            assert extras != null;
            String referrerString = extras.getString("referrer");
            if (referrerString != null && !referrerString.isEmpty()) {
                Map<String, String> getParams = getHashMapFromQuery(Objects.requireNonNull(referrerString));
                for (String key : getParams.keySet()) {
                    String value;
                    if ((value = getParams.get(key)) != null && !value.isEmpty())
                        properties.put(key, value);
                }
            }

            Log.i("Campaign_keys", "Install referrer event logged");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SegmentTracker.getInstance(context).trackEvent(INSTALL_REFERRER, properties);
            CommonUtils.setInstallRefererEventTracked(true);
        }
    }
}