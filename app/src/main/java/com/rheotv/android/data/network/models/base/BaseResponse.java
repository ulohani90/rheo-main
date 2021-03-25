/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 1:23 PM
 *
 */

package com.rheotv.android.data.network.models.base;

import com.rheotv.android.data.network.models.common.Status;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BaseResponse<T> extends ApiResponse{
    @SerializedName("data")
    @Expose
    private T t;
    @SerializedName("status")
    @Expose
    private Status status;

    public T getPayload() {
        return t;
    }

    public void setPayload(T payload) {
        this.t = payload;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
