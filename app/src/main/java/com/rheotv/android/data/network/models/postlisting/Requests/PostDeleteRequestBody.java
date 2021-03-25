package com.rheotv.android.data.network.models.postlisting.Requests;

import com.google.gson.annotations.SerializedName;

public class PostDeleteRequestBody {
    @SerializedName("post_id")
    String postId;

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public PostDeleteRequestBody(String postId) {
        this.postId = postId;
    }
}
