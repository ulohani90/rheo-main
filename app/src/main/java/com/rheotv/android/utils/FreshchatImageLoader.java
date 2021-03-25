package com.rheotv.android.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freshchat.consumer.sdk.FreshchatImageLoaderRequest;


public class FreshchatImageLoader implements com.freshchat.consumer.sdk.FreshchatImageLoader {

    @Override
    public void load(@NonNull FreshchatImageLoaderRequest request, @NonNull ImageView imageView) {
        BindingUtils.setImageUrlUsingCache(imageView, request.getUri().toString(), true);
        // your code to download story and set to imageView
    }

    @Nullable
    @Override
    public Bitmap get(@NonNull FreshchatImageLoaderRequest request) {
        return null;
    }

    @Override
    public void fetch(@NonNull FreshchatImageLoaderRequest request) {

    }
}
