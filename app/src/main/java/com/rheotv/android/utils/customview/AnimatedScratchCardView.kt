package com.rheotv.android.utils.customview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.makeramen.roundedimageview.RoundedImageView
import com.rheotv.android.R
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.ViewUtils
import java.util.*

class AnimatedScratchCardView : FrameLayout {

    private var mIsViewRemoved = false
    private var mScratchCard: RoundedImageView? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context.obtainStyledAttributes(attrs, R.styleable.AnimatedScratchCardView))
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context.obtainStyledAttributes(attrs, R.styleable.AnimatedScratchCardView, defStyleAttr, 0))
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context.obtainStyledAttributes(attrs, R.styleable.AnimatedScratchCardView, defStyleAttr, 0))
    }

    private fun performOutAnimation(duration: Int, viewAnimation: ViewAnimation) {
        if (mIsViewRemoved) return
        mIsViewRemoved = true
        if (viewAnimation is ViewAnimation.Animation) {
            startAnimation(viewAnimation.getAnimation(this, duration.toLong(),
                    object : Animation.AnimationListener {

                        override fun onAnimationStart(animation: Animation) = Unit
                        override fun onAnimationRepeat(animation: Animation) = Unit

                        override fun onAnimationEnd(animation: Animation) {
                            postOnAnimation {
                                val parent = parent
                                bottomMargin = 0
                                endMargin = 0
                                (parent as? ViewGroup)?.removeView(this@AnimatedScratchCardView)
                            }
                        }

                    }))
        } else if (viewAnimation is ViewAnimation.Animator) {
            val animat = viewAnimation.getAnimator(this, duration.toLong(),
                    object : Animator.AnimatorListener {

                        override fun onAnimationRepeat(animation: Animator?) = Unit
                        override fun onAnimationCancel(animation: Animator?) = Unit
                        override fun onAnimationStart(animation: Animator?) = Unit

                        override fun onAnimationEnd(animation: Animator?) {
                            postOnAnimation {
                                val parent = parent
                                (parent as? ViewGroup)?.removeView(this@AnimatedScratchCardView)
                            }
                        }
                    })
            animat?.start()
        }
        visibility = View.GONE
    }

    private fun performInAnimation(duration: Int, viewAnimation: ViewAnimation) {
        mIsViewRemoved = false
        if (viewAnimation is ViewAnimation.Animation) {
            startAnimation(viewAnimation.getAnimation(this, duration.toLong(),
                    object : Animation.AnimationListener {

                        override fun onAnimationStart(animation: Animation) {
                            visibility = View.VISIBLE
                        }

                        override fun onAnimationRepeat(animation: Animation) = Unit

                        override fun onAnimationEnd(animation: Animation) {
                            postOnAnimationDelayed({
                                performOutAnimation(duration, mOutAnimation)
                            }, AppConstants.SCRATCH_CARD_VISIBILITY_DURATION.toLong())
                        }

                    }))
        } else if (viewAnimation is ViewAnimation.Animator) {
            val animat = viewAnimation.getAnimator(this, duration.toLong(),
                    object : Animator.AnimatorListener {

                        override fun onAnimationRepeat(animation: Animator?) = Unit
                        override fun onAnimationCancel(animation: Animator?) = Unit
                        override fun onAnimationStart(animation: Animator?) {
                            visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            postOnAnimationDelayed({
                                performOutAnimation(duration, mOutAnimation)
                            }, AppConstants.SCRATCH_CARD_VISIBILITY_DURATION.toLong())
                        }
                    })
            animat?.start()
        }
    }

    private fun init() {
        mScratchCard = RoundedImageView(context)
        mScratchCard?.cornerRadius = ViewUtils.dpToPx(40).toFloat()
        addView(mScratchCard)
    }

    private fun init(typedArray: TypedArray) {
        init()
        val drawable = typedArray.getDrawable(R.styleable.AnimatedScratchCardView_image_id)
        if (drawable != null && mScratchCard != null) {
            mScratchCard?.setImageDrawable(drawable)
        }
    }

    fun setRandomScratchCard(): Int {
        var scratchCardImageResource = R.drawable.ic_scratch_card_1
        if (mScratchCard != null) {
            val randomValue = Random().nextInt(3)
            if (randomValue == 1) {
                scratchCardImageResource = R.drawable.ic_scratch_card_2
            } else if (randomValue == 2) {
                scratchCardImageResource = R.drawable.ic_scratch_card_3
            }
            mScratchCard?.setImageResource(scratchCardImageResource)
        }
        return scratchCardImageResource
    }

    fun setScratchCardImage(@DrawableRes imageId: Int) {
        if (mScratchCard != null) {
            mScratchCard?.setImageResource(imageId)
        }
    }

    fun setScratchCardImage(drawable: Drawable?) {
        if (mScratchCard != null) {
            mScratchCard?.setImageDrawable(drawable)
        }
    }

    fun setScratchCardImage(bitmap: Bitmap?) {
        if (mScratchCard != null) {
            mScratchCard?.setImageBitmap(bitmap)
        }
    }

    fun setScratchCardImage(uri: Uri?) {
        if (mScratchCard != null) {
            mScratchCard?.setImageURI(uri)
        }
    }

    private lateinit var mInAnimation: ViewAnimation
    private lateinit var mOutAnimation: ViewAnimation
    fun addTo(container: ViewGroup?, bottomMargin: Int, endMargin: Int, viewInAnimation: ViewAnimation,
              viewOutAnimation: ViewAnimation, scratchCardVisibilityListener: ScratchCardVisibilityListener?) {
        if (container == null) return
        val cardWidth = ViewUtils.dpToPx(WIDTH)
        val cardHeight = ViewUtils.dpToPx(HEIGHT)
        AnimatedScratchCardView.bottomMargin = bottomMargin
        AnimatedScratchCardView.endMargin = endMargin
        id = View.generateViewId()
        val x = ViewUtils.getScreenWidthInPx(context).toInt() / 2 - cardWidth / 2
        val y = ViewUtils.getScreenHeightInPx(context).toInt() / 2 - cardHeight / 2
        val layoutParams = if (container is ConstraintLayout) ConstraintLayout.LayoutParams(cardWidth, cardHeight) else MarginLayoutParams(cardWidth, cardHeight)
        layoutParams.marginStart = x
        layoutParams.topMargin = y
        container.addView(this, layoutParams)
        if (container is ConstraintLayout) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(container)
            constraintSet.connect(id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
//            constraintSet.applyToLayoutParams(id, layoutParams as ConstraintLayout.LayoutParams)
            constraintSet.applyTo(container)
        }

        this.tag = TAG
        visibility = View.GONE
        mInAnimation = viewInAnimation
        mOutAnimation = viewOutAnimation
        performInAnimation(ANIMATION_DURATION, viewInAnimation)
        scratchCardVisibilityListener?.performAction()
    }

    fun setAction(listener: OnClickListener?) {
        setOnClickListener { view: View? ->
            if (listener != null) {
                clearAnimation()
                performOutAnimation(ANIMATION_DURATION, mOutAnimation)
                listener.onClick(view)
            }
        }
    }

    interface ScratchCardVisibilityListener {
        fun performAction()
    }

    companion object {
        private const val ANIMATION_DURATION = 500
        private const val WIDTH = 102
        private const val HEIGHT = 94

        private var bottomMargin: Int = 0
        private var endMargin: Int = 0

        @JvmStatic
        val TAG = "AnimatedScratchCardView"

        fun getSlideInAnimation(): ViewAnimation.Animation = ViewAnimation.Animation.InAnimation.SlideFromBottom
        fun getSlideOutAnimation(): ViewAnimation.Animation = ViewAnimation.Animation.OutAnimation.SlideToBottom
        fun getPathInAnimator(): ViewAnimation.Animator = ViewAnimation.Animator.InAnimator.ScaleInThenSlideToRightBottomCornerAnimator
        fun getScaleInThenSlideToRightBottomCornerAnimation(): ViewAnimation.Animation = ViewAnimation.Animation.InAnimation.ScaleInThenSlideToRightBottomCornerAnimation
    }

    sealed class ViewAnimation {

        sealed class Animation : ViewAnimation() {

            sealed class InAnimation : Animation() {

                object SlideFromBottom : InAnimation() {

                    override fun getAnimation(viewToBeAnimated: View, animationDuration: Long, animationListener: android.view.animation.Animation.AnimationListener?): AnimationSet? =
                            AnimationSet(true).also {
                                it.addAnimation(
                                        TranslateAnimation(0f, 0f, ViewUtils.dpToPx(225).toFloat(), 0f).apply
                                        {
                                            fillAfter = true
                                            duration = animationDuration
                                            setAnimationListener(animationListener)
                                        })
                            }

                }

                object ScaleInThenSlideToRightBottomCornerAnimation : InAnimation() {
                    override fun getAnimation(viewToBeAnimated: View, animationDuration: Long, animationListener: android.view.animation.Animation.AnimationListener?): AnimationSet? =
                            AnimationSet(false).also {
                                it.addAnimation(ScaleAnimation(0f, 1.2f, 0f, 1.2f,
                                        android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                                        android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f).apply {
                                    duration = 500
                                    setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                                        override fun onAnimationRepeat(animation: android.view.animation.Animation?) {

                                        }

                                        override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                                            val x = (ViewUtils.getScreenWidthInPx(viewToBeAnimated.context) - ViewUtils.dpToPx(102)) / 2 - ViewUtils.dpToPx(102)
                                            val y = (ViewUtils.getScreenHeightInPx(viewToBeAnimated.context) - ViewUtils.dpToPx(94)) / 2 - ViewUtils.dpToPx(225)
                                            Handler(Looper.getMainLooper())
                                                    .postDelayed({
                                                        viewToBeAnimated.startAnimation(TranslateAnimation(android.view.animation.Animation.RELATIVE_TO_SELF, 0f,
                                                                android.view.animation.Animation.RELATIVE_TO_SELF, x, android.view.animation.Animation.RELATIVE_TO_SELF, 0f,
                                                                android.view.animation.Animation.RELATIVE_TO_SELF, y)
                                                                .apply {
                                                                    duration = 500
                                                                })
                                                    }, 3000)
                                        }

                                        override fun onAnimationStart(animation: android.view.animation.Animation?) {
                                        }
                                    })
                                })
                                it.setAnimationListener(animationListener)
                            }
                }
            }

            sealed class OutAnimation : Animation() {
                object SlideToBottom : OutAnimation() {

                    override fun getAnimation(viewToBeAnimated: View, animationDuration: Long, animationListener: android.view.animation.Animation.AnimationListener?): AnimationSet? =
                            AnimationSet(false)
                                    .also {
                                        it.addAnimation(TranslateAnimation(0f, 0f, 0f, ViewUtils.dpToPx(HEIGHT + bottomMargin).toFloat()).apply {
                                            fillAfter = true
                                            duration = animationDuration
                                            setAnimationListener(animationListener)
                                        })
                                    }


                }

            }

            abstract fun getAnimation(viewToBeAnimated: View, animationDuration: Long, animationListener: android.view.animation.Animation.AnimationListener?): AnimationSet?
        }

        sealed class Animator : ViewAnimation() {

            sealed class InAnimator : Animator() {
                object ScaleInThenSlideToRightBottomCornerAnimator : InAnimator() {
                    override fun getAnimator(viewToBeAnimated: View, animationDuration: Long, animationListener: android.animation.Animator.AnimatorListener?): AnimatorSet? =
                            AnimatorSet().also {
                                val screenWidth = ViewUtils.getScreenWidthInPx(viewToBeAnimated.context) / 2 - ViewUtils.dpToPx(WIDTH / 2 + endMargin)
                                val screenHeight = ViewUtils.getScreenHeightInPx(viewToBeAnimated.context) / 2 - ViewUtils.dpToPx(HEIGHT + bottomMargin)
                                it.addListener(animationListener)
                                it.play(AnimatorSet().apply {
                                    play(ObjectAnimator.ofFloat(viewToBeAnimated, "scaleX", 0f, 1.5f).setDuration(500))
                                            .with(ObjectAnimator.ofFloat(viewToBeAnimated, "scaleY", 0f, 1.5f).setDuration(500))
                                    addListener(object : android.animation.Animator.AnimatorListener {
                                        override fun onAnimationRepeat(animation: android.animation.Animator?) {

                                        }

                                        override fun onAnimationEnd(animation: android.animation.Animator?) {
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                AnimatorSet()
                                                        .apply {
                                                            play(ObjectAnimator.ofFloat(viewToBeAnimated, "translationX", screenWidth).setDuration(500))
                                                                    .with(ObjectAnimator.ofFloat(viewToBeAnimated, "translationY", screenHeight).setDuration(500))
                                                                    .with(ObjectAnimator.ofFloat(viewToBeAnimated, "scaleX", 1.5f, 1f).setDuration(500))
                                                                    .with(ObjectAnimator.ofFloat(viewToBeAnimated, "scaleY", 1.5f, 1f).setDuration(500))
                                                        }.start()
                                            }, 1500)
                                        }

                                        override fun onAnimationCancel(animation: android.animation.Animator?) {
                                        }

                                        override fun onAnimationStart(animation: android.animation.Animator?) {
                                        }
                                    })
                                })

                            }
                }
            }

            abstract fun getAnimator(viewToBeAnimated: View, animationDuration: Long, animationListener: android.animation.Animator.AnimatorListener?): AnimatorSet?
        }
    }
}