package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.common.Status;

import java.util.List;

public class DailyRewardsResponse {

    @SerializedName("milestones")
    @Expose
    List<Reward> results;

    @SerializedName("status")
    @Expose
    private Status status;

    @SerializedName("total_coins")
    @Expose
    private String totalCoins;

    @SerializedName("can_give_feedback")
    @Expose
    private boolean canGiveFeedback;

    @SerializedName("coda_enabled")
    @Expose
    private boolean codaEnabled;

    public List<Reward> getResults() {
        return results;
    }

    public void setResults(List<Reward> results) {
        this.results = results;
    }

    public String getTotalCoins() {
        return totalCoins;
    }

    public void setTotalCoins(String totalCoins) {
        this.totalCoins = totalCoins;
    }

    public boolean getCanGiveFeedback() {
        return canGiveFeedback;
    }

    public void setCanGiveFeedback(boolean canGiveFeedback) {
        this.canGiveFeedback = canGiveFeedback;
    }

    public boolean isCodaEnabled() {
        return codaEnabled;
    }

    public void codaEnabled(boolean codaEnabled) {
        this.codaEnabled = codaEnabled;
    }
}