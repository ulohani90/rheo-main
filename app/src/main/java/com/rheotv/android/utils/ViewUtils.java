
/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 12:38 PM
 *
 */

package com.rheotv.android.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.rheotv.android.R;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;


public final class ViewUtils {
    public static Snackbar downloadSBar;
    private static String TAG = ViewUtils.class.getSimpleName();
    public static int MODE_EDGE_TO_EDGE = 2;

    private ViewUtils() {
        // This class is not publicly instantiable
    }

    public static void changeIconDrawableToGray(Context context, Drawable drawable) {
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static int dpToPx(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static int pxToDp(int px) {
        float densityDpi = Resources.getSystem().getDisplayMetrics().densityDpi;
        return (int) (px / (densityDpi / 160f));
    }

    public static void initSnackBar(View baseView, int length, String msg) {

        try {
            downloadSBar = Snackbar.make(baseView, msg, length);
            View view = ViewUtils.downloadSBar.getView();
//             set view color as red
            view.setBackgroundColor(baseView.getContext().getResources().getColor(R.color.colorPrimary));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean showSnackbar(Context context) {
        if (downloadSBar != null) {
            downloadSBar.show();
            return true;
        }
        return false;
//        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
//        return true;
    }

    public static void setSnackbarProgress(int progress, Context applicationContext) {

        try {
            if (true) {
                return;
            }
            Log.d(TAG, "download progress " + progress);
            if (downloadSBar != null) {
                if (progress < 100 && (progress % 12 == 0)) {
                    downloadSBar.setText(applicationContext.getResources().getString(R.string.downloading) + " " + progress + " %");
                    downloadSBar.show();
                } else if (100 <= progress) {
                    downloadSBar.setText(applicationContext.getResources().getString(R.string.downloading));
                    downloadSBar.show();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean dismissSnackbar() {
        try {
            if (downloadSBar != null) {
                downloadSBar.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    public static void showMessageOKCancel(Context context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(context.getResources().getString(R.string.all_good), okListener)
                .show();
    }


    public static int getTotalHeightofRecyclerViewInDP(RecyclerView recyclerView) {

        RecyclerView.Adapter mAdapter = recyclerView.getAdapter();

        int totalHeight = 0;

        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            View mView = recyclerView.findViewHolderForAdapterPosition(i).itemView;

            mView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            totalHeight += mView.getMeasuredHeight();
        }

        return pxToDp(totalHeight);
    }

    public static float getScreenWidthInDP(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / displayMetrics.density;

    }

    public static float getScreenWidthInPx(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static float getScreenHeightInDP(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels / displayMetrics.density;
    }

    public static float getScreenHeightInPx(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    public static String capitalize(String str) {
        if (str != null && str.length() > 1) {
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
        return str;
    }


    public static Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe) {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    /**
     * Compares two bitmaps and gives the percentage of similarity
     *
     * @param bitmap1 input bitmap 1
     * @param bitmap2 input bitmap 2
     * @return a value between 0.0 to 1.0 . Note the method will return 0.0 if either of bitmaps are null nor of same size.
     */
    public static float compareEquivalance(Bitmap bitmap1, Bitmap bitmap2) {
        if (bitmap1 == null || bitmap2 == null || bitmap1.getWidth() != bitmap2.getWidth() || bitmap1.getHeight() != bitmap2.getHeight()) {
            return 0f;
        }

        ByteBuffer buffer1 = ByteBuffer.allocate(bitmap1.getHeight() * bitmap1.getRowBytes());
        bitmap1.copyPixelsToBuffer(buffer1);

        ByteBuffer buffer2 = ByteBuffer.allocate(bitmap2.getHeight() * bitmap2.getRowBytes());
        bitmap2.copyPixelsToBuffer(buffer2);

        byte[] array1 = buffer1.array();
        byte[] array2 = buffer2.array();

        int len = array1.length; // array1 and array2 will be of some length.
        int count = 0;

        for (int i = 0; i < len; i++) {
            if (array1[i] == array2[i]) {
                count++;
            }
        }

        return ((float) (count)) / len;
    }

    /**
     * Finds the percentage of pixels that do are empty.
     *
     * @param bitmap input bitmap
     * @return a value between 0.0 to 1.0 . Note the method will return 0.0 if either of bitmaps are null nor of same size.
     */
    public static float getTransparentPixelPercent(Bitmap bitmap) {
        if (bitmap == null) {
            return 0f;
        }

        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getHeight() * bitmap.getRowBytes());
        bitmap.copyPixelsToBuffer(buffer);

        byte[] array = buffer.array();

        int len = array.length;
        int count = 0;

        for (int i = 0; i < len; i++) {
            if (array[i] == 0) {
                count++;
            }
        }

        return ((float) (count)) / len;
    }

    public static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    /**
     * Generates the random between two given integers.
     */
    public static int generateRandomBetween(int start, int end) {

        Random random = new Random();
        int rand = random.nextInt(Integer.MAX_VALUE - 1) % end;

        if (rand < start) {
            rand = start;
        }
        return rand;
    }

    public static Drawable setTint(Drawable d, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(d);
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static Bitmap getThumbnailFromList(List<String> videoList) {
        for (String url : videoList) {
//            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(url, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            try {
                URL url1 = new URL("url");
                Bitmap image = BitmapFactory.decodeStream(url1.openConnection().getInputStream());
                if (image != null) return image;
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Drawable getRectangularDrawable(String backgroundColor, int cornerRadius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(cornerRadius);
        shape.setColor(Color.parseColor(backgroundColor));
        return shape;
    }

    public static Drawable getRectangularDrawable(int backgroundColor, int cornerRadius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(cornerRadius);
        shape.setColor(backgroundColor);
        return shape;
    }

    public static Drawable getRectangularDrawable(int startColor, int endColor, int cornerRadius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(cornerRadius);
        shape.setColors(new int[]{startColor, endColor});
        return shape;
    }

    public static int isEdgeToEdgeEnabled(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
        if (resourceId > 0) {
            return resources.getInteger(resourceId);
        }
        return 0;
    }

    public static String getFollowersCountString(int followersCount) {
        if (followersCount == 0)
            return "0 Follower";

        String count = CommonUtils.formatValue(followersCount);
        return CommonUtils.getPlural("Follower", followersCount, count);
    }
}
