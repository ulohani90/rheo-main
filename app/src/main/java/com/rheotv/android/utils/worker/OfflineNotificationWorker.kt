package com.rheotv.android.utils.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.rheotv.android.R
import com.rheotv.android.db.ClipDao
import com.rheotv.android.factories.ChildWorkerFactory
import com.rheotv.android.ui.activities.splash.SplashActivity
import com.rheotv.android.utils.SharedPrefsUtils
import com.rheotv.android.utils.TimeUtils
import com.rheotv.android.utils.ViewUtils
import com.rheotv.android.utils.randomFromArrays
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * IMPORTANT NOTE!
 *
 * The [Context] need to be named with [appContext] and [WorkerParameters] with [params]
 * as long as these name are identical with [ChildWorkerFactory.create]'s method parameters
 *
 */
class OfflineNotificationWorker @AssistedInject constructor(
        @Assisted private val appContext: Context,
        @Assisted params: WorkerParameters,
        private val dao: ClipDao,
        private val sharedPrefsUtils: SharedPrefsUtils
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            Log.i(TAG, "Work started ${System.currentTimeMillis()}")
            val timeDiff = System.currentTimeMillis() - sharedPrefsUtils.getLongPreference(appContext, SharedPrefsUtils.LAST_APP_OPEN_TIME, 0)
            if (timeDiff < TimeUtils.MILLIS_IN_DAY)
                Result.success()

            val clip = loadClip()
            buildNotification(clip)
            Log.i(TAG, "Work completed ${System.currentTimeMillis()}")
        } catch (e: Exception) {
            e.printStackTrace()
            return@coroutineScope Result.retry()
        }
        Result.success()
    }

    private suspend fun loadClip(): List<String> = withContext(Dispatchers.IO) {
        dao.getClipVideoUrlList()
    }

    private suspend fun buildNotification(clips: List<String>) {
        withContext(Dispatchers.Main) {
            Log.i(TAG, "Building Notification")
            val pair = appContext.randomFromArrays(R.array.clip_notification_title, R.array.clip_notification_description)
            val title = pair.first
            val description = pair.second

            val payload = HashMap<String, Any>()
            payload["title"] = title
            payload["body"] = description
            trackAnalytics(payload)

            val mBuilder = NotificationCompat.Builder(appContext, "notification_101")
            val intent = Intent(appContext, SplashActivity::class.java)
            intent.action = "action_$NOTIFICATION_ID"
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("notification_payload", payload)
            val pendingIntent = PendingIntent.getActivity(appContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val bitmap = ViewUtils.getThumbnailFromList(clips)

            val bigPicture = NotificationCompat.BigPictureStyle()
            bigPicture.bigPicture(bitmap)
            bigPicture.bigLargeIcon(null)

            val uri = Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(appContext.packageName)
                    .path(R.raw.arrow3.toString())
                    .build()

            mBuilder.setContentIntent(pendingIntent)
            mBuilder.setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
            mBuilder.setLargeIcon(bitmap)
            mBuilder.setContentTitle(title)
            mBuilder.setContentText(description)
            mBuilder.priority = NotificationCompat.PRIORITY_HIGH
            mBuilder.setStyle(bigPicture)
            mBuilder.setSound(uri, AudioManager.STREAM_NOTIFICATION)
            mBuilder.setAutoCancel(true)

            val mNotificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val attributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()

                val channel = NotificationChannel(
                        CHANNEL_ID,
                        "Clips",
                        NotificationManager.IMPORTANCE_HIGH)
                channel.setSound(uri, attributes)
                mNotificationManager.createNotificationChannel(channel)
                mBuilder.setChannelId(CHANNEL_ID)
            }

            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build())
        }
    }

    private fun trackAnalytics(data: HashMap<String, Any>) {
        SegmentTracker.getInstance(appContext).trackEvent(SegmentConstants.EVENT_NOTIFICATION_RECEIVED, data)
    }

    @AssistedInject.Factory
    interface Factory : ChildWorkerFactory

    companion object {

        const val TAG = "NotificationWorker"
        const val TAG_NOTIFY_CLIP = "notify_data"
        const val NOTIFY_WORK_NAME = "work_notify"
        val NOTIFICATION_ID = Random.nextInt(9999)
        const val CHANNEL_ID = "rheo_clip_channel"
        private const val SELF_REMINDER_HOUR = 15

        fun scheduleNotification(context: Context) {
            val delay = if (DateTime.now().hourOfDay < SELF_REMINDER_HOUR) {
                Duration(DateTime.now(), DateTime.now().withTimeAtStartOfDay().plusHours(SELF_REMINDER_HOUR)).standardMinutes
            } else {
                Duration(DateTime.now(), DateTime.now().withTimeAtStartOfDay().plusDays(1).plusHours(SELF_REMINDER_HOUR)).standardMinutes
            }

            val workRequest = PeriodicWorkRequest.Builder(
                    OfflineNotificationWorker::class.java,
                    12,
                    TimeUnit.HOURS,
                    PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS,
                    TimeUnit.MILLISECONDS
            ).setInitialDelay(delay, TimeUnit.MINUTES)
                    .addTag(TAG_NOTIFY_CLIP)
                    .build()

            WorkManager
                    .getInstance(context)
                    .enqueueUniquePeriodicWork(NOTIFY_WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
            Log.i(TAG, "Worker Initiated")
        }
    }
}