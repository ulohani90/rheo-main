package com.rheotv.android.data.network.models.stickers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sticker {

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("sticker_url")
    @Expose
    String stickerUrl;

    @SerializedName("value")
    @Expose
    int value;

    @SerializedName("type")
    @Expose
    String type;

    @SerializedName("duration")
    @Expose
    long duration;

    @SerializedName("title")
    @Expose
    String title;

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public long getDuration() {
        return duration;
    }

    public String getId() {
        return id;
    }

    public String getStickerUrl() {
        return stickerUrl;
    }

    public int getValue() {
        return value;
    }
}
