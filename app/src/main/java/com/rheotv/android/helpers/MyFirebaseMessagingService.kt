package com.rheotv.android.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabaseLockedException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle
import android.support.v4.media.session.MediaSessionCompat

import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freshchat.consumer.sdk.Freshchat
import com.freshchat.consumer.sdk.FreshchatNotificationConfig
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.moengage.push.PushManager
import com.moengage.pushbase.push.MoEngageNotificationUtils
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.db.AppDatabase
import com.rheotv.android.db.AppPushNotification
import com.rheotv.android.db.PushNotificationDao
import com.rheotv.android.ui.activities.onboarding.v2.view.fragment.OnBoardingLoginFragment
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment
import com.rheotv.android.ui.activities.player.activity.newPlayer.VideoCallJobIntentService
import com.rheotv.android.ui.activities.player.activity.newPlayer.VideoCallJobIntentService.Companion.isServiceRunning
import com.rheotv.android.ui.activities.player.activity.newPlayer.activities.FullScreenVideoActivity
import com.rheotv.android.ui.activities.splash.SplashActivity
import com.rheotv.android.utils.*
import com.rheotv.android.utils.LinkHandler.getMojoTargetPath
import com.rheotv.android.utils.LinkHandler.getPostId
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.rheotv.android.utils.worker.SyncFcmTokenWorker
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val sharedPrefsUtils = SharedPrefsUtils()
    private val TAG = MyFirebaseMessagingService::class.java.simpleName
    private var notificationManager: NotificationManager? = null
    private var builder: NotificationCompat.Builder? = null
    private var analyticsFields: Array<String> = arrayOf("title", "body", "target_url", "image_url", "author_username")

    private val dao: PushNotificationDao by lazy { AppDatabase.getInstance(RheoTvApp.getNonUiContext()).notificationDao() }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(RheoTvApp.TAG, "got message")
        trackEvent(remoteMessage)
        if (isFromFreshChat(remoteMessage) || isFromMoEngage(remoteMessage) || isForShowAlarm(remoteMessage.data)) return
        buildNotification(remoteMessage.data, this)
    }

    private fun trackEvent(remoteMessage: RemoteMessage?) {
        AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext()).notificationReceived()
        val tracker = SegmentTracker.getInstance(this)

        remoteMessage?.data?.let { data ->
            val properties = HashMap<String, Any?>()
            try {
                data.entries.forEach {
                    if (analyticsFields.contains(it.key))
                        properties[it.key] = it.value
                }

                AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext()).sendNotificationData(data)
                properties["isFromMoEngagePlatform"] = MoEngageNotificationUtils.isFromMoEngagePlatform(data)
                properties["isSilentPush"] = MoEngageNotificationUtils.isSilentPush(data.toBundle())
                tracker.trackEvent(SegmentConstants.EVENT_NOTIFICATION_RECEIVED, properties)
            } catch (e: Exception) {
                e.printStackTrace()
                tracker.trackEvent(SegmentConstants.EVENT_NOTIFICATION_RECEIVED, properties)
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun configureBuilderNew(context: Context, type: String?): String {
        val importance = NotificationManager.IMPORTANCE_HIGH
        var channelId = NOTIFICATION_CHANNEL_ID
        var channelName = context.getString(R.string.app_name)
        if (type?.contains(NOTIFICATION_TYPE_REWARD, ignoreCase = true) == true) {
            channelId = NOTIFICATION_REWARD_CHANNEL_ID
            channelName = NOTIFICATION_NAME_REWARD
        } else if (type?.contains("call_request", ignoreCase = true) == true) {
            channelId = "calling_channel_id"
        }

        val mChannel: NotificationChannel = notificationManager?.getNotificationChannel(channelId)
                ?: NotificationChannel(channelId, channelName, importance)

        if (type?.contains(NOTIFICATION_TYPE_REWARD, ignoreCase = true) == true) {
            val uri = Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(context.packageName)
                    .path(R.raw.mario_coin_sound.toString())
                    .build()

            val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

            mChannel.setSound(uri, attributes)
        }

        mChannel.setShowBadge(true)
        mChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        this.notificationManager?.createNotificationChannel(mChannel)
        return channelId
//        this.builder?.setChannelId(channelId)
    }

    fun buildNotification(data: Map<String, String>, context: Context) {
        runBlocking { createNotification(data, context) }
    }

    private suspend fun createNotification(data: Map<String, String>, context: Context) = coroutineScope {
        if (isForGoLive(data) || isNotificationExpired(data))
            return@coroutineScope

        val notificationId = registerNotification().toInt()
        isForPlayRequest(data, notificationId)

        val title = data["title"]?.ellipse(90) ?: "You have a new notification"
        val body: String? = data["body"]
        val imageUrl = data["image_url"] ?: data["gcm_image_url"]
        val bitmap: Bitmap? = loadBitmap(imageUrl)
        val isLive: Boolean = data.containsKey("is_live") && data["is_live"] == "true"

        val intent = Intent(context, SplashActivity::class.java)
        intent.action = "dummy_action_$notificationId"
        intent.putExtra("target_url", data["target_url"])
        intent.putExtra("type", data["type"])
        intent.putExtra("notification_payload", HashMap(data))
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationManager = notificationManager
                ?: context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Log.d(TAG, "Inside createNotif 1")
        if ("call_request".equals(data["type"], ignoreCase = true)) {
            if (data["state"].equals(AppConstants.VIDEO_CALL_STATE_DENIED)) {
                VideoCallJobIntentService.stopService()
                LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(AppConstants.INTENT_FILTER_DENIED_CALLING_REQUEST))
            } else {
                var channelId = data["channel_id"]
                if (!VideoCallJobIntentService.Companion.isServiceRunning && (CommonUtils.getLastCallChannelId() == null || (CommonUtils.getLastCallChannelId() != null && !CommonUtils.getLastCallChannelId().equals(channelId, true)))) {
                    CommonUtils.setLastCallChannelId(channelId)
                    val job: JobInfo = JobInfo.Builder(
                            123,
                            ComponentName(context, VideoCallJobIntentService::class.java))
                            .setOverrideDeadline(0)
                            .setExtras(PersistableBundle().also {
                                data.keys.forEach { key ->
                                    it.putString(key, data[key])
                                }
                            })
                            .build()
                    val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler
                    jobScheduler?.schedule(job)
                    Log.i(TAG, "Video call service started")
                }
            }
        } else {
            builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(context, configureBuilderNew(context, data["type"]))
            } else
                NotificationCompat.Builder(context)

            builder?.setCustomContentView(getRemoteView(context, title, body, isLive, bitmap))
            bitmap?.let {
                builder?.setStyle(NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(bitmap)
                        .setBigContentTitle(title))
            }

            builder?.setContentIntent(pendingIntent)
                    ?.setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
                    ?.setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                    ?.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    ?.setPriority(NotificationCompat.PRIORITY_HIGH)
                    ?.setContentTitle(title)
                    ?.setContentText(body)
                    ?.setGroup(generateRandomString())
                    ?.setAutoCancel(true)

            notificationManager?.notify(notificationId, builder?.build())
        }
//        if (bitmap != null && !bitmap.isRecycled) bitmap.recycle()
    }

    private fun getRemoteView(context: Context, title: String, body: String?, isLive: Boolean, bitmap: Bitmap?): RemoteViews {
        val view = RemoteViews(context.packageName, R.layout.notification_collapsed)
        view.setTextViewText(R.id.content_title, title)
        view.setViewVisibility(R.id.live_text, if (isLive) View.VISIBLE else View.GONE)
        body?.let { view.setTextViewText(R.id.content_text, it) }
        if (bitmap == null)
            view.setImageViewBitmap(R.id.big_icon, BitmapFactory.decodeResource(context.resources, R.drawable.poster_splash))
        else
            view.setImageViewBitmap(R.id.big_icon, bitmap)
        return view
    }

    private suspend fun registerNotification() =
            withContext(Dispatchers.IO) {
                try {
                    dao.insertNotification(AppPushNotification())
                } catch (e: SQLiteDatabaseLockedException) {
                    e.printStackTrace()
                    Random().nextLong()
                }
            }

    private suspend fun loadBitmap(imageUrl: String?) =
            withContext(Dispatchers.IO) {
                try {
                    if (!imageUrl.isNullOrEmpty()) Picasso.get().load(imageUrl).resize(ViewUtils.dpToPx(60), ViewUtils.dpToPx(60)).get() else null
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

    private fun generateRandomString(): String {
        val array = ByteArray(7)
        Random().nextBytes(array)
        return String(array, Charset.forName("UTF-8"))
    }

    private fun isFromFreshChat(remoteMessage: RemoteMessage): Boolean {
        if (Freshchat.isFreshchatNotification(remoteMessage)) {
            val notificationConfig = FreshchatNotificationConfig()
                    .setNotificationSoundEnabled(true)
                    .setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

            Freshchat.getInstance(applicationContext).setNotificationConfig(notificationConfig)
            Freshchat.handleFcmMessage(RheoTvApp.getNonUiContext(), remoteMessage)
            Log.d(TAG, " remote message data is from FreshChat")
            return true
        }

        return false
    }

    private fun isFromMoEngage(remoteMessage: RemoteMessage): Boolean {
        if (MoEngageNotificationUtils.isFromMoEngagePlatform(remoteMessage.data)) {
            if (!MoEngageNotificationUtils.isSilentPush(remoteMessage.data.toBundle()))
                PushManager.getInstance().pushHandler?.handlePushPayload(applicationContext, remoteMessage.data)
            Log.d(TAG, " remote message data is from MoEngage")
            return true
        }

        return false
    }

    private fun isForGoLive(data: Map<String, String>): Boolean {
        if (data[EVENT_GO_LIVE] != null) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(EVENT_GO_LIVE))
            return true
        }

        return false
    }

    private fun isForPlayRequest(data: Map<String, String>, notificationId: Int) {
        if (data.containsKey("type") && Objects.requireNonNull<String>(data["type"]).equals("play_request", ignoreCase = true))
            baseContext.sendBroadcast(StreamPlayerContainerFragment.getNotificationIntent(notificationId, getMojoTargetPath(getPostId(data["target_url"]))))
    }

    private fun isNotificationExpired(data: Map<String, String>): Boolean {
        val isLive: Boolean = data.containsKey("is_live") && data["is_live"] == "true"
        try {
            val createAt: Long = (if (data.containsKey("created_at")) data["created_at"]?.toUnixTime()
                    ?: 0L else 0L)
            if (isLive && createAt > 0L && (System.currentTimeMillis() - createAt >= 2 * TimeUtils.MILLIS_AN_HOUR))
                return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sharedPrefsUtils.setStringPreference(this, AppConstants.FCM_TOKEN, token)
        SyncFcmTokenWorker.syncToken(applicationContext)
        Log.i(javaClass.name, "fms_onNewToken : $token")
    }

    private fun isForShowAlarm(data: Map<String, String>): Boolean {
        if (data.containsKey(TYPE) && Objects.requireNonNull<String>(data[TYPE]).equals(TYPE_BANNER_ALARM, ignoreCase = true)) {
            scheduleJob(PersistableBundle().apply {
                putString(AppConstants.ARG_TITLE, data["title"] ?: "")
                putString(AppConstants.EVENT_IMAGE_URL, data["image_url"] ?: "")
                putString(AppConstants.EVENT_POST_ID, data["post_id"] ?: "")
                putString(AppConstants.SOURCE, data["source"] ?: "")
                putString(AppConstants.START_TIME, data["start_time"] ?: "")
            })
            return true
        }

        return false
    }

    private fun scheduleJob(bundle: PersistableBundle) {
        val job: JobInfo = JobInfo.Builder(
                kotlin.random.Random.nextInt(0, Int.MAX_VALUE),
                ComponentName(this, AlarmService::class.java))
                .setOverrideDeadline(0)
                .setExtras(bundle)
                .build()
        val jobScheduler = this.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler
        jobScheduler?.schedule(job)
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "com.rheotv.android"
        private const val NOTIFICATION_REWARD_CHANNEL_ID = "com.rheotv.android.reward"
        private const val NOTIFICATION_NAME_REWARD = "Rewards"
        private const val NOTIFICATION_TYPE_REWARD = "reward"
        private const val EVENT_GO_LIVE = "go-live-event"

        // types
        private const val TYPE = "type"
        private const val TYPE_BANNER_ALARM = "banner_alarm_notification"
    }
}