/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 12:38 PM
 *
 */

package com.rheotv.android.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;

public final class ViewAnimationUtils {

    private ViewAnimationUtils() {
        // This class is not publicly instantiable
    }

    public static void scaleAnimateView(View view) {
        ScaleAnimation animation =
                new ScaleAnimation(
                        1.15f, 1, 1.15f, 1,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);

        view.setAnimation(animation);
        animation.setDuration(100);
        animation.start();
    }

    public static void expand(final View v, int targetHeight) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density) + 200);
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();
        collapse(v, (int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density) + 200);
    }

    public static void collapse(final View v, int duration) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(duration);
        v.startAnimation(a);
    }

    public static void reveal(final View view, final View anchor) {
        anchor.setVisibility(View.INVISIBLE);
        view.setVisibility(View.VISIBLE);

//        int x = view.getRight();
//        int y = view.getBottom();
//
//        int startRadius = 0;
//        int endRadius = (int) Math.hypot(view.getWidth(), view.getHeight());
//
//        Animator anim = android.view.ViewAnimationUtils.createCircularReveal(view, x, y, startRadius, endRadius);
//        anim.setDuration(400);
//        anim.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//                anchor.setVisibility(View.INVISIBLE);
//                view.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//        anim.start();
    }

    public static void unReveal(final View view, final View anchor) {
        anchor.setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
//        int x = view.getRight();
//        int y = view.getBottom();
//
//        int startRadius = (int) Math.hypot(view.getWidth(), view.getHeight());
//        int endRadius = 0;
//
//        Animator anim = android.view.ViewAnimationUtils.createCircularReveal(view, x, y, startRadius, endRadius);
//        anim.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//                anchor.setVisibility(View.VISIBLE);
//                view.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//        anim.start();

    }
}
