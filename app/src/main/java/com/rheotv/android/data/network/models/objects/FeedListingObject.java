package com.rheotv.android.data.network.models.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeedListingObject {

    @SerializedName("count")
    @Expose
    int count;

    @SerializedName("next")
    @Expose
    String next;

    @SerializedName("previous")
    @Expose
    String previous;

    @SerializedName("tags")
    @Expose
    List<PostTags> tags;

    @SerializedName("results")
    @Expose
    List<FeedObject> results;

    @SerializedName("enable_clips")
    @Expose
    boolean enableClips;

    @SerializedName("show_go_live")
    @Expose
    boolean showGoLive;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public List<FeedObject> getResults() {
        return results;
    }

    public void setResults(List<FeedObject> results) {
        this.results = results;
    }

    public boolean isEnableClips() {
        return enableClips;
    }

    public void setEnableClips(boolean enableClips) {
        this.enableClips = enableClips;
    }

    public boolean isShowGoLive() {
        return showGoLive;
    }

    public void setShowGoLive(boolean showGoLive) {
        this.showGoLive = showGoLive;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getPrevious() {
        return previous;
    }

    public List<PostTags> getTags() {
        return tags;
    }
}
