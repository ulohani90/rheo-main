package com.rheotv.android.data.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AnalyticsResponseData {

    @SerializedName("daily_views")
    @Expose
    List<GraphDataObject> dailyViews;

    public List<GraphDataObject> getDailyViews() {
        return dailyViews;
    }
}
