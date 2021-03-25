package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.common.Status;

import java.util.ArrayList;
import java.util.List;

public class Rewards {
    @SerializedName("next")
    @Expose
    String next;

    @SerializedName("user_reward_plans_history")
    @Expose
    List<Reward> results = new ArrayList<>();

    @SerializedName("previous")
    @Expose
    String previous;

    @SerializedName("status")
    @Expose
    private Status status;

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public List<Reward> getResults() {
        return results;
    }

    public void setResults(List<Reward> results) {
        this.results = results;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }
}
