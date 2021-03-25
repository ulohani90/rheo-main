package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

public class CodaVoucherRequest {
    @SerializedName("coins")
    int coins;

    @SerializedName("params")
    CodeVoucherParamItem params;

    public CodaVoucherRequest(int coins, CodeVoucherParamItem params) {
        this.coins = coins;
        this.params = params;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public CodeVoucherParamItem getParams() {
        return params;
    }

    public void setParams(CodeVoucherParamItem params) {
        this.params = params;
    }
}
