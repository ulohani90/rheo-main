package com.rheotv.android.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SearchItemLinearDecorator extends RecyclerView.ItemDecoration {
    private int space;
    private int bottomScaleMultiplier = 5;

    public SearchItemLinearDecorator(int space) {
        this.space = space;
    }

    public SearchItemLinearDecorator(int space, int buttomScaleMultiplier) {
        this.space = space;
        this.bottomScaleMultiplier = buttomScaleMultiplier;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int itemCount = state.getItemCount();

        final int itemPosition = parent.getChildAdapterPosition(view);

        // no position, leave it alone
        if (itemPosition == RecyclerView.NO_POSITION) {
            return;
        }

        // first item
        if (itemPosition == 0) {
            outRect.set(space, space, space, space / 2);
        }
        // last item
        else if (itemCount > 0 && itemPosition == itemCount - 1) {
            outRect.set(space, space / 2, space, space * bottomScaleMultiplier);
        }
        // every other item
        else {
            outRect.set(space, space / 2, space, space / 2);
        }
    }
}