package com.rheotv.android.utils;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.rheotv.android.R;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.io.File;
import java.util.HashMap;

import static android.content.Context.DOWNLOAD_SERVICE;
import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class DownloadShareManager {
    private Builder mBuilder;

    private long downloadID;
    private BroadcastReceiver downloadListener;
    private ProgressDialog progressDialog;

    private DownloadShareManager(Builder builder) {
        this.mBuilder = builder;
        initBoardCast();
        startDownload();
    }

    private void startDownload() {
        try {
            if (isAppInstalled(AppConstants.WHATSAPP_PACKAGE) || isAppInstalled(AppConstants.WHATSAPP_PACKAGE_BUSINESS)) {
                if (!progressDialog.isShowing()) progressDialog.show();
                File file = new File(Environment.getExternalStorageDirectory() + mBuilder.dirType);
                if (!file.exists()) {
                    file.mkdirs();
                }
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mBuilder.downloadLink))
                        .setTitle(mBuilder.title)
                        .setDescription(mBuilder.description)
                        .setNotificationVisibility(mBuilder.notificationVisibility)
                        .setDestinationInExternalPublicDir(mBuilder.dirType, mBuilder.subPath)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    request.setRequiresCharging(false);
                }

                DownloadManager downloadManager = (DownloadManager) mBuilder.context.getSystemService(DOWNLOAD_SERVICE);
                downloadID = downloadManager.enqueue(request);

                Toast.makeText(mBuilder.context, "Downloading 1 file. You will be notified once download is complete", Toast.LENGTH_SHORT).show();
                HashMap<String, Object> properties = new HashMap<>();
                properties.put("shareTitle", mBuilder.shareTitle);
                properties.put("shareLink", mBuilder.downloadLink);
                SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_CLIP_DOWNLOAD_STARTED, properties);
            } else {
                Toast.makeText(mBuilder.context, mBuilder.context.getString(R.string.whatsapp_not_installed_toast), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isAppInstalled(String uri) {
        try {
            PackageManager packageManager = getNonUiContext().getPackageManager();
            boolean isAppInstalled;
            try {
                packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
                isAppInstalled = true;
            } catch (PackageManager.NameNotFoundException e) {
                isAppInstalled = false;
            }
            return isAppInstalled;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initBoardCast() {
        downloadListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Fetching the download id received with the broadcast
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                intent.getStringExtra(DownloadManager.COLUMN_LOCAL_URI);

                //Checking if the received broadcast is for our enqueued download by matching download id
                if (downloadID == id) {
                    Toast.makeText(mBuilder.context, "Download Completed", Toast.LENGTH_SHORT).show();
                    HashMap<String, Object> properties = new HashMap<>();
                    properties.put("shareTitle", mBuilder.shareTitle);
                    properties.put("shareLink", mBuilder.downloadLink);
                    SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_CLIP_DOWNLOAD_COMPLETED, properties);

                    File fileToShare = new File(Environment.getExternalStorageDirectory() + mBuilder.dirType, mBuilder.subPath);
                    Uri fileUri = FileProvider.getUriForFile(mBuilder.context, mBuilder.context.getPackageName() + ".app.provider", fileToShare);
                    if (fileUri != null) {
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType(mBuilder.mimeType);
                        share.putExtra(Intent.EXTRA_STREAM, fileUri);
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        share.putExtra(Intent.EXTRA_TEXT, "*" + mBuilder.shareTitle + "*" + mBuilder.shareMessage + " #rheoapp");
                        share.setPackage(mBuilder.sharePackageName);
                        mBuilder.context.startActivity(share);
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                        mBuilder.context.unregisterReceiver(downloadListener);
                        HashMap<String, Object> mapProperties = new HashMap<>();
                        mapProperties.put("shareTitle", mBuilder.shareTitle);
                        mapProperties.put("shareLink", mBuilder.downloadLink);
                        SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_CLIP_SHARED, mapProperties);
                    }
                }
            }
        };

        progressDialog = ProgressDialog.show(mBuilder.context, null, "Downloading...");
        mBuilder.context.registerReceiver(downloadListener, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public static class Builder {
        private String title = "Rheo";
        private String description = "Downloading";
        private int notificationVisibility = DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE;
        private String dirType = "/rheo_clips/";
        private String subPath = System.currentTimeMillis() + "clip_video.mp4";
        private String downloadLink; // link of content required to be downloaded

        private String shareTitle = "Hey, Did you watch this amazing clip on Rheo!";
        private String shareMessage = "\n\nFor more such clips download the *Rheo* app now\nhttps://rheotv.page.link";
        private String sharePackageName = AppConstants.WHATSAPP_PACKAGE;
        private String mimeType = "video/*";
        private Context context;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setNotificationVisibility(int notificationVisibility) {
            this.notificationVisibility = notificationVisibility;
            return this;
        }

        public Builder setDirType(String dirType) {
            this.dirType = dirType;
            return this;
        }

        public Builder setSubPath(String subPath) {
            this.subPath = subPath;
            return this;
        }

        public Builder setDownloadLink(String downloadLink) {
            this.downloadLink = downloadLink;
            return this;
        }

        public Builder setShareTitle(String shareTitle) {
            this.shareTitle = shareTitle;
            return this;
        }

        public Builder setShareMessage(String shareMessage) {
            this.shareMessage = shareMessage;
            return this;
        }

        public Builder setSharePackageName(String sharePackageName) {
            this.sharePackageName = sharePackageName;
            return this;
        }

        public Builder setMimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public DownloadShareManager build() {
            return new DownloadShareManager(this);
        }
    }
}
