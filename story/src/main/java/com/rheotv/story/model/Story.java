package com.rheotv.story.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.rheotv.story.Constants;

import java.util.ArrayList;

public class Story implements Parcelable {

    @SerializedName("id")
    private String id;

    @SerializedName("file")
    private String url;

    @SerializedName("thumbnail")
    private String placeholderThumbnail;

    @SerializedName("duration")
    private int duration;

    @SerializedName("file_type")
    private String type;

    @SerializedName("state")
    private String state;

    @SerializedName("watched")
    private boolean watched;

    @SerializedName("author")
    private String author;

    @SerializedName("expire_at")
    private String expireAt;

    @SerializedName("created_at")
    private long createdAt;

    @SerializedName("watch_count")
    private int watchCount;

    @SerializedName("cta")
    private ArrayList<StoryCTA> storyCTAS = new ArrayList<>();

    @SerializedName("type_meta")
    private String metaData;

    @SuppressWarnings("unchecked")
    protected Story(Parcel in) {
        id = in.readString();
        url = in.readString();
        duration = in.readInt();
        type = in.readString();
        state = in.readString();
        watched = in.readByte() != 0;
        author = in.readString();
        expireAt = in.readString();
        createdAt = in.readLong();
        storyCTAS = in.readArrayList(StoryCTA.class.getClassLoader());
        placeholderThumbnail = in.readString();
        watchCount = in.readInt();
        metaData = in.readString();
    }

    public static final Creator<Story> CREATOR = new Creator<Story>() {
        @Override
        public Story createFromParcel(Parcel in) {
            return new Story(in);
        }

        @Override
        public Story[] newArray(int size) {
            return new Story[size];
        }
    };

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public int getDuration() {
        return duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getPlaceholderThumbnail() {
        return placeholderThumbnail;
    }

    public String getAuthor() {
        return author;
    }

    public ArrayList<StoryCTA> getStoryCTAS() {
        return storyCTAS;
    }

    public void setStoryCTAS(ArrayList<StoryCTA> storyCTAS) {
        this.storyCTAS = storyCTAS;
    }

    public Story(String type) {
        this.type = type;
    }

    public int getWatchCount() {
        return watchCount;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public Story(String url, String type) {
        this.url = url;
        this.type = type;
    }

    public Story(String url, int duration, String type) {
        this.url = url;
        this.duration = duration;
        this.type = type;
    }

    public Story(String url, int duration, String type, boolean watched) {
        this.url = url;
        this.duration = duration;
        this.type = type;
        this.watched = watched;
    }

    public Story(String id, String url, int duration, String type) {
        this.id = id;
        this.url = url;
        this.duration = duration;
        this.type = type;
    }

    public Story(String url, int duration, String type, boolean watched, long createdAt) {
        this.url = url;
        this.duration = duration;
        this.type = type;
        this.watched = watched;
        this.createdAt = createdAt;
    }

    public Story(String id, String url, int duration, String type, long createdAt) {
        this.id = id;
        this.url = url;
        this.duration = duration;
        this.type = type;
        this.createdAt = createdAt;
    }

    public Story(String id, String url, int duration, String type, String state, boolean watched, String author, String expireAt, long createdAt, String metaData) {
        this.id = id;
        this.url = url;
        this.duration = duration;
        this.type = type;
        this.state = state;
        this.watched = watched;
        this.author = author;
        this.expireAt = expireAt;
        this.createdAt = createdAt;
        this.metaData = metaData;
    }

    public void addLoveCTA() {
        if (storyCTAS.isEmpty())
            storyCTAS = new ArrayList<>();
        storyCTAS.add(new StoryCTA(Constants.PLAY_REQUEST_INTERESTED_CTA, new StoryCTAData()));
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(url);
        dest.writeInt(duration);
        dest.writeString(type);
        dest.writeString(state);
        dest.writeByte((byte) (watched ? 1 : 0));
        dest.writeString(author);
        dest.writeString(expireAt);
        dest.writeLong(createdAt);
        dest.writeList(storyCTAS);
        dest.writeString(placeholderThumbnail);
        dest.writeInt(watchCount);
        dest.writeString(metaData);
    }

    public static class TextStory {
        private String text;
        private String backgroundColor;
        private String attach_file_type;

        public TextStory(String text, String backgroundColor, String attach_file_type) {
            this.text = text;
            this.backgroundColor = backgroundColor;
            this.attach_file_type = attach_file_type;
        }

        public TextStory(String text, String backgroundColor) {
            this.text = text;
            this.backgroundColor = backgroundColor;
        }

        public TextStory() {
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public String getAttachFileType() {
            return attach_file_type;
        }

        public void setAttachFileType(String attach_file_type) {
            this.attach_file_type = attach_file_type;
        }
    }
}
