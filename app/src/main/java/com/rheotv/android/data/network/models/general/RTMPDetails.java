package com.rheotv.android.data.network.models.general;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RTMPDetails {
    @SerializedName("key")
    @Expose
    String key;

    @SerializedName("base_url")
    @Expose
    String base_url;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBase_url() {
        return base_url;
    }

    public void setBase_url(String base_url) {
        this.base_url = base_url;
    }
}
