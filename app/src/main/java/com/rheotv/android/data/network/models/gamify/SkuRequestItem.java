package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

public class SkuRequestItem {
    @SerializedName("sku")
    private String sku;

    @SerializedName("quantity")
    private int quantity;

    public SkuRequestItem(String sku, int quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
