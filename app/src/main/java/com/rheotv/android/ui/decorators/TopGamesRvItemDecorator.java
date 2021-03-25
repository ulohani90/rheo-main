package com.rheotv.android.ui.decorators;

import android.graphics.Rect;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TopGamesRvItemDecorator extends RecyclerView.ItemDecoration {

    int spacing;

    public TopGamesRvItemDecorator(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildLayoutPosition(view);
        if (position == 0) {
            outRect.right = spacing / 2;
        } else if (position == parent.getAdapter().getItemCount() - 1) {
            outRect.left = spacing / 2;
        } else {
            outRect.left = spacing / 2;
            outRect.right = spacing / 2;
        }
    }
}
