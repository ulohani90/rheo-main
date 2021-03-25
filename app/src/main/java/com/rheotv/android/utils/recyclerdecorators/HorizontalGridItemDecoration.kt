package com.rheotv.android.utils.recyclerdecorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalGridItemDecoration(val space: Int, val spanCount: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        outRect.left = if (position < spanCount) space else space / 2
        outRect.top = if (position % spanCount == 0) 0 else space / 2
        outRect.bottom = if (position % spanCount == 1) 0 else space / 2
        outRect.right = space / 2
    }
}