package com.rheotv.android.data.network.models.gamify;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class RewardMeta implements Parcelable {

    @SerializedName("player_id")
    private String playerId;

    @SerializedName("type")
    private String rewardType;

    @SerializedName("order_id")
    private String transactionId;

    @SerializedName("voucher")
    private String voucherCode;

    @SerializedName("game_icon")
    private String thumbnail;

    @SerializedName("coins")
    private int coins;

    @SerializedName("date")
    private String date;

    @SerializedName("game_name")
    private String game;

    @SerializedName("game_currency")
    private String currency;

    @SerializedName("game_redeem_url")
    private String redeemUrl;

    public RewardMeta() {
    }

    public RewardMeta(String playerId, String rewardType, String transactionId, String voucherCode, String thumbnail, int coins, String date, String game, String currency, String redeemUrl) {
        this.playerId = playerId;
        this.rewardType = rewardType;
        this.transactionId = transactionId;
        this.voucherCode = voucherCode;
        this.thumbnail = thumbnail;
        this.coins = coins;
        this.date = date;
        this.game = game;
        this.currency = currency;
        this.redeemUrl = redeemUrl;
    }

    protected RewardMeta(Parcel in) {
        playerId = in.readString();
        rewardType = in.readString();
        transactionId = in.readString();
        voucherCode = in.readString();
        thumbnail = in.readString();
        coins = in.readInt();
        date = in.readString();
        game = in.readString();
        currency = in.readString();
        redeemUrl = in.readString();
    }

    public static final Creator<RewardMeta> CREATOR = new Creator<RewardMeta>() {
        @Override
        public RewardMeta createFromParcel(Parcel in) {
            return new RewardMeta(in);
        }

        @Override
        public RewardMeta[] newArray(int size) {
            return new RewardMeta[size];
        }
    };

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRedeemUrl() {
        return redeemUrl;
    }

    public void setRedeemUrl(String redeemUrl) {
        this.redeemUrl = redeemUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(playerId);
        parcel.writeString(rewardType);
        parcel.writeString(transactionId);
        parcel.writeString(voucherCode);
        parcel.writeString(thumbnail);
        parcel.writeInt(coins);
        parcel.writeString(date);
        parcel.writeString(game);
        parcel.writeString(currency);
        parcel.writeString(redeemUrl);
    }
}
