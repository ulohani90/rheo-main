package com.rheotv.android.utils.viewpager2.transformer

import android.view.View
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class TranslateEdgeToEdgeTransformer(private val mPageOffset: Int, private val mTranslationOffset: Int) : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        val viewPager = page.parent.parent as ViewPager2
        val offset = position * mPageOffset
        if (viewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
            if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                page.translationX = -offset
            } else {
                page.translationX = offset
            }
        } else {
            page.translationY = offset
        }
        page.translationY = abs(position) * mTranslationOffset
    }
}

class TranslateEdgeToEdgeTransformerV1(private val mPageOffset: Int, private val mTranslationOffset: Int) : ViewPager.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        (page.parent as? ViewPager)?.let { viewPager ->
            val offset = position * mPageOffset
            if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                page.translationX = -offset
            } else {
                page.translationX = offset
            }
            page.translationY = abs(position) * mTranslationOffset
        }
    }
}