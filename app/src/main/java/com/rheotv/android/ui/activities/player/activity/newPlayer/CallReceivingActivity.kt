package com.rheotv.android.ui.activities.player.activity.newPlayer

import android.Manifest
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.ActivityReceivingCallLayoutBinding
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.RequestVideoCallViewModel
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.VideoCallAction
import com.rheotv.android.ui.base.BaseActivity
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.BindingUtils
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.rheotv.android.utils.showToast
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import java.io.IOException
import javax.inject.Inject

class CallReceivingActivity : BaseActivity<ActivityReceivingCallLayoutBinding, RequestVideoCallViewModel>(), HasAndroidInjector, SurfaceHolder.Callback, Camera.PictureCallback {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var mViewModel: RequestVideoCallViewModel

    private var notificationId = 0
    private var channelName: String? = null
    private var isAcceptBtnClick = false

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.activity_receiving_call_layout

    private var surfaceHolder: SurfaceHolder? = null
    private var camera: Camera? = null

    private var surfaceView: SurfaceView? = null

    override fun getViewModel(): RequestVideoCallViewModel = mViewModel.also {
        it.postId = intent.getStringExtra(AppConstants.KEY_POST_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndKeyguardOff()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        /*getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
        channelName = intent.getStringExtra(VideoChatViewActivity.ARG_KEY_CHANNEL_ID)
        notificationId = intent.getIntExtra("notification_id", -1)
        val username = intent.getStringExtra("streamer_user_name")
        val userProfilePic = intent.getStringExtra("streamer_profile_pic")
        viewDataBinding?.usernameMessageTextView?.text = username
        BindingUtils.setImageUrlCircular(viewDataBinding.profileImageView, userProfilePic,96,96)
        /*BindingUtils.setRoundImageUri(viewDataBinding?.profileImageView,
                intent?.getStringExtra("streamer_profile_pic"),
                intent?.getStringExtra("streamer_user_name"))*/
        viewDataBinding?.acceptView?.setOnClickListener {
            isAcceptBtnClick = true
            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_CAMERA_CALLING_ANSWER_CLICKED, hashMapOf<String, Any?>(
                    "streamer_name" to username,
                    "post_id" to intent.getStringExtra("post_id"),
                    "channel_name" to channelName
            ))
            turnScreenOnAndKeyguardOn()
            openVideoCallingActivity()
        }
        viewDataBinding?.denyView?.setOnClickListener {
            mViewModel?.manageVideoCall(null, CommonUtils.getUserID(), VideoCallAction.Deny) { _, _, _, _, _ ->
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COHOST_CAMERA_CALLING_DECLINE_CLICKED, hashMapOf<String, Any?>(
                        "streamer_name" to username,
                        "post_id" to intent.getStringExtra("post_id"),
                        "channel_name" to channelName
                ))
                removeNotification();
                finish()
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            viewDataBinding.cameraView.visibility = View.VISIBLE
            surfaceView = findViewById(R.id.camera_view)
            setupSurfaceHolder()
        } else {
            viewDataBinding.cameraView.visibility = View.GONE
        }

        registerReceiver(screenUnlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))

        registerReceiver(stopCallingReceiver, IntentFilter(VideoCallJobIntentService.Companion.FILTER).also {
            it.addAction(VideoCallJobIntentService.Companion.ACTION_DENY)
        })
        LocalBroadcastManager.getInstance(this).registerReceiver(closeCallingReceiver, IntentFilter(AppConstants.INTENT_FILTER_CLOSE_CALLING_ACTIVITY))
        LocalBroadcastManager.getInstance(this).registerReceiver(deniedCallRequestReceiver, IntentFilter(AppConstants.INTENT_FILTER_DENIED_CALLING_REQUEST))
    }

    private fun removeNotification() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancel(notificationId)
    }

    private fun setViewVisibility(id: Int, visibility: Int) {
        val view = findViewById<View>(id)
        view!!.visibility = visibility
    }

    private fun setupSurfaceHolder() {
        surfaceHolder = surfaceView!!.holder
        surfaceHolder!!.addCallback(this)
    }

    private fun captureImage() {
        if (camera != null) {
            camera!!.takePicture(null, null, this)
        }
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        startCamera()
    }

    private fun startCamera() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
        camera!!.setDisplayOrientation(90)
        try {
            camera!!.setPreviewDisplay(surfaceHolder)

            camera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        resetCamera()
    }

    private fun resetCamera() {
        if (surfaceHolder!!.surface == null) {
            // Return if preview surface does not exist
            return
        }

        // Stop if preview surface is already running.
        camera!!.stopPreview()
        try {
            // Set preview display
            camera!!.setPreviewDisplay(surfaceHolder)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Start the camera preview...
        camera!!.startPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        releaseCamera()
    }

    private fun releaseCamera() {
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }

    private var stopCallingReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            closeCallingInterface()
        }
    }


    private var deniedCallRequestReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            showToast("Call denied by streamer")
            closeCallingInterface()
        }
    }
    private var closeCallingReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            closeCallingInterface()
        }
    }
    private val isScreenLocked: Boolean
        get() {
            val myKM = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            return myKM.inKeyguardRestrictedInputMode()
        }
    private var screenUnlockReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (isAcceptBtnClick) {
                closeCallingInterface()
            }
        }
    }

    fun closeCallingInterface() {
        finish()
    }

    private fun turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            /*getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );*/
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    private fun turnScreenOnAndKeyguardOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    fun openKeygaurd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    private fun openVideoCallingActivity() {
        VideoChatViewActivity.startMe(this, intent?.extras ?: Bundle())
        if (!isScreenLocked) {
            closeCallingInterface()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isActivityRunning = false
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(closeCallingReceiver)
            if (screenUnlockReceiver != null) {
                unregisterReceiver(screenUnlockReceiver)
            }
            if (stopCallingReceiver != null) {
                unregisterReceiver(stopCallingReceiver)
            }
            if (deniedCallRequestReceiver != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(deniedCallRequestReceiver)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VideoCallJobIntentService.stopService()
    }

    companion object {
        var isActivityRunning = true
    }

    override fun onPictureTaken(p0: ByteArray?, p1: Camera?) {
        TODO("Not yet implemented")
    }
}