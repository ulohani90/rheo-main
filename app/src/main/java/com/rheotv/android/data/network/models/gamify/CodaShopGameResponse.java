package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CodaShopGameResponse {

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("count")
    private int count;

    @SerializedName("results")
    private List<CodaShopGame> results;

    public void setNext(String next) {
        this.next = next;
    }

    public String getNext() {
        return next;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getPrevious() {
        return previous;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setResults(List<CodaShopGame> results) {
        this.results = results;
    }

    public List<CodaShopGame> getResults() {
        return results;
    }

    @Override
    public String toString() {
        return "CodaShopGameResponse{" +
                "next = '" + next + '\'' +
                ",previous = '" + previous + '\'' +
                ",count = '" + count + '\'' +
                ",results = '" + results + '\'' +
                "}";
    }
}