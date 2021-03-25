package com.rheotv.android.data.network.models.postlisting.responses;

import com.google.gson.annotations.SerializedName;

public class Invoice {
    @SerializedName("month")
    private String title;
    @SerializedName("amount")
    private String amount;

    public String getAmount() {
        return "Money Credit ₹" + amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
