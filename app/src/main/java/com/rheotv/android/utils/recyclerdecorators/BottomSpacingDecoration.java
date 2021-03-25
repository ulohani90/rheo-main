package com.rheotv.android.utils.recyclerdecorators;

import android.graphics.Rect;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class BottomSpacingDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public BottomSpacingDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        outRect.bottom = space;

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = space;
        } else {
            outRect.top = 0;
        }

        if (position == parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = (space * 4);
        }
    }
}
