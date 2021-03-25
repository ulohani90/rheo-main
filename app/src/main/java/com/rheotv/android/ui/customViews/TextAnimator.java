package com.rheotv.android.ui.customViews;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.rheotv.android.R;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

public class TextAnimator {
    private WeakReference<Context> contextRef;
    private Runnable textRunner;
    private Handler handler;
    private Animation inAnimation, outAnimation;
    private CharSequence[] texts;
    private int position = 0;
    private long duration = 20000;
    private TextView textView;
    private String userName;

    public TextAnimator(Context context, TextView textView, int resId) {
        this.contextRef = new WeakReference<>(context);
        this.textView = textView;
        this.texts = contextRef.get().getResources().getStringArray(resId);
        this.inAnimation = AnimationUtils.loadAnimation(contextRef.get(), R.anim.fade_in);
        this.outAnimation = AnimationUtils.loadAnimation(contextRef.get(), R.anim.fade_out);
        this.handler = new Handler();
        this.textRunner = this::fadeOut;
    }

    public TextAnimator(Context context, TextView textView, int resId, String userName) {
        this(context, textView, resId);
        this.userName = userName;
    }

    public TextAnimator(Context context, TextView textView, @Nullable Integer resId, int inAnimation, int outAnimation, long duration, String userName) {
        this.contextRef = new WeakReference<>(context);
        this.textView = textView;
        if (resId != null)
            this.texts = contextRef.get().getResources().getStringArray(resId);
        this.inAnimation = AnimationUtils.loadAnimation(contextRef.get(), inAnimation);
        this.outAnimation = AnimationUtils.loadAnimation(contextRef.get(), outAnimation);
        this.handler = new Handler();
        this.textRunner = this::fadeOut;
        this.duration = duration;
        this.userName = userName;
    }

    public void start() {
        Log.i(getClass().getSimpleName(), "start");
        handler.postDelayed(textRunner, duration);
    }

    public void stop() {
        Log.i(getClass().getSimpleName(), "stop");
        handler.removeCallbacks(textRunner);
    }

    private void fadeOut() {
        Log.i(getClass().getSimpleName(), "fadeOut_called");
        textView.startAnimation(outAnimation);
        outAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.i(getClass().getSimpleName(), "fadeOut_start");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.i(getClass().getSimpleName(), "fadeOut_end");
                if (texts != null && texts.length > 0) {
                    textView.setText(texts[position] + (userName != null ? " " + userName : ""));
                    position = position == texts.length - 1 ? 0 : position + 1;
                }
                fadeIn();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void fadeIn() {
        Log.i(getClass().getSimpleName(), "fadeIn_called");
        textView.startAnimation(inAnimation);
        inAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.i(getClass().getSimpleName(), "fadeIn_start");

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.i(getClass().getSimpleName(), "fadeIn_end");
                start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

}
