package com.rheotv.android.ui.activities.player.activity.streamplayer.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.rheotv.android.R
import com.rheotv.android.utils.EventBusModel
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment
import com.rheotv.android.ui.activities.player.activity.StreamPlayerFragment
import com.rheotv.android.ui.activities.player.activity.newPlayer.StreamPlayerFragmentV2
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.PlayerHeadServiceHelper
import com.rheotv.android.utils.showToast
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import javax.inject.Inject

class StreamPlayerActivity : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector
    private val mStreamPlayerContainerFragment = StreamPlayerContainerFragment.Builder().build()
    private val mActionQueue: Queue<Runnable> = LinkedList()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        setUpTransparentToolbar(Resources.getSystem().configuration.orientation)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stream_player)

        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container,
                        mStreamPlayerContainerFragment.also {
                            it.arguments = intent?.extras
                        },
                        StreamPlayerContainerFragment.TAG)
                .commitNow()
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        while (mActionQueue.isNotEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed(mActionQueue.poll(), 3000)
        }
    }

    override fun onStop() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        super.onStop()
    }

    private var isBackPressed = false

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageReceived(eventBusModel: EventBusModel.StartStreamService) {
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PlayerHeadServiceHelper.CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            mActionQueue.add(Runnable {
                mStreamPlayerContainerFragment?.playerFragment?.also {
                    PlayerHeadServiceHelper.getInstance().checkPermission(it, this, false)
                }
            })
        }
    }

    override fun onBackPressed() {
        if (!isBackPressed && !PlayerHeadServiceHelper.getInstance().isServiceRunning) {
            mStreamPlayerContainerFragment.playerFragment?.let {
                if (Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    return
                }

                PlayerHeadServiceHelper.getInstance().checkAndStartService(it, this,
                        mStreamPlayerContainerFragment?.baseProperties, false)
                return
            }
        }
        super.onBackPressed()
    }

    fun startAudioService() {
        if (CommonUtils.getStreamQuality().equals("audio", ignoreCase = true)) {
            (mStreamPlayerContainerFragment as? StreamPlayerContainerFragment)?.playerFragment?.playerHolder?.let {
                if (!PlayerHeadServiceHelper.getInstance().startAudioService(it))
                    checkAndShowAudioModeToast()
            }
        } else {
            (mStreamPlayerContainerFragment as? StreamPlayerContainerFragment)?.playerFragment?.let {
                PlayerHeadServiceHelper.getInstance().checkAndRunVideoWidgetService(
                        it, this, (mStreamPlayerContainerFragment as? StreamPlayerContainerFragment)?.baseProperties, false
                )
            }
        }
    }

    private fun checkAndShowAudioModeToast() {
        val count = CommonUtils.getAudioToastCount()
        if (count < 3) {
            this.showToast("Rheo switched to Audio mode.\nTo change audio settings go to Profile -> Settings")
            CommonUtils.setAudioToastCount(count + 1)
        }
    }

    fun stopAudioService() {
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setUpTransparentToolbar(newConfig.orientation)
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

    val REQUEST_CAMERA_PERMISSION = 1;

    fun checkPermissionForCamera(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults != null && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                broadcastCameraPermissionReceivedAction();
            }
        }
    }

    private fun broadcastCameraPermissionReceivedAction() {
        val intent = Intent("ACTION_CAMERA_PERMISSION")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    companion object {
        fun startActivity(context: Context, bundle: Bundle) =
                context.startActivity(Intent(context, StreamPlayerActivity::class.java)
                        .also {
                            it.putExtras(bundle)
                        })

        fun startActivity(context: Context, bundle: Bundle, flags: List<Int>) =
                context.startActivity(Intent(context, StreamPlayerActivity::class.java)
                        .also {
                            flags.forEach { flag -> it.addFlags(flag) }
                            it.putExtras(bundle)
                        })
    }
}