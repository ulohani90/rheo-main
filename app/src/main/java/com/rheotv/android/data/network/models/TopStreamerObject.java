package com.rheotv.android.data.network.models;

import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.postlisting.responses.User;

import java.util.List;

public class TopStreamerObject {

    @SerializedName("user")
    User user;

    @SerializedName("profile_pic")
    String profilePic;

    List<String> tags;

    public User getUser() {
        return user;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getCommaSeparatedTags() {
        if (tags == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String tag : tags) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(tag);
        }

        return builder.toString();
    }
}

