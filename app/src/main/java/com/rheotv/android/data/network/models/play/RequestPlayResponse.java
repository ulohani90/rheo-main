package com.rheotv.android.data.network.models.play;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RequestPlayResponse {

    @SerializedName("next")
    private String next;

    @SerializedName("can_request_to_play")
    private boolean isAllowedToRequest;

    @SerializedName("custom_room_enabled")
    private boolean isCustomRoomEnabled;

    @SerializedName("previous")
    private String previous;

    @SerializedName("coins_required")
    private String coinsRequired;

    @SerializedName("count")
    private int count;

    @SerializedName("waiting_number")
    private String waitingNumber;

    @SerializedName("game_username")
    private String gamerUserName;

    @SerializedName("results")
    private List<ResultsItem> results = new ArrayList<>();

    @SerializedName("custom_room_username")
    private String customRoomUsername;

    @SerializedName("custom_room_password")
    private String customRoomPassword;

    @SerializedName("custom_room_winner_username")
    private String customRoomWinnerUsername;

    @SerializedName("user_play_request")
    private ResultsItem selfPlayRequest;


    public void setNext(String next) {
        this.next = next;
    }

    public String getNext() {
        return next;
    }

    public void setIsAllowedToRequest(boolean isAllowedToRequest) {
        this.isAllowedToRequest = isAllowedToRequest;
    }

    public boolean isIsAllowedToRequest() {
        return isAllowedToRequest;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getPrevious() {
        return previous;
    }

    public void setCoinsRequired(String coinsRequired) {
        this.coinsRequired = coinsRequired;
    }

    public String getCoinsRequired() {
        return coinsRequired;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setResults(ArrayList<ResultsItem> results) {
        this.results = results;
    }

    public List<ResultsItem> getResults() {
        return results;
    }

    public String getWaitingNumber() {
        return waitingNumber;
    }

    public void setWaitingNumber(String waitingNumber) {
        this.waitingNumber = waitingNumber;
    }

    public String getGamerUserName() {
        return gamerUserName;
    }

    public void setGamerUserName(String gamerUserName) {
        this.gamerUserName = gamerUserName;
    }

    public boolean isCustomRoomEnabled() {
        return isCustomRoomEnabled;
        //return true;
    }

    public String getCustomRoomWinnerUsername() {
        return customRoomWinnerUsername;
    }


    public void setCustomRoomEnabled(boolean customRoomEnabled) {
        isCustomRoomEnabled = customRoomEnabled;
    }

    public String getCustomRoomPassword() {
        return customRoomPassword;
    }

    public String getCustomRoomUsername() {
        return customRoomUsername;
    }

    public ResultsItem getSelfPlayRequest() {
        return selfPlayRequest;
    }

    public void setSelfPlayRequest(ResultsItem selfPlayRequest) {
        this.selfPlayRequest = selfPlayRequest;
    }

    @Override
    public String toString() {
        return
                "RequestPlayResponse{" +
                        "next = '" + next + '\'' +
                        ",is_allowed_to_request = '" + isAllowedToRequest + '\'' +
                        ",previous = '" + previous + '\'' +
                        ",coins_required = '" + coinsRequired + '\'' +
                        ",count = '" + count + '\'' +
                        ",results = '" + results + '\'' +
                        "}";
    }
}