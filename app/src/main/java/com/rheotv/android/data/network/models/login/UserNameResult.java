package com.rheotv.android.data.network.models.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserNameResult {
    @SerializedName("username")
    String userName;

    @Expose
    @SerializedName("created")
    boolean created;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isUserCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

}
