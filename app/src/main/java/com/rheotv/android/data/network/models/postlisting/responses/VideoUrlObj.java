package com.rheotv.android.data.network.models.postlisting.responses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VideoUrlObj implements Parcelable {

    @SerializedName("network_type")
    @Expose
    private String networkType;

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("name")
    @Expose
    private String name;

    public VideoUrlObj(String networkType, String url, String name) {
        this.networkType = networkType;
        this.url = url;
        this.name = name;
    }

    protected VideoUrlObj(Parcel in) {
        networkType = in.readString();
        url = in.readString();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(networkType);
        dest.writeString(url);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoUrlObj> CREATOR = new Creator<VideoUrlObj>() {
        @Override
        public VideoUrlObj createFromParcel(Parcel in) {
            return new VideoUrlObj(in);
        }

        @Override
        public VideoUrlObj[] newArray(int size) {
            return new VideoUrlObj[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
