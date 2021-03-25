package com.rheotv.android.services

import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.google.gson.Gson
import com.rheotv.android.R
import com.rheotv.android.data.network.models.streamUpdates.StreamEventResponse
import com.rheotv.android.data.network.requestLayer.ApiService
import com.rheotv.android.di.module.AppModule
import com.rheotv.android.helpers.grpc.GrpcConnectionManager
import com.rheotv.android.helpers.grpc.IncomingChatListener
import com.rheotv.android.ui.activities.audioroom.model.AudioConnection
import com.rheotv.android.ui.activities.audioroom.model.AudioRoomDetail
import com.rheotv.android.ui.activities.audioroom.model.ChatRoomActionResponse
import com.rheotv.android.ui.activities.audioroom.model.OwnerDetail
import com.rheotv.android.ui.activities.audioroom.view.AudioChatRoomActivity
import com.rheotv.android.ui.activities.audioroom.view.AudioChatRoomFragment
import com.rheotv.android.ui.activities.audioroom.viewmodel.AudioChatRoomActivityViewModel
import com.rheotv.android.ui.activities.profile.viewprofile.utils.CommentAction
import com.rheotv.android.utils.*
import com.rheotv.android.utils.hourglass.Hourglass
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.squareup.picasso.Picasso
import goChat.Services
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap

const val AUDIO_ACTION = "audio_action"
const val AUDIO_DETAIL = "audio_detail"
const val AUDIO_IS_SELF_MUTE = "is_self_mute"
const val AUDIO_MUTE_UNMUTE_UID = "mute_unmute_uid"
const val AUDIO_MUTE_USER = "mute_user"
const val AUDIO_START_CALL = "start_call"
const val NOTIFICATION_ID = 0x010101
const val AUDIO_ROOM_FILTER = "audio_room_filter"
const val ACTION_SPEAKER = "action_speaker"
const val ACTION_MIC = "action_mic"
const val ACTION_LEAVE_ROOM = "action_leave_room"
const val AUDIO_ROOM_PROPERTIES = "audio_room_properties"
const val SHOW_AUDIO_ROOM_CONTROL_HEAD = "show_audio_room_control_head"
const val MUTE_ALL = "mute_all"
const val STOP_SERVICE = "stop_service"
const val IS_BACKGROUND = "is_background"

class AudioRoomService : Service() {
    private val TAG = javaClass.simpleName

    // Binder given to clients
    //private val binder = AudioBinder()

    private val agoraConnectionUtils by lazy { AgoraConnectionUtils() }
    private val apiService by lazy { buildEventService() }
    private var details: AudioRoomDetail? = null
    private var analyticsProperties: HashMap<String, Any?>? = null
    private var isRoomActive = false

    var roomControllerView: View? = null
    var mWindowManager: WindowManager? = null
    var muteBtn: ImageView? = null
    var optionsLayout: LinearLayout? = null
    var chatHeadButtonHeight: Int = 0
    private var roomState = "foreground"
    private var mStartTime = System.currentTimeMillis()
    private var isBackground: Boolean = true
    val closeChatRoomHandler = Handler()
    val grpcConnectionManager by lazy { GrpcConnectionManager() }
    val closeControllerRunnable = Runnable { collapseAnimation(optionsLayout) }
    val gson by lazy { Gson() }

    private val chatConnectionCallbackListener = object : IncomingChatListener() {
        override fun waitAndReconnect() {
            connectGrpc()
        }

        override fun onConnectionComplete() {
            if (isServiceInBackground)
                connectGrpc()
        }

        override fun onDynamicAction(chatMessage: Services.ChatMessage) {
            val response: StreamEventResponse = gson.fromJson(chatMessage.message, StreamEventResponse::class.java)
            val username = response.participant?.participantDetails?.username
            if (response.type == AppConstants.MSG_TYPE_AUDIO_ROOM) {
                when (response.action) {
                    AppConstants.STATUS_MUTE -> {
                        if (username == CommonUtils.getUserName()) {
                            isSelfMuted = true
                            agoraConnectionUtils.muteLocalAudio()
                            sendBroadcast(Intent().apply {
                                action = AUDIO_ACTION
                                putExtra(AUDIO_ACTION, AudioConnection.SelfMute(isSelfMuted))
                            })
                            handleControllerBtnState()
                            buildNotification()
                            Handler(Looper.getMainLooper()).post {
                                this@AudioRoomService?.showToast("You have been muted by room owner.")
                            }
                        }
                    }

                    AppConstants.STATUS_UNMUTE -> {
                        if (username == CommonUtils.getUserName()) {
                            if (highlightedUser != null && !(CommonUtils.getUserID() == highlightedUser?.id || CommonUtils.getUserID() == details?.audioGroup?.ownerDetails?.id)) return
                            isSelfMuted = false
                            agoraConnectionUtils.unMuteLocalAudio()
                            sendBroadcast(Intent().apply {
                                action = AUDIO_ACTION
                                putExtra(AUDIO_ACTION, AudioConnection.SelfMute(isSelfMuted))
                            })
                            handleControllerBtnState()
                            buildNotification()
                        }
                    }

                    AppConstants.STATUS_CHATROOM_ENDED -> {
                        removeRoomController()
                        stopSelf()
                        sendBroadcast(Intent().apply {
                            action = AUDIO_ACTION
                            putExtra(AUDIO_ACTION, AudioConnection.ExitRoom("Room ended"))
                        })
                        Handler(Looper.getMainLooper()).post {
                            this@AudioRoomService?.showToast("Room Ended")
                        }
                    }

                    AppConstants.STATUS_BLOCKED -> {
                        if (username == CommonUtils.getUserName()) {
                            removeRoomController()
                            stopSelf()
                            sendBroadcast(Intent().apply {
                                action = AUDIO_ACTION
                                putExtra(AUDIO_ACTION, AudioConnection.ExitRoom(getString(R.string.blocked_from_room)))
                            })

                            Handler(Looper.getMainLooper()).post {
                                this@AudioRoomService?.showToast(getString(R.string.blocked_from_room))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun connectGrpc(delay: Long = 2000) {
        CoroutineScope(Dispatchers.IO).doAfter(delay) {
            grpcConnectionManager.connectToGroup(
                    deviceId = "${CommonUtils.getDevId()}_background",
                    groupId = details?.grpcConnectionId,
                    listener = chatConnectionCallbackListener
            )
        }
    }

    private val notificationBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_MIC -> {
                    if (highlightedUser != null && !(CommonUtils.getUserID() == highlightedUser?.id || CommonUtils.getUserID() == details?.audioGroup?.ownerDetails?.id)) return
                    isSelfMuted = !isSelfMuted
                    buildNotification()
                    handleControllerBtnState()
                    toggleMute()
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_MUTE_BUTTON_CLICKED,
                            HashMap(analyticsProperties ?: hashMapOf()).apply {
                                put("muted_username", details?.text)
                                put("referrer", "notification_tray")
                            })
                    sendBroadcast(Intent().apply {
                        action = AUDIO_ACTION
                        putExtra(AUDIO_ACTION, AudioConnection.SelfMute(isSelfMuted))
                    })

                    muteUnMuteParticipant(if (isSelfMuted) "mute" else "unmute")
                }
                ACTION_LEAVE_ROOM -> {
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_EXIT_BUTTON_CLICKED,
                            HashMap(analyticsProperties ?: hashMapOf()).apply {
                                put("connection_duration", (System.currentTimeMillis() - mStartTime) / 1000)
                            })
                    removeRoomController()
                    stopSelf()
                    sendBroadcast(Intent().apply {
                        action = AUDIO_ACTION
                        putExtra(AUDIO_ACTION, AudioConnection.ExitRoom())
                    })
                }
            }
        }
    }

    private var mHeadsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.hasExtra("state")) {
                if (intent.getIntExtra("state", 0) == 0) {
                    agoraConnectionUtils?.switchToSpeaker(true)

                } else if (intent.getIntExtra("state", 0) == 1) {
                    agoraConnectionUtils?.switchToSpeaker(false)
                }
            }
        }
    }

    private var mBluetoothHeadsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val action = intent?.action
            var device = intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE);
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    //Device found
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    agoraConnectionUtils.switchToSpeaker(false)
                    //Device is now connected
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    //Done searching
                }
                BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> {
                    //Device is about to disconnect
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    //Device has disconnected
                    agoraConnectionUtils.switchToSpeaker(true)
                }
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        //return binder
        return null
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        buildNotification()
        registerBroadcastReceiver()
        setUpAgora()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.hasExtra(STOP_SERVICE) == true && intent.getBooleanExtra(STOP_SERVICE, false)) {
            leaveRoom()
            stopSelf()
            return START_NOT_STICKY
        }

        (intent?.getParcelableExtra(AUDIO_DETAIL) as? AudioRoomDetail)?.let {
            Log.i(TAG, "onStartCommand: ${it.chatRoomId} and ${details?.chatRoomId}")
            if (it.chatRoomId != details?.chatRoomId && !it.chatRoomId.isNullOrEmpty()) {
                // leave if room already created
                details?.chatRoomId?.let { id ->
                    if (id.isNotEmpty()) {
                        agoraConnectionUtils.endCall()
                        leaveRoom()
                        audioRoomTimer.stopTimer()
                    }
                }
                connectedRoomId = it.chatRoomId
                connectedRoomOwner = it.audioGroup?.ownerDetails?.username
                isSelfMuted = intent.getBooleanExtra(AUDIO_IS_SELF_MUTE, false)

                details = it
                startCall()

                joinRoom(isSelfMuted)
                audioRoomTimer.startTimer()
                mStartTime = System.currentTimeMillis()
            }
        }

        analyticsProperties = (intent?.getSerializableExtra(AUDIO_ROOM_PROPERTIES) as? HashMap<String, Any?>
                ?: hashMapOf())
        isBackground = intent?.getBooleanExtra(IS_BACKGROUND, true) ?: true

        if (intent?.hasExtra(AUDIO_MUTE_UNMUTE_UID) == true && intent.hasExtra(AUDIO_MUTE_USER)) {
            val uid = intent.getIntExtra(AUDIO_MUTE_UNMUTE_UID, -1)
            if (uid == CommonUtils.getUserID()) {
                isSelfMuted = intent.getBooleanExtra(AUDIO_MUTE_USER, true)
                toggleMute()
            } else {
                if (intent.getBooleanExtra(AUDIO_MUTE_USER, false))
                    agoraConnectionUtils.muteRemoteAudio(uid)
                else
                    agoraConnectionUtils.unmuteRemoteAudio(uid)
            }
        }

        roomState = "foreground"
        if (intent?.hasExtra(SHOW_AUDIO_ROOM_CONTROL_HEAD) == true && intent.getBooleanExtra(SHOW_AUDIO_ROOM_CONTROL_HEAD, false)) {
            if (isBackground && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)))
                addViewToService()
            isServiceInBackground = true
            connectGrpc(0)
            roomState = "background"
        }

        if (intent?.hasExtra(MUTE_ALL) == true && intent.getBooleanExtra(MUTE_ALL, false)) {
            isSelfMuted = !(highlightedUser != null && (highlightedUser?.id == CommonUtils.getUserID() || details?.audioGroup?.ownerDetails?.id == CommonUtils.getUserID()))
            agoraConnectionUtils.muteAllRemoteAudio()
        }
        analyticsProperties?.set("current_state", roomState)
        (analyticsProperties ?: hashMapOf()).apply { put("on_click", "notification") }

        buildNotification()
        return START_NOT_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addViewToService() {
        val LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            WindowManager.LayoutParams.TYPE_PHONE;
        }

        roomControllerView = (baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.chatroom_overlay_view_layout, null)
        val params: WindowManager.LayoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.LEFT //Initially view will be added to top-left corner

        params.x = resources.displayMetrics.widthPixels - AppUtilsKt.convertDpToPx(this, 62f)
        params.y = resources.displayMetrics.heightPixels - AppUtilsKt.convertDpToPx(this, 148f)
        //params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        //Add the view to the window
        //params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        //Add the view to the window
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        mWindowManager?.addView(roomControllerView ?: return, params)
        optionsLayout = roomControllerView?.findViewById<LinearLayout>(R.id.options_layout)
        val headImage = roomControllerView?.findViewById<ImageView>(R.id.head)
        BindingUtils.setImageUrlCircular(headImage, details?.thumbnail
                ?: AppConstants.DEFAULT_AVATAR, 56, 56)
        muteBtn = roomControllerView?.findViewById<ImageView>(R.id.btn_mute)
        handleControllerBtnState()
        muteBtn?.setOnClickListener {
            closeChatRoomHandler.removeCallbacks(closeControllerRunnable)
            sendBroadcast(Intent(AUDIO_ROOM_FILTER).setAction(ACTION_MIC))
            closeChatRoomHandler.postDelayed(closeControllerRunnable, 5000)
        }
        roomControllerView?.findViewById<ImageView>(R.id.btn_exit)?.setOnClickListener {
            (analyticsProperties ?: hashMapOf()).apply { put("on_click", "floating_bubble") }
            closeChatRoomHandler.removeCallbacks(closeControllerRunnable)
            sendBroadcast(Intent(AUDIO_ROOM_FILTER).setAction(ACTION_LEAVE_ROOM))
            closeChatRoomHandler.postDelayed(closeControllerRunnable, 5000)
        }
        roomControllerView?.findViewById<ImageView>(R.id.btn_view)?.setOnClickListener {
            startAudioChatRoomActivity()
        }
        headImage?.setOnTouchListener(object : View.OnTouchListener {
            private var lastAction = 0
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            var clickStartTime: Long = 0
            var clickEndTime: Long = 0

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (roomControllerView?.isAttachedToWindow == false) return true
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        clickStartTime = System.currentTimeMillis()
                        //remember the initial position.
                        initialX = params.x
                        initialY = params.y

                        //get the touch location
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        Log.i(PlayerHeadService::class.java.canonicalName, "Initial X $initialX")
                        Log.i(PlayerHeadService::class.java.canonicalName, "Initial Y $initialX")
                        Log.i(PlayerHeadService::class.java.canonicalName, "Initial Touch X $initialTouchX")
                        Log.i(PlayerHeadService::class.java.canonicalName, "Initial Touch Y $initialTouchX")
                        lastAction = event.action
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.
                        clickEndTime = System.currentTimeMillis()
                        if (clickEndTime - clickStartTime <= 200) {
                            if (optionsLayout?.visibility == View.VISIBLE) {
                                collapseAnimation(optionsLayout)
                                closeChatRoomHandler.removeCallbacks(closeControllerRunnable)
                            } else {
                                expandViewAnimation(optionsLayout)
                                closeChatRoomHandler.postDelayed(closeControllerRunnable, 5000)
                            }
                        } else {

                        }
                        lastAction = event.action
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        Log.i("Current Position", "Moving to X::" + params.x + "  Y::" + params.y)
                        if (params.x < 0) {
                            params.x = 0
                        }
                        if (params.y < 0) {
                            params.y = 0
                        }
                        mWindowManager?.updateViewLayout(roomControllerView, params)
                        lastAction = event.action
                        return true
                    }
                }
                return false
            }
        })
        chatHeadButtonHeight = AppUtilsKt.convertDpToPx(this, 144f)
    }

    fun expandViewAnimation(v: View?) {
        v ?: return
        val initialHeight = 56

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.layoutParams.width = AppUtilsKt.convertDpToPx(this, 56f)
        v.visibility = View.VISIBLE
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                Log.i("ExpandAnim", "$interpolatedTime::$chatHeadButtonHeight")
                v.layoutParams.height = if (interpolatedTime == 1f) WindowManager.LayoutParams.WRAP_CONTENT else ((chatHeadButtonHeight - initialHeight) * interpolatedTime).toInt() + initialHeight
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        a.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                closeChatRoomHandler.removeCallbacks(closeControllerRunnable)
                closeChatRoomHandler.postDelayed(closeControllerRunnable, 5000)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        // Expansion speed of 1dp/ms
        a.duration = ((chatHeadButtonHeight - initialHeight) / resources.displayMetrics.density).toLong()
        v.startAnimation(a)
    }

    fun collapseAnimation(v: View?) {
        v ?: return
        val targetHeight: Int = AppUtilsKt.convertDpToPx(this, 48f)
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                v.layoutParams.height = chatHeadButtonHeight - ((chatHeadButtonHeight - targetHeight) * interpolatedTime).toInt()
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        a.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                closeChatRoomHandler.removeCallbacks(closeControllerRunnable)
                v.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        if (chatHeadButtonHeight < targetHeight) {
            return
        }
        // Collapse speed of 1dp/ms
        a.duration = ((chatHeadButtonHeight - targetHeight) / resources.displayMetrics.density).toLong()
        v.startAnimation(a)
    }

    private fun startAudioChatRoomActivity() {
        val contentIntent = Intent(this, AudioChatRoomActivity::class.java)
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        contentIntent.putExtras(bundleOf(
                AudioChatRoomActivity.ARG_GROUP_DETAILS to details?.audioGroup,
                AudioChatRoomActivity.ARG_ONLINE_COUNT to details?.onlineMemberCount,
                AudioChatRoomActivity.ARG_CHAT_ROOM_ID to details?.chatRoomId))
        startActivity(contentIntent)
    }

    fun handleControllerBtnState() {
        if (isSelfMuted) {
            muteBtn?.setImageResource(R.drawable.avd_mic_off_24)
        } else {
            muteBtn?.setImageResource(R.drawable.avd_mic_24)
        }
    }

    private fun toggleMute() {
        if (isSelfMuted) {
            Log.e("universal", "local_muted")
            agoraConnectionUtils.muteLocalAudio()
        } else {
            Log.e("universal", "local_un_muted")
            agoraConnectionUtils.unMuteLocalAudio()
        }
    }

    private fun setUpAgora() {
        agoraConnectionUtils.apply {
            initEngineForAudioCall(this@AudioRoomService)
            onCallConnected = {
                isConnected = true
                sendBroadcast(Intent().apply {
                    action = AUDIO_ACTION
                    putExtra(AUDIO_ACTION, AudioConnection.CallConnected)
                })
            }

            onCallDisconnected = {
                isConnected = false
                sendBroadcast(Intent().apply {
                    action = AUDIO_ACTION
                    putExtra(AUDIO_ACTION, AudioConnection.CallDisconnected)
                })
            }

            onCallLeft = {
                sendBroadcast(Intent().apply {
                    action = AUDIO_ACTION
                    putExtra(AUDIO_ACTION, AudioConnection.CallLeft(it))
                })
            }

            onUserJoined = {
                sendBroadcast(Intent().apply {
                    action = AUDIO_ACTION
                    putExtra(AUDIO_ACTION, AudioConnection.UserJoined(it))
                })
            }

            onFirstUser = {
                sendBroadcast(Intent().apply {
                    action = AUDIO_ACTION
                    putExtra(AUDIO_ACTION, AudioConnection.FirstUser)
                })
            }

            onSpeakerIndicate = {
                sendBroadcast(Intent().apply {
                    action = AUDIO_ACTION
                    putExtra(AUDIO_ACTION, AudioConnection.SpeakerIndicate(it?.map { it.uid }))
                })
            }

            onUserCount = {
                sendBroadcast(Intent().apply {
                    action = AUDIO_ACTION
                    putExtra(AUDIO_ACTION, AudioConnection.UserCountUpdate(it))
                })
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun removeAudioChatRoomController(param: EventBusModel.RemoveChatroomController) {
        removeRoomController()
    }

    private fun removeRoomController() {
        isServiceInBackground = false
        grpcConnectionManager.closeGroupConnection("${CommonUtils.getDevId()}_background")
        if (roomControllerView != null && roomControllerView?.isAttachedToWindow == true) {
            mWindowManager?.removeView(roomControllerView)
        }
    }

    // handle 0 active user count when fragment creates after clicking notification
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun lastAudioRoomState(param: EventBusModel.FetchLastAudioRoomState) {
        sendBroadcast(Intent().apply {
            action = AUDIO_ACTION
            putExtra(AUDIO_ACTION, AudioConnection.UserCountUpdate(agoraConnectionUtils.activeUserCount))
        })
    }

    private fun startCall() {
        agoraConnectionUtils.apply {
            agoraAccessToken = details?.authToken ?: ""
            agoraChannelId = details?.channelId ?: ""
            Log.i(TAG, "agora: $agoraAccessToken and $agoraChannelId")
            startAudioCall(this@AudioRoomService, CommonUtils.getUserID())
        }
    }

    private fun buildNotification() {
        //details ?: return
        /*val notification = this.createNotification(this@AudioRoomService)
        startForeground(NOTIFICATION_ID, notification)*/
        this.createNotification(this@AudioRoomService, null)?.let { startForeground(NOTIFICATION_ID, it) }
        this.loadBitmap(details?.thumbnail ?: AppConstants.DEFAULT_AVATAR) { bitmap ->
            this.createNotification(this@AudioRoomService, bitmap ?: return@loadBitmap)?.let {
                startForeground(NOTIFICATION_ID, it)
            }
        }
    }

    lateinit var notificationBuilder: NotificationCompat.Builder

    private fun createNotification(ctx: Context, bitmap: Bitmap?): Notification? {
        try {
            val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                NotificationCompat.Builder(ctx, AudioChatRoomFragment.NOTIFICATION_CHANNEL)
            else
                NotificationCompat.Builder(ctx)
            val contentIntent = Intent(ctx, AudioChatRoomActivity::class.java)
            details?.let {
                contentIntent.putExtras(bundleOf(
                        AudioChatRoomActivity.ARG_GROUP_DETAILS to details?.audioGroup,
                        AudioChatRoomActivity.ARG_ONLINE_COUNT to details?.onlineMemberCount,
                        AudioChatRoomActivity.ARG_CHAT_ROOM_ID to details?.chatRoomId)
                )
            }
            val array = ByteArray(7)
            Random().nextBytes(array)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_LOW
                val channelId = AudioChatRoomFragment.NOTIFICATION_CHANNEL
                val channelName = ctx.getString(R.string.app_name)
                val mChannel: NotificationChannel = notificationManager.getNotificationChannel(channelId)
                        ?: NotificationChannel(channelId, channelName, importance)
                mChannel.setShowBadge(true)
                mChannel.setSound(null, null)
                mChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                notificationManager.createNotificationChannel(mChannel)
                notificationBuilder?.setChannelId(channelId)
            }

            notificationBuilder?.setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0)
                    .setShowCancelButton(true)
            )
            val pendingIntentVolume = PendingIntent.getBroadcast(ctx, 0,
                    Intent(AUDIO_ROOM_FILTER).setAction(ACTION_SPEAKER), PendingIntent.FLAG_UPDATE_CURRENT)
            val pendingIntentMic = PendingIntent.getBroadcast(ctx, 0,
                    Intent(AUDIO_ROOM_FILTER).setAction(ACTION_MIC), PendingIntent.FLAG_UPDATE_CURRENT)
            val pendingIntentLeaveRoom = PendingIntent.getBroadcast(ctx, 0,
                    Intent(AUDIO_ROOM_FILTER).setAction(ACTION_LEAVE_ROOM), PendingIntent.FLAG_UPDATE_CURRENT)
            notificationBuilder?.setSmallIcon(R.drawable.ic_app_logo_transparent_bg_42)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(true)
                    .setLargeIcon(bitmap
                            ?: BitmapFactory.decodeResource(resources, R.drawable.ic_app_logo_transparent_bg_42))
                    .setContentIntent(PendingIntent.getActivity(this, 0x0101, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setContentTitle("Audio Chat Room")
                    .setContentText(details?.text ?: "Live Chat")
                    .setSubText("Live Chat")
                    .setLargeIcon(bitmap
                            ?: BitmapFactory.decodeResource(getResources(), R.drawable.ic_app_logo_transparent_bg_42))
//                .addAction(if (details?.isVolumeEnabled == true) R.drawable.ic_volume_up_white_24dp else R.drawable.ic_volume_off_white_24dp, "Volume", pendingIntentVolume)
                    .addAction(if (isSelfMuted) R.drawable.avd_mic_off_24 else R.drawable.avd_mic_24, "Mic", pendingIntentMic)
                    .setGroup(String(array, Charset.forName("UTF-8")))
            if (isBackground)
                notificationBuilder?.addAction(R.drawable.avd_exit, "Leave Room", pendingIntentLeaveRoom)
//        notificationManager.notify(AudioChatRoomFragment.NOTIFICATION_ID, notificationBuilder.build())
            val notification = notificationBuilder.build()
            return notification
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        agoraConnectionUtils.endCall()
        leaveRoom()
        unRegisterBroadcastReceiver()
        agoraConnectionUtils.releaseAgoraEngine()
        removeRoomController()
        stopForeground(true)
        connectedRoomId = null
        connectedRoomOwner = null
        audioRoomTimer.stopTimer()
    }

    /**
     * System Defined Broadcast
     */
    private fun registerBroadcastReceiver() {
        try {
            registerReceiver(notificationBroadcastReceiver, IntentFilter(AUDIO_ROOM_FILTER).apply {
                addAction(ACTION_LEAVE_ROOM)
                addAction(ACTION_SPEAKER)
                addAction(ACTION_MIC)
            })

            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }

            registerReceiver(mBluetoothHeadsetReceiver, filter)
            registerReceiver(mHeadsetReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))

            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unRegisterBroadcastReceiver() {
        try {
            unregisterReceiver(notificationBroadcastReceiver)
            unregisterReceiver(mBluetoothHeadsetReceiver)
            unregisterReceiver(mHeadsetReceiver)
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildEventService(): ApiService {
        val interceptor = AppModule.getServiceInterceptor(this)
        val httpLoggingInterceptor = AppModule.httpLoggingInterceptor()
        val cache = AppModule.provideCache(this)
        val client = AppModule.provideOkhttp(interceptor, httpLoggingInterceptor, cache)
        return AppModule.provideApiService(client, Gson())
    }

    private fun joinRoom(isMuted: Boolean) {
        if (isRoomActive) return
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_JOIN_CHANNEL, (analyticsProperties
                ?: hashMapOf()).apply {
            "chatroom_id" to details?.groupId ?: ""
        })
        val body: MutableMap<String, Any> = HashMap()
        body["group_id"] = details?.groupId ?: ""
        body["chatroom_id"] = details?.chatRoomId ?: ""
        body["is_muted"] = isMuted
        EventBus.getDefault().post(EventBusModel.AudioRoomConnected(connectedRoomId))
        Log.i(TAG, "joinRoom: $body")

        apiService.doChatRoomAction(body, "join")
                ?.enqueue(object : Callback<ChatRoomActionResponse> {
                    override fun onResponse(call: Call<ChatRoomActionResponse>, response: Response<ChatRoomActionResponse>) {
                        if (response.isSuccessful) {
                            Log.e("universal", "${response.body()?.toString()}")
                            isRoomActive = true
                            startCall()
                            SegmentTracker.getInstance()
                                    .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_JOIN_CHANNEL_SUCCESS,
                                            analyticsProperties ?: hashMapOf())

                            response.body()?.ownerDetail?.let {
                                sendBroadcast(Intent().apply {
                                    action = AUDIO_ACTION
                                    putExtra(AUDIO_ACTION, AudioConnection.UserJoinRoom(listOf(it)))
                                })
                            }
                        } else {
                            Log.e("universal", "${response.errorBody()?.string()?.toString()}")
                            SegmentTracker.getInstance()
                                    .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_JOIN_CHANNEL_FAILURE,
                                            HashMap(analyticsProperties ?: hashMapOf()).also {
                                                it["error"] = response.errorBody()?.string()
                                            })

                            sendBroadcast(Intent().apply {
                                action = AUDIO_ACTION
                                putExtra(AUDIO_ACTION, AudioConnection.ExitRoom(reason = "Unable to join room. Please try again."))
                            })

                            try {
                                val error = Gson().fromJson(response.errorBody()?.string(), ChatRoomActionResponse::class.java)
                                Log.i(TAG, "Error --> ${error?.error}")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ChatRoomActionResponse>, t: Throwable) {
                        SegmentTracker.getInstance()
                                .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_JOIN_CHANNEL_FAILURE,
                                        HashMap(analyticsProperties ?: hashMapOf()).also {
                                            it["error"] = t.message
                                        })
                        t.printStackTrace()

                        sendBroadcast(Intent().apply {
                            action = AUDIO_ACTION
                            putExtra(AUDIO_ACTION, AudioConnection.ExitRoom(reason = "Unable to join room. Please try again."))
                        })
                    }
                })
    }

    private fun leaveRoom() {
        isRoomActive = false
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_LEAVE_CHANNEL, analyticsProperties
                ?: hashMapOf())
        val body: MutableMap<String, Any> = HashMap()
        body["chatroom_id"] = details?.chatRoomId ?: ""
        EventBus.getDefault().post(EventBusModel.AudioRoomDisconnected(connectedRoomId))

        apiService.doChatRoomAction(body, "leave")?.enqueue(object : Callback<ChatRoomActionResponse> {
            override fun onResponse(call: Call<ChatRoomActionResponse>, response: Response<ChatRoomActionResponse>) {
                if (response.isSuccessful) {
                    Log.e("universal", "${response.body()?.toString()}")
                    SegmentTracker.getInstance()
                            .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_LEAVE_CHANNEL_SUCCESS,
                                    analyticsProperties ?: hashMapOf())
                    sendBroadcast(Intent().apply {
                        action = AUDIO_ACTION
                        putExtra(AUDIO_ACTION, AudioConnection.UserLeaveRoom(
                                AudioChatRoomActivityViewModel.AudioRoomAction.AddUser, AudioChatRoomActivityViewModel.UpdateData(
                                ownerDetail = OwnerDetail(
                                        id = CommonUtils.getUserID(),
                                        username = CommonUtils.getUserName(),
                                        profileImageUrl = CommonUtils.getUserProfilePic(),
                                        intro = ""
                                ))
                        ))
                    })
                } else {
                    Log.e("universal", "${response.errorBody()?.string()?.toString()}")
                    SegmentTracker.getInstance()
                            .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_LEAVE_CHANNEL_FAILURE,
                                    HashMap(analyticsProperties ?: hashMapOf()).also {
                                        it["error"] = response.errorBody()?.string()
                                    })
                    Log.i(TAG, "Error --> ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ChatRoomActionResponse>, t: Throwable) {
                SegmentTracker.getInstance()
                        .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_LEAVE_CHANNEL_FAILURE,
                                HashMap(analyticsProperties ?: hashMapOf()).also {
                                    it["error"] = t.message
                                })
                t.printStackTrace()
            }
        })
    }

    private fun muteUnMuteParticipant(action: String?) {
        val body: MutableMap<String, Any?> = hashMapOf()
        body["group_id"] = details?.groupId
        body["chatroom_id"] = details?.chatRoomId
        body["participant_id"] = CommonUtils.getUserID().toString()
        body["participant_username"] = CommonUtils.getUserName()

        apiService.doChatRoomAction(body, action)
                ?.enqueue(object : Callback<ChatRoomActionResponse> {
                    override fun onResponse(call: Call<ChatRoomActionResponse>, response: Response<ChatRoomActionResponse>) {
                        if (response.isSuccessful)
                            Log.i("universal", "Response -- > ${response.body()?.toString()}")
                        else
                            Log.i("universal", "Error --> ${response.errorBody()?.string()}")
                    }

                    override fun onFailure(call: Call<ChatRoomActionResponse>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
    }

    private val audioRoomTimer: Hourglass = object : Hourglass(Date().time, 1000) {
        override fun onTimerTick(timeRemaining: Long, passedTime: Long) {
            val ttl = passedTime / 1000
            val properties = analyticsProperties
            if (ttl == 0L || ttl == 30L || (ttl > 30 && ttl % (4.5 * 60).toInt() == 0L)) {
                Log.i(javaClass.simpleName, "time --> $ttl")
                properties?.set("time_elapsed", ttl)
                SegmentTracker.getInstance(this@AudioRoomService).trackEvent(SegmentConstants.EVENT_AUDIO_CHATROOM_ACTIVE, properties)
            }
        }

        override fun onTimerFinish() {}
    }

    /*inner class AudioBinder : Binder() {
        // Return this instance of AudioRoomService so clients can call public methods
        fun getService(): AudioRoomService = this@AudioRoomService
    }*/

    companion object {
        var isRunning = false
        var isConnected = false
        var isSelfMuted = false
        var connectedRoomId: String? = null
        var connectedRoomOwner: String? = null
        var isServiceInBackground = false
        var highlightedUser: OwnerDetail? = null
        fun isNonFeaturedRoomRunning(id: String) = isRunning && isConnected && connectedRoomId != id
        fun isFeaturedRoomRunning(id: String) = isRunning && isConnected && connectedRoomId == id
    }
}