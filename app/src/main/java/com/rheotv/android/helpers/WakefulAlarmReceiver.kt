package com.rheotv.android.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.*
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.legacy.content.WakefulBroadcastReceiver
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.rheotv.android.R
import com.rheotv.android.data.network.requestLayer.ApiService
import com.rheotv.android.di.module.AppModule
import com.rheotv.android.ui.activities.onboarding.v2.model.LatestPostResponse
import com.rheotv.android.ui.activities.player.activity.newPlayer.FullscreenAlarmNotificationActivity
import com.rheotv.android.ui.activities.splash.SplashActivity
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

class WakefulAlarmReceiver : WakefulBroadcastReceiver() {
    private val TAG = javaClass.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.apply {
            val userId = intent?.getIntExtra(AppConstants.USER_ID, 0) ?: 0
            buildEventService(this).fetchLatestPostByUser(userId).enqueue(object: Callback<LatestPostResponse> {
                override fun onResponse(call: Call<LatestPostResponse>, response: Response<LatestPostResponse>) {
                    if (response.isSuccessful) {
                        val job: JobInfo = JobInfo.Builder(
                                Random.nextInt(0, Int.MAX_VALUE),
                                ComponentName(context, AlarmService::class.java))
                                .setOverrideDeadline(0)
                                .setExtras(PersistableBundle().also {
                                    intent?.extras?.keySet()?.forEach { key ->
                                        it.putString(key, intent.getStringExtra(key))
                                    }
                                    it.putString(AppConstants.EVENT_POST_ID, response.body()?.results?.id)
                                })
                                .build()
                        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler
                        jobScheduler?.schedule(job)
                    }
                }

                override fun onFailure(call: Call<LatestPostResponse>, t: Throwable) {
                    Log.i(TAG, "fail to fetch latest post")
                }
            })
        }
    }

    private fun buildEventService(context: Context): ApiService {
        val interceptor = AppModule.getServiceInterceptor(context)
        val httpLoggingInterceptor = AppModule.httpLoggingInterceptor()
        val cache = AppModule.provideCache(context)
        val client = AppModule.provideOkhttp(interceptor, httpLoggingInterceptor, cache)
        return AppModule.provideApiService(client, Gson())
    }
}

class AlarmService : JobService() {
    private var mTitle: String = ""
    private var mImageUrl: String = ""
    private var mPostId: String = ""
    private var mSource: String = ""
    private var mStartTime: String = ""

    override fun onCreate() {
        super.onCreate()
        registerReceiver(alarmButtonBroadcastReceiver, IntentFilter(FILTER).also {
            it.addAction(ACTION_CANCEL)
        })
        mService = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private suspend fun sendNotification(msg: String) {
        Log.d("AlarmService", "Preparing to send notification...: $msg")
        val alarmNotificationManager = this
                .getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        isRunning.set(true)
        startForeground(NOTIFICATION_ID,
                getNotificationBuilder(alarmNotificationManager, msg, NotificationCompat.PRIORITY_HIGH).build())

        val uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(packageName)
                .path(R.raw.reminder_notification.toString())
                .build()
        if (mRingtone == null) {
            mRingtone = RingtoneManager.getRingtone(this, uri)
        }
        if (mRingtone?.isPlaying == false)
            mRingtone?.play()

        with(Dispatchers.IO) {
            while (mRingtone?.isPlaying == true) {
                delay(100)
            }
            with(Dispatchers.Main) {
                if (isRunning.get()) {
                    alarmNotificationManager.notify(NOTIFICATION_ID,
                            getNotificationBuilder(alarmNotificationManager, msg, NotificationCompat.PRIORITY_DEFAULT).build())
                }
            }
        }
        Log.d("AlarmService", "Notification sent.")
    }

    private fun getNotificationBuilder(notificationManager: NotificationManager, message: String, priority: Int = NotificationCompat.PRIORITY_DEFAULT): NotificationCompat.Builder {
        val fullscreenPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, FullscreenAlarmNotificationActivity::class.java).also {
                    it.putExtra(AppConstants.EVENT_POST_ID, mPostId)
                    it.putExtra(AppConstants.EVENT_IMAGE_URL, mImageUrl)
                    it.putExtra(AppConstants.EVENT_POST_ID, mPostId)
                    it.putExtra(AppConstants.ARG_TITLE, mTitle)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }, PendingIntent.FLAG_UPDATE_CURRENT)
        val contentIntent: PendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, SplashActivity::class.java).also {
                    it.putExtra("target_url", "https://rheotv.com/post/${mPostId}/")
                    it.putExtra("alarm_notification", true)
                    it.putExtra(AppConstants.ARG_TITLE, mTitle)
                    it.putExtra(AppConstants.EVENT_POST_ID, mPostId)
                    it.putExtra(AppConstants.SCREEN_SOURCE, mSource)
                }, PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel: NotificationChannel = notificationManager?.getNotificationChannel("com.rheotv.android")
                    ?: NotificationChannel("com.rheotv.android", this.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            channel.setShowBadge(true)
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, "com.rheotv.android")
                .setContentTitle("Watch Now!")
                .setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(priority)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(-1, "Cancel", PendingIntent.getBroadcast(this, 0,
                        Intent(FILTER).setAction(ACTION_CANCEL),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(-1, "Watch Now", contentIntent)
                .setFullScreenIntent(fullscreenPendingIntent, true)
                .setContentText(message)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setTimeoutAfter(5 * 60 * 1000)
                .setContentIntent(contentIntent)
    }

    override fun onDestroy() {
        mRingtone?.stop()
        mRingtone = null
        try {
            unregisterReceiver(alarmButtonBroadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        mTitle = params?.extras?.getString(AppConstants.ARG_TITLE) ?: ""
        mImageUrl = params?.extras?.getString(AppConstants.EVENT_IMAGE_URL) ?: ""
        mPostId = params?.extras?.getString(AppConstants.EVENT_POST_ID) ?: ""
        mSource = params?.extras?.getString(AppConstants.SOURCE) ?: ""
        mStartTime = params?.extras?.getString(AppConstants.START_TIME) ?: ""

        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_ALARM_NOTIFICATION_RECEIVED,
                hashMapOf<String, Any?>(
                        "title" to mTitle,
                        "post_id" to mPostId,
                        "source" to mSource,
                        "start_time" to mStartTime
                )
        )
        CoroutineScope(Dispatchers.IO).launch {
            sendNotification(if (mTitle.isEmpty()) "The stream is live now!" else mTitle)
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    private fun stopAudio() {
        isRunning.set(false)
        mService?.mRingtone?.stop()
        stopForeground(true)
        (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancel(NOTIFICATION_ID)
        stopSelf()
    }

    private val isRunning = AtomicBoolean(false)

    private var mRingtone: Ringtone? = null
    private val alarmButtonBroadcastReceiver = AlarmButtonBroadcastReceiver()

    inner class AlarmButtonBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_CANCEL) {
                stopAudio()
            }
        }
    }

    companion object {
        private const val FILTER = "alarm_filter"
        private const val ACTION_CANCEL = "cancel"
        private const val NOTIFICATION_ID = 798656

        private var mService: AlarmService? = null

        fun stopService() {
            try {
                mService?.stopAudio()
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

}
