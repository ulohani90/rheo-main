package com.rheotv.android.data.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RecentlyRedeemedObject {

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("profile_pic")
    @Expose
    private String profilePic;

    @SerializedName("coins")
    @Expose
    private String coins;

    public String getUsername() {
        return username;
    }

    public String getCoins() {
        return coins;
    }

    public String getProfilePic() {
        return profilePic;
    }
}
