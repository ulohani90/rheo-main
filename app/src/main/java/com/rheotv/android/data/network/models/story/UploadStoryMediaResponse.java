package com.rheotv.android.data.network.models.story;

import com.google.gson.annotations.SerializedName;

public class UploadStoryMediaResponse {

    @SerializedName("story_id")
    String storyId;

    public String getStoryId() {
        return storyId;
    }
}
