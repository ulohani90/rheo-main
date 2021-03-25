package com.rheotv.android.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.UnrecognizedInputFormatException
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistTracker.PlaylistStuckException
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.rheotv.android.data.network.models.objects.PostObject
import com.rheotv.android.data.network.requestLayer.ApiService
import com.rheotv.android.data.network.requestLayer.EventsApiService
import com.rheotv.android.di.module.AppModule
import com.rheotv.android.services.PlaybackService
import com.rheotv.android.ui.customViews.streamPlayer.StreamUtils
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.PlayerHeadServiceHelper
import com.rheotv.android.utils.hourglass.Hourglass
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max

class PlaybackService : Service() {
    private var exoPlayer: SimpleExoPlayer? = null
    private var playbackNotification: PlaybackNotification? = null
    private var volume = 1f
    private var isPlaying = true
    private var isVolumeEnable = true
    private var isLiked = false
    private var holder: PlayerHeadHolder? = null

    private var properties = HashMap<String, Any>()
    var timeElapsedOnPlayerPage: Long = 0
    var timeElapsed: Long = 0
    private var isFirstWatchEventTracked = false
    private var deviceId = ""
    private var postId = ""
    private var authorId = ""
    private var authorName = ""
    private var eventsApiService: EventsApiService? = null
    private var apiService: ApiService? = null
    private var resumeWindow = 0
    private var resumePosition: Long = 0

//    private val screenOnOffReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val strAction = intent.action
//            val myKM = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//            if (strAction == Intent.ACTION_USER_PRESENT || strAction == Intent.ACTION_SCREEN_OFF || strAction == Intent.ACTION_SCREEN_ON) {
//                if (myKM.isKeyguardLocked) {
//                    isPlaying = false
//                    exoPlayer?.playWhenReady = false
//                    println("Screen off " + "LOCKED")
//                    streamTimer.pauseTimer()
//                } else {
//                    isPlaying = true
//                    exoPlayer?.playWhenReady = true
//                    println("Screen off " + "UNLOCKED")
//                    streamTimer.resumeTimer()
//                }
//                buildNotification()
//            }
//        }
//    }

    private val notificationListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == null) return
            when (intent.action) {
                PlaybackNotification.ACTION_PLAY -> {
                    isPlaying = !(exoPlayer?.isPlaying ?: false)
                    exoPlayer?.playWhenReady = isPlaying
                    if (isPlaying) streamTimer.resumeTimer() else streamTimer.pauseTimer()
                    buildNotification()
//                    stopForeground(isPlaying)

                }
                PlaybackNotification.ACTION_VOLUME -> {
                    volume = if (exoPlayer?.volume == 0f) 1f else 0f
                    exoPlayer?.volume = volume
                    isVolumeEnable = volume == 1f
                    buildNotification()
                }
                PlaybackNotification.ACTION_LIKE -> {
                    if (!isLiked) {  // if not liked yet, mark as like
                        isLiked = !isLiked
                        buildNotification()
                        postHeart()
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(javaClass.simpleName, "playback_service: onCreate")
        playbackNotification = PlaybackNotification(this)
        initNotification()
        registerBroadcastReceiver()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(javaClass.simpleName, "playback_service: onStartCommand")
        if (holder == null && intent.hasExtra(AppConstants.ARG_PLAYER_HOLDER) && intent.getParcelableExtra(AppConstants.ARG_PLAYER_HOLDER) as? PlayerHeadHolder != null)
            holder = intent.getParcelableExtra(AppConstants.ARG_PLAYER_HOLDER) as? PlayerHeadHolder
        Handler().postDelayed(this::initNotification, 1000)
        //        prepareExoPlayerFromFileUri(Uri.parse("http://bbcwssc.ic.llnwd.net/stream/bbcwssc_mp1_ws-einws"));
        prepareExoPlayerFromFileUri(Uri.parse(holder?.post?.audioUrl?.url))
        holder?.post?.let { setupAnalytics(it) }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        clear()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        stopSelf()
    }

    private fun buildNotification() {
        playbackNotification?.createNotification(
                this@PlaybackService,
                holder?.post?.title ?: "Rheo Live",
                holder?.post?.game?.name ?: "Starting Audio",
                holder?.post?.thumbnail ?: AppConstants.DEFAULT_PROFILE_PIC,
                holder?.post?.shareUrl ?: "",
                isPlaying,
                isVolumeEnable,
                isLiked)
        {
            if (!isPlaying) {
                playbackNotification?.showNotification(it)
                stopForeground(false)
            } else {
                registerBroadcastReceiver()
                startForeground(PlaybackNotification.NOTIFICATION_ID, it)
            }
        }
    }

    private fun initNotification() {
        try {
            playbackNotification?.createNotification(
                    this@PlaybackService,
                    holder?.post?.title ?: "Rheo Live",
                    holder?.post?.game?.name ?: "Starting Audio",
                    holder?.post?.thumbnail ?: AppConstants.DEFAULT_PROFILE_PIC,
                    holder?.post?.shareUrl ?: "",
                    isPlaying,
                    isVolumeEnable,
                    isLiked)
            {
                startForeground(PlaybackNotification.NOTIFICATION_ID, it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * System Defined Broadcast
     */
    private fun registerBroadcastReceiver() {
        try {
//            val filter = IntentFilter()
////            filter.addAction(Intent.ACTION_SCREEN_ON)
////            filter.addAction(Intent.ACTION_SCREEN_OFF)
////            filter.addAction(Intent.ACTION_USER_PRESENT)
////            registerReceiver(screenOnOffReceiver, filter)
            val notificationFilter = IntentFilter(PlaybackNotification.PLAYBACK_FILTER)
            notificationFilter.addAction(PlaybackNotification.ACTION_PLAY)
            notificationFilter.addAction(PlaybackNotification.ACTION_VOLUME)
            notificationFilter.addAction(PlaybackNotification.ACTION_LIKE)
            registerReceiver(notificationListener, notificationFilter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clear() {
        try {
            streamTimer.stopTimer()
//            unregisterReceiver(screenOnOffReceiver)
            exoPlayer?.release()
            PlayerHeadServiceHelper.getInstance().stopPlayAudioService()
            unregisterReceiver(notificationListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val eventListener: Player.EventListener = object : Player.EventListener {
        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
            Log.i(TAG, "onTracksChanged")
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            Log.i(TAG, "onLoadingChanged")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.i(TAG, "onPlayerStateChanged: playWhenReady = $playWhenReady playbackState = $playbackState")
            when (playbackState) {
                ExoPlayer.STATE_ENDED -> {
                    Log.i(TAG, "Playback ended!")
                    stopSelf()
                }
                ExoPlayer.STATE_READY -> {
                }
                ExoPlayer.STATE_BUFFERING -> Log.i(TAG, "Playback buffering!")
                ExoPlayer.STATE_IDLE -> Log.i(TAG, "ExoPlayer idle!")
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            try {
                Log.i(TAG, "onPlaybackError: " + error.message)
                val cause = error.cause?.localizedMessage
                cause ?: return
                if (error.cause is UnrecognizedInputFormatException ||
                        error.cause is PlaylistStuckException) {
                    stopSelf()
                } else if (cause.equals("Response code: 404", ignoreCase = true)
                        || error.cause is SocketTimeoutException) {
                    updateResumePosition()
                    resumePosition += 4000
                    exoPlayer?.seekTo(resumeWindow, resumePosition)
                } else {
                    updateResumePosition()
                    resumePosition += 8000
                    exoPlayer?.seekTo(resumeWindow, resumePosition)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun updateResumePosition() {
            resumeWindow = exoPlayer?.currentWindowIndex ?: 0
            resumePosition = max(0L, exoPlayer?.contentPosition ?: 0L)
        }
    }

    /**
     * Prepares exoplayer for audio playback from a local file
     *
     * @param uri
     */
    private fun prepareExoPlayerFromFileUri(uri: Uri) {
        val factory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
        exoPlayer = getPlayer(DefaultTrackSelector(factory))
        exoPlayer?.volume = volume
        exoPlayer?.playWhenReady = true
        buildPlayer(uri)
    }

    private fun getPlayer(selector: DefaultTrackSelector): SimpleExoPlayer {
        val builder = DefaultLoadControl.Builder()
        /* This is 50000 milliseconds in ExoPlayer 2.9.6 */
        val loadControlBufferMs = 50000

        /* Configure the DefaultLoadControl to use the same value for */
        builder.setBufferDurationsMs(loadControlBufferMs,
                loadControlBufferMs,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
        val loadControl = builder.createDefaultLoadControl()
        val player = ExoPlayerFactory.newSimpleInstance(this,
                DefaultRenderersFactory(this), selector, loadControl)
        player.addListener(eventListener)
        return player
    }

    private fun buildPlayer(uri: Uri) {
        val mediaSource = StreamUtils.buildMediaSource(uri)
        exoPlayer?.prepare(mediaSource, true, true)
    }

    private val streamTimer: Hourglass = object : Hourglass(Date().time, 1000) {
        override fun onTimerTick(timeRemaining: Long, passedTime: Long) {
            val ttl = passedTime / 1000
            if (ttl > 0) {
                val exoPosition: Long = (exoPlayer?.currentPosition ?: 1) / 1000
                if (ttl % 10 == 0L) {
                    Log.i(TAG, "hit_10th_api : $ttl")
                    makeViewApiCall(exoPosition)
                }
            }
            if (ttl == 0L || ttl == 30L || (ttl > 30 && ttl % (4.5 * 60).toInt() == 0L)) {
                properties["time_elapsed"] = ttl
                Log.i(TAG, "time --> $ttl")
                SegmentTracker.getInstance(this@PlaybackService).trackEvent(SegmentConstants.EVENT_VIDEO_PLAYER_WIDGET_WATCH_STREAM, properties)
                properties["time_elapsed"] = ttl + timeElapsedOnPlayerPage
                if (!isFirstWatchEventTracked) {
                    isFirstWatchEventTracked = true
                    if (CommonUtils.isFirstWatchEventNotTracked()) {
                        CommonUtils.setFirstWatchEventTracked()
                        SegmentTracker.getInstance(this@PlaybackService).trackEvent(SegmentConstants.EVENT_FIRST_WATCH_STREAM, properties)
                    }
                }
                SegmentTracker.getInstance(this@PlaybackService).trackEvent(SegmentConstants.EVENT_WATCH_STREAM, properties)
            }
        }

        override fun onTimerFinish() {}
    }

    private fun setupAnalytics(post: PostObject) {
        try {
            properties["media_url"] = post.audioUrl ?: ""
            properties["username"] = post.author?.user?.username ?: ""
            properties["game"] = post.game?.name ?: ""
            properties["game_id"] = post.game?.id ?: ""
            properties["is_live"] = post.isLive ?: false
            properties["type"] = if (post.isLive) "live" else "fullRecorded"
            properties["language"] = post.language ?: ""
            properties["title"] = post.title ?: ""
            properties["name"] = post.author?.user?.userFullName ?: ""
            properties["author_id"] = post.author?.user?.id?.toString() ?: ""
            properties["isLoggedIn"] = CommonUtils.isUserLoggedin()
            properties["in_window_mode"] = true
            properties["format"] = "audio"
            this.deviceId = CommonUtils.getDevId(this)
            this.postId = post.id ?: ""
            this.authorId = post.author?.user?.id?.toString() ?: ""
            this.authorName = post.author?.user?.username ?: ""
            SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_PLAYER_WIDGET_SHOWN, properties)
            buildEventService()

            streamTimer.startTimer()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun makeViewApiCall(exoPosition: Long) {
        try {
            val otherInfoJson = JSONObject()
            try {
                otherInfoJson.put("post_id", postId)
                otherInfoJson.put("author_id", authorId)
                otherInfoJson.put("author_username", authorName)
                otherInfoJson.put("viewer_username", deviceId)
                otherInfoJson.put("duration", 10)
                otherInfoJson.put("time_elapsed", exoPosition)
                otherInfoJson.put("format", "audio")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            Log.i(TAG, "playback_makeViewApiCall at " + System.currentTimeMillis() + " for 10 secs and player time" + exoPosition)
            val otherInfo = otherInfoJson.toString()
            val otherInfoReqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), otherInfo)

            eventsApiService?.postVideoView(otherInfoReqBody)?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {}
                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun buildEventService() {
        val interceptor = AppModule.getServiceInterceptor(this)
        val httpLoggingInterceptor = AppModule.httpLoggingInterceptor()
        val cache = AppModule.provideCache(this)
        val client = AppModule.provideOkhttp(interceptor, httpLoggingInterceptor, cache)
        eventsApiService = AppModule.provideEventsService(client, Gson())
        apiService = AppModule.provideApiService(client, Gson())

    }

    private fun postHeart() {
        val requestParam = JSONObject()
        requestParam.put("post_id", postId)
        val bodyParam = requestParam.toString()
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyParam)
        apiService?.postHeart(body)?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    Log.i(TAG, "post_heart_success")
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.i(TAG, "post_heart_fail $t")
            }
        })
    }

    companion object {
        private val TAG = PlaybackService::class.java.simpleName
    }
}