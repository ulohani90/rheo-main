package com.rheotv.android.ui.activities.player.activity.newPlayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.rheotv.android.R
import com.rheotv.android.helpers.AlarmService
import com.rheotv.android.ui.activities.splash.SplashActivity
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.BindingUtils
import com.rheotv.android.utils.turnScreenOffAndKeyguardOn
import com.rheotv.android.utils.turnScreenOnAndKeyguardOff

class FullscreenAlarmNotificationActivity : AppCompatActivity() {

    private var mIsActionTaken = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_alarm_notification)
        turnScreenOnAndKeyguardOff()
        findViewById<ImageView?>(R.id.banner_image_view)?.let {
            BindingUtils.setImageUri(it, intent?.getStringExtra(AppConstants.EVENT_IMAGE_URL))
        }
        findViewById<View?>(R.id.close_button)?.setOnClickListener {
            AlarmService.stopService()
            finish()
        }

        findViewById<View?>(R.id.watch_now_button)?.setOnClickListener { _ ->
            AlarmService.stopService()
            startActivity(Intent(this, SplashActivity::class.java).apply {
                intent?.getStringExtra(AppConstants.EVENT_REDIRECT_URL)?.let {
                    putExtra("target_url", it)
                }
            })
            mIsActionTaken = true
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!mIsActionTaken)
            turnScreenOffAndKeyguardOn()
        AlarmService.stopService()
    }
}