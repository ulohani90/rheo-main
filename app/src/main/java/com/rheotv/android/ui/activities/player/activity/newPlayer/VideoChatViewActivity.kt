package com.rheotv.android.ui.activities.player.activity.newPlayer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.ActivityVideoChatViewBinding
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.RequestVideoCallViewModel
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.VideoCallAction
import com.rheotv.android.ui.base.BaseActivity
import com.rheotv.android.utils.*
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.agora.rtc.RtcEngine
import javax.inject.Inject

class VideoChatViewActivity : BaseActivity<ActivityVideoChatViewBinding, RequestVideoCallViewModel>(), HasAndroidInjector {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var mViewModel: RequestVideoCallViewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.activity_video_chat_view

    override fun getViewModel(): RequestVideoCallViewModel = mViewModel.also {
        it.postId = intent.getStringExtra(AppConstants.KEY_POST_ID)
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    private var mCallEnd = false
    private var mMuted = false
    private var connectionUtils: AgoraConnectionUtils? = null
    private var channelName: String? = null

    private var mAccessToken = ""
    private var userIcon: String? = null
    private var streamerUserName: String? = null
    private var postId: String? = null

    private var connectionStartTime: Long = System.currentTimeMillis()
    private var connectionCompletionTime: Long = 0;
    private var didCallConnect: Boolean = false
    private var callStartTime: Long = 0;
    private var callEndTime: Long = 0;

    private fun setupRemoteVideo(uid: Int) {
        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
        connectionCompletionTime = System.currentTimeMillis()
        var connectionTime = (connectionCompletionTime - connectionStartTime) / 1000
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_VIEWER_CALL_CONNECTED, hashMapOf<String, Any?>(
                "streamer_name" to streamerUserName,
                "post_id" to postId,
                "channel_id" to channelName,
                "connection_time" to connectionTime
        ))
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
        viewDataBinding?.remoteVideoViewContainer?.addView(connectionUtils?.getRemoteView(baseContext, uid))
    }

    private fun onRemoteUserLeft() {
        callEndTime = System.currentTimeMillis()
        var duration = (callEndTime - callStartTime) / 1000
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_VIEWER_END_CALL_RECEIVED, hashMapOf<String, Any?>(
                "post_id" to postId,
                "streamer_name" to streamerUserName,
                "channel_id" to channelName,
                "did_call_connect" to didCallConnect,
                "duration" to duration
        ))
        removeRemoteVideo()
        endCall()
    }

    private fun removeRemoteVideo() {
        if (connectionUtils != null) {
            viewDataBinding?.remoteVideoViewContainer?.removeView(connectionUtils?.getRemoteView(baseContext, -1))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        VideoCallJobIntentService.stopService()

        channelName = intent.getStringExtra(ARG_KEY_CHANNEL_ID)
        streamerUserName = intent.getStringExtra("streamer_user_name");
        postId = intent.getStringExtra("post_id")
//        channelName = "testingCalling"
        mAccessToken = intent?.getStringExtra("requester_agora_token") ?: ""
        if (intent?.getBooleanExtra("is_from_notification", false) == true) {
            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_NOTIFICATION_ANSWER_CLICKED, hashMapOf<String, Any?>(
                    "streamer_name" to streamerUserName,
                    "post_id" to postId,
                    "channel_name" to channelName
            ))
        }
        userIcon = intent?.getStringExtra("user_icon")
        userIcon?.let { BindingUtils.setImageUrlCircular(viewDataBinding.userIcon, it, 80, 80) }
        initUI()
        mViewModel?.manageVideoCall(channelName, CommonUtils.getUserID(), VideoCallAction.Accept) { error, isSuccessful, _, _, _ ->
            if (!error.isNullOrEmpty()) {
                return@manageVideoCall
            }
            if (isSuccessful) {
                //showLongToast("accept Api completed")
            }
        }
        // Ask for permissions at runtime.
        // This is just an example set of permissions. Other permissions
        // may be needed, and please refer to our online documents.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel()
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent().setAction(AppConstants.INTENT_FILTER_CLOSE_CALLING_ACTIVITY))
    }

    private fun initUI() {
        // Sample logs are optional.
        showSampleLogs()
    }

    private fun showSampleLogs() {
        Log.i(VideoChatViewActivity::class.java.canonicalName, "Welcome to Agora 1v1 video call")
        Log.i(VideoChatViewActivity::class.java.canonicalName, "You will see custom logs here")
        Log.i(VideoChatViewActivity::class.java.canonicalName, "You can also use this to show errors")
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE)
                finish()
                return
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            initEngineAndJoinChannel()
        }
    }

    private fun showLongToast(msg: String) {
        runOnUiThread { Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show() }
    }

    private fun initEngineAndJoinChannel() {
        // This is our usual steps for joining
        // a channel and starting a call.
        connectionUtils = AgoraConnectionUtils()
        channelName?.let {
            connectionUtils?.initEngineForVideoCall(this)
            connectionUtils?.onVideoCallJoined = { uid, _, _, _ ->

                runOnUiThread {
                    callStartTime = System.currentTimeMillis()
                    didCallConnect = true
                    setupRemoteVideo(uid)
                }
            }
            connectionUtils?.onCallLeft = {
                runOnUiThread { onRemoteUserLeft() }
            }
            setupLocalVideo()
        }
    }

    private fun setupLocalVideo() {
        viewDataBinding?.localVideoViewContainer?.addView(connectionUtils?.getLocalView(baseContext))
        channelName?.let { name ->
            connectionUtils?.apply {
                agoraAccessToken = mAccessToken
                agoraChannelId = name
                startCall(this@VideoChatViewActivity, CommonUtils.getUserID())
            }
            isOngoingCall = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!mCallEnd) {
            connectionUtils?.endVideoCall()
        }
        /*
          Destroys the RtcEngine instance and releases all resources used by the Agora SDK.

          This method is useful for apps that occasionally make voice or video calls,
          to free up resources for other operations when not making calls.
         */
        RtcEngine.destroy()
        isOngoingCall = false
    }

    fun onLocalAudioMuteClicked(view: View?) {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_VIEWER_MUTE_CLICKED, hashMapOf<String, Any?>(
                "streamer_name" to streamerUserName,
                "post_id" to postId,
                "channel_id" to channelName
        ))
        mMuted = !mMuted
        // Stops/Resumes sending the local audio stream.
        connectionUtils?.muteAudio(mMuted)
        val res = if (mMuted) R.drawable.btn_mute else R.drawable.btn_unmute
        viewDataBinding?.btnMute?.setImageResource(res)
    }

    fun onSwitchCameraClicked(view: View?) {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_VIEWER_SWITCH_CAMERA_CLICKED, hashMapOf<String, Any?>(
                "streamer_name" to streamerUserName,
                "post_id" to postId,
                "channel_id" to channelName
        ))
        connectionUtils?.switchCamera()
    }

    fun onCallClicked(view: View?) {
        if (mCallEnd) {
            startCall()
            mCallEnd = false
            showButtons(!mCallEnd)
        } else {
            callEndTime = System.currentTimeMillis()
            var duration = (callEndTime - callStartTime) / 1000
            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_VIEWER_END_CALL_CLICKED, hashMapOf<String, Any?>(
                    "post_id" to postId,
                    "streamer_name" to streamerUserName,
                    "channel_id" to channelName,
                    "did_call_connect" to didCallConnect,
                    "duration" to duration
            ))
            mViewModel?.manageVideoCall(null, CommonUtils.getUserID(), VideoCallAction.End) { _, _, _, _, _ ->
                mCallEnd = true
                endCall()
            }
        }
    }

    private fun startCall() {
        setupLocalVideo()
    }

    override fun onBackPressed() {
        showToast("End call to close this screen ")
        return
    }

    private fun endCall() {
        removeLocalVideo()
        removeRemoteVideo()
        connectionUtils?.endVideoCall()
        showToast("Call has ended!")
        finish()
    }

    private fun removeLocalVideo() {
        if (connectionUtils != null) {
            viewDataBinding?.localVideoViewContainer?.removeView(connectionUtils?.getLocalView(baseContext))
        }
    }

    private fun showButtons(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        viewDataBinding?.btnMute?.visibility = visibility
        viewDataBinding?.btnSwitchCamera?.visibility = visibility
    }

    companion object {
        private val TAG = VideoChatViewActivity::class.java.simpleName
        private const val PERMISSION_REQ_ID = 22
        const val ARG_KEY_CHANNEL_ID = "channel_id"
        var isOngoingCall = false

        // Permission WRITE_EXTERNAL_STORAGE is not mandatory
        // for Agora RTC SDK, just in case if you wanna save
        // logs to external sdcard.
        private val REQUESTED_PERMISSIONS = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        fun startMe(context: Context, bundle: Bundle) = context.startActivity(Intent(context, VideoChatViewActivity::class.java).apply {
            putExtras(bundle)
        })
    }
}