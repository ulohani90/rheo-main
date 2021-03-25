package com.rheotv.android.ui.decorators;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostFeedItemDecorator extends RecyclerView.ItemDecoration {
    int spacing;

        public PostFeedItemDecorator(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildLayoutPosition(view);
            if (position != 0) {
                outRect.bottom = spacing;
            }
        }
}