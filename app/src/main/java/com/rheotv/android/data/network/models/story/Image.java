package com.rheotv.android.data.network.models.story;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Media, Parcelable {

    String id;

    String uri;

    public Image(String uri) {
        this.uri = uri;
    }

    protected Image(Parcel in) {
        uri = in.readString();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    @Override
    public String mediaId() {
        return id;
    }

    @Override
    public void setMediaId(String id) {
        this.id = id;
    }

    @Override
    public int mimeType() {
        return Media.IMAGE;
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
}
