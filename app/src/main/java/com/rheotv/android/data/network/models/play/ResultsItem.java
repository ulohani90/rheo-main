package com.rheotv.android.data.network.models.play;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class ResultsItem implements Parcelable {

    @SerializedName("from_user_profile")
    private FromUserProfile fromUserProfile;

    @SerializedName("to_post")
    private String toPost;

    @SerializedName("index")
    private int index;

    @SerializedName("id")
    private String id;

    @SerializedName("state")
    private String state;

    @SerializedName("game_username")
    private String gameUsername;

    private String type;

    @SerializedName("message")
    private String message;

    @SerializedName("is_winner")
    private boolean isWinner;

    public ResultsItem() {
    }

    protected ResultsItem(Parcel in) {
        fromUserProfile = in.readParcelable(FromUserProfile.class.getClassLoader());
        toPost = in.readString();
        index = in.readInt();
        id = in.readString();
        state = in.readString();
        gameUsername = in.readString();
        type = in.readString();
        message = in.readString();
        isWinner = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(fromUserProfile, flags);
        dest.writeString(toPost);
        dest.writeInt(index);
        dest.writeString(id);
        dest.writeString(state);
        dest.writeString(gameUsername);
        dest.writeString(type);
        dest.writeString(message);
        dest.writeByte((byte) (isWinner ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ResultsItem> CREATOR = new Creator<ResultsItem>() {
        @Override
        public ResultsItem createFromParcel(Parcel in) {
            return new ResultsItem(in);
        }

        @Override
        public ResultsItem[] newArray(int size) {
            return new ResultsItem[size];
        }
    };

    public void setFromUserProfile(FromUserProfile fromUserProfile) {
        this.fromUserProfile = fromUserProfile;
    }

    public FromUserProfile getFromUserProfile() {
        return fromUserProfile;
    }

    public void setToPost(String toPost) {
        this.toPost = toPost;
    }

    public String getToPost() {
        return toPost;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setGameUsername(String gameUsername) {
        this.gameUsername = gameUsername;
    }

    public String getGameUsername() {
        return gameUsername;
    }

    public String getType() {
        return type;
    }

    public ResultsItem setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultsItem that = (ResultsItem) o;
        return index == that.index &&
                isWinner == that.isWinner &&
                Objects.equals(fromUserProfile, that.fromUserProfile) &&
                Objects.equals(toPost, that.toPost) &&
                Objects.equals(id, that.id) &&
                Objects.equals(state, that.state) &&
                Objects.equals(gameUsername, that.gameUsername) &&
                Objects.equals(type, that.type) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromUserProfile, toPost, index, id, state, gameUsername, type, message, isWinner);
    }

    @Override
    public String toString() {
        return "ResultsItem{" +
                "from_user_profile = '" + fromUserProfile + '\'' +
                ",to_post = '" + toPost + '\'' +
                ",index = '" + index + '\'' +
                ",id = '" + id + '\'' +
                ",state = '" + state + '\'' +
                ",game_username = '" + gameUsername + '\'' +
                ",id = '" + id + '\'' + message +
                "}";
    }
}