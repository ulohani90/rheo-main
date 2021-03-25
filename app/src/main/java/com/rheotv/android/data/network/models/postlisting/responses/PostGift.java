package com.rheotv.android.data.network.models.postlisting.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostGift {

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("user")
    @Expose
    int user;

    @SerializedName("post")
    @Expose
    String postId;

    @SerializedName("type")
    @Expose
    String type;

    @SerializedName("text")
    @Expose
    String message;

    @SerializedName("start_time")
    @Expose
    String startTimeTs;

    @SerializedName("end_time")
    @Expose
    String endTimeTs;

    @SerializedName("username")
    @Expose
    String username;

    @SerializedName("profile_pic")
    @Expose
    String profilePic;

    @SerializedName("background_tint_color")
    @Expose
    String backgroundTintColor;

    public String getBackgroundTintColor() {
        return backgroundTintColor;
    }

    public void setBackgroundTintColor(String backgroundTintColor) {
        this.backgroundTintColor = backgroundTintColor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStartTimeTs() {
        return startTimeTs;
    }

    public void setStartTimeTs(String startTimeTs) {
        this.startTimeTs = startTimeTs;
    }

    public String getEndTimeTs() {
        return endTimeTs;
    }

    public void setEndTimeTs(String endTimeTs) {
        this.endTimeTs = endTimeTs;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePic() {
        return profilePic;
    }
}
