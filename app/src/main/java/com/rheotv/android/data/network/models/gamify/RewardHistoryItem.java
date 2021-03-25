package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

public class RewardHistoryItem {

    @SerializedName("log_type")
    private String logType;

    @SerializedName("coins")
    private int coins;

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("username")
    private String username;

    @SerializedName("logged_on")
    private String completedOn;

    @SerializedName("meta")
    private RewardMeta rewardMeta;

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getLogType() {
        return logType;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getCoins() {
        return coins;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(String completedOn) {
        this.completedOn = completedOn;
    }

    public RewardMeta getRewardMeta() {
        return rewardMeta;
    }

    public void setRewardMeta(RewardMeta rewardMeta) {
        this.rewardMeta = rewardMeta;
    }

    @Override
    public String toString() {
        return "ResultsItem{" +
                "log_type = '" + logType + '\'' +
                ",coins = '" + coins + '\'' +
                ",id = '" + id + '\'' +
                ",title = '" + title + '\'' +
                ",username = '" + username + '\'' +
                ",completedOn = '" + completedOn + '\'' +
                "}";
    }
}