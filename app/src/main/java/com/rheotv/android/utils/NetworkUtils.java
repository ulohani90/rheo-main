/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 12:38 PM
 *
 */

package com.rheotv.android.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.telephony.TelephonyManager;

import com.google.android.material.snackbar.Snackbar;

public final class NetworkUtils {

    public static final String NOTIFICATION_CHANNEL_ID = "com.rheo.android";
    public static final String NOTIFICATION_CHANNEL_NAME = "Rheo TV";
    public static final int CONNECTION_WIFI_CONNECTED = 1;
    public static final int CONNECTION_WWAN_CONNECTED = 2;
    public static final int CONNECTION_NOT_CONNECTED = 3;
    private final static String CONNECTIVITY_ACTION = "CONNECTIVITY_ACTION";
    public static String DEVICE_ID_ENCRYPTION_KEY = "S1A3A7V1N19P20I3";
    public static Snackbar downloadSBar;
    public static String userName = "";
    public static String userArea = "";
    public static String phoneNumber = "";
    private static WifiManager.WifiLock wifiLock;
    private static PowerManager.WakeLock powerLock;
    private static BroadcastReceiver connectionChangeReciever;
    private static boolean launchConnectionChange = true;

    private NetworkUtils() {
        // This class is not publicly instantiable
    }

    public static boolean isNetworkConnected(Context context) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    public static int getConnectionStatus(Context context) {
        try {
            ConnectivityManager connectManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectManager == null) {
                return CONNECTION_NOT_CONNECTED;
            }
            NetworkInfo netInfo = connectManager.getActiveNetworkInfo();

            if (netInfo == null || !netInfo.isConnectedOrConnecting()) {
                return CONNECTION_NOT_CONNECTED;
            } else {
                if ((netInfo.getType() == ConnectivityManager.TYPE_WIFI) || (netInfo.getType() == ConnectivityManager.TYPE_WIMAX)) {
                    return CONNECTION_WIFI_CONNECTED;
                }
                if ((netInfo.getType() == ConnectivityManager.TYPE_MOBILE) || (netInfo.getType() == ConnectivityManager.TYPE_MOBILE_DUN) || (netInfo.getType() == ConnectivityManager.TYPE_MOBILE_HIPRI) ||
                        (netInfo.getType() == ConnectivityManager.TYPE_MOBILE_SUPL)
                        || (netInfo.getType() == ConnectivityManager.TYPE_MOBILE_MMS)) {
                    return CONNECTION_WWAN_CONNECTED;
                }
            }
            return CONNECTION_NOT_CONNECTED;
        } catch (Exception e) {
            e.printStackTrace();
            return CONNECTION_NOT_CONNECTED;
        }
    }

    public static String getNetworkType(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return NETWORK_TYPE_WIFI;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to mobile data
                return NETWORK_TYPE_MOBILE;
            }
        } else {
            return NETWORK_UNKNOWN;
            // not connected to the internet
        }

        return NETWORK_UNKNOWN;
    }

    public static String getNetworkGeneration(Context context) {

        if (getConnectionStatus(context) == CONNECTION_WIFI_CONNECTED) {
            return NETWORK_QUALITY_HIGH;
        }

        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephonyManager == null) {
            return NETWORK_UNKNOWN;
        }
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_QUALITY_LOW;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_QUALITY_MEDIUM;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK_QUALITY_HIGH;
            default:
                return NETWORK_UNKNOWN;
        }

    }

    public static final String NETWORK_QUALITY_HIGH = "HIGH";
    public static final String NETWORK_QUALITY_MEDIUM = "MED";
    public static final String NETWORK_QUALITY_LOW = "LOW";
    public static final String NETWORK_UNKNOWN = "UNKNOWN";
    public static final String NETWORK_TYPE_MOBILE = "MOBILE";
    public static final String NETWORK_TYPE_WIFI = "WIFI";
}
