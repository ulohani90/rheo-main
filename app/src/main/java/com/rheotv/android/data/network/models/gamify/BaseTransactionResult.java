package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BaseTransactionResult {

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("status")
    private String status;

    @SerializedName("game_redeem_url")
    private String redeemUrl;

    @SerializedName("items")
    private ArrayList<VoucherItem> items;

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setItems(ArrayList<VoucherItem> items){
        this.items = items;
    }

    public ArrayList<VoucherItem> getItems(){
        return items;
    }

    public String getRedeemUrl() {
        return redeemUrl;
    }

    public void setRedeemUrl(String redeemUrl) {
        this.redeemUrl = redeemUrl;
    }

    @Override
    public String toString() {
        return "TopupResult{" +
                "orderId = '" + orderId + '\'' +
                ",status = '" + status + '\'' +
                ",items = '" + items + '\'' +
                ",redeemUrl = '" + redeemUrl + '\'' +
                "}";
    }
}