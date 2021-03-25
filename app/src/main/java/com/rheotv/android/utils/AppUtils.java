/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 12:38 PM
 *
 */

package com.rheotv.android.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;

import androidx.core.util.Predicate;

import com.rheotv.android.R;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

public final class AppUtils {

    private AppUtils() {
        // This class is not publicly instantiable
    }

    public static void openPlayStoreForApp(Context context) {
        final String appPackageName = context.getPackageName();
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(context
                            .getResources()
                            .getString(R.string.app_market_link) + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(context
                            .getResources()
                            .getString(R.string.app_google_play_store_link) + appPackageName)));
        }
    }


    public static void startActivity(Activity activity, Class destination, boolean endPrevious) {
        Intent intent = new Intent(activity, destination);

        if (endPrevious) {
            activity.finish();
        }
        activity.startActivity(intent);
    }

    public static String getAppVersionNumber(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int randomColorForText(Context context, int position) {
        switch (position) {
            case 0:
                return context.getResources().getColor(R.color.light_pink);

            case 1:
                return context.getResources().getColor(R.color.light_musturd);

            case 2:
                return context.getResources().getColor(R.color.color_streamer_first_place);

            case 3:
                return context.getResources().getColor(R.color.orange);

            case 4:
                return context.getResources().getColor(R.color.grad1);

            case 5:
                return context.getResources().getColor(R.color.background_color);

            default:
                return context.getResources().getColor(R.color.light_bright_green);


        }
    }


    public static boolean hasPostId(String[] remindedPosts, String id) {
        for (String postId : remindedPosts) {
            if (id.equalsIgnoreCase(postId)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static void changeTopDrawable(TextView tv, int resId) {
        Drawable drawableTop = tv.getContext().getResources().getDrawable(resId);
        drawableTop.setBounds(0, 0, drawableTop.getIntrinsicWidth(), drawableTop.getIntrinsicHeight());
        tv.setCompoundDrawables(null, drawableTop, null, null);
    }

    public static String getClipShareUrl(String id) {
        StringBuilder builder = new StringBuilder();
        builder.append("http://www.rheotv.com/content/clips/" + id);
        return builder.toString();
    }

    public static String getStoryShareUrl(String id) {
        StringBuilder builder = new StringBuilder();
        builder.append("https://rheotv.com/api/content/stories/?story_id=" + id);
        return builder.toString();
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static double getRandomDoubleBetweenRange(double min, double max) {
        double x = (Math.random() * ((max - min) + 1)) + min;
        return x;
    }

    public static String getIPAddress() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                if (networkInterface.getHardwareAddress() != null && networkInterface.getHardwareAddress().length > 0) {
                    Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
                    while (inetAddressEnumeration.hasMoreElements()) {
                        InetAddress inetAddress = inetAddressEnumeration.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.getHostAddress().contains(":")) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getMACAddress() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                if (networkInterface.getHardwareAddress() != null && networkInterface.getInetAddresses() != null && networkInterface.getInetAddresses().hasMoreElements()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (byte mac : networkInterface.getHardwareAddress()) {
                        stringBuilder.append(String.format("%02X:", mac));
                    }
                    if (stringBuilder.length() > 0) {
                        return stringBuilder.substring(0, stringBuilder.length() - 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static <T> void filterList(List<T> list, Predicate<T> condition) {
        for (int i = 0; i < list.size(); i++) {
            if (condition.test(list.get(i))) {
                list.remove(i);
                i--;
            }
        }
    }

    public static String getUserRedeemUrl() {
        return "https://www.rheotv.com/redeem/username/".replace("username", CommonUtils.getUserName());
    }
}
