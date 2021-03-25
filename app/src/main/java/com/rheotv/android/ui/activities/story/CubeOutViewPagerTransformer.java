package com.rheotv.android.ui.activities.story;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class CubeOutViewPagerTransformer implements ViewPager2.PageTransformer {

    int distanceMultiplier = 20;
    boolean isPagingEnabled = true;

    @Override
    public void transformPage(@NonNull View page, float position) {
        float clampedPosition = clampPosition(position);
        onPreTransform(page, clampedPosition);
        onTransform(page, clampedPosition);
    }

    private float clampPosition(float position) {
        if (position < -1f) {
            return -1f;
        } else if (position > 1f) {
            return 1f;
        } else if (Float.isNaN(position)) {
            return 0f;
        } else {
            return position;
        }
    }

    private void onPreTransform(View page, float position) {
        float width = page.getWidth();

        page.setRotationX(0f);
        page.setRotationY(0f);
        page.setRotation(0f);
        page.setScaleX(1f);
        page.setScaleY(1f);
        page.setPivotX(0f);
        page.setPivotY(0f);
        page.setTranslationY(0f);
        if (isPagingEnabled) {
            page.setTranslationX(0f);
        } else {
            page.setTranslationX(-(width * position));
        }

        float alpha = 1f;
        if (position <= -1f || position >= 1f) {
            alpha = 0f;
        }
        page.setAlpha(alpha);
        page.setEnabled(false);
    }

    private void onTransform(View page, float position) {
        page.setCameraDistance(page.getWidth() * distanceMultiplier);
        if (position < 0f) {
            page.setPivotX(page.getWidth());
        } else {
            page.setPivotX(0f);
        }
        page.setPivotY(page.getHeight() * 0.5f);
        page.setRotationY(90f * position);
    }
}
