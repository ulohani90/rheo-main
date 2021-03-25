package com.rheotv.android.data.network.models.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginUserResponse {
    @SerializedName("username")
    String userName;

    @Expose
    @SerializedName("user_id")
    int userId;

    @SerializedName("is_new_user")
    @Expose
    boolean isNew;

    @SerializedName("access_token")
    @Expose
    String accessToken;

    @SerializedName("extra_info")
    @Expose
    String extraInfo;

    @SerializedName("is_streamer")
    @Expose
    boolean isStreamer;

    @SerializedName("author_id")
    @Expose
    String authorId;

    @SerializedName("profile_pic")
    @Expose
    String profileUrl;

    @SerializedName("languages")
    @Expose
    List languages;

    @SerializedName("is_content_moderator")
    @Expose
    boolean isContentModerator;

    public boolean isContentModerator() {
        return isContentModerator;
    }

    public void setContentModerator(boolean contentModerator) {
        isContentModerator = contentModerator;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public List getLanguages() {
        return languages;
    }

    public boolean getIsStreamer() {
        return isStreamer;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

}
