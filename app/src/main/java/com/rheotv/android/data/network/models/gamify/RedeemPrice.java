package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

public class RedeemPrice {

    @SerializedName("amount")
    private int amount;

    @SerializedName("currency")
    private String currency;

    @SerializedName("discount")
    private String discount;

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public String getAmountString() {
        return amount + "";
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    @Override
    public String toString() {
        return "Price{" +
                "amount = '" + amount + '\'' +
                ",currency = '" + currency + '\'' +
                ",discount = '" + discount + '\'' +
                "}";
    }
}