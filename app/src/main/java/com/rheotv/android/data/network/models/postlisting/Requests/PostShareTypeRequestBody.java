package com.rheotv.android.data.network.models.postlisting.Requests;

import com.google.gson.annotations.SerializedName;

public class PostShareTypeRequestBody {
    @SerializedName("post_id")
    private String postId;

    @SerializedName("source")
    private int source;

    public PostShareTypeRequestBody(String postId, int source) {
        this.postId = postId;
        this.source = source;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
