package com.rheotv.android.data.network.models.postlisting.Requests;

import com.google.gson.annotations.SerializedName;

public class PostTypeRequestBody {
    @SerializedName("post_id")
    private String postId;

    @SerializedName("video_file_url")
    private String segmentUrl;

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getSegmentUrl() {
        return segmentUrl;
    }

    public void setSegmentUrl(String segmentUrl) {
        this.segmentUrl = segmentUrl;
    }

    public PostTypeRequestBody(String postId) {
        this.postId = postId;
    }

    public PostTypeRequestBody(String postId, String segmentUrl) {
        this.postId = postId;
        this.segmentUrl = segmentUrl;
    }
}