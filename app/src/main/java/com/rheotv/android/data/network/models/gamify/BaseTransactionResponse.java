package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

public class BaseTransactionResponse {

    @SerializedName("result")
    private BaseTransactionResult result;

    @SerializedName("id")
    private String id;

    @SerializedName("jsonrpc")
    private String jsonrpc;

    public void setTopupResult(BaseTransactionResult topupResult) {
        this.result = topupResult;
    }

    public BaseTransactionResult getResult() {
        return result;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    @Override
    public String toString() {
        return "TopupResponse{" +
                "result = '" + result + '\'' +
                ",id = '" + id + '\'' +
                ",jsonrpc = '" + jsonrpc + '\'' +
                "}";
    }
}