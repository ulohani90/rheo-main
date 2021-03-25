package com.rheotv.android.ui.customViews.pageTransformation

import android.content.Context
import android.view.View
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import androidx.core.view.ViewCompat.setScaleY
import android.opengl.ETC1.getHeight
import com.rheotv.android.R
import kotlin.math.abs

class PageScaleTransformation(
        private val context: Context
) : ViewPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val pageMargin = context.resources.getDimensionPixelOffset(R.dimen.margin_30).toFloat()
        val pageOffset = context.resources.getDimensionPixelOffset(R.dimen.margin_30).toFloat()

        val myOffset = position * -(2 * pageOffset + pageMargin)
        when {
            position < -1 -> page.translationX = -myOffset
            position <= 1 -> {
                val scaleFactor = Math.max(0.7f, 1 - Math.abs(position - 0.14285715f))
                page.translationX = myOffset
                page.scaleY = scaleFactor
                //page.alpha = scaleFactor
            }
            else -> {
               // page.alpha = 0f
                page.translationX = myOffset
            }
        }
    }
}
