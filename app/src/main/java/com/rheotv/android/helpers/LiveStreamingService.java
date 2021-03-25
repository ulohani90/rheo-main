package com.rheotv.android.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.rheotv.android.R;
import com.rheotv.android.utils.AppConstants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LiveStreamingService extends Service {


    public Notification notification;

    public static boolean isServiceRunning = false;

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    private static final int SERVICE_ID = 3;

    private AsyncTask<String, Void, String> streamingTask;

    private static final int PERMISSION_CODE = 1;
    private int mScreenDensity;
    private static final int DISPLAY_WIDTH = 480;
    private static final int DISPLAY_HEIGHT = 640;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private MediaProjectionCallback mMediaProjectionCallback;
    private VirtualDisplay mVirtualDisplay;
    private MediaRecorder mMediaRecorder;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:
                    if (isServiceRunning == true) {
                        stopStreamingService();
                    }
                    handleIntentAndStartRecording(intent);
                    isServiceRunning = true;
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopStreamingService();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIntentAndStartRecording(@Nullable Intent intent) {
        initRecorder();
        prepareRecorder();
        mMediaProjectionCallback = new MediaProjectionCallback();
        //shareScreen();


    }
    /*private void shareScreen() {
        if (mMediaProjection == null) {
            //startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);

            *//*mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            mMediaProjection.registerCallback(mMediaProjectionCallback, null);
            mVirtualDisplay = createVirtualDisplay();
            mMediaRecorder.start();*//*

            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }*/


    private void buildNotification() {
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
                    .setContentTitle("वीडियो अपलोड का कार्य प्रगति पर है")
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(SERVICE_ID, notification);
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
                    .setContentTitle("वीडियो अपलोड का कार्य प्रगति पर है")
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(SERVICE_ID, notification);
        }
    }



    private void stopStreamingService() {
        stopForeground(true);
        stopSelf();
        stopSelf(SERVICE_ID);
        if (streamingTask != null) {
            streamingTask.cancel(true);
        }
        isServiceRunning = false;
    }

    private void initRecorder() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mMediaRecorder.setVideoFrameRate(30);
            //mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);

            mMediaRecorder.setOutputFile(getFilePath());
        }
    }

    public String getFilePath() {
        final String directory = Environment.getExternalStorageDirectory() + File.separator + "Recordings";
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "Failed to get External Storage", Toast.LENGTH_SHORT).show();
            return null;
        }
        final File folder = new File(directory);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        String filePath;
        if (success) {
            String videoName = ("capture_" + getCurSysDate() + ".mp4");
            filePath = directory + File.separator + videoName;
        } else {
            Toast.makeText(this, "Failed to create Recordings directory", Toast.LENGTH_SHORT).show();
            return null;
        }
        return filePath;
    }

    public String getCurSysDate() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    }

    private void prepareRecorder() {
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            //finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionCallback extends MediaProjection.Callback {
        /*
        * Called when the MediaProjection session is no longer valid.
        * Once a MediaProjection has been stopped, it's up to the application to release any resources it may be holding
        * */
        @Override
        public void onStop() {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Log.v(AppConstants.TAG, "Recording Stopped");
            mMediaProjection = null;
            stopScreenSharing();
            Log.i(AppConstants.TAG, "MediaProjection Stopped");
        }
    }

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mMediaRecorder.release();
    }



}
