package com.rheotv.android.utils.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Handler
import android.os.Looper
import android.widget.ImageView

class CustomRequestAnimation(private val mImageView: ImageView) {

    private var mAnimationDuration: Long = 30000
    private val mHandler: Handler = Handler(Looper.getMainLooper())
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
    private val mBlinkAnimationSet: AnimatorSet = AnimatorSet()
    private val mBlinkListener = ShareAnimationListener().also {
        it.animationEndListener = {
            it.animationCount++
            if (it.animationCount < 3) {
                mHandler.post(mBlinkAnimationRunnable)
            }
        }
    }

    init {
        mBlinkAnimationSet.playSequentially(mScaleUpAnimation, mScaleDownAnimation)
        mBlinkAnimationSet.addListener(mBlinkListener)
    }


    private val mBlinkAnimationRunnable = Runnable { mBlinkAnimationSet.start() }

    fun setAnimationDuration(duration: Long) {
        mBlinkAnimationSet.duration = 300
        mAnimationDuration = duration
    }

    fun startAnimation() {
        mHandler.postDelayed(mBlinkAnimationRunnable, mAnimationDuration)
    }

    fun stopAnimation() {
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