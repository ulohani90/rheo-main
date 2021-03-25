package com.rheotv.android.utils.segmentTracker;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EqualSpaceItemDecorator extends RecyclerView.ItemDecoration {
    int spacing;

    public EqualSpaceItemDecorator(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildLayoutPosition(view);
//        if (position == 0) {
//            outRect.bottom = spacing;
//        }
        outRect.top = spacing;
        outRect.left = spacing;
        outRect.right = spacing;
    }
}
