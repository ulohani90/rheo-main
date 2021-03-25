package com.rheotv.android.data.network.models.postlisting.responses;

import com.google.gson.annotations.SerializedName;

public class InvoiceResponse {
    @SerializedName("month")
    String month;
    @SerializedName("amount")
    String amount;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
