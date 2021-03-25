package com.rheotv.android.ui.customViews;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeActionListener implements View.OnTouchListener {
    private GestureDetector gestureDetector;

    public OnSwipeActionListener(Context c) {
        gestureDetector = new GestureDetector(c, new GestureListener());
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }

    public void onSwipeDown() {

    }

    public void onSwipeUp() {

    }

    public void onSwipeRight() {

    }

    public void onSwipeLeft() {

    }

    public void performTouch() {

    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            performTouch();
            return false;
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                Log.d("TAGGER X", "x : " + diffX);
                Log.d("TAGGER Y", "y : " + diffY);

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                        /*change swipe limit according to customization in future*/
                        Log.d("TAGGER", "event happened");

                        if (diffX > 0) {
                            Log.d("TAGGER", "right swipe happened");
                            onSwipeRight();
                        } else {
                            Log.d("TAGGER", "left swipe happened");
                            onSwipeLeft();
                        }
                    }

                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown();
                        } else {
                            onSwipeUp();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
}
