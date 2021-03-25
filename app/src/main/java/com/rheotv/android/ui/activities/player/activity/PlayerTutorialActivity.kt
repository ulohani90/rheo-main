package com.rheotv.android.ui.activities.player.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.rheotv.android.R
import com.rheotv.android.databinding.ActivityPlayerTutorialBinding
import com.rheotv.android.utils.SharedPrefsUtils
import com.rheotv.android.utils.ViewUtils

class PlayerTutorialActivity : AppCompatActivity() {

    private lateinit var mViewBinding: ActivityPlayerTutorialBinding

    private val mViewState: MutableLiveData<TutorialPlayerViewState> = MutableLiveData()

    private val landscapeText: ObservableField<String> = ObservableField("")
    private val portraitText: ObservableField<String> = ObservableField("")
    private val portraitVisibility: ObservableField<Boolean> = ObservableField(false)
    private val landscapeVisibility: ObservableField<Boolean> = ObservableField(false)

    private var mClickCount = 0

    private var mVerticalAnimation: Animation? = null
    private var mHorizontalAnimation: Animation? = null
    private var isMomentsScreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player_tutorial)

        mViewBinding.landscapeText = landscapeText
        mViewBinding.portraitText = portraitText
        mViewBinding.landscapeVisibility = landscapeVisibility
        mViewBinding.portraitVisibility = portraitVisibility
        isMomentsScreen = intent.getBooleanExtra("is_moments_screen", false);
        mVerticalAnimation = TranslateAnimation(
                0f,
                0f,
                -ViewUtils.dpToPx(41).toFloat(),
                ViewUtils.dpToPx(37).toFloat()
        ).apply {
            duration = 1100
            repeatCount = -1
            repeatMode = Animation.REVERSE
            interpolator = LinearInterpolator()
        }
        mHorizontalAnimation = TranslateAnimation(
                -ViewUtils.dpToPx(44).toFloat(),
                ViewUtils.dpToPx(34).toFloat(),
                0f, 0f
        ).apply {
            duration = 1100
            repeatCount = -1
            repeatMode = Animation.REVERSE
            interpolator = LinearInterpolator()
        }

        mViewBinding.portraitHand.startAnimation(mVerticalAnimation)

        mViewState.observe(this, Observer {
            it?.let { data ->
                when (data) {
                    is TutorialPlayerViewState.Vertical -> {
                        landscapeVisibility.set(false)
                        portraitVisibility.set(true)
                        landscapeText.set(null)
                        portraitText.set(data.text)
                        mHorizontalAnimation?.cancel()
                        mViewBinding.portraitHand.startAnimation(mVerticalAnimation)
                    }
                    is TutorialPlayerViewState.Horizontal -> {
                        portraitVisibility.set(false)
                        landscapeVisibility.set(true)
                        portraitText.set(null)
                        landscapeText.set(data.text)
                        mVerticalAnimation?.cancel()
                        mViewBinding.landscapeHand.startAnimation(mHorizontalAnimation)
                    }
                }
                return@Observer
            }
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        })

        mViewBinding.root.setOnClickListener {
            if (isMomentsScreen) {
                if (mClickCount == 0 && resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mClickCount++
                    mViewState.value = TutorialPlayerViewState.Horizontal("Swipe right to see full video")
                } else {
                    mViewState.value = null
                }
            } else {
                mViewState.value = null
            }
        }
        mViewState.value = TutorialPlayerViewState.Vertical(if (isMomentsScreen) "Drag up and down to switch moments" else "Drag up and down to switch streams")

        SharedPrefsUtils().setBooleanPreference(this, if (isMomentsScreen) "moments_tutorial_shown" else "player_tutorial_shown", true)
    }

    sealed class TutorialPlayerViewState {
        data class Vertical(val text: String) : TutorialPlayerViewState()
        data class Horizontal(val text: String) : TutorialPlayerViewState()
    }

    companion object {

        fun startTutorial(context: Context, isMomentsScreen: Boolean) =
                context.startActivity(Intent(context, PlayerTutorialActivity::class.java).putExtra("is_moments_screen", isMomentsScreen),
                        ActivityOptionsCompat.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
    }
}
