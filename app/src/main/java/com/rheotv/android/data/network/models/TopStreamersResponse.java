package com.rheotv.android.data.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TopStreamersResponse {

    @SerializedName("data")
    @Expose
    TopStreamersList data;

    public TopStreamersList getData() {
        return data;
    }
}
