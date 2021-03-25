package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CodeVoucherParamItem {
    @SerializedName("items")
    private List<SkuRequestItem> skus;

    public CodeVoucherParamItem(List<SkuRequestItem> skus) {
        this.skus = skus;
    }

    public List<SkuRequestItem> getSkus() {
        return skus;
    }

    public void setSkus(List<SkuRequestItem> skus) {
        this.skus = skus;
    }
}
