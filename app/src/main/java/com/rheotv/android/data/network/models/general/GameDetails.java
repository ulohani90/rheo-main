package com.rheotv.android.data.network.models.general;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.useProfile.responses.GameWiseUser;

public class GameDetails {

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("thumbnail")
    @Expose
    String thumbnail;

    @SerializedName("accept_play_request")
    @Expose
    boolean acceptPlayRequest;

    @SerializedName("enable_custom_room")
    @Expose
    boolean customRoomEnabled;

    @SerializedName("game_user_name")
    @Expose
    String gameUsername;

    @SerializedName("action")
    @Expose
    String userAction;

    public GameDetails(String id, String name, String thumbnail, boolean acceptPlayRequest, boolean customRoomEnabled) {
        this.id = id;
        this.name = name;
        this.thumbnail = thumbnail;
        this.acceptPlayRequest = acceptPlayRequest;
        this.customRoomEnabled = customRoomEnabled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean canAcceptPlayRequest() {
        return acceptPlayRequest;
    }

    public void setAcceptPlayRequest(boolean acceptPlayRequest) {
        this.acceptPlayRequest = acceptPlayRequest;
    }

    public boolean isCustomRoomEnabled() {
        return customRoomEnabled;
    }

    public String getGameUsername() {
        return gameUsername;
    }

    public void setGameUsername(String gameUsername) {
        this.gameUsername = gameUsername;
    }

    public String getUserAction() {
        return userAction;
    }

    public void setUserAction(String userAction) {
        this.userAction = userAction;
    }
}
