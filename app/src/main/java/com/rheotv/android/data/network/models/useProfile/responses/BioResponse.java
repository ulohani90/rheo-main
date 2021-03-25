package com.rheotv.android.data.network.models.useProfile.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BioResponse {
    @SerializedName("bio")
    @Expose
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
