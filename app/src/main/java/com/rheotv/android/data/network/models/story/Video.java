package com.rheotv.android.data.network.models.story;

import android.os.Parcel;
import android.os.Parcelable;

public class Video implements Media, Parcelable {

    String id;

    String uri;

    public Video(String uri) {
        this.uri = uri;
    }

    protected Video(Parcel in) {
        uri = in.readString();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    @Override
    public int mimeType() {
        return Media.VIDEO;
    }

    @Override
    public String path() {
        return uri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uri);
    }

    @Override
    public String mediaId() {
        return id;
    }

    @Override
    public void setMediaId(String id) {
        this.id = id;
    }
}
