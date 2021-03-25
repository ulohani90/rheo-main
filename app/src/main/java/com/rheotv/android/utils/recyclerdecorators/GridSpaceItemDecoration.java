package com.rheotv.android.utils.recyclerdecorators;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.ui.activities.player.activity.StickerGridRecyclerAdapter;

public class GridSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int mSizeGridSpacingPx;
    int columnCount;

    public GridSpaceItemDecoration(int gridSpacingPx, int columnCount) {
        mSizeGridSpacingPx = gridSpacingPx;
        this.columnCount = columnCount;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (parent.getAdapter() != null && parent.getAdapter().getItemViewType(position) == StickerGridRecyclerAdapter.ITEM_VIEW_TYPE_GREETING) {
            outRect.left = 0;
            outRect.right = 0;
            outRect.top = mSizeGridSpacingPx;
            outRect.bottom = mSizeGridSpacingPx;
        } else {
            if (position % columnCount == 1) {
                outRect.left = mSizeGridSpacingPx;
                outRect.right = mSizeGridSpacingPx / 2;
            } else if (position % columnCount == 0) {
                outRect.right = mSizeGridSpacingPx;
                outRect.left = mSizeGridSpacingPx / 2;
            } else {
                outRect.left = mSizeGridSpacingPx / 2;
                outRect.right = mSizeGridSpacingPx / 2;
            }

            outRect.bottom = mSizeGridSpacingPx;
        }
    }
}
