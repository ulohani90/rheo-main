/*
 * *
 *  * Created by Asheesh Sharma on 9/3/19 4:53 PM
 *  * Copyright (c) 1st January 2019 . All rights reserved.
 *
 */

package com.rheotv.android.helpers;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.ui.activities.tabcontainer.videoUpload.VideoUploadFragment;
import com.rheotv.android.utils.AppConstants;

import java.io.File;

import id.zelory.compressor.Compressor;

public class FileUploadServiceHelper extends Service {
    public Notification notification;

    public static boolean isServiceRunning = false;

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    private static final int SERVICE_ID = 2;

    private AsyncTask<String, Void, Boolean> uploadTask;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(getClass().getSimpleName(), "onBind: upload video http");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(getClass().getSimpleName(), "onStartCommand: upload video http");

        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:
                    if (isServiceRunning == true) {
                        stopUploaderService();
                    }
//                    buildNotification();
                    buildProgressNotification();
                    handleIntentAndBeginUpload(intent);
                    isServiceRunning = true;
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopUploaderService();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIntentAndBeginUpload(@Nullable Intent intent) {
        Log.d(getClass().getSimpleName(), "handleIntentAndBeginUpload: upload video http");

        if (intent != null && intent.getExtras() != null &&
                intent.getExtras().getString(AppConstants.UPLOAD_URL_VIDEO) != null && intent.getExtras().getString(AppConstants.VIDEO_FILE_NAME) != null) {
            String fileName = intent.getExtras().getString(AppConstants.VIDEO_FILE_NAME);
            String urlPathToUpload = intent.getExtras().getString(AppConstants.UPLOAD_URL_VIDEO);
            intent.setAction(VideoUploadFragment.FILTER_ACTION_KEY);
            uploadTask = new UploadVideoTask(urlPathToUpload, fileName, intent).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void buildNotification() {
        Log.d(getClass().getSimpleName(), "buildNotification: upload video http");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
                    .setContentTitle(RheoTvApp.getNonUiContext().getString(R.string.upload_in_progress))
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(SERVICE_ID, notification);
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
                    .setContentTitle(RheoTvApp.getNonUiContext().getString(R.string.upload_in_progress))
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(SERVICE_ID, notification);
        }
    }

    private NotificationManagerCompat notificationManagerCompat;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private int UPLOAD_NOTIFICATION_ID = 1000;
    int PROGRESS_MAX = 100;
    int PROGRESS_CURRENT = 0;

    private void buildProgressNotification() {
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(RheoTvApp.getNonUiContext().getString(R.string.upload_in_progress))
                .setContentText("0 %")
                .setSmallIcon(R.drawable.upload_cloud)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(chan);
        }

        // Issue the initial notification with zero progress
        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
        startForeground(UPLOAD_NOTIFICATION_ID, builder.build());
    }

    public void updateNotificationProgress(int progress) {
        if (progress == 200) {
            PROGRESS_MAX = 0;
            progress = 0;
            builder.setContentTitle(RheoTvApp.getNonUiContext().getString(R.string.upload_completed)).setContentText(null);
        } else {
            if (progress == 100)
                progress -= 1;
            builder.setContentText(progress + " %");
        }

        builder.setProgress(PROGRESS_MAX, progress, false);
        notificationManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
    }

    public class UploadVideoTask extends AsyncTask<String, Void, Boolean> {

        String urlToUpload, filePath;
        Intent intent;

        public UploadVideoTask(String urlToUpload, String filePath, Intent intent) {
            this.urlToUpload = urlToUpload;
            this.filePath = filePath;
            this.intent = intent;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return uploadVideoHTTP(urlToUpload, filePath, intent);
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if (s) {
                sendProgressBroadcast(intent, 200);
                stopUploaderService();
            } else {
                Log.i(getClass().getSimpleName(), "Reached onPost");
            }
        }
    }

    private void sendProgressBroadcast(Intent intent, int progress) {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent.putExtra("contentData", progress));
    }

    private boolean uploadVideoHTTP(String urlToUpload, String filePath, Intent intent) {
        Log.d(getClass().getSimpleName(), "uploadVideoHTTP: upload video http");
        try {
            File videoFileToUpload = new File(filePath);
            UploaderService fileUploadService;
            if (intent.hasExtra(AppConstants.STORAGE_TYPE) &&
                    intent.getIntExtra(AppConstants.STORAGE_TYPE, AppConstants.AZURE_STORAGE) == AppConstants.S3_STORAGE) {
                fileUploadService = new S3FileUploader();
            } else {
                fileUploadService = new AzureFileUploader();
            }
            fileUploadService.setUploadProgressListener(progress -> broadcastProgress(progress, intent));
            fileUploadService.startUpload(videoFileToUpload, urlToUpload);
            if (fileUploadService.getStatusCode() == UploaderService.StatusCode.SUCCESS) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    private void broadcastProgress(int progressVal, Intent intent) {
       /* if (progressVal == 200) {
            stopUploaderService();
        }*/
        sendProgressBroadcast(intent, progressVal);
        updateNotificationProgress(progressVal);
    }

    private void stopUploaderService() {
        stopForeground(true);
        stopSelf();
        //stopSelf(SERVICE_ID);
        /*if (uploadTask != null) {
            uploadTask.cancel(true);
        }*/
        isServiceRunning = false;
    }

}