package com.rheotv.android.data.network.models.story;

import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.story.model.Story;

import java.util.ArrayList;

public class StoryResponse {

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("author_data")
    ProfileResult profileResult;

    @SerializedName("results")
    ArrayList<Story> stories;

    public void setCount(int count) {
        this.count = count;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public void setStories(ArrayList<Story> stories) {
        this.stories = stories;
    }

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public ArrayList<Story> getStories() {
        return stories;
    }

    public ProfileResult getProfileResult() {
        return profileResult;
    }
}
