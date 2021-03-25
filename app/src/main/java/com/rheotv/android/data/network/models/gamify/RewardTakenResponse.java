package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RewardTakenResponse {
    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("coins_won")
    @Expose
    private String coinWon;

    public RewardTakenResponse() {
    }

    public RewardTakenResponse(String message, String coinWon) {
        this.message = message;
        this.coinWon = coinWon;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCoinWon() {
        return coinWon;
    }

    public void setCoinWon(String coinWon) {
        this.coinWon = coinWon;
    }

    public boolean isSuccessful() {
        return message.equalsIgnoreCase("Success");
    }
}
