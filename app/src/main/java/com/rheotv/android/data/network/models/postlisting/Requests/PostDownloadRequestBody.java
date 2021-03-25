package com.rheotv.android.data.network.models.postlisting.Requests;

import com.google.gson.annotations.SerializedName;

public class PostDownloadRequestBody {
    @SerializedName("post_id")
    String postId;

    @SerializedName("resolution")
    String resolution;

    public String getPostId() {
        return postId;
    }

    public String getResolution() {
        return resolution;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public PostDownloadRequestBody(String postId, String resolution) {
        this.postId = postId;
        this.resolution = resolution;
    }
}
