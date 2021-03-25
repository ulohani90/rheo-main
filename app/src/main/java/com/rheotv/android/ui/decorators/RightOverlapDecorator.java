package com.rheotv.android.ui.decorators;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class RightOverlapDecorator extends RecyclerView.ItemDecoration {
    private final int mSpace;

    public RightOverlapDecorator(int space) {
        this.mSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int last = parent.getAdapter() != null ? parent.getAdapter().getItemCount() : 0;
        if (position != last - 1 && outRect.left == 0)
            outRect.left = mSpace;
    }
}