package com.rheotv.android.ui.decorators

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView

class SimpleDividerItemDecoration(private val mDivider: Drawable?, private val orientation: Orientation) : RecyclerView.ItemDecoration() {
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (orientation == Orientation.Horizontal) {
            drawHorizontal(c, parent)
        } else {
            drawVertical(c, parent)
        }
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = mDivider?.intrinsicHeight?.plus(top)
            mDivider?.setBounds(left, top, right, bottom ?: 0)
            mDivider?.draw(c)
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left = child.left + params.marginStart
            val right = mDivider?.intrinsicHeight?.plus(left)
            mDivider?.setBounds(left, top, right ?: 0, bottom)
            mDivider?.draw(c)
        }
    }

    sealed class Orientation {
        object Vertical : Orientation()
        object Horizontal : Orientation()
    }
}