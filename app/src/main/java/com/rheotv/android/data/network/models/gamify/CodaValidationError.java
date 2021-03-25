package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

public class CodaValidationError {

    @SerializedName("message")
    String message;

    @SerializedName("code")
    int code;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
