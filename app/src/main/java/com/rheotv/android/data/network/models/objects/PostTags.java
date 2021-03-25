package com.rheotv.android.data.network.models.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PostTags {

    @SerializedName("results")
    @Expose
    ArrayList<TagResults> tags;

    public ArrayList<TagResults> getTags() {
        return tags;
    }

    public void setTags(ArrayList<TagResults> tags) {
        this.tags = tags;
    }
}
