package com.rheotv.android.data.network.models.story;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Story {

    @SerializedName("profile_pic")
    @Expose
    String profilePic;

    public Story(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getProfilePic() {
        return profilePic;
    }
}
