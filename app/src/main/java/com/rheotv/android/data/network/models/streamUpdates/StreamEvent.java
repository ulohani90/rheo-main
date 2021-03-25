package com.rheotv.android.data.network.models.streamUpdates;

import com.google.gson.annotations.SerializedName;

public class StreamEvent {

    @SerializedName("profile_pic")
    private String profilePic;

    @SerializedName("username")
    private String username;

    @SerializedName("from_user_profile")
    private StreamEventUser userProfile;

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public StreamEvent(String profilePic, String username) {
        this.profilePic = profilePic;
        this.username = username;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public StreamEventUser getUserProfile() {
        return userProfile;
    }

    @Override
    public String toString() {
        return "ResultItem{" +
                "profile_pic = '" + profilePic + '\'' +
                ",username = '" + username + '\'' +
                "}";
    }

}