package com.rheotv.android.data.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecentlyRedeemedResponse {

    @SerializedName("results")
    @Expose
    private List<RecentlyRedeemedObject> results;

    @SerializedName("title")
    @Expose
    private String title;

    public String getTitle() {
        return title;
    }

    public List<RecentlyRedeemedObject> getResults() {
        return results;
    }
}
