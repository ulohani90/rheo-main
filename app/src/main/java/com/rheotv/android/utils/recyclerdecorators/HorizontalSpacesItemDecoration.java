package com.rheotv.android.utils.recyclerdecorators;

import android.graphics.Rect;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class HorizontalSpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public HorizontalSpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        int itemCount = state.getItemCount();
        final int itemPosition = parent.getChildAdapterPosition(view);

        // no position, leave it alone
        if (itemPosition == RecyclerView.NO_POSITION) {
            return;
        }

        // first item
        if (itemPosition == 0) {
            outRect.set(space, 0, space / 2, 0);
        }
        // last item
        else if (itemCount > 0 && itemPosition == itemCount - 1) {
            outRect.set(space / 2, 0, space, 0);
        }
        // every other item
        else {
            outRect.set(space / 2, 0, space / 2, 0);
        }
    }
}