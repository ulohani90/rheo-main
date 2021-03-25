package com.rheotv.android.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.rheotv.android.R
import com.rheotv.android.ui.activities.splash.SplashActivity
import com.rheotv.android.utils.BindingUtils.loadBitmap
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.loadBitmap
import java.nio.charset.Charset
import java.util.*

class PlaybackNotification constructor(
        val context: Context
) {
    private val TAG = PlaybackNotification::class.java.simpleName
    private var notificationManager: NotificationManager? = null
    private var builder: NotificationCompat.Builder? = null
    private var notification: Notification? = null
    private lateinit var pendingIntentPlay: PendingIntent
    private lateinit var pendingIntentVolume: PendingIntent
    private lateinit var pendingIntentHeart: PendingIntent
    private var remoteView: RemoteViews? = null

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun configureBuilderNew(context: Context) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channelId = NOTIFICATION_CHANNEL_ID
        val channelName = context.getString(R.string.app_name)

        val mChannel: NotificationChannel = notificationManager?.getNotificationChannel(channelId)
                ?: NotificationChannel(channelId, channelName, importance)

        mChannel.setShowBadge(true)
        mChannel.setSound(null, null)
        mChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        this.notificationManager?.createNotificationChannel(mChannel)
        this.builder?.setChannelId(channelId)
    }

    fun showNotification(notification: Notification) {
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    public fun cancelNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    fun createNotification(context: Context,
                           title: String?,
                           body: String?,
                           imageUrl: String?,
                           postUrl: String?,
                           isPlaying: Boolean,
                           isVolumeEnabled: Boolean,
                           isLiked: Boolean,
                           onStartForeground: (Notification) -> Unit
    ) {
        context.loadBitmap(imageUrl!!) { bitmap ->
            val id = (System.currentTimeMillis() / 1000L).toString()
            pendingIntentPlay = PendingIntent.getBroadcast(context, 0,
                    Intent(PLAYBACK_FILTER).setAction(ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT)
            pendingIntentVolume = PendingIntent.getBroadcast(context, 0,
                    Intent(PLAYBACK_FILTER).setAction(ACTION_VOLUME), PendingIntent.FLAG_UPDATE_CURRENT)
            pendingIntentHeart = PendingIntent.getBroadcast(context, 0,
                    Intent(PLAYBACK_FILTER).setAction(ACTION_LIKE), PendingIntent.FLAG_UPDATE_CURRENT)

            val intent = Intent(context, SplashActivity::class.java)
            intent.action = "dummy_action_$NOTIFICATION_ID"
            intent.putExtra("target_url", postUrl)
            intent.putExtra("type", "media_notification")
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            notificationManager = notificationManager
                    ?: context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            builder = NotificationCompat.Builder(context, id)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                configureBuilderNew(context)

            builder?.setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1)
                    .setShowCancelButton(true)
            )

            builder?.let {
                //it.setCustomContentView(getRemoteView(context, title, body, "https://i.pinimg.com/736x/2f/34/7e/2f347eb7cad5f97a6cf6eb4097e9e3bd.jpg", isPlaying, isVolumeEnabled, true))
                it.setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
                        .setContentIntent(pendingIntent)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setLargeIcon(bitmap)
                        .setSound(null)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSubText("Live Stream")
                        .setGroup(generateRandomString())
                        .addAction(if (isVolumeEnabled) R.drawable.ic_volume_up_white_24dp else R.drawable.ic_volume_off_white_24dp, "Volume", pendingIntentVolume)
                        .addAction(if (isPlaying) R.drawable.ic_pause_button_white_24dp else R.drawable.ic_play_button_white_24dp, "Play", pendingIntentPlay)
                        .addAction(if (isLiked) R.drawable.avd_heart_filled else R.drawable.avd_heart_outline, "Heart", pendingIntentHeart)
                        .setAutoCancel(true)
            }
            notification = builder?.build()!!
            onStartForeground.invoke(notification!!)
        }
    }

    private fun getRemoteView(context: Context, title: String, body: String, url: String, isPlaying: Boolean, isVolumeEnabled: Boolean, isHeartSent: Boolean): RemoteViews {
        remoteView = RemoteViews(context.packageName, R.layout.layout_playback_notification)
        remoteView?.setTextViewText(R.id.sub_header_text_view, title)
        remoteView?.setTextViewText(R.id.detail_text_view, body)
        Glide.with(this.context).asBitmap()
                .load(url)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(14)))
                .addListener(object : RequestListener<Bitmap?> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap?>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap?>?, dataSource: com.bumptech.glide.load.DataSource?, isFirstResource: Boolean): Boolean {
                        remoteView?.setImageViewBitmap(R.id.poster_image_view, resource)
                        return true
                    }
                }).submit()
        remoteView?.setOnClickPendingIntent(R.id.play_button, pendingIntentPlay)
        remoteView?.setOnClickPendingIntent(R.id.volume_button, pendingIntentVolume)
        remoteView?.setOnClickPendingIntent(R.id.heart_button, pendingIntentHeart)
        remoteView?.setImageViewResource(R.id.volume_button, if (isVolumeEnabled) R.drawable.avd_unmute else R.drawable.avd_mute)
        remoteView?.setImageViewResource(R.id.play_button, if (isPlaying) R.drawable.ic_pause_button_white_24dp else R.drawable.ic_play_button_white_24dp)
        remoteView?.setImageViewResource(R.id.heart_button, if (isHeartSent) R.drawable.avd_heart_filled else R.drawable.avd_heart_outline)
        return remoteView!!
    }

    fun onPlayStateChange(isPlaying: Boolean) {
        remoteView?.setImageViewResource(R.id.play_button, if (isPlaying) R.drawable.ic_pause_button_white_24dp else R.drawable.ic_play_button_white_24dp)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    fun onVolumeChange(isVolumeEnabled: Boolean) {
        remoteView?.setImageViewResource(R.id.volume_button, if (isVolumeEnabled) R.drawable.avd_unmute else R.drawable.avd_mute)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    private fun generateRandomString(): String {
        val array = ByteArray(7)
        Random().nextBytes(array)
        return String(array, Charset.forName("UTF-8"))
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "com.rheotv.android"
        const val ACTION_PLAY = "action_play"
        const val ACTION_VOLUME = "action_volume"
        const val ACTION_LIKE = "action_like"
        const val NOTIFICATION_ID = 0x000111
        const val PLAYBACK_FILTER = "playback_filter"
    }
}