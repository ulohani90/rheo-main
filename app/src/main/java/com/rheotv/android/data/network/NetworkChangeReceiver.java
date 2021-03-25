package com.rheotv.android.data.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager
                    .getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                //TODO: Update UI when internet is available
            } else {
                //TODO: Update UI when internet is not available
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
