package com.rheotv.android.data.network.models.useProfile.responses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.postlisting.responses.User;

public class PictureUploadResult implements Parcelable {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("url")
    @Expose
    private String url;

    protected PictureUploadResult(Parcel in) {
        id = in.readString();
        url = in.readString();
    }
    public String getUrl() {
        return url;
    }
    public String getId() {
        return id;
    }

    public static final Creator<PictureUploadResult> CREATOR = new Creator<PictureUploadResult>() {
        @Override
        public PictureUploadResult createFromParcel(Parcel in) {
            return new PictureUploadResult(in);
        }

        @Override
        public PictureUploadResult[] newArray(int size) {
            return new PictureUploadResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(url);
    }
}
