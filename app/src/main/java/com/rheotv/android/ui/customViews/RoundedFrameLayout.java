package com.rheotv.android.ui.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.rheotv.android.R;

import static android.graphics.Path.Direction.CCW;
import static android.graphics.Path.Direction.CW;

public class RoundedFrameLayout extends FrameLayout {
    private float mRadius;
    private Path mPath = new Path();
    private RectF mRect = new RectF();

    public RoundedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRadius = context.getResources().getDimension(R.dimen.corner_radius);
        //TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, , context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int savedState = canvas.save();
        float w = getWidth();
        float h = getHeight();
        mPath.reset();
        mRect.set(0, 0, w, h);
        mPath.addRoundRect(mRect, new float[]{mRadius, mRadius, mRadius, mRadius, mRadius, mRadius, mRadius, mRadius}, CCW);
        mPath.close();
        boolean debug = canvas.clipPath(mPath);
        super.onDraw(canvas);
        canvas.restoreToCount(savedState);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // compute the mPath
        float centerX = w / 2f; // calculating half width
        float centerY = h / 2f; // calculating half height
        mRect.set(0, 0, w, h);
        mPath.reset();
        mPath.addRoundRect(mRect, new float[]{mRadius, mRadius, mRadius, mRadius, mRadius, mRadius, mRadius, mRadius}, CCW);
        mPath.close();

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(mPath);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }
}