package com.rheotv.android.ui.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.rheotv.android.R;
import com.rheotv.android.utils.CommonUtils;

public class CircularIndeterminateProgressBar extends View {
    private float backgroundWidth = 20f;
    private float progressWidth = 14f;

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Float progress = 0f;

    private RectF oval = new RectF();
    private Float centerX = 0f;
    private Float centerY = 0f;
    private Float radius = 0f;

    private Rect bounds;
    private Paint textPaint;
    private float textX;
    private float textY;
    private String mText = "00.00";
    private boolean showTimer = true;

    public CircularIndeterminateProgressBar(Context context) {
        super(context);
        init(null);
    }

    public CircularIndeterminateProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context.obtainStyledAttributes(attrs, R.styleable.CircularIndeterminateProgressBar));
    }

    public CircularIndeterminateProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context.obtainStyledAttributes(attrs, R.styleable.CircularIndeterminateProgressBar, defStyleAttr, 0));
    }

    public CircularIndeterminateProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context.obtainStyledAttributes(attrs, R.styleable.CircularIndeterminateProgressBar, defStyleAttr, 0));
    }

    private void init(TypedArray typedArray) {
        int progressColor = ContextCompat.getColor(getContext(), R.color.follow_btn_normal_color);
        int backgroundColor = ContextCompat.getColor(getContext(), R.color.app_background_color);
        if (typedArray != null) {
            backgroundColor = typedArray.getColor(R.styleable.CircularIndeterminateProgressBar_backgroundColor, backgroundColor);
            progressColor = typedArray.getColor(R.styleable.CircularIndeterminateProgressBar_progressColor, progressColor);
        }

        progressPaint = new Paint();
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(backgroundWidth);
        progressPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(progressWidth);
        backgroundPaint.setAntiAlias(true);

        if (showTimer) {
            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getContext().getResources().getDisplayMetrics()));
            bounds = new Rect();
            textPaint.getTextBounds(mText, 0, mText.length(), bounds);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = ((float) w) / 2;
        centerY = ((float) w) / 2;
        radius = (((float) w) / 2) - progressWidth;
        oval.set(centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);
        // center the text vertically
        textX = centerX;
        textY = centerY + 16f;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);
        canvas.drawArc(oval, 270f, (-360f * progress), false, progressPaint);
        if (showTimer)
            canvas.drawText(mText, textX, textY, textPaint);
    }

    public Float getProgress() {
        return progress;
    }

    public void setProgress(Float progress, long ms) {
        this.progress = progress;
        if (showTimer)
            this.mText = CommonUtils.convertSecondsToMmSs(ms);
        invalidate();
    }

    public void setProgress(int progress, long ms) {
        this.progress = (float) progress;
        if (showTimer)
            this.mText = CommonUtils.convertSecondsToMmSs(ms);
        invalidate();
    }

    public void setShowTimer(boolean showTimer) {
        this.showTimer = showTimer;
        invalidate();
    }
}
