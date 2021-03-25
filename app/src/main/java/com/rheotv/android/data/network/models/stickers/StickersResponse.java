package com.rheotv.android.data.network.models.stickers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StickersResponse {

    @SerializedName("count")
    @Expose
    int count;


    @SerializedName("next")
    @Expose
    String next;


    @SerializedName("previous")
    @Expose
    String previous;

    @SerializedName("results")
    @Expose
    List<Sticker> results;


    public int getCount() {
        return count;
    }

    public String getPrevious() {
        return previous;
    }

    public List<Sticker> getResults() {
        return results;
    }

    public String getNext() {
        return next;
    }
}
