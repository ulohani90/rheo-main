package com.rheotv.android.data.network.models.useProfile.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PartnerProgressData {
    @SerializedName("progress")
    @Expose
    private float progress;
    @SerializedName("title")
    @Expose
    private String label1;
    @SerializedName("description")
    @Expose
    private String label2;

    public String getLabel2() {
        return label2;
    }

    public float getProgress() {
        return progress;
    }

    public String getLabel1() {
        return label1;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }
}
