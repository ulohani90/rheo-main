package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

public class SkusItem {

    @SerializedName("price")
    private RedeemPrice price;

    @SerializedName("description")
    private String description;

    @SerializedName("sku")
    private String sku;

    @SerializedName("reward_icon")
    private String thumbnail;

    public void setPrice(RedeemPrice price) {
        this.price = price;
    }

    public RedeemPrice getPrice() {
        return price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        return "SkusItem{" +
                "price = '" + price + '\'' +
                ",description = '" + description + '\'' +
                ",sku = '" + sku + '\'' +
                ",thumbnail = '" + thumbnail + '\'' +
                "}";
    }
}