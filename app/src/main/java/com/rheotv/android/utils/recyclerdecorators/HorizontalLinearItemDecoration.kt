package com.rheotv.android.utils.recyclerdecorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalLinearItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val itemCount = state.itemCount
        val itemPosition = parent.getChildAdapterPosition(view)

        // no position, leave it alone
        if (itemPosition == RecyclerView.NO_POSITION) {
            return
        }
        // first item
        if (itemPosition == 0) {
            outRect[space, space, space / 2] = space
        } else
        // last item
            if (itemCount > 0 && itemPosition == itemCount - 1) {
                outRect[space / 2, space, space] = space
            } else
            // other item
            {
                outRect[space / 2, space, space / 2] = space
            }
    }
}