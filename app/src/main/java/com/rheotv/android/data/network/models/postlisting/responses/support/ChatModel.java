
package com.rheotv.android.data.network.models.postlisting.responses.support;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChatModel {

    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("support_executive")
    @Expose
    private SupportExecutive supportExecutive;
    @SerializedName("message")
    @Expose
    private String message;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SupportExecutive getSupportExecutive() {
        return supportExecutive;
    }

    public void setSupportExecutive(SupportExecutive supportExecutive) {
        this.supportExecutive = supportExecutive;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
