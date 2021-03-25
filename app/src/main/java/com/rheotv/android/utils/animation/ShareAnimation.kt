package com.rheotv.android.utils.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.rheotv.android.R
import com.rheotv.android.utils.AppUtilsKt
import kotlin.random.Random

class ShareAnimation(private val mImageView: ImageView) {

    private val mBlinkDuration = 500L
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private val mFlipAnimationSet: AnimatorSet = AnimatorSet()
    private val mBlinkAnimationSet: AnimatorSet = AnimatorSet()
    private val mForwardFlip: ObjectAnimator = ObjectAnimator
            .ofFloat(mImageView, "rotationY", 0f, 90f)
    private val mReverseFlip: ObjectAnimator = ObjectAnimator
            .ofFloat(mImageView, "rotationY", 90f, 0f)
    private val mScaleUpAnimation: ObjectAnimator = ObjectAnimator
            .ofPropertyValuesHolder(mImageView,
                    PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.1f))
            .also { it.duration = 100 }
    private val mScaleDownAnimation: ObjectAnimator = ObjectAnimator
            .ofPropertyValuesHolder(mImageView,
                    PropertyValuesHolder.ofFloat("scaleX", 1.0f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.0f))
            .also { it.duration = 100 }
    private val mBaseImageResource = R.drawable.ic_share_white_24dp
    private var mRandomImageResource: Int = -1
    private var mSelectedImageResource: Int = mBaseImageResource
    private var mAnimationDuration: Long = 30000

    private val mBlinkListener = ShareAnimationListener().also {
        it.animationEndListener = {
            it.animationCount++
            if (it.animationCount < 3) {
                mHandler.postDelayed(mBlinkAnimationRunnable,
                        if (it.animationCount % 3L == 0L) mBlinkDuration * 2 else mBlinkDuration)
            } else {
                it.animationCount = 0
            }
        }
    }

    private val mFlipAnimationRunnable = Runnable { mFlipAnimationSet.start() }

    private val mBlinkAnimationRunnable = Runnable { mBlinkAnimationSet.start() }

    private val mFlipListener = ShareAnimationListener().also { listener ->
        listener.animationEndListener = {
            listener.animationCount++
            mHandler.post(mBlinkAnimationRunnable)
            mHandler.postDelayed(mFlipAnimationRunnable, listener.duration)
        }
        mForwardFlip.addListener(ShareAnimationListener().also {
            it.animationEndListener = {
                mReverseFlip.start()
                mHandler.post {
                    mSelectedImageResource = if (listener.animationCount % 2L == 0L) mRandomImageResource else mBaseImageResource
                    mImageView.setImageResource(mSelectedImageResource)
                }
            }
        })
    }

    init {
        mFlipAnimationSet.playSequentially(mForwardFlip, mReverseFlip)
        mBlinkAnimationSet.playSequentially(mScaleUpAnimation, mScaleDownAnimation)
        mBlinkAnimationSet.addListener(mBlinkListener)
    }

    fun getSelectedImageResource() = mSelectedImageResource
    fun setupDrawableList(context: Context?) {
        if (context == null) return
        val list = AppUtilsKt.getInstalledAppPackages(context, Intent(Intent.ACTION_SEND).also { it.type = "video/*" })
        mRandomImageResource = if (list.isEmpty()) {
            mBaseImageResource
        } else {
            val randomIndex = Random.nextInt(list.size)
            when {
                list[randomIndex].activityInfo.packageName.contains("whatsapp") -> R.drawable.ic_whatsapp
                list[randomIndex].activityInfo.packageName.contains("instagram") -> R.drawable.avd_instagram
                list[randomIndex].activityInfo.packageName.contains("facebook") -> R.drawable.ic_facebook
                else -> {
                    var drawable = R.drawable.ic_share_white_24dp
                    for (item in list) {
                        if (item.activityInfo.packageName.contains("whatsappp")) {
                            drawable = R.drawable.ic_whatsapp
                            break
                        } else if (item.activityInfo.packageName.contains("instagram")) {
                            drawable = R.drawable.avd_instagram
                            break
                        } else if (item.activityInfo.packageName.contains("facebook")) {
                            drawable = R.drawable.ic_facebook
                            break
                        }
                    }
                    drawable
                }
            }
        }
    }

    fun setAnimationDuration(duration: Long) {
        mAnimationDuration = duration
        mFlipAnimationSet.duration = 300
        mSelectedImageResource = mBaseImageResource
        mImageView.setImageResource(mSelectedImageResource)

        if (mFlipAnimationSet.listeners == null) {
            mFlipAnimationSet.addListener(mFlipListener)
        }
        mFlipListener.duration = duration
    }

    fun startAnimation() {
        mHandler.postDelayed(mFlipAnimationRunnable, mAnimationDuration)
    }

    fun stopAnimation() {
        mHandler.removeCallbacks(mFlipAnimationRunnable)
        mHandler.removeCallbacks(mBlinkAnimationRunnable)
    }

    inner class ShareAnimationListener(var animationEndListener: (() -> Unit)? = null) : Animator.AnimatorListener {
        var animationCount: Long = 0
        var duration: Long = 0
        override fun onAnimationRepeat(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            animationEndListener?.invoke()
            if (animationCount > (Long.MAX_VALUE / 2) && animationCount % 4L == 0L && animationCount % 2L == 0L)
                animationCount = 0
        }

        override fun onAnimationCancel(animation: Animator?) = Unit
        override fun onAnimationStart(animation: Animator?) = Unit
    }
}