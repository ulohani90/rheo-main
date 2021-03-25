package com.rheotv.android.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RewardCustomWebviewResponse {

    @SerializedName("custom_url")
    @Expose
    private String url;

    @SerializedName("title")
    @Expose
    private String title;

    public RewardCustomWebviewResponse(String url, String title) {

    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
