/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 1:23 PM
 *
 */

package com.rheotv.android.data.network.middleware;



import androidx.annotation.NonNull;

import com.rheotv.android.data.network.callbacks.INetworkCallbacks;
import com.rheotv.android.data.network.models.base.ApiResponse;
import com.rheotv.android.utils.AppLogger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkLayer<T extends ApiResponse, Y> {
    private Call<T> mCall;
    private INetworkCallbacks<Y> mINetworkCallbacks;
    private String mType;

    public NetworkLayer(Call<T> call, INetworkCallbacks<Y> iNetworkCallbacks, String type) {
        this.mCall = call;
        this.mINetworkCallbacks = iNetworkCallbacks;
        this.mType = type;
    }

    public void callNetworkAPI() {
        mCall.enqueue(new Callback<T>() {
            @Override
            public void onResponse  (@NonNull Call<T> call, @NonNull Response<T> response) {
                AppLogger.d("MOJOTIMES----> RESPONSE", response);
                mINetworkCallbacks.onComplete();
                mINetworkCallbacks.onSuccess((Y) response.body(), mType);
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                AppLogger.d("MOJOTIMES----> ERROR", t.getLocalizedMessage());
                mINetworkCallbacks.onComplete();
                mINetworkCallbacks.onError(t.getMessage(), mType);
            }
        });
    }
}
