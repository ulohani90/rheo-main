package com.rheotv.android.data.network.models.postlisting.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InvoiceAPIResponse {
    @SerializedName("results")
    @Expose
    List<InvoiceResponse> invoiceResponseList;

    public List<InvoiceResponse> getInvoiceResponseList() {
        return invoiceResponseList;
    }

    public void setInvoiceResponseList(List<InvoiceResponse> invoiceResponseList) {
        this.invoiceResponseList = invoiceResponseList;
    }
}
