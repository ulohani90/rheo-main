package com.rheotv.android.data.network.models.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VideoListingResponse {

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
    List<PostObject> results;

    @SerializedName("slot_banner_details")
    @Expose
    private SlotEventData slotBannerDetails;

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

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public List<PostObject> getResults() {
        return results;
    }

    public void setResults(List<PostObject> results) {
        this.results = results;
    }

    public SlotEventData getSlotBannerDetails() {
        return slotBannerDetails;
    }

    public void setSlotBannerDetails(SlotEventData slotBannerDetails) {
        this.slotBannerDetails = slotBannerDetails;
    }
}
