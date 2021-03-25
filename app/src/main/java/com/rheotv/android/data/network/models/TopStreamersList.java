package com.rheotv.android.data.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TopStreamersList {

    @SerializedName("next")
    String next;

    @SerializedName("results")
    List<TopStreamerObject> results;


    public String getNext() {
        return next;
    }

    public List<TopStreamerObject> getResults() {
        return results;
    }
}
