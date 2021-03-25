package com.rheotv.android.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RewardCustomWebviewApiResponse {

    @SerializedName("result")
    @Expose
    RewardCustomWebviewResponse result;

    public RewardCustomWebviewResponse getResult() {
        return result;
    }

    public void setResult(RewardCustomWebviewResponse result) {
        this.result = result;
    }
}
