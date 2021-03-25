package com.rheotv.android.ui.activities.player.activity.newPlayer.activities

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat
import com.rheotv.android.databinding.ActivityFullScreenVideoBinding
import com.rheotv.android.helpers.grpc.ChatHelper
import com.rheotv.android.ui.activities.player.activity.ChatHelperCallbacks
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.RequestVideoCallViewModel
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.VideoCallAction
import com.rheotv.android.ui.adapters.ChatListAdapter
import com.rheotv.android.ui.base.BaseActivity
import com.rheotv.android.utils.*
import com.rheotv.android.utils.segmentTracker.EqualSpaceItemDecorator
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import goChat.Services
import kotlinx.android.synthetic.main.activity_full_screen_video.*
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.CancellationException
import javax.inject.Inject
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView

class FullScreenVideoActivity : BaseActivity<ActivityFullScreenVideoBinding, RequestVideoCallViewModel>(),
        HasAndroidInjector, ChatListAdapter.ChatItemClickListenerV2 {

    @Inject
    lateinit var mFragmentInject: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var mViewModel: RequestVideoCallViewModel

    @Inject
    lateinit var mAgoraConnectionUtils: AgoraConnectionUtils

    override fun androidInjector(): AndroidInjector<Any> = mFragmentInject

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.activity_full_screen_video

    override fun getViewModel(): RequestVideoCallViewModel = mViewModel

    private var mChannelName: String? = null
    private var mAccessToken = ""
    private var mCallEnd = false
    private var mMuted = false
    private var userId: Int = -1
    private var userIcon: String? = null
    private var userName: String? = null
    private var didCallConnect: Boolean? = false
    private var callStartTime: Long = 0;

    private var callEndTime: Long = 0;
    private var connectionSetupStartTime: Long = System.currentTimeMillis()
    private var connectionSetupCompleteTime: Long = 0;

    private var chatAdapter: ChatListAdapter? = null

    private var chatHelper: ChatHelper? = null

    private val messageHandler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        setUpTransparentToolbar(Resources.getSystem().configuration.orientation)
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mChannelName = intent.getStringExtra(ARG_KEY_CHANNEL_ID)
        mAccessToken = intent?.getStringExtra(ARG_KEY_AGORA_ACCESS_TOKEN) ?: ""
        mViewModel.postId = intent?.getStringExtra(ARG_KEY_POST_ID)
        userId = intent?.getIntExtra("user_id", -1) ?: -1
        userIcon = intent?.getStringExtra("user_icon")
        userIcon?.let { BindingUtils.setImageUrlCircular(viewDataBinding.userIcon, it, 48, 48) }
        mAgoraConnectionUtils.initEngineForVideoCall(this)
        userName = intent?.getStringExtra("user_name");
        userName?.let {
            viewDataBinding.username.visibility = View.VISIBLE
            viewDataBinding.username.text = "Connecting $it..."
        }
        viewDataBinding?.root?.setOnClickListener {
            toggleButtonVisibility()
        }

        viewDataBinding?.chatButton?.setOnClickListener(View.OnClickListener {
            mViewModel.unreadChatCount.set(0)
            if (mViewModel.chatListShown.get()!!) {
                mViewModel.chatListShown.set(false)
                hideChatRoom()
            } else {
                mViewModel.chatListShown.set(true)
                addChatRoomAndChanegVisibility()
            }
        })
        viewDataBinding.unreadButton?.setOnClickListener(View.OnClickListener {
            recyclerView.smoothScrollToPosition(0)
            viewDataBinding.unreadButton?.visibility = View.GONE
        })
        if (chatAdapter == null) {
            viewDataBinding?.recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
            if (chatAdapter == null) chatAdapter = ChatListAdapter(ArrayList(), Resources.getSystem().configuration.orientation, false, true)
            chatAdapter?.setChatStickerSize(stickerSize())
            chatAdapter?.setListener(this)
            viewDataBinding?.recyclerView?.addItemDecoration(EqualSpaceItemDecorator(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()))
            viewDataBinding?.recyclerView?.setAdapter(chatAdapter)
        }


        mViewModel.comments.observe(this, Observer {
            chatAdapter?.addItems(it)
        })
        mViewModel.loadComments()

        /*mViewModel?.manageVideoCall(mChannelName, userId, VideoCallAction.Accept) { error, isSuccessful, channelId, token ->
            if (!error.isNullOrEmpty()) {
                return@manageVideoCall
            }
            if (isSuccessful) {
                showToast("accept Api completed")
            }
        }*/
        var layoutManager = viewDataBinding.recyclerView?.layoutManager
        viewDataBinding.recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = (layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                        ?: 0
                if (mViewModel?.isLoadingComments?.get() == false && mViewModel.commentNextUrl != null && totalItemCount >= 10 && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    mViewModel.loadComments()
                }
            }
        })
        // Ask for permissions at runtime.
        // This is just an example set of permissions. Other permissions
        // may be needed, and please refer to our online documents.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel()
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(deniedCallRequestReceiver, IntentFilter(AppConstants.INTENT_FILTER_DENIED_CALLING_REQUEST))
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent().setAction(AppConstants.INTENT_FILTER_CLOSE_CALLING_ACTIVITY))

    }

    private fun scrollToBottom() {

    }

    private fun addChatRoomAndChanegVisibility() {
        viewDataBinding.recyclerView?.visibility = View.VISIBLE
        viewDataBinding.chatButton?.setImageResource(R.drawable.avd_close);
        viewDataBinding.chatBackground?.visibility = View.VISIBLE
        viewDataBinding.recyclerView?.smoothScrollToPosition(0)
    }

    private fun hideChatRoom() {
        viewDataBinding.recyclerView?.visibility = View.GONE
        viewDataBinding.chatButton?.setImageResource(R.drawable.avd_chat);
        viewDataBinding.chatBackground?.visibility = View.GONE
    }

    private fun stickerSize(): Int {
        return if (Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_PORTRAIT) (Resources.getSystem().displayMetrics.widthPixels - (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics)).toInt()) / 3 else (Resources.getSystem().displayMetrics.widthPixels / 2 - (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics)).toInt()) / 3
    }


    val deniedCallRequestReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            showToast("Call denied by co-host")
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        joinChatRoom()
    }

    private fun joinChatRoom() {
        if (chatHelper == null)
            chatHelper = ChatHelper.getInstance()
        chatHelper?.setPostChatJoinTask(WeakReference<ChatHelperCallbacks>(chatHelperCallback), mViewModel.postId, true, mViewModel.authorName);
    }

    /**
     * Chat connect and disconnect handling
     */
    var chatHelperCallback = object : ChatHelperCallbacks {

        override fun onMessageDelete(chatMessage: Services.ChatMessage?) {

        }

        override fun waitAndReconnect() {
            messageHandler?.post {
                if (chatHelper != null && chatHelper?.getChatState() == ChatHelper.CHAT_STATE_DISCONNECTED) {
                    joinChatRoom()
                }
            }
        }

        override fun setUpViewersRequest() {

        }

        override fun showToast(message: String?) {

        }

        override fun onConnectionComplete() {

        }

        override fun updateLiveCount(liveCount: String?) {

        }

        override fun onMessageSend(chatMessage: Services.ChatMessage) {
            if (messageHandler != null) messageHandler.post(object : Runnable {
                override fun run() {
                    if (chatMessage.sender != null && !chatMessage.sender.isEmpty()) {
                        Log.i(javaClass.name, "chat_update " + chatMessage.msgType + " and " + chatMessage.message)
                        // todo
                        if (chatMessage.msgType.equals(AppConstants.MSG_SCORE, ignoreCase = true)
                                || chatMessage.msgType.equals(AppConstants.MSG_PIN, ignoreCase = true)
                                || chatMessage.msgType.equals(AppConstants.MSG_DYNAMIC_PLAYER_ACTIONS, ignoreCase = true)
                                || chatMessage.msgType.equals(AppConstants.MSG_TYPE_DELETED, ignoreCase = true)
                                || chatMessage.msgType.equals(AppConstants.MSG_TYPE_BLOCKED, ignoreCase = true)
                                || chatMessage.message.equals(AppConstants.MSG_HEART, ignoreCase = true)) {
                            //updateScore(chatMessage)
                            return
                        }
                        publishChat(CommentChat.getComment(chatMessage))
                    }
                }
            })
        }
    }

    private fun publishChat(comment: CommentChat?) {
        chatAdapter?.addItem(comment)
        checkChatRVState()
    }

    private fun checkChatRVState() {
        if (mViewModel.chatListShown.get()!!) {
            if ((viewDataBinding?.recyclerView?.getLayoutManager() as? LinearLayoutManager)?.findFirstVisibleItemPosition() != 0) {
                mViewModel.unreadChatCount.set(mViewModel.unreadChatCount.get()?.plus(1))
            } else {
                viewDataBinding?.recyclerView?.scrollToPosition(0)
            }
        } else {
            mViewModel.unreadChatCount.set(mViewModel.unreadChatCount.get()?.plus(1))
            viewDataBinding.chatBtnUnreadCount?.visibility = View.VISIBLE
            if (mViewModel.unreadChatCount?.get()!! > 99) {
                viewDataBinding.chatBtnUnreadCount?.text = "99+"
            } else {
                viewDataBinding.chatBtnUnreadCount?.text = "${viewModel.unreadChatCount.get()}"
            }
        }
    }


    override fun onResume() {
        super.onResume()

    }

    override fun onStop() {
        super.onStop()
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(deniedCallRequestReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        /* try {
             unregisterReceiver(deniedCallRequestReceiver)
         } catch (e: Exception) {
             e.printStackTrace()
         }*/
        mAgoraConnectionUtils.endVideoCall()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE)
                finish()
                return
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            initEngineAndJoinChannel()
        }
    }

    private var mDelayJob: Job? = null

    private fun toggleButtonVisibility() {
        val slide = Slide()
        slide.addTarget(R.id.btn_call)
        val nextTransition = Slide()
        slide.duration = 500
        val set = TransitionSet()
        nextTransition.addTarget(R.id.btn_mute)
        nextTransition.addTarget(R.id.btn_switch_camera)
        nextTransition.duration = 500
        nextTransition.startDelay = 100
        set.addTransition(slide)
        set.addTransition(nextTransition)
        viewDataBinding?.actionButtonGroup?.visibility =
                if (viewDataBinding?.actionButtonGroup?.visibility == View.VISIBLE) {
                    (viewDataBinding?.root as? ViewGroup)?.let {
                        TransitionManager.beginDelayedTransition(it, set)
                    }
                    View.GONE
                } else {
                    (viewDataBinding?.root as? ViewGroup)?.let { TransitionManager.beginDelayedTransition(it, set) }
                    mDelayJob?.cancel(CancellationException("job scheduled again"))
                    mDelayJob = lifecycleScope.launch(Dispatchers.IO) {
                        delay(2 * 1000)
                        withContext(Dispatchers.Main) {
                            (viewDataBinding?.root as? ViewGroup)?.let {
                                if (viewDataBinding?.actionButtonGroup?.visibility == View.VISIBLE) {
                                    TransitionManager.beginDelayedTransition(it, set)
                                    viewDataBinding?.actionButtonGroup?.visibility = View.GONE
                                }
                            }
                        }
                    }
                    View.VISIBLE
                }
    }

    private fun setUpTransparentToolbar(orientation: Int) {
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            navigationBarColor = Color.TRANSPARENT
            statusBarColor = Color.TRANSPARENT

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            } else {
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode)
            return false
        }
        return true
    }

    private fun setupRemoteVideo(uid: Int) {
        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
        connectionSetupCompleteTime = System.currentTimeMillis()
        var connectionTime = (connectionSetupCompleteTime - connectionSetupStartTime) / 1000
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_STREAMER_CALL_CONNECTED, hashMapOf<String, Any?>(
                "post_id" to mViewModel.postId,
                "user_name" to userName,
                "user_id" to userId,
                "connection_time" to connectionTime
        ))
        viewDataBinding.userIcon.visibility = View.GONE
        viewDataBinding.username.visibility = View.GONE
        val count = viewDataBinding?.remoteVideoViewContainer?.childCount ?: 0
        var view: View? = null
        for (i in 0 until count) {
            val v = viewDataBinding?.remoteVideoViewContainer?.getChildAt(i)
            if (v?.tag is Int && v.tag as Int == uid) {
                view = v
            }
        }
        if (view != null) {
            return
        }
        val surfaceView = mAgoraConnectionUtils.getRemoteView(baseContext, uid);
        val parent = surfaceView.parent as? ViewGroup
        if (parent != null) {
            parent.removeView(surfaceView)
        }
        viewDataBinding?.remoteVideoViewContainer?.addView(mAgoraConnectionUtils.getRemoteView(baseContext, uid))
    }

    private fun onRemoteUserLeft() {
        callEndTime = System.currentTimeMillis()
        val duration = if (didCallConnect == true) (callEndTime - callStartTime) / 1000 else 0
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_STREAMER_END_CALL_RECEIVED, hashMapOf<String, Any?>(
                "post_id" to mViewModel.postId,
                "user_name" to userName,
                "user_id" to userId,
                "did_call_connect" to didCallConnect,
                "duration" to duration
        ))
        removeRemoteVideo()
        endCall()
    }

    private fun removeRemoteVideo() {
        viewDataBinding?.remoteVideoViewContainer?.removeView(mAgoraConnectionUtils.getRemoteView(baseContext, -1))
    }


    private fun initEngineAndJoinChannel() {
        // This is our usual steps for joining
        // a channel and starting a call.
        mChannelName?.let {
            mAgoraConnectionUtils.initEngineForVideoCall(this)
            mAgoraConnectionUtils.onVideoCallJoined = { uid, _, _, _ ->
                runOnUiThread {
                    callStartTime = System.currentTimeMillis()
                    didCallConnect = true
                    setupRemoteVideo(uid)
                }
            }
            mAgoraConnectionUtils.onCallLeft = {
                runOnUiThread { onRemoteUserLeft() }
            }
            setupLocalVideo()
        }
    }

    private fun setupLocalVideo() {
        viewDataBinding?.localVideoViewContainer?.addView(mAgoraConnectionUtils.getLocalView(baseContext))
        mChannelName?.let {
            mAgoraConnectionUtils.apply {
                agoraAccessToken = mAccessToken
                agoraChannelId = it
                startCall(this@FullScreenVideoActivity, CommonUtils.getUserID())
            }
            isOngoingCall = true
        }
    }

    fun onLocalAudioMuteClicked(view: View?) {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_STREAMER_MUTE_CLICKED, hashMapOf<String, Any?>(
                "post_id" to mViewModel.postId,
                "user_name" to userName,
                "user_id" to userId,
                "did_call_connect" to didCallConnect
        ))
        mMuted = !mMuted
        // Stops/Resumes sending the local audio stream.
        mAgoraConnectionUtils.muteAudio(mMuted)
        val res = if (mMuted) R.drawable.btn_mute else R.drawable.btn_unmute
        viewDataBinding?.btnMute?.setImageResource(res)
    }

    fun onSwitchCameraClicked(view: View?) {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_STREAMER_SWITCH_CAMERA_CLICKED, hashMapOf<String, Any?>(
                "post_id" to mViewModel.postId,
                "user_name" to userName,
                "user_id" to userId,
                "did_call_connect" to didCallConnect
        ))
        mAgoraConnectionUtils.switchCamera()
    }

    fun onCallClicked(view: View?) {
        if (mCallEnd) {
            startCall()
            mCallEnd = false
            showButtons(!mCallEnd)
        } else {
            callEndTime = System.currentTimeMillis()
            val duration = if (didCallConnect == true) (callEndTime - callStartTime) / 1000 else 0
            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_STREAMER_END_CALL_CLICKED, hashMapOf<String, Any?>(
                    "post_id" to mViewModel.postId,
                    "user_name" to userName,
                    "user_id" to userId,
                    "did_call_connect" to didCallConnect,
                    "duration" to duration
            ))
            mViewModel?.manageVideoCall(null, userId, VideoCallAction.End) { _, _, _, _, _ ->
                mCallEnd = true
                endCall()
            }
        }
    }

    private fun startCall() {
        setupLocalVideo()
    }

    private fun endCall() {
        removeLocalVideo()
        removeRemoteVideo()
        mAgoraConnectionUtils.endVideoCall()
        showToast("Call has ended!")
        finish()
    }

    private fun removeLocalVideo() {
        viewDataBinding?.localVideoViewContainer?.removeView(mAgoraConnectionUtils.getLocalView(baseContext))
    }

    private fun showButtons(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        viewDataBinding?.btnMute?.visibility = visibility
        viewDataBinding?.btnSwitchCamera?.visibility = visibility
    }

    override fun onBackPressed() {
        showToast("End call to close this screen ")
        return
    }

    companion object {
        private const val PERMISSION_REQ_ID = 22
        const val ARG_KEY_CHANNEL_ID = "channel_id"
        const val ARG_KEY_AGORA_ACCESS_TOKEN = "requester_agora_token"
        private const val ARG_KEY_POST_ID = "post_id"

        var isOngoingCall = false


        // Permission WRITE_EXTERNAL_STORAGE is not mandatory
        // for Agora RTC SDK, just in case if you wanna save
        // logs to external sdcard.
        private val REQUESTED_PERMISSIONS = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        fun startMe(context: Context, bundle: Bundle) = context.startActivity(Intent(context, FullScreenVideoActivity::class.java).apply {
            putExtras(bundle)
        })

    }

    override fun onUserClicked(commentChat: CommentChat?) {

    }

    override fun onMediaClicked(commentChat: CommentChat?) {

    }

    override fun onCommentClicked(commentChat: CommentChat?) {

    }

}