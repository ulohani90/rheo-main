package com.rheotv.android.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.utils.AppConstants;

import java.lang.ref.WeakReference;

import javax.inject.Singleton;

import static com.rheotv.android.utils.PlayerHeadServiceHelper.PLAYER_SERVICE_STOP_BROADCAST;

@Singleton
public class PlaybackServiceManager {
    private static volatile PlaybackServiceManager mInstance;
    private boolean isPlaying = false;
    private Intent serviceIntent;
    private WeakReference<Context> context;

    public static PlaybackServiceManager getInstance() {
        if (mInstance == null) {
            synchronized (PlaybackServiceManager.class) {
                if (mInstance == null) {
                    mInstance = new PlaybackServiceManager(RheoTvApp.getNonUiContext());
                }
            }
        }

        return mInstance;
    }

    private PlaybackServiceManager(Context context) {
        this.context = new WeakReference(context);
    }

    public void startPlaybackService(PlayerHeadHolder holder) {
        if (serviceIntent == null) {
            isPlaying = true;
            serviceIntent = new Intent(context.get(), PlaybackService.class);
            serviceIntent.putExtra(AppConstants.ARG_PLAYER_HOLDER, holder);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.get().startForegroundService(serviceIntent);
            } else {
                context.get().startService(serviceIntent);
            }
        }
    }

    public void stopPlaybackService() {
        if (context.get() != null && serviceIntent != null) {
            context.get().stopService(serviceIntent);
            serviceIntent = null;
            isPlaying = false;
            sendLocalBroadcastForPlayerClose();
        }
    }

    private void sendLocalBroadcastForPlayerClose() {
        Intent intent = new Intent(PLAYER_SERVICE_STOP_BROADCAST);
        LocalBroadcastManager.getInstance(context.get()).sendBroadcast(intent);
    }

}
