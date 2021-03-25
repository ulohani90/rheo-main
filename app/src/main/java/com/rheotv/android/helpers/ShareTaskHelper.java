package com.rheotv.android.helpers;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.FBconnectionclass.ConnectionClassManager;
import com.rheotv.android.utils.FBconnectionclass.ConnectionQuality;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class ShareTaskHelper {

    private static final String TAG = ShareTaskHelper.class.getSimpleName();
    private static ShareTaskHelper shareTaskHelper;
    private static Context context;
    private AsyncTask downloadAndShareTask = null;
    // Create the Handler object (on the main thread by default)
    private Handler handler = new Handler();
    private Result post = null;
    private DownloadManager manager;

    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    // Define the code block to be executed
    private Runnable runnableCode = () -> {
        // cancel asynctask and share thumbnail
        stopTimer();
        if (downloadAndShareTask != null) {
//            ViewUtils.dismissSnackbar();
            downloadAndShareTask.cancel(true);
        }
        new SharePostWithImage(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    };

    public static ShareTaskHelper getNewInstance(Context currentContext) {
        if (shareTaskHelper == null) {
            context = currentContext;
            shareTaskHelper = new ShareTaskHelper();
        }
        return shareTaskHelper;
    }

    public void share(Context context, String message, String localFile, FileUtilityHelper.FileType fileType, ShareTarget shareTarget) {
        String installMessage = context.getString(R.string.download_app_message);
        message = message + "\n\n" + installMessage;

        try {
            shareImageOnWhatsapp(context, message, localFile, fileType, shareTarget, false);
        } catch (Exception e) {
            shareImageOnWhatsapp(context, message, localFile, fileType, shareTarget, true);
        }
    }

    private void shareImageOnWhatsapp(Context context, String message, String localFile, FileUtilityHelper.FileType fileType, ShareTarget shareTarget, boolean isFallback) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(FileUtilityHelper.getMimeType(localFile, fileType));
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(localFile));
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isFallback) {
            share.setPackage(shareTarget.getFallbackPackageName());//package name of the app
        } else {
            share.setPackage(shareTarget.getPackageName());//package name of the app
        }
        share.putExtra(Intent.EXTRA_TEXT, message + " #rheoapp");
        ShareTaskHelper.context.startActivity(share);
    }

    private void shareOthers(Context context, String messageBody) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, messageBody + " #rheoapp");
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void share(Context context, String message, ShareTarget shareTarget) {
        try {
            if (shareTarget == ShareTarget.Whatsapp) {
                shareTextOnWhatsapp(context, message, shareTarget, false);
            } else {
                shareOthers(context, message);
            }
        } catch (Exception e) {
            shareTextOnWhatsapp(context, message, shareTarget, true);
            Toast.makeText(context, "whatsapp इंस्टॉल करे !!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void share(Context context, String messageBody, String sharePackage) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, messageBody + " #rheoapp");
        sharingIntent.setPackage(sharePackage);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void shareTextOnWhatsapp(Context context, String message, ShareTarget shareTarget, boolean isFallback) {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, message + " #rheoapp");
            sendIntent.setType("text/plain");
            if (isFallback) {
                sendIntent.setPackage(shareTarget.getFallbackPackageName());
            } else {
                sendIntent.setPackage(shareTarget.getPackageName());
            }
            context.startActivity(sendIntent);
        } catch (Exception e) {
            Toast.makeText(RheoTvApp.getNonUiContext(), "whatsapp इंस्टॉल करे !!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void downloadAndSharePostOnWhatsApp(Result post1) {
        try {
            if (isAppInstalled(AppConstants.WHATSAPP_PACKAGE) || isAppInstalled(AppConstants.WHATSAPP_PACKAGE_BUSINESS)) {
                int totalShareCount = AppConstants.POST_SHARE_COUNT;
                if (totalShareCount >= 5 && sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_NAME) == null) {
                    new LoginFragmentBottomDialog().show(((FragmentActivity) context).getSupportFragmentManager(), null);
                    ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
                    return;
                }

                if (post1 == null) {
                    return;
                }
                post = post1;

                ConnectionQuality quality = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
                if (quality.equals(ConnectionQuality.POOR)) {
                    new SharePostWithImage(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    downloadAndShareTask = new DownloadAndShareTask(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    handler.postDelayed(runnableCode, 10 * 1000);
                }
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.whatsapp_not_installed_toast), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAppInstalled(String uri) {
        PackageManager packageManager = RheoTvApp.getNonUiContext().getPackageManager();
        boolean isAppInstalled;
        try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            isAppInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            isAppInstalled = false;
        }
        return isAppInstalled;
    }

    public void downloadAndSharePostOnFacebook(Context context, Result post1) {
        try {
            int totalShareCount = AppConstants.POST_SHARE_COUNT;
            if (totalShareCount >= 5 && sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_NAME) == null) {
                new LoginFragmentBottomDialog().show(((FragmentActivity) context).getSupportFragmentManager(), null);
                ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
                return;
            }
            if (post1 == null) {
                return;
            }
            post = post1;

            ConnectionQuality quality = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
            if (quality.equals(ConnectionQuality.POOR)) {
                new ShareImageOnFBTask(context, post1.getShareThumbnail()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            } else {
                new ShareVideoOnFBTask(context, post1.getShareUrl(), post1.getShareThumbnail()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sharePostWithImageOnFB(Context context, String url) {
        ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);

        FacebookSdk.sdkInitialize(context.getApplicationContext());

        CallbackManager callbackManager = CallbackManager.Factory.create();

        ShareDialog shareDialog = new ShareDialog((Activity) context);

        shareDialog.registerCallback(callbackManager, callback);

        ShareButton shareButton = new ShareButton(context);

        String fbMessage = "*" + post.getTitle() + "* " + "\uD83D\uDC47\n" + addReferrerToURL(post.getShareUrl(), "I", "facebook");

        Bitmap bitmap = null;
        try {

            bitmap = Picasso.get().load(url).get();
            SharePhoto photo = new SharePhoto.Builder().setBitmap(bitmap)
                    .setCaption(fbMessage)
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .setShareHashtag(new ShareHashtag.Builder().setHashtag("#MojoTimes").build())
                    .build();

            shareButton.setShareContent(content);
            shareButton.performClick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sharePostWithVideoOnFB(Context context, String url, String thumbnail) {
        ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);

        FacebookSdk.sdkInitialize(context.getApplicationContext());

        CallbackManager callbackManager = CallbackManager.Factory.create();

        ShareDialog shareDialog = new ShareDialog((Activity) context);

        shareDialog.registerCallback(callbackManager, callback);

        try {
            Uri videoFileUri = Uri.parse(url);

            /*ShareVideo video = new ShareVideo.Builder()
                    .setLocalUrl(videoFileUri)
                    .build();
            ShareVideoContent content = new ShareVideoContent.Builder()
                    .setVideo(video)
                    .build();

            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);*/

            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(videoFileUri)
                    .setImageUrl(Uri.parse(thumbnail))
                    .setShareHashtag(new ShareHashtag.Builder().setHashtag("#MojoTimes").build())
                    .build();
            shareDialog.show(linkContent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private FacebookCallback<Sharer.Result> callback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onSuccess(Sharer.Result result) {
            Log.v("FBUpload", "Successfully posted");
            ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
            // Write some code to do some operations when you shared content successfully.
        }

        @Override
        public void onCancel() {
            Log.v("FBUpload", "Sharing cancelled");
            ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
            // Write some code to do some operations when you cancel sharing content.
        }

        @Override
        public void onError(FacebookException error) {
            Log.v("FBUpload", error.getMessage());
            ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
            // Write some code to do some operations when some error occurs while sharing content.
        }
    };

    private class ShareImageOnFBTask extends AsyncTask<Void, Void, String> {
        Context context;
        String shareThumbnailUrl;

        public ShareImageOnFBTask(Context context, String shareThumbnailUrl) {
            this.context = context;
            this.shareThumbnailUrl = shareThumbnailUrl;
        }

        @Override
        protected String doInBackground(Void... voids) {
            sharePostWithImageOnFB(context, shareThumbnailUrl);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            /*if(localFilePath!=null) {
                sharePostWithVideoOnFB(context, Uri.parse(localFilePath).toString());
            }*/
        }
    }

    private class ShareVideoOnFBTask extends AsyncTask<Void, Void, String> {
        Context context;
        String shareVideoUrl;
        String shareThumbnail;

        public ShareVideoOnFBTask(Context context, String shareVideoUrl, String shareThumbnail) {
            this.context = context;
            this.shareVideoUrl = shareVideoUrl;
            this.shareThumbnail = shareThumbnail;
        }

        @Override
        protected String doInBackground(Void... voids) {
//            localFilePath = FileUtilityHelper.downloadAndGetLocalPath(context,shareVideoUrl, FileUtilityHelper.FileType.Video);
            sharePostWithVideoOnFB(context, shareVideoUrl, shareThumbnail);
            ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
            return null;
        }

    }

    public void shareApp(Context context) {
        if (isAppInstalled(AppConstants.WHATSAPP_PACKAGE) || isAppInstalled(AppConstants.WHATSAPP_PACKAGE_BUSINESS)) {
            new ShareAppTask(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.whatsapp_not_installed_toast), Toast.LENGTH_LONG).show();
        }
    }


    public void sendMessageToParticularNumber(Context context, String message, String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + message));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void stopTimer() {
        handler.removeCallbacks(runnableCode);
    }

    private String addReferrerToURL(String url, String type, String medium) {
        url = url + "?referrer=" + medium + "&type=" + type;
        return url;
    }


    public enum ShareTarget {
        Whatsapp,
        Facebook,
        Others;

        public String getPackageName() {
            if (this == Whatsapp) {
                return "com.whatsapp";
            } else {
                return "";
            }
        }

        public String getFallbackPackageName() {
            if (this == Whatsapp) {
                return "com.whatsapp.w4b";
            } else {
                return "";
            }
        }
    }

    private class SharePostWithImage extends AsyncTask<Void, Void, String> {

        Context context;
        View view;
        FileUtilityHelper.FileType fileType = FileUtilityHelper.FileType.Image;
        ShareTarget shareTarget;

        public SharePostWithImage(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            AnalyticsHelper.getInstance(context).sendPostShareSuccess(post.getAuthor().getUser().getUsername(),
                    String.valueOf(post.getAuthor().getUser().getId()), post.getId(), post.getTitle(), "I");
            try {
                return FileUtilityHelper.downloadAndGetLocalPath(context, post.getShareThumbnail(), FileUtilityHelper.FileType.Image);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String localFile) {
            ViewUtils.dismissSnackbar();
            ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
            if (localFile != null) {
                String whatsAppMessage = "*" + post.getTitle() + "* " + "\uD83D\uDC47\n" + addReferrerToURL(post.getShareUrl(), "I", "whatsapp");
                share(context, whatsAppMessage, localFile, fileType, ShareTaskHelper.ShareTarget.Whatsapp);
            } else {
                String whatsAppMessage = "*" + post.getTitle() + "* " + "\uD83D\uDC47\n" + post.getShareUrl();
                share(context, whatsAppMessage, ShareTaskHelper.ShareTarget.Whatsapp);
            }
        }
    }

    private class DownloadAndShareTask extends AsyncTask<Void, Void, String> {

        Context context;
        View view;
        FileUtilityHelper.FileType fileType = FileUtilityHelper.FileType.Video;

        public DownloadAndShareTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.d(TAG, "background downloading the content");
            try {
                String videoUrl = FileUtilityHelper.downloadAndGetLocalPath(context, post.getGistUrl(), FileUtilityHelper.FileType.Video);
                if (videoUrl != null) {
                    AnalyticsHelper.getInstance(context).sendPostShareSuccess(post.getAuthor().getUser().getUsername(),
                            post.getId(), post.getId(), post.getTitle(), "V");
                    return videoUrl;
                } else {
                    String thumbnail = FileUtilityHelper.downloadAndGetLocalPath(context, post.getShareThumbnail(), FileUtilityHelper.FileType.Image);
                    fileType = FileUtilityHelper.FileType.Image;
                    AnalyticsHelper.getInstance(context).sendPostShareSuccess(post.getAuthor().getUser().getUsername(),
                            String.valueOf(post.getAuthor().getUser().getId()), post.getId(), post.getTitle(), "I");
                    return thumbnail;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String localFile) {
            ViewUtils.dismissSnackbar();
            ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
            stopTimer();
            String whatsAppMessage;
            if (localFile != null) {
                if (fileType.equals(FileUtilityHelper.FileType.Image)) {
                    whatsAppMessage = "*" + post.getTitle() + "* " + "\uD83D\uDC47 \n" + addReferrerToURL(post.getShareUrl(), "I", "whatsapp");
                } else {
                    whatsAppMessage = "*" + post.getTitle() + "* " + "\uD83D\uDC47 \n" + addReferrerToURL(post.getShareUrl(), "V", "whatsapp");
                }
                share(context, whatsAppMessage, localFile, fileType, ShareTaskHelper.ShareTarget.Whatsapp);
            } else {
                whatsAppMessage = "*" + post.getTitle() + "* " + "\n" + post.getShareUrl();
                share(context, whatsAppMessage, ShareTaskHelper.ShareTarget.Whatsapp);
            }

        }
    }


    private class ShareAppTask extends AsyncTask<Void, Void, String> {

        Context context;
        FileUtilityHelper.FileType fileType = FileUtilityHelper.FileType.Image;

        public ShareAppTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.d(TAG, "background downloading the content");
            String imageUrl = "https://storage.googleapis.com/unheard-files/common/toolbar_share_whatsapp.jpg";
            try {
                return FileUtilityHelper.downloadAndGetLocalPath(context, imageUrl, FileUtilityHelper.FileType.Image);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String localFile) {
            ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
            if (localFile != null) {
                ShareTaskHelper.getNewInstance(context)
                        .share(context, "", localFile, FileUtilityHelper.FileType.Image, ShareTaskHelper.ShareTarget.Whatsapp);
            } else {
                ShareTaskHelper.getNewInstance(context)
                        .share(context, "", ShareTaskHelper.ShareTarget.Whatsapp);
            }
        }
    }

    public void downloadFromUrl(String url, String title, Context context) {
        try {
            int totalShareCount = AppConstants.POST_SHARE_COUNT;
            if (totalShareCount >= 5 && sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_NAME) == null) {
                new LoginFragmentBottomDialog().show(((FragmentActivity) context).getSupportFragmentManager(), null);
                ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
                return;
            }
            Uri downloadUri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);

            //Restrict the types of networks over which this download may proceed.
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

            //Set whether this download may proceed over a roaming connection.
//        request.setAllowedOverRoaming(false);

            //Set the title of this download, to be displayed in notifications (if enabled).
            request.setTitle(title);

            //Set a description of this download, to be displayed in notifications (if enabled)
            request.setDescription("Downloading complete");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            }

            //Set the local destination for the downloaded file to a path within the application's external files directory
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + ".mp4");

            //Enqueue a new download and save the referenceId

            manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
//        long index = manager.enqueue(request);
//        View view = ((Activity)context).findViewById(R.id.video_upload_text);
//        ViewUtils.initSnackBar(view, Snackbar.LENGTH_INDEFINITE, context.getResources().getString(R.string.downloading));
//        ViewUtils.showSnackbar(context);
//        context.registerReceiver(receiver_complete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BroadcastReceiver receiver_complete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                Bundle extras = intent.getExtras();
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
                Cursor c = manager.query(q);
                ((RheoTvApp) RheoTvApp.getNonUiContext()).setDownloadStatus(false);
                if (c.moveToFirst()) {
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {


                    }
                }
            }
        }
    };
}
