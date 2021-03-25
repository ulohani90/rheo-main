package com.rheotv.android.ui.customViews;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.rheotv.android.R;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.NetworkUtils;

import java.lang.ref.WeakReference;

public class HeartAnimator {
    private WeakReference<Context> contextRef;
    private ImageView imageView;
    private WeakReference<FrameLayout> containerLayout;
    private WeakReference<HeartAnimatorInteractionListener> listener;

    private Runnable heartRunner;
    private Handler heartHandler = new Handler();
    private boolean isHeartFilling = false;
    private long addedHeartCount = 0;

    public HeartAnimator(FrameLayout containerLayout, HeartAnimatorInteractionListener listener) {
        this.containerLayout = new WeakReference<>(containerLayout);
        this.listener = new WeakReference<>(listener);
        contextRef = new WeakReference<>(containerLayout.getContext());
    }

    public void start(WeakReference<View> view) {
        WeakReference<ImageView> imageView = new WeakReference<>((ImageView) view.get());
        imageView.get().setVisibility(View.VISIBLE);
        Animatable animatable = (Animatable) imageView.get().getDrawable();
        animatable.start();

        heartRunner = () -> {
            Log.i(getClass().getSimpleName(), "checking_heart_state");
            animatable.stop();
            isHeartFilling = false;
            scaleHeart(view.get());
        };
        heartHandler.postDelayed(heartRunner, 9000);
    }

    public void stop() {
        heartHandler.removeCallbacks(heartRunner);
    }

    private void scaleHeart(View view) {
        Log.i(AppConstants.TAG, "scaling_Heart");
        ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 2f),
                PropertyValuesHolder.ofFloat("scaleY", 2f));
        scaleUp.setDuration(250);
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f));
        scaleDown.setDuration(250);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleDown).after(scaleUp);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
//                ((ImageView) view).getDrawable().setTint(ContextCompat.getColor(view.getContext(), R.color.red_heart));
            }

            @Override
            public void onAnimationEnd(Animator animator) {
//                ((ImageView) view).getDrawable().setTint(ContextCompat.getColor(view.getContext(), android.R.color.white));
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                ((ImageView) view).getDrawable().setTint(ContextCompat.getColor(view.getContext(), R.color.white));
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }

    public void fadeAndScaleHeart(WeakReference<View> view) {
        if (!NetworkUtils.isNetworkConnected(contextRef.get())) {
            showToast("Please check you internet connection");
            return;
        }

        if (!CommonUtils.isUserLoggedin()) {
            if (listener != null)
                listener.get().askLogin();
            return;
        }

        if (isHeartFilling) {
            showToast("Filling Heart for you!");
            return;
        } else
            isHeartFilling = true;

//        String segmentUrl = mListener.getSegmentUrl();
//        mViewModel.addHeart(segmentUrl, currentPlayingPost.getId(), CommonUtils.getUserName(getContext()), currentPlayingPost.getAuthor().getUser().getUsername(), getContext());
        if (listener != null)
            listener.get().onHeartUp();

        ObjectAnimator fade = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fade.setDuration(300);
        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 2.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 2.0f));
        scale.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fade, scale);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                ((ImageView) view.get()).getDrawable().setTint(ContextCompat.getColor(view.get().getContext(), R.color.red_heart));
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.get().setAlpha(1.0f);
                view.get().setScaleX(1.0f);
                view.get().setScaleY(1.0f);
                ((ImageView) view.get()).getDrawable().setTint(ContextCompat.getColor(view.get().getContext(), android.R.color.white));
                start(view);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }

    public void animateHeartUp() {
        if (contextRef.get() != null) {
            if (containerLayout.get().getChildCount() == 20) {
                return;
            }
            addedHeartCount++;
//            mViewModel.updateHeartCount();
//            mListener.onHeartCountUpdate(mViewModel.localHeartCounter);
            ImageView heartImageView = new ImageView(contextRef.get());
            heartImageView.setImageResource(R.drawable.ic_heart_filled_16);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            if (addedHeartCount % 3 == 1) {
                lp.leftMargin = CommonUtils.toPix(12);
            } else if (addedHeartCount % 3 == 2) {
                lp.rightMargin = CommonUtils.toPix(12);
            }

            containerLayout.get().addView(heartImageView, lp);

            ObjectAnimator animator1 = ObjectAnimator.ofFloat(heartImageView, View.TRANSLATION_Y, 0, -containerLayout.get().getHeight());
            ObjectAnimator animator3 = ObjectAnimator.ofFloat(heartImageView, View.SCALE_X, 1.0f, 1.5f);
            ObjectAnimator animator4 = ObjectAnimator.ofFloat(heartImageView, View.SCALE_Y, 1.0f, 1.5f);
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(heartImageView, View.ALPHA, 1, 0);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(2000);

            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    heartImageView.setVisibility(View.GONE);
                    containerLayout.get().removeView(heartImageView);
                    addedHeartCount--;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.playTogether(animator1, animator2, animator3, animator4);
            animatorSet.start();
        }
    }

    private void showToast(String message) {
        Toast.makeText(contextRef.get(), message, Toast.LENGTH_SHORT).show();
    }

    public interface HeartAnimatorInteractionListener {
        void onHeartUp();

        void askLogin();
    }

}
