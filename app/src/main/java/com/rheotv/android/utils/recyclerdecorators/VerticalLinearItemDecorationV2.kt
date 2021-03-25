package com.rheotv.android.utils.recyclerdecorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class VerticalLinearItemDecorationV2(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val itemCount = state.itemCount
        val itemPosition = parent.getChildAdapterPosition(view)

        // no position, leave it alone
        if (itemPosition == RecyclerView.NO_POSITION) {
            return
        }

        // first item
        if (itemPosition == 0) {
            outRect.set(0, space, 0, if (itemCount == 1) space else space / 2)
        }
        //last item
        else if (itemCount > 0 && itemPosition == itemCount - 1) {
            outRect[0, space / 2, 0] = space
        }
        //every other item
        else {
            outRect.set(0, space / 2, 0, space / 2)
        }
    }

}