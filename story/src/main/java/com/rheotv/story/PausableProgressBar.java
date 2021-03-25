package com.rheotv.story;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

public class PausableProgressBar extends ProgressBar {
    int durationInSeconds;
    private int index;
    private ObjectAnimator objectAnimator;
    private boolean hasStarted = false;
    private ProgressTimeWatcher timeWatcher;
    private int mProgressDrawable;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PausableProgressBar(Context context, int index, int durationInSeconds, ProgressTimeWatcher timeWatcher, @DrawableRes int mProgressDrawable, int defStyle) {
        super(context, null, 0, defStyle);
        this.durationInSeconds = durationInSeconds;
        this.index = index;
        this.timeWatcher = timeWatcher;
        this.mProgressDrawable = mProgressDrawable;
        intViews();
    }

    public PausableProgressBar(Context context, int index, int durationInSeconds, ProgressTimeWatcher timeWatcher, @Nullable @DrawableRes int mProgressDrawable) {
        super(context, null, 0);
        this.durationInSeconds = durationInSeconds;
        this.index = index;
        this.timeWatcher = timeWatcher;
        this.mProgressDrawable = mProgressDrawable;
        intViews();
    }

    private void intViews() {
        objectAnimator = ObjectAnimator.ofInt(this, "progress", this.getProgress(), 100);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );

        params.rightMargin = toPix(5);
        this.setMax(100);
        this.setProgress(0);
        this.setLayoutParams(params);
        this.setProgressDrawable(ContextCompat.getDrawable(getContext(), mProgressDrawable));
    }

    public int toPix(int dm) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dm * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public void startProgress() {
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                timeWatcher.onEnd(index);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.removeAllListeners();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        objectAnimator.setDuration(durationInSeconds * 1000);
        objectAnimator.start();
        hasStarted = true;
    }

    void cancelProgress() {
        objectAnimator.cancel();
    }

    public void pauseProgress() {
        objectAnimator.pause();
    }

    public void resumeProgress() {
        if (hasStarted) {
            objectAnimator.resume();
        }
    }

    public void editDurationAndResume(int newDurationInSeconds) {
        this.durationInSeconds = newDurationInSeconds;
        cancelProgress();
    }
}
