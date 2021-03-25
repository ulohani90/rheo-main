package com.rheotv.android.data.network.models.postlisting.Requests;

import com.google.gson.annotations.SerializedName;

public class CommentTypeRequestBody {
    @SerializedName("post_id")
    private String postId;

    @SerializedName("username")
    private String username;

    @SerializedName("text")
    private String comment;

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public CommentTypeRequestBody(String postId, String username, String comment) {
        this.postId = postId;
        this.username = username;
        this.comment = comment;
    }
}
