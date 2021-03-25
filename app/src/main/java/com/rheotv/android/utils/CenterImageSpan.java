package com.rheotv.android.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class CenterImageSpan extends ImageSpan {

    private WeakReference<Drawable> mDrawableRef;

    CenterImageSpan(Context context, final int drawableRes) {
        super(context, drawableRes);
    }

    public CenterImageSpan(@NonNull Drawable d) {
        super(d);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text,
                     int start, int end, float x,
                     int top, int y, int bottom, @NonNull Paint paint) {
        Drawable b = getCachedDrawable();
        canvas.save();
        int transY = top + (bottom - top - b.getBounds().bottom)/2;
        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }

    // Redefined locally because it is a private member from DynamicDrawableSpan
    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;

        if (wr != null)
            d = wr.get();

        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<>(d);
        }

        return d;
    }
}