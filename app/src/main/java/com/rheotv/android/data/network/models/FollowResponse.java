package com.rheotv.android.data.network.models;

import com.google.gson.annotations.SerializedName;

public class FollowResponse {
    public boolean isFollow() {
        return isFollow;
    }

    public void setFollow(boolean follow) {
        isFollow = follow;
    }

    @SerializedName("is_follow")
    boolean isFollow;

    @SerializedName("can_comment")
    boolean canComment;

    public boolean isCanComment() {
        return canComment;
    }

    public void setCanComment(boolean canComment) {
        this.canComment = canComment;
    }
}
