package com.rheotv.android.data.network.models.useProfile.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecentViewersResponse {

    @SerializedName("results")
    @Expose
    List<RecentViewer> result;

    public List<RecentViewer> getResult() {
        return result;
    }

    public void setResult(List<RecentViewer> result) {
        this.result = result;
    }
}
