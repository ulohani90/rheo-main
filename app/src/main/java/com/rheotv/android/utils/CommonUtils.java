/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 12:38 PM
 *
 */

package com.rheotv.android.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.common.base.Joiner;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.annotations.SerializedName;
import com.moe.pushlibrary.MoEHelper;
import com.moengage.core.model.AppStatus;
import com.rheotv.android.BuildConfig;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;
import com.rheotv.android.data.network.models.postlisting.responses.User;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.ui.adapters.LevelType;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import goChat.Services;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;


public final class CommonUtils {

    private CommonUtils() {
        // This utility class is not publicly instantiable
    }

    public static String STAR_ALIEN_PIC = "https://storage.googleapis.com/unheard-files/common/default_gamer_profile.png";
    public static String SAFE_CHAT_ID = "safe_chat";

    public static String getDeviceSerialNumber() {

        String serialNo = null;
        try {
            serialNo = Build.SERIAL;
            if (serialNo != null && !serialNo.equals(""))
                return serialNo;

            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            serialNo = (String) (get.invoke(c, "ro.serialno", ""));
            if (serialNo != null && !serialNo.equals(""))
                return serialNo;

            serialNo = (String) get.invoke(c, "sys.serialnumber", "");
            if (serialNo != null && !serialNo.equals(""))
                return serialNo;

            serialNo = (String) get.invoke(c, "ril.serialnumber", "");
            if (serialNo != null && !serialNo.equals(""))
                return serialNo;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String encryptString(String value, String key) {

        try {
            Cipher c = Cipher.getInstance("AES");
            SecretKeySpec k = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            c.init(Cipher.ENCRYPT_MODE, k);
            byte[] encBytes = c.doFinal(value.getBytes("UTF-8"));
            String encStr = new String(Base64.encode(encBytes, Base64.NO_WRAP), "UTF-8");
            return encStr;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }


    public static boolean isUserLoggedin() {
        SharedPrefsUtils prefsUtils = new SharedPrefsUtils();
        return prefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_LOGGED_IN, false);
        /*FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) {
            return false;
        }
        String email = user.getEmail();
        return true;*/
    }

    public static String getUserEmailAddress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) {
            return null;
        }
        return user.getEmail();
    }


    public static String getPlural(String word, int count) {
        count = Math.abs(count);
        if (count == 1) {
            return count + " " + word;
        } else if (count > 1) {
            return count + " " + word + "s";
        }
        return "";
    }

    public static String getPlural(String word, int count, String viewText) {
        count = Math.abs(count);
        if (count == 1) {
            return viewText + " " + word;
        } else if (count > 1) {
            return viewText + " " + word + "s";
        }
        return "";
    }

    public static String pluralise(double number, String key) {
        if (number > 1)
            return key + "s";
        else
            return key;
    }

    public static String getPluralSpan(String word, int count, String viewText) {
        count = Math.abs(count);
        if (count == 1) {
            return viewText + " " + word;
        } else if (count > 1) {
            return viewText + " " + word + "s";
        }
        return "";
    }

    public static String getFormattedNumberString(int count, String suffix) {
        return getPlural(suffix, count, formatValue(count));
    }

    public static String formatValue(double value) {
        char[] suffix = {' ', 'K', 'M', 'B', 'T', 'P', 'E'};
        long numValue = (long) value;
        int value1 = (int) Math.floor(Math.log10(numValue));
        int base = value1 / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.#").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }

    public static String getPlural(String word, String count, String viewText) {
        int c = 0;
        try {
            c = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return getPlural(word, c, viewText);
    }

    public static Bitmap getBitmapFromUrl(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 2;
        return BitmapFactory.decodeFile(path, options);
    }

    public static String getUserName() {
        return getUserName(getNonUiContext());
    }

    public static String getUserName(Context context) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            String userName = sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.USER_NAME);
            if (userName == null || userName.isEmpty()) {
                return getDevId(context);
            }
            return userName;
        }
        return "";
    }

    public static void setUserName(String username) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setStringPreference(getNonUiContext(), SharedPrefsUtils.USER_NAME, username);
    }

    public static String getUserProfilePic() {
        return getUserProfilePic(getNonUiContext());
    }

    public static String getUserProfilePic(Context context) {
        if (context == null) context = RheoTvApp.getNonUiContext();
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        String userProfilePic = sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.USER_PROFILE_PIC);
        if (userProfilePic == null || userProfilePic.isEmpty()) {
            return "";
        }
        return userProfilePic;
    }

    public static int getUserID() {
        return getUserID(getNonUiContext());
    }

    public static int getUserID(Context context) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getIntegerPreference(context, SharedPrefsUtils.USER_ID, 0);
    }

    public static String getDevId() {
        return getDeviceId(RheoTvApp.getNonUiContext());
    }

    @SuppressLint("all")
    public static String getDevId(Context context) {
        if (context == null)
            context = RheoTvApp.getNonUiContext();
        return getDeviceId(context);

//        SharedPreferences mPrefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE);
//        String deviceId = mPrefs.getString("device_id", "none");
//
//        if (deviceId == null || deviceId.equals("") || deviceId.equals("none")) {
//            // Nothing in pref. Probably fresh install
//            String unencDevice_id = "";
//            String serialNo = getDeviceSerialNumber();
//            if (serialNo == null || serialNo.equals(""))
//                serialNo = "Not_available";
//
//            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//            if (android_id == null || android_id.equals("")) {
//                android_id = "Not_available";
//            }
//
//            if (!android_id.equals("Not_available") || !serialNo.equals("Not_available")) {
//                // you have some data, atleast
//                unencDevice_id = serialNo + "-" + android_id;
//            } else {
//                String buildModel = Build.MODEL;
//                if (buildModel == null)
//                    buildModel = "unknown";
//                String manufacturer = Build.MANUFACTURER;
//                if (manufacturer == null)
//                    manufacturer = "unknown";
//                unencDevice_id = "Mojo." + buildModel + "." + manufacturer + "." + currentTimeMillis();
//            }
//
//            String encDevice_id = encryptString(unencDevice_id, NetworkUtils.DEVICE_ID_ENCRYPTION_KEY);
//            if (encDevice_id == null) {
//                try {
//                    unencDevice_id = URLEncoder.encode(unencDevice_id, "UTF-8");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                new SharedPrefsUtils().setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.deviceId, unencDevice_id);
//                return unencDevice_id;
//            }
//
//            try {
//                encDevice_id = URLEncoder.encode(encDevice_id, "UTF-8");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            new SharedPrefsUtils().setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.deviceId, encDevice_id);
//            return encDevice_id;
//
//        } else {
//            return deviceId;
//        }
    }

    public static void setAskReadPhoneState(Context context) {
        if (context == null) return;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(context, SharedPrefsUtils.ASK_PHONE_STATE_PERMISSION, false);
    }

    public static boolean shouldAskReadPhoneState(Context context) {
        if (context == null) return false;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getBooleanPreference(context, SharedPrefsUtils.ASK_PHONE_STATE_PERMISSION, true);
    }

    public static void setSavedDeviceId(Context context, String deviceId) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setStringPreference(context, SharedPrefsUtils.DEVICE_ID, deviceId);
        }
    }

    public static String getSavedDeviceId(Context context) {
        if (context == null) return null;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.DEVICE_ID);
    }

    public static String getDeviceId(Context context) {
        if (context == null) return null;
        String savedDeviceId = getSavedDeviceId(context);
        if (savedDeviceId != null)
            return savedDeviceId;

        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        setSavedDeviceId(context, deviceId);
        return deviceId;
    }


    public static String getCapital(String sentence) {
        StringBuilder result = new StringBuilder(sentence.length());
        String words[] = sentence.split("\\ ");
        for (int i = 0; i < words.length; i++) {
            result.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1)).append(" ");

        }
        return result.toString();
    }

    public static String getTimestamp() {
        return new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.US).format(new Date());
    }

    public static boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static String loadJSONFromAsset(Context context, String jsonFileName) throws IOException {
        AssetManager manager = context.getAssets();
        InputStream is = manager.open(jsonFileName);

        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        return new String(buffer, "UTF-8");
    }

    private static boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
                : false;
    }

    public static List<PackageInfo> getGamePackageInfoList(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> allPackageList = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        List<PackageInfo> packageList = new ArrayList<PackageInfo>();

        /*To filter out System apps*/
        for (PackageInfo packageInfo : allPackageList) {
            if (!isSystemPackage(packageInfo)) {
                String packageName = packageInfo.packageName;
                packageList.add(packageInfo);
            }
        }
        return packageList;
    }

    public static ProgressDialog showLoadingDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    public static boolean isPermissionGrantedForStreaming(Context context) {
        String permissions[] = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void showKeyboard(Context context, EditText editText) {
        if (context == null) return;
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboardFrom(Context context, View view) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //shift in viewmodel.
    public static MultipartBody.Part getMultiPartFile(Context context, Bitmap thumbnail, String imageKey) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        String fileName = System.currentTimeMillis() + ".jpg";
        File destination = new File(context.getCacheDir(), fileName);

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
            RequestBody reqFile = RequestBody.create(MediaType.parse("story/jpeg"), destination);
            MultipartBody.Part result = MultipartBody.Part.createFormData(imageKey, URLEncoder.encode(fileName, "utf-8"), reqFile);
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPathFromUrl(Activity activity, Uri originalUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.managedQuery(originalUri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
       /* String pathsegment[] = originalUri.getLastPathSegment().split(":");
        String id = pathsegment[0];
        final String[] imageColumns = {MediaStore.Images.Media.DATA};
        final String imageOrderBy = null;

        Uri uri = getUri();
        Cursor imageCursor = activity.getContentResolver().query(uri, imageColumns,
                MediaStore.Images.Media._ID + "=" + id, null, null);

        if (imageCursor.moveToFirst()) {
            return imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
        }

        return null;*/
    }

    public static Uri getUri() {
        String state = Environment.getExternalStorageState();
        if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public static String getUrlWithoutParameters(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return new URI(uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                null, // Ignore the query part of the input url
                uri.getFragment()).toString();
    }

    public static String getQueryParamValue(String url, String queryParam) {
        Uri uri = Uri.parse(url);
        Set<String> qpms = uri.getQueryParameterNames();
        return uri.getQueryParameter(queryParam);
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max)
            throw new IllegalArgumentException("max must be greater than min");

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static String convertSecondsToMmSs(long milliSec) {
        long sec = (milliSec / 1000) % 60;
        long min = ((milliSec / 1000) / 60) % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public static String convertToMmSs(long milliSec) {
        String message = "New reward will be available in ";
        long sec = (milliSec / 1000) % 60;
        long min = ((milliSec / 1000) / 60) % 60;
        if (min != 0 && sec != 0)
            message += min + " minutes " + sec + " seconds";
        else if (min != 0)
            message += min + " minutes";
        else if (sec != 0)
            message += sec + " seconds";
        else message = "Reward is available now";
        return message;
    }

    public static String convertToMinAndSec(long milliSec) {
        String message = "";
        long sec = (milliSec / 1000) % 60;
        long min = ((milliSec / 1000) / 60) % 60;

        if (!RewardManager.getInstance().isTenMinuteStreamRewardAvailable())
            message = "You're Rewarded";
        else if (min != 0 && sec != 0)
            message += min + " Minutes " + sec + " Seconds";
        else if (min != 0)
            message += min + " Minutes";
        else if (sec != 0)
            message += sec + " Seconds";
        else
            message = "Reward is available now";
        return message;
    }

    public static String convertToRewardFriendly(long milliSec) {
        String message = "";
        long sec = (milliSec / 1000) % 60;
        long min = ((milliSec / 1000) / 60) % 60;

        if (!RewardManager.getInstance().isTenMinuteStreamRewardAvailable())
            message = "Rewarded";
        else if (min != 0 && sec != 0)
            message += min + " min " + sec + " sec";
        else if (min != 0)
            message += min + " min";
        else if (sec != 0)
            message += sec + " sec";
        else
            message = "Reward Unlocked";
        return message;
    }

    public static String getNumberOrdinal(int i) {
        String[] sufixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }

    public static String getNumberOrdinal(String i) {
        int num = i != null ? Integer.parseInt(i) : 0;
        return getNumberOrdinal(num);
    }

    public static boolean showVideoTooltip(Context context) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            return sharedPrefsUtils.getBooleanPreference(context, SharedPrefsUtils.SHOW_VIDEO_TOOL_TIP, true);
        }
        return false;
    }

    public static void enableVideoTooltip(Context context) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setBooleanPreference(context, SharedPrefsUtils.SHOW_VIDEO_TOOL_TIP, true);
        }
    }

    public static void disableVideoTooltip(Context context) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setBooleanPreference(context, SharedPrefsUtils.SHOW_VIDEO_TOOL_TIP, false);
        }
    }

    public static boolean showCommentTooltip(Context context) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            return sharedPrefsUtils.getBooleanPreference(context, SharedPrefsUtils.SHOW_COMMENT_TOOL_TIP, true);
        }
        return false;
    }

    public static void enableCommentTooltip(Context context) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setBooleanPreference(context, SharedPrefsUtils.SHOW_COMMENT_TOOL_TIP, true);
        }
    }

    public static void disableCommentTooltip(Context context) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setBooleanPreference(context, SharedPrefsUtils.SHOW_COMMENT_TOOL_TIP, false);
        }
    }

    public static void resetTrainingTooltip(Context context) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setBooleanPreference(context, SharedPrefsUtils.SHOW_VIDEO_TOOL_TIP, true);
            sharedPrefsUtils.setBooleanPreference(context, SharedPrefsUtils.SHOW_COMMENT_TOOL_TIP, true);
        }
    }

    public static boolean shouldShowRateUs(Context context) {
        if (!isAppRated(context)) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            int count = sharedPrefsUtils.getIntegerPreference(context, SharedPrefsUtils.SHOW_RATE_US_DIALOG, 0);
            Log.i("CommonUtils", "shouldShowRateUs: " + count);
            if (count == 3) return true;
            else return count > 0 && count % 6 == 0;
        }

        return false;
    }

    public static boolean shouldRateNow(Context context) {
        return RewardManager.getInstance().isRatingAvailable() && isRatingAskedToday(context);
    }

    private static boolean isRatingAskedToday(Context context) {
        if (context != null && isUserLoggedin()) {
            long lastShownTimeDiff = (System.currentTimeMillis() - getLastRatingAskedTime(context));
            if (lastShownTimeDiff >= TimeUtils.MILLIS_IN_DAY) {
                setLastRatingAskedTime(context, System.currentTimeMillis());
                return true;
            }
        }
        return false;
    }

    private static long getLastRatingAskedTime(Context context) {
        if (context != null) {
            return new SharedPrefsUtils().getLongPreference(context, SharedPrefsUtils.LAST_SHOW_RATING_PERMISSION_TS, 0);
        }

        return 0;
    }

    public static void setLastRatingAskedTime(Context context, long ttl) {
        if (context != null) {
            new SharedPrefsUtils().setLongPreference(context, SharedPrefsUtils.LAST_SHOW_RATING_PERMISSION_TS, ttl);
        }
    }

    public static void incrementRateAppSession(Context context) {
        if (context != null && isUserLoggedin()) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            int count = sharedPrefsUtils.getIntegerPreference(context, SharedPrefsUtils.SHOW_RATE_US_DIALOG, 0) + 1;
            sharedPrefsUtils.setIntegerPreference(context, SharedPrefsUtils.SHOW_RATE_US_DIALOG, count);
            Log.i("CommonUtils", "incrementRateAppSession: " + count);
        }
    }

    public static void markAppRated(Context context) {
        if (context != null && isUserLoggedin()) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setBooleanPreference(context, SharedPrefsUtils.APP_RATED, true);
        }
    }

    private static boolean isAppRated(Context context) {
        if (context != null && isUserLoggedin()) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            return sharedPrefsUtils.getBooleanPreference(context, SharedPrefsUtils.APP_RATED, false);
        }

        return false;
    }


    public static Map<String, String> getHashMapFromQuery(String query)
            throws UnsupportedEncodingException, StringIndexOutOfBoundsException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        query = URLDecoder.decode(query, "UTF-8");
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx >= 0) {
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                        URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                Log.i("Campaign_keys", URLDecoder.decode(pair.substring(0, idx), "UTF-8") + " :: " + URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        }
        return query_pairs;
    }

    public static Map<String, String> getHashMapFromQueryFirebase(String query)
            throws UnsupportedEncodingException, StringIndexOutOfBoundsException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String refrrerString = CommonUtils.getReferrerString(query);
        if (refrrerString != null && !refrrerString.isEmpty()) {
            return getHashMapFromQuery(refrrerString);
        }

        return query_pairs;
    }

    public static Map<String, String> getQueryParams(String url) {
        try {
            Map<String, String> params = new HashMap<>();
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1) {
                String query = urlParts[1];
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = "";
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], "UTF-8");
                    }
                    params.put(key, value == null ? "" : value);
                }
            }
            return params;
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String getFormattedTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        return sdf.format(timestamp);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getFormattedDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(timestamp);
    }

    public static int toPix(int dm) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dm * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public static int getMoEngageAppTrackedVersion(Context context) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            return sharedPrefsUtils.getIntegerPreference(context, SharedPrefsUtils.MOENGAGE_TRACKED_VERSION, -1);
        }

        return 0;
    }

    public static void setMoEngageAppTrackedVersion(Context context, int version) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setIntegerPreference(context, SharedPrefsUtils.MOENGAGE_TRACKED_VERSION, version);
        }
    }

    /*public static void trackMoEngageInstallAndUpdateEvent(Context context) {
        if (context != null) {
            int currentVersion = getMoEngageAppTrackedVersion(context);
            if (currentVersion == -1) {
                MoEHelper.getInstance(context).setAppStatus(AppStatus.INSTALL);
                setMoEngageAppTrackedVersion(context, BuildConfig.VERSION_CODE);
                setNewAppUser(true);
            } else if (BuildConfig.VERSION_CODE > getMoEngageAppTrackedVersion(context)) {
                MoEHelper.getInstance(context).setAppStatus(AppStatus.UPDATE);
                setMoEngageAppTrackedVersion(context, BuildConfig.VERSION_CODE);
                setNewAppUser(false);
            }

            // else event is already tracked
        }
    }*/


    public static void trackAmplitudeInstallAndUpdateEvent(Context context) {
        if (context != null) {
            int currentVersion = getMoEngageAppTrackedVersion(context);
            if (currentVersion == -1) {
                HashMap<String, Object> properties = new HashMap<>();
                properties.put("build", BuildConfig.VERSION_CODE);
                properties.put("version", BuildConfig.VERSION_NAME);
                MoEHelper.getInstance(context).setAppStatus(AppStatus.INSTALL);
                setMoEngageAppTrackedVersion(context, BuildConfig.VERSION_CODE);
                setNewAppUser(true);
                SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_APP_INSTALL, properties);
                SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_APPLICATION_INSTALLED, properties);
            } else if (BuildConfig.VERSION_CODE > getMoEngageAppTrackedVersion(context)) {
                MoEHelper.getInstance(context).setAppStatus(AppStatus.UPDATE);
                setMoEngageAppTrackedVersion(context, BuildConfig.VERSION_CODE);
                setNewAppUser(false);
                SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_APP_UPDATE, new HashMap<>());
            }

            // else event is already tracked
        }
    }


    public static boolean isAndroidEmulator() {
        String product = Build.PRODUCT;
        boolean isEmulator = false;
        if (product != null)
            isEmulator = product.equals("sdk") || product.contains("_sdk") || product.contains("sdk_");
        Log.d("CommonUtils", "isEmulator=" + isEmulator);
        return isEmulator;
    }

    public static void setBranchExtraInfo(Context context, String campaign) {
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setStringPreference(context, SharedPrefsUtils.ARG_BRANCH_EXTRA_INFO, campaign);
        }
    }

    public static String getBranchExtraInfo(Context context) {
        if (context == null) return null;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.ARG_BRANCH_EXTRA_INFO);
    }

    public static void setUserLanguage(Context context, List language) {
        if (context != null && language != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            String lang = joinList(language);
            sharedPrefsUtils.setStringPreference(context, SharedPrefsUtils.ARG_USER_LANGUAGE, lang);
            SegmentTracker.getInstance().setIdentityLanguage(lang);
        }
    }

    public static void setUserLanguage(Context context, String language) {
        if (context != null && language != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setStringPreference(context, SharedPrefsUtils.ARG_USER_LANGUAGE, language);
            SegmentTracker.getInstance().setIdentityLanguage(language);
        }
    }

    private static String joinList(List<String> list) {
        if (list == null) return null;

//        StringBuilder sb = new StringBuilder();
//
//        for (String a: list) {
//            sb.append(a);
//            if (list.indexOf(a) == list.size() - 1)
//                sb.append(",");
//        }

        return Joiner.on(',').join(list);
    }

    public static String getUserLanguage() {
        return getUserLanguage(RheoTvApp.getNonUiContext());
    }

    public static String getUserLanguage(Context context) {
        if (context == null) return null;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.ARG_USER_LANGUAGE);
    }

    public static String mapToString(HashMap<String, String> map) {
        String tag = Joiner.on(',').join(map.values());
        Log.i("CommonUtils", "mapToString " + tag);
        return tag;
    }

    public static boolean isMoenageIdentitySet() {
        SharedPrefsUtils prefsUtils = new SharedPrefsUtils();
        return prefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_MOENGAGE_IDENTITY_SET, false);
    }

    public static void markMoenageIdentitySet() {
        SharedPrefsUtils prefsUtils = new SharedPrefsUtils();
        prefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_MOENGAGE_IDENTITY_SET, true);
    }

    public static void setMoenageUniqueId() {
        try {
            if (!isMoenageIdentitySet()) {
                if (isUserLoggedin())
                    MoEHelper.getInstance(getNonUiContext()).setUniqueId(getUserEmailAddress());
                else
                    MoEHelper.getInstance(getNonUiContext()).setUniqueId(getDevId(getNonUiContext()));
                markMoenageIdentitySet();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setInstalledFromBranchTracked() {
        SharedPrefsUtils prefsUtils = new SharedPrefsUtils();
        prefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_INSTALLED_FROM_TRACKED, false);
    }

    public static boolean isInstalledFromBranchTracked() {
        Context context = getNonUiContext();
        if (context != null) {
            SharedPrefsUtils prefsUtils = new SharedPrefsUtils();
            boolean isBoarded = prefsUtils.getBooleanPreference(context, SharedPrefsUtils.IS_ONBOARDING_DONE, false);
            boolean installedBefore = prefsUtils.getBooleanPreference(context, SharedPrefsUtils.IS_INSTALLED_FROM_TRACKED, false);
            return isBoarded && installedBefore;
        }

        return false;
    }

    public static String[] getMediaDetailFromIntent(Intent data, Context context) {
        try {
            Uri selectedUri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE};
            Cursor cursor = context.getContentResolver().query(selectedUri, projection, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();

                int pathColumnIndex = cursor.getColumnIndex(projection[0]);
                int mimeTypeColumnIndex = cursor.getColumnIndex(projection[1]);

                String contentPath = cursor.getString(pathColumnIndex);
                String mimeType = getMimeType(contentPath);
                if (mimeType == null)
                    mimeType = cursor.getString(mimeTypeColumnIndex);
                mimeType = mimeType.substring(0, mimeType.indexOf("/"));
                cursor.close();
                return new String[]{mimeType, contentPath};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getPath(final Context context, final Uri uri) {

        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            } else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author Deep Patel
     */
    private static String getDataColumn(@NonNull Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(@NonNull Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(@NonNull Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(@NonNull Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static File bitmapConvertToFile(Context context, Bitmap bitmap, String
            dirType, String subPath) {
        FileOutputStream fileOutputStream = null;
        File bitmapFile = null;

        try {
            File file = new File(Environment.getExternalStorageDirectory() + dirType);
            if (!file.exists()) {
                file.mkdir();
            }

            bitmapFile = new File(Environment.getExternalStorageDirectory() + dirType, subPath);
            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".app.provider", bitmapFile);

            if (fileUri != null) {
                fileOutputStream = new FileOutputStream(bitmapFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                MediaScannerConnection.scanFile(context, new String[]{bitmapFile.getAbsolutePath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                    @Override
                    public void onMediaScannerConnected() {

                    }

                    @Override
                    public void onScanCompleted(String s, Uri uri) {
                        Log.i("CommonUtils", "file saved");
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return bitmapFile;
    }

    public static void setAuthorId(String authorId) {
        Context context = getNonUiContext();
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setStringPreference(context, SharedPrefsUtils.AUTHOR_ID, authorId);
        }
    }

    public static String getAuthorId() {
        Context context = getNonUiContext();
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            return sharedPrefsUtils.getStringPreference(context, SharedPrefsUtils.AUTHOR_ID);
        }

        return null;
    }

    public static void setProfileImageUrl(String url) {
        Context context = getNonUiContext();
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setStringPreference(getNonUiContext(), SharedPrefsUtils.USER_PROFILE_PIC, url);
        }
    }

    public static void setIsUSerStreamer(boolean isStreamer) {
        Context context = getNonUiContext();
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_STREAMER, isStreamer);
        }
    }

    public static void setIsUserContentModerator(boolean isStreamer) {
        Context context = getNonUiContext();
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_CONTENT_MODERATOR, isStreamer);
        }
    }

    public static boolean isUserContentModerator() {
        Context context = getNonUiContext();
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            return sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_CONTENT_MODERATOR, false);
        }

        return false;
    }


    public static boolean isUserStreamer() {
        Context context = getNonUiContext();
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            return sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_STREAMER, false);
        }

        return false;
    }

    public static ProfileResult getSelfProfile() {
        return new ProfileResult(
                CommonUtils.getAuthorId(),
                CommonUtils.getUserProfilePic(),
                new User(CommonUtils.getUserID(), CommonUtils.getUserName()));
    }

    public static ProfileResult getWithoutLoginSelfStoryProfile() {
        return new ProfileResult(
                "me",
                "@drawable/avd_avatar",
                new User(CommonUtils.getUserID(), "Your Story"));
    }

    public static ProfileResult getSelfStoryProfile() {
        return new ProfileResult(
                CommonUtils.getAuthorId(),
                CommonUtils.getUserProfilePic(),
                new User(CommonUtils.getUserID(), "Your Story"));
    }

    public static boolean isNewAppUser() {
        Context context = getNonUiContext();
        if (context == null) return false;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_NEW_APP_USER, true);
    }

    public static void setNewAppUser(boolean flag) {
        Context context = getNonUiContext();
        if (context == null) return;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_NEW_APP_USER, flag);
    }

    public static boolean isFirstWatchEventNotTracked() {
        Context context = getNonUiContext();
        if (context == null) return false;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        boolean isFirstWatch = sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_WATCH_EVENT_TRACKED, false);
        return isNewAppUser() && !isFirstWatch;
    }

    public static boolean isFirstWatchEvent5MinsNotTracked() {
        Context context = getNonUiContext();
        if (context == null) return false;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        boolean isFirstWatch5Mins = sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_WATCH_EVENT_5_MINS_TRACKED, false);
        return isNewAppUser() && !isFirstWatch5Mins;
    }

    public static void setFirstWatchEvent5MinsTracked() {
        Context context = getNonUiContext();
        if (context == null) return;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_WATCH_EVENT_5_MINS_TRACKED, true);
    }


    public static void setFirstWatchEventTracked() {
        Context context = getNonUiContext();
        if (context == null) return;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_WATCH_EVENT_TRACKED, true);
    }

    public static boolean isFirstCommentSendNotTracked() {
        Context context = getNonUiContext();
        if (context == null) return false;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        boolean isFirstComment = sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_COMMENT_SEND_EVENT_TRACKED, false);
        return isNewAppUser() && !isFirstComment;
    }


    public static void setFirstCommentSentEventTracked() {
        Context context = getNonUiContext();
        if (context == null) return;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_COMMENT_SEND_EVENT_TRACKED, true);
    }

    public static boolean isFirstRequestToPlayNotTracked() {
        Context context = getNonUiContext();
        if (context == null) return false;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        boolean isFirstComment = sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_REQUEST_TO_PLAY_EVENT_TRACKED, false);
        return isNewAppUser() && !isFirstComment;
    }


    public static void setFirstRequestToPlayEventTracked() {
        Context context = getNonUiContext();
        if (context == null) return;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_REQUEST_TO_PLAY_EVENT_TRACKED, true);
    }


    public static boolean isFirstStoryClickedNotTracked() {
        Context context = getNonUiContext();
        if (context == null) return false;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        boolean isFirstComment = sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_STORY_CLICKED_EVENT_TRACKED, false);
        return isNewAppUser() && !isFirstComment;
    }


    public static void setFirstStoryClickedEventTracked() {
        Context context = getNonUiContext();
        if (context == null) return;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_STORY_CLICKED_EVENT_TRACKED, true);
    }


    public static boolean isFirstGoLiveGenerateKeyNotTracked() {
        Context context = getNonUiContext();
        if (context == null) return false;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        boolean isFirstGoLiveKeyGenerated = sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_GO_LIVE_GENERATE_KEY_EVENT_TRACKED, false);
        return isNewAppUser() && !isFirstGoLiveKeyGenerated;
    }


    public static void setFirstGoLiveGenerateKeyEventTracked() {
        Context context = getNonUiContext();
        if (context == null) return;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_GO_LIVE_GENERATE_KEY_EVENT_TRACKED, true);
    }

    public static boolean isFirstEventHomeViewNotTracked() {
        Context context = getNonUiContext();
        if (context == null) return false;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        boolean isFirstHomeViewTracked = sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_EVENT_HOMEVIEW_TRACKED, false);
        return isNewAppUser() && !isFirstHomeViewTracked;
    }


    public static void setFirstEventHomeViewEventTracked() {
        Context context = getNonUiContext();
        if (context == null) return;
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_EVENT_HOMEVIEW_TRACKED, true);
    }

    public static void setAnalyticsFileCount(int count) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setIntegerPreference(getNonUiContext(), SharedPrefsUtils.ANALYTICS_FILE_COUNT, count);
    }

    public static int getAnalyticsFileCount() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getIntegerPreference(getNonUiContext(), SharedPrefsUtils.ANALYTICS_FILE_COUNT, 0);
    }

    public static String getStreamQuality() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getStringPreference(getNonUiContext(), SharedPrefsUtils.VIDEO_FORMAT_REQUESTED);
    }

    public static void setStreamQuality(String quality) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setStringPreference(getNonUiContext(), SharedPrefsUtils.VIDEO_FORMAT_REQUESTED, quality);
    }

    public static CommentChat commentChatFromChat(Services.ChatMessage message) {
        return new CommentChat("", message.getMessage(), message.getSender(), message.getProfilePic(), "true".equalsIgnoreCase(message.getIsContentModerator()));
    }

    public static int getNumberFromFormat(String value) {
        int num = 0;
        if (value == null || value.isEmpty()) return num;
        if (value.toLowerCase().endsWith("k")) {
            return (int) (Float.valueOf(value.toLowerCase().replace("k", "").trim()) * 1000);
        } else if (value.toLowerCase().endsWith("m")) {
            return (int) (Float.valueOf(value.toLowerCase().replace("m", "").trim()) * 1000000);
        }
        return Integer.parseInt(value);
    }

    public static void setDirectVideoWatchUser() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.DIRECT_VIDEO_WATCH_USER, true);
    }

    public static boolean isDirectVideoWatchUser() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.DIRECT_VIDEO_WATCH_USER, false);
    }

    public static boolean isOnBoarded() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_ONBOARDING_DONE, false);
    }

    public static void copyToClipboard(Context context, String label, String copyText) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, copyText);
        clipboard.setPrimaryClip(clip);
    }

    public static boolean isShareTutorialShown() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_SHARE_TUTORIAL_SHOWN, false);
    }

    public static void setShareTutorialShown() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_SHARE_TUTORIAL_SHOWN, true);
    }

    public static String firstShareRewardState() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        String state = sharedPrefsUtils.getStringPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_SHARE_DONE);
        return state != null ? state : AppConstants.SHARE_PENDING;
    }

    public static void markFirstShareDone() {
        if (firstShareRewardState().equals(AppConstants.SHARE_AVAILABLE)) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setStringPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_SHARE_DONE, AppConstants.SHARE_DONE);
        }
    }

    public static void markFirstShareAvailable() {
        if (firstShareRewardState().equals(AppConstants.SHARE_PENDING)) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setStringPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_SHARE_DONE, AppConstants.SHARE_AVAILABLE);
        }
    }

    public static void setPaymentModel(int paymentModel) {
        Context context = getNonUiContext();
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            sharedPrefsUtils.setIntegerPreference(getNonUiContext(), SharedPrefsUtils.PAYMENT_MODEL, paymentModel);
        }
    }

    public static int getPaymentModel(Context context) {
        if (context == null)
            context = getNonUiContext();
        if (context != null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            return sharedPrefsUtils.getIntegerPreference(getNonUiContext(), SharedPrefsUtils.PAYMENT_MODEL, 1);
        }
        return 1;
    }

    public static LevelType getLevelType(Context context) {
        if (context == null) context = getNonUiContext();
        if (context != null) {
            String levelType = new SharedPrefsUtils().getStringPreference(context, SharedPrefsUtils.LEVEL_TYPE);
            return LevelType.Companion.getLevelType(levelType);
        }
        return LevelType.Unassigned.INSTANCE;
    }

    public static void setLevelType(LevelType levelType) {
        Context context = getNonUiContext();
        if (context != null) {
            new SharedPrefsUtils().setStringPreference(context, SharedPrefsUtils.LEVEL_TYPE, levelType.toString());
        }
    }

    public static String getClipLink(String id) {
        return "content/clips/" + id;
    }

    public static boolean isLocalUri(@NonNull String uri) {
        return !uri.startsWith("https");
    }

    public static boolean isFirstShare() {
        return isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_SHARE, false);
    }

    public static void setFirstShare() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_SHARE, isNewAppUser());
    }

    public static boolean isReLogin() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_RE_LOGIN, false);
    }

    public static void setReLogin() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_RE_LOGIN, !isNewAppUser());
    }

    public static boolean isFirstPlayRequest() {
        return isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_PLAY_REQUEST, false);
    }

    public static void setFirstPlayRequest() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_PLAY_REQUEST, isNewAppUser());
    }

    public static boolean isFirstTimePageChange() {
        return isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_PAGE_CHANGE, false);
    }

    public static void setFirstTimePageChange() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_PAGE_CHANGE, isNewAppUser());
    }

    public static boolean isFirstTimeCoinsClicked() {
        return isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_COINS_CLICKED, false);
    }

    public static void setFirstTimeCoinsClicked() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_COINS_CLICKED, isNewAppUser());
    }

    public static boolean isFirstTimeLiveStreamKeyCreated() {
        return isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_LIVE_STREAM_KEY_CREATED, false);
    }

    public static void setFirstTimeLiveStreamKeyCreated() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_LIVE_STREAM_KEY_CREATED, isNewAppUser());
    }

    public static boolean isFirstStorySeen() {
        return isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_STORY_SEEN, false);
    }

    public static void setFirstStorySeen() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_STORY_SEEN, isNewAppUser());
    }

    public static boolean isFirstStickerSent() {
        return isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_STICKER_SENT, false);
    }

    public static void setFirstStickerSent() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_STICKER_SENT, isNewAppUser());
    }

    public static boolean isFirstTimeLiked() {
        return isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_LIKED, false);
    }

    public static void setFirstTimeLiked() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_LIKED, isNewAppUser());
    }

    public static boolean isFirsTimeSelfProfileVisited(boolean isSelf) {
        return isSelf && isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_SELF_PROFILE_VISITED, false);
    }

    public static void setFirsTimeSelfProfileVisited() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_SELF_PROFILE_VISITED, isNewAppUser());
    }

    public static boolean isFirstTimeWatchRewardScratched() {
        return isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_WATCH_REWARD_SCRATCHED, false);
    }

    public static void setFirstTimeWatchRewardScratched() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_WATCH_REWARD_SCRATCHED, isNewAppUser());
    }

    public static boolean isFirstTimeLeaderBoardClicked() {
        return isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_LEADER_BOARD_CLICKED, false);
    }

    public static void setFirstTimeLeaderBoardClicked() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_LEADER_BOARD_CLICKED, isNewAppUser());
    }

    public static boolean isFirstTimeFollow() {
        return isNewAppUser() && !new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_FOLLOW_CLICK, false);
    }

    public static void setFirstTimeFollow() {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_TIME_FOLLOW_CLICK, isNewAppUser());
    }

    public static boolean isInstallRefererEventTracked() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_INSTALL_REFERER_EVENT_TRACKED, false);
    }

    public static boolean isFirstTimeUser() {
        return getMoEngageAppTrackedVersion(getNonUiContext()) == -1;
    }

    public static void setInstallRefererEventTracked(boolean isTracked) {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_INSTALL_REFERER_EVENT_TRACKED, isTracked);
    }

    public static String getReferrerString(String url) {
        int indexOfReferrer = url.indexOf("referrer=");
        if (indexOfReferrer == -1)
            return null;
        return url.substring(indexOfReferrer + 9);
    }

    public static void convertObjToMapReflection(Object object) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Field[] allFields = object.getClass().getDeclaredFields();
        for (Field field : allFields) {
            if (field != null) {
                field.setAccessible(true);
                Object value = field.get(object);
                if (field.getAnnotation(SerializedName.class) != null) {
                    map.put(field.getAnnotation(SerializedName.class).value(), value);
                }
            }
        }
        System.out.println(map);
    }

    public static void setSplashBannerValidTillTS(long ts) {
        new SharedPrefsUtils().setLongPreference(getNonUiContext(), SharedPrefsUtils.SPLASH_VALID_TILL_TIMESTAMP, ts);
    }

    public static long getSplashBannerValidTillTS() {
        //return 1597429800000L;
        return new SharedPrefsUtils().getLongPreference(getNonUiContext(), SharedPrefsUtils.SPLASH_VALID_TILL_TIMESTAMP, -1);
    }

    public static void setSplashBannerUrl(String url) {
        new SharedPrefsUtils().setStringPreference(getNonUiContext(), SharedPrefsUtils.SPLASH_BANNER_URL, url);
    }

    public static void setSplashBannerTarget(String target) {
        new SharedPrefsUtils().setStringPreference(getNonUiContext(), SharedPrefsUtils.SPLASH_TARGET_URL, target);
    }

    public static String getSplashBannerUrl() {
        //return "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&w=1000&q=80";
        return new SharedPrefsUtils().getStringPreference(getNonUiContext(), SharedPrefsUtils.SPLASH_BANNER_URL);
    }

    public static String getSplashBannerTarget() {
        return new SharedPrefsUtils().getStringPreference(getNonUiContext(), SharedPrefsUtils.SPLASH_TARGET_URL);
    }

    public static void setSplashBannerAdName(String adName) {
        new SharedPrefsUtils().setStringPreference(getNonUiContext(), SharedPrefsUtils.SPLASH_AD_NAME, adName);
    }

    public static String getSplashBannerAdName() {
        return new SharedPrefsUtils().getStringPreference(getNonUiContext(), SharedPrefsUtils.SPLASH_AD_NAME);
    }


    public static void setContactsUploadSuccess(boolean isSuccess) {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.CONTACTS_UPLOAD_SUCCESS, isSuccess);
    }

    public static boolean getContactsUploadSuccess() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.CONTACTS_UPLOAD_SUCCESS, false);
    }

    public static void setContactAllowed(boolean allowed) {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.CONTACTS_UPLOAD_SUCCESS, allowed);
    }

    public static boolean getContactAllowed() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.CONTACTS_UPLOAD_SUCCESS, true);
    }

    public static void setHideSyncContacts(boolean hideSyncContacts) {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.HIDE_SYNC_CONTACTS, hideSyncContacts);
    }

    public static boolean getHideSyncContacts() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.HIDE_SYNC_CONTACTS, false);
    }

    public static String isContentModerator() {
        return isUserContentModerator() ? "true" : "false";
    }

    public static void updateSearchBadgeVisibilityCount() {
        new SharedPrefsUtils().setIntegerPreference(getNonUiContext(),
                SharedPrefsUtils.SEARCH_BADGE_APP_UP_COUNT,
                new SharedPrefsUtils().getIntegerPreference(getNonUiContext(), SharedPrefsUtils.SEARCH_BADGE_APP_UP_COUNT, 0) + 1);
    }

    public static int getSearchBadgeVisibilityAppUpCount() {
        return new SharedPrefsUtils().getIntegerPreference(getNonUiContext(), SharedPrefsUtils.SEARCH_BADGE_APP_UP_COUNT, 0);
    }

    public static void setPlayerMode(int mode) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setIntegerPreference(getNonUiContext(), SharedPrefsUtils.PLAYER_MODE, mode);
    }

    public static int getPlayerMode() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getIntegerPreference(getNonUiContext(), SharedPrefsUtils.PLAYER_MODE, 0);
    }

    public static int getAudioToastCount() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getIntegerPreference(getNonUiContext(), SharedPrefsUtils.AUDIO_MODE_TOAST_COUNT, 0);
    }

    public static void setAudioToastCount(int count) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setIntegerPreference(getNonUiContext(), SharedPrefsUtils.AUDIO_MODE_TOAST_COUNT, count);
    }

    public static void setAudioModeFlag(boolean enabled) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_AUDIO_MODE_ENABLED, enabled);
    }

    public static boolean getAudioModeFlag() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_AUDIO_MODE_ENABLED, true);
    }

    public static void setSelectedUser(boolean enabled) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_SELECTED_USER, enabled);
        SegmentTracker.getInstance().setIdentitySelectedUser(enabled);
    }

    public static boolean isSelectedUser() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
//        return sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_SELECTED_USER, false);
        return true;
    }

    public static void setTopShowUser(boolean enabled) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_TOP_SHOW_USER, enabled);
        SegmentTracker.getInstance().setIdentityTopShowUser(enabled);
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_SET_TOP_SHOW_USER, new HashMap<>());
    }

    public static boolean isTopShowUser() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_TOP_SHOW_USER, false);
//        return true;
    }

    public static void setUserWelcomed(boolean enabled) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_USER_WELCOMED, enabled);
    }

    public static boolean getUserWelcomed() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        return sharedPrefsUtils.getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_USER_WELCOMED, false);
    }

    public static void setNewUser(boolean isNewUser) {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_NEW_USER, isNewUser);
    }

    public static boolean isNewUser() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_NEW_USER, false);
    }

    public static boolean isPreferredLanguageBoardingUser() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_PREFERRED_LANGUAGE_ONBOARDING_USER, false);
    }

    public static void setPreferredLanguageBoardingUser(boolean flag) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_PREFERRED_LANGUAGE_ONBOARDING_USER, flag);
        SegmentTracker.getInstance().setIdentityPreferredLanguageUser(flag);
    }

    public static boolean isNewTopFanIconUser() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_NEW_TOP_FAN_ICON_USER, false);
    }

    public static void setNewTopFanIconUser(boolean flag) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_NEW_TOP_FAN_ICON_USER, flag);
        SegmentTracker.getInstance().setIdentityNewTopFanIconUse(flag);
    }

    public static boolean isFeaturedRoomEnabled() {
        //return true;
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FEATURED_ROOM_ENABLED, false);
    }

    public static void setFeaturedRoomEnabled(boolean flag) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FEATURED_ROOM_ENABLED, flag);
        SegmentTracker.getInstance().setIdentityFeaturedRoom(flag);
    }

    public static boolean isUnMuteAudioRoomTooltipShown() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_UN_MUTE_AUDIO_ROOM_TOOLTIP_SHOWN, false);
    }

    public static void setUnMuteAudioRoomTooltipShown() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_UN_MUTE_AUDIO_ROOM_TOOLTIP_SHOWN, true);
    }

    public static boolean isFirstAgoraAudioCallDone() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_AGORA_CALL_DONE, false);
    }

    public static void setFirstAgoraAudioCallDone() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_FIRST_AGORA_CALL_DONE, true);
    }

    public static void setLastCallChannelId(String channelId) {
        new SharedPrefsUtils().setStringPreference(getNonUiContext(), SharedPrefsUtils.LAST_CALLED_CHANNEL_ID, channelId);
    }

    public static String getLastCallChannelId() {
        return new SharedPrefsUtils().getStringPreference(getNonUiContext(), SharedPrefsUtils.LAST_CALLED_CHANNEL_ID);
    }

    public static void setRequestVideoCallDNDAlertShown() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_VIDEO_CALL_DND_ALERT_SHOWN, true);
    }

    public static boolean isRequestVideoCallDNDAlertShown() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_VIDEO_CALL_DND_ALERT_SHOWN, false);
    }

    public static void setOnBoardClipEnabled(boolean flag) {
        new SharedPrefsUtils().setBooleanPreference(getNonUiContext(), SharedPrefsUtils.ONBOARD_BACKGROUND_CLIP, flag);
        SegmentTracker.getInstance().setIdentityOnboardClipUser(flag);
    }

    public static boolean isOnBoardClipEnabled() {
//        return true;
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.ONBOARD_BACKGROUND_CLIP, false);
    }

    public static boolean isAddToCardEventAlreadyHit() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.ADD_TO_CART_EVENT_HIT, false);
    }

    public static void setAddToCartEventHit() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.ADD_TO_CART_EVENT_HIT, true);
    }

    public static boolean isAchievementUnlockedEventAlreadyHit() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.ACHIEVEMENT_UNLOCKED_EVENT_HIT, false);
    }

    public static void setAchievementUnlockedEventHit() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.ACHIEVEMENT_UNLOCKED_EVENT_HIT, true);
    }

    public static boolean isInstallRefererFBEventTracked() {
        return new SharedPrefsUtils().getBooleanPreference(getNonUiContext(), SharedPrefsUtils.INSTALL_REFERRER_FB_EVENT_TRACKED, false);
    }

    public static void setInstallReferrerFbEventTracked(boolean isInstallReferrerFBEventTracked) {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.INSTALL_REFERRER_FB_EVENT_TRACKED, isInstallReferrerFBEventTracked);
    }
}
