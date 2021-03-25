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

import java.util.List;

public class BaseCollectionResponse<T> extends ApiResponse {
    @SerializedName("data")
    @Expose
    private List<T> t;
    @SerializedName("status")
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<T> getT() {
        return t;
    }

    public void setT(List<T> t) {
        this.t = t;
    }
}
