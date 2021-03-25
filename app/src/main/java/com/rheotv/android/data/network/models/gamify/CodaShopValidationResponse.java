package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

public class CodaShopValidationResponse {

    @SerializedName("result")
    private ValidationResult result;

    @SerializedName("error")
    private CodaValidationError error;

    @SerializedName("id")
    private String id;

    @SerializedName("jsonrpc")
    private String jsonrpc;

    public void setResult(ValidationResult result) {
        this.result = result;
    }

    public ValidationResult getResult() {
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

    public CodaValidationError getError() {
        return error;
    }

    public void setError(CodaValidationError error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "CodaShopValidationResponse{" +
                "result = '" + result + '\'' +
                "error = '" + error + '\'' +
                ",id = '" + id + '\'' +
                ",jsonrpc = '" + jsonrpc + '\'' +
                "}";
    }
}