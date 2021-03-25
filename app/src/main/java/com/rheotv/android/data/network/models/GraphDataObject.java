package com.rheotv.android.data.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GraphDataObject {
    @SerializedName("views")
    @Expose
    int views;

    @SerializedName("duration")
    @Expose
    float duration;

    @SerializedName("value")
    @Expose
    float value;

    @SerializedName("date")
    @Expose
    long date;


    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int getViews() {
        return views;
    }

    public long getDate() {
        return date;
    }

    public float getDuration() {
        return duration;
    }

    public GraphDataObject(int views, float duration, long date) {
        this.views = views;
        this.duration = duration;
        this.date = date;
    }
}
