package com.rheotv.android.ui.customViews.pageTransformation

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

import androidx.viewpager.widget.ViewPager
import kotlin.math.abs

class FlipViewPager @JvmOverloads constructor(context: Context,
                                              attrs: AttributeSet? = null
) : ViewPager(context, attrs), ViewPager.PageTransformer {
    private var MAX_SCALE = 0.0f
    private var mPageMargin: Int = 0
    private var animationEnabled = true
    private var fadeEnabled = false
    private var fadeFactor = 0.5f

    init {
        // clipping should be off on the pager for its children so that they can scale out of bounds.
        clipChildren = false
        clipToPadding = false
        // to avoid fade effect at the end of the page
        overScrollMode = 2
        setPageTransformer(false, this)
        offscreenPageLimit = 3
        mPageMargin = dp2px(context.resources, 40)
        setPadding(mPageMargin, mPageMargin / 2, mPageMargin, mPageMargin / 2)
    }

    private fun dp2px(resource: Resources, dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resource.displayMetrics).toInt()
    }

    fun setAnimationEnabled(enable: Boolean) {
        this.animationEnabled = enable
    }

    fun setFadeEnabled(fadeEnabled: Boolean) {
        this.fadeEnabled = fadeEnabled
    }

    fun setFadeFactor(fadeFactor: Float) {
        this.fadeFactor = fadeFactor
    }

    override fun setPageMargin(marginPixels: Int) {
        mPageMargin = marginPixels
        setPadding(mPageMargin, mPageMargin, mPageMargin, mPageMargin)
    }

    override fun transformPage(page: View, position: Float) {
        if (mPageMargin <= 0 || !animationEnabled)
            return
        page.setPadding(mPageMargin / 3, mPageMargin / 3, mPageMargin / 3, mPageMargin / 3)

        if (MAX_SCALE == 0.0f && position > 0.0f && position < 1.0f) {
            MAX_SCALE = position
        }
        val factor = position - MAX_SCALE
        val absolutePosition = abs(factor)
        if (factor <= -1.0f || factor >= 1.0f) {
            if (fadeEnabled)
                page.alpha = fadeFactor
            // Page is not visible -- stop any running animations
        } else if (factor == 0.0f) {
            // Page is selected -- reset any views if necessary
            page.scaleX = 1 + MAX_SCALE
            page.scaleY = 1 + MAX_SCALE
            page.alpha = 1f
        } else {
            page.scaleX = 1 + MAX_SCALE * (1 - absolutePosition)
            page.scaleY = 1 + MAX_SCALE * (1 - absolutePosition)
            if (fadeEnabled)
                page.alpha = fadeFactor.coerceAtLeast(1 - absolutePosition)
        }
    }

    companion object {
        val TAG = "FlipViewPager"
    }
}