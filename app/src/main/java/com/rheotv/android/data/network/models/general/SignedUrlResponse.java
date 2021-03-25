package com.rheotv.android.data.network.models.general;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SignedUrlResponse {

    @SerializedName("upload_url")
    @Expose
    private String uploadUrl;

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

}