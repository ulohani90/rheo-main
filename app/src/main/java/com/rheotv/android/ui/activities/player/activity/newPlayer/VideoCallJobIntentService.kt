package com.rheotv.android.ui.activities.player.activity.newPlayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.network.models.postlisting.responses.VideoCallResponse
import com.rheotv.android.di.module.AppModule
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.VideoCallAction
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.rheotv.android.utils.toBundle
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoCallJobIntentService : JobService() {

    private val mVideoCallActionBroadcastReceiver = VideoCallActionBroadcastReceiver()
    private var data: MutableMap<String, String> = hashMapOf()
    private var mMediaPlayer: MediaPlayer? = null
    private var mVibrator: Vibrator? = null
    private var job: Job? = null


    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        try {
            registerReceiver(mVideoCallActionBroadcastReceiver, IntentFilter(FILTER).also {
                it.addAction(ACTION_DENY)
            })
        } catch (e: Exception) {
           e.printStackTrace()
        }
        mService = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_NOT_STICKY
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        params?.extras?.keySet()?.forEach {
            data[it] = params.extras.getString(it) ?: ""
        }
        buildNotification()
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            stopSound()
            unregisterReceiver(mVideoCallActionBroadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildNotification() {

        if (data["title"]?.contains("declined") == true) {
            stopSelf()
            return
        }
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_CALL_NOTIFICATION_RECEIVED, hashMapOf<String, Any?>(
                "streamer_name" to data["streamer_user_name"],
                "post_id" to data["post_id"],
                "channel_id" to data["channel_id"]
        ))
        val channelId = "channel_id"
        mPostId = data["post_id"]
        val streamerProfilePic = data["streamer_profile_pic"]
        val streamerUserName = data["streamer_user_name"]
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val fullScreenIntent = Intent(this, CallReceivingActivity::class.java)
        fullScreenIntent.putExtra("notification_id", NOTIFICATION_ID)
        fullScreenIntent.putExtra("streamer_profile_pic", streamerProfilePic)
        fullScreenIntent.putExtra("streamer_user_name", streamerUserName)
        fullScreenIntent.putExtras(data.toBundle())
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val acceptCallIntent = Intent(this, VideoChatViewActivity::class.java)
        acceptCallIntent.putExtras(data.toBundle())
        acceptCallIntent.putExtra("is_from_notification", true)
        acceptCallIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val acceptCallPendingIntent = PendingIntent.getActivity(this, 1,
                acceptCallIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        val denyCallIntent = Intent(this, VideoChatViewActivity::class.java)
        acceptCallIntent.putExtra("notification_id", NOTIFICATION_ID)
        denyCallIntent.putExtras(data.toBundle())
        denyCallIntent.putExtra("denied", true)

        var remoteView = RemoteViews(packageName, R.layout.receiving_call_notification_layout);

        val iconId = R.id.user_icon;

        remoteView.setTextViewText(R.id.title, streamerUserName)
        remoteView.setTextViewText(R.id.sub_title, "Accept video call to become co-host")
        remoteView.setOnClickPendingIntent(R.id.answer, acceptCallPendingIntent)
        remoteView.setOnClickPendingIntent(R.id.decline, PendingIntent.getBroadcast(this, 0,
                Intent(FILTER).setAction(ACTION_DENY).putExtra("is_deny_clicked", true), PendingIntent.FLAG_UPDATE_CURRENT))

        val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
                .setCustomHeadsUpContentView(remoteView)
                .setCustomContentView(remoteView)
                .setCustomBigContentView(remoteView)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                /*.addAction(R.drawable.ic_baseline_call_24, "Accept", acceptCallPendingIntent)
                .addAction(R.drawable.ic_baseline_call_end_24, "Deny", PendingIntent.getBroadcast(this, 0,
                        Intent(FILTER).setAction(ACTION_DENY),
                        PendingIntent.FLAG_UPDATE_CURRENT))*/
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setContentIntent(fullScreenPendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel: NotificationChannel? = notificationManager?.getNotificationChannel(channelId)
            if (channel == null) {
                channel = NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            }
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.setSound(null, null)
            builder.setChannelId(channelId)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = builder.build();
        Picasso.get().load(streamerProfilePic).transform(CropCircleTransformation()).into(remoteView, iconId, NOTIFICATION_ID, notification);
        startForeground(NOTIFICATION_ID, notification)
        playSound()
        job = CoroutineScope(Dispatchers.IO).launch {
            delay(1000 * 60)
            with(Dispatchers.Main) {
                var intent = Intent(FILTER)
                intent.setAction(ACTION_DENY)
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_CALL_REQUEST_NOT_PICKED_UP, hashMapOf<String, Any?>(
                        "streamer_name" to data["streamer_user_name"],
                        "post_id" to data["post_id"],
                        "channel_id" to data["channel_id"]
                ))
                sendBroadcast(intent)
            }
        }
    }

    private fun playSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
            mMediaPlayer?.isLooping = true
        }
        if (mVibrator == null) {
            mVibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            mVibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 1000), 0))
        } else {
            mVibrator?.vibrate(longArrayOf(0, 100, 1000), 0)
        }
        mMediaPlayer?.start()
    }

    private fun stopSound() {
        if (mMediaPlayer?.isPlaying == true) {
            mMediaPlayer?.stop()
            mMediaPlayer?.release()
        }
        mVibrator?.cancel()
    }

    companion object {
        var isServiceRunning = false
        const val NOTIFICATION_ID = 0x3
        const val FILTER = "video_call_filter"
        const val ACTION_DENY = "deny"
        private var mPostId: String? = null

        private const val TAG = "VideoCallService"

        private var mService: VideoCallJobIntentService? = null

        fun stopService() {
            try {
                isServiceRunning = false
                mService?.stopSound()
                mService?.stopForeground(true)
                (mService?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancel(NOTIFICATION_ID)
                mService?.job?.cancel()
                // mService?.unregisterReceiver(mVideoCallActionBroadcastReceiver)
                mService?.stopSelf()
                mService = null

                Log.i(TAG, "Service stopped successfully")
            } catch (e: Exception) {
                Log.i(TAG, "Service stopped failed ${e.message}")
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    inner class VideoCallActionBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "Action performed --> ${intent?.action}")
            if (intent?.action == ACTION_DENY) {
                if (intent?.getBooleanExtra("is_deny_clicked", false)) {
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_NOTIFICATION_DECLINE_CLICKED, hashMapOf<String, Any?>(
                            "streamer_name" to data["streamer_user_name"],
                            "post_id" to data["post_id"]
                    ))
                }
                val interceptor = AppModule.getServiceInterceptor(RheoTvApp.getNonUiContext())
                val httpLoggingInterceptor = AppModule.httpLoggingInterceptor()
                val cache = AppModule.provideCache(RheoTvApp.getNonUiContext())
                val client = AppModule.provideOkhttp(interceptor, httpLoggingInterceptor, cache)
                AppModule.provideApiService(client, Gson())
                        .manageVideoCalls(CommonUtils.getUserID(), mPostId, VideoCallAction.Deny.name)
                        .enqueue(object : Callback<VideoCallResponse> {
                            override fun onResponse(call: Call<VideoCallResponse>, response: Response<VideoCallResponse>) {
                                if (response.isSuccessful)
                                    Log.i(TAG, "video call denied successfully")
                                else
                                    Log.i(TAG, "video call denied failed ---> ${response.errorBody()?.string()}")
                            }

                            override fun onFailure(call: Call<VideoCallResponse>, t: Throwable) {
                                t.printStackTrace()
                            }
                        })
                stopService()
            }
        }
    }
}