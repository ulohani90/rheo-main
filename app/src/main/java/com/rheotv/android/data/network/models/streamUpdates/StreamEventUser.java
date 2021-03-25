package com.rheotv.android.data.network.models.streamUpdates;

import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.postlisting.responses.User;

public class StreamEventUser {

    @SerializedName("bio")
    String bio;

    @SerializedName("followers_count")
    long followersCount;

    @SerializedName("is_verified")
    boolean isVerified;

    @SerializedName("profile_pic")
    String profilePic;

    @SerializedName("cover_pic")
    String coverPic;

    @SerializedName("user")
    private User user;

    public String getBio() {
        return bio;
    }

    public long getFollowersCount() {
        return followersCount;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public String getCoverPic() {
        return coverPic;
    }

    public User getUser() {
        return user;
    }
}
