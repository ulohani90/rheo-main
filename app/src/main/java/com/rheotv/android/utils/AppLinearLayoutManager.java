package com.rheotv.android.utils;

import android.content.Context;

import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

public class AppLinearLayoutManager extends LinearLayoutManager {

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    public AppLinearLayoutManager(Context context) {
        super(context);
    }

    public AppLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public AppLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
