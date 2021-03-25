package com.rheotv.android.ui.decorators;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class LeftOverlapDecorator extends RecyclerView.ItemDecoration {
    private final int mSpace;

    public LeftOverlapDecorator(int space) {
        this.mSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position != 0 && outRect.left == 0)
            outRect.left = mSpace;
    }
}
