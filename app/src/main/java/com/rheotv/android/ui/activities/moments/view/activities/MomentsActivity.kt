package com.rheotv.android.ui.activities.moments.view.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.rheotv.android.R
import com.rheotv.android.ui.activities.moments.view.fragments.MomentsContainerFragment
import com.rheotv.android.ui.activities.moments.view.fragments.MomentsSlidingPagerFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class MomentsActivity : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private val mContainerFragment: MomentsContainerFragment = MomentsContainerFragment.Companion.Builder().build()

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        setUpTransparentToolbar(Resources.getSystem().configuration.orientation)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stream_player)
        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container,
                        MomentsSlidingPagerFragment().also {
                            it.arguments = intent?.extras
                        },
                        MomentsSlidingPagerFragment.TAG)
                .commitNow()
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

    companion object {
        fun startMe(context: Context?) = context?.startActivity(Intent(context, MomentsActivity::class.java))
    }
}