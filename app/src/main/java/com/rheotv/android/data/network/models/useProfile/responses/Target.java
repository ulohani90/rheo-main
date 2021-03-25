package com.rheotv.android.data.network.models.useProfile.responses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Target implements Parcelable {
    @SerializedName("title")
    @Expose
    String title;

    @SerializedName("data")
    @Expose
    List<TargetData> data;


    protected Target(Parcel in) {
        title = in.readString();
        data = in.createTypedArrayList(TargetData.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeTypedList(data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Target> CREATOR = new Creator<Target>() {
        @Override
        public Target createFromParcel(Parcel in) {
            return new Target(in);
        }

        @Override
        public Target[] newArray(int size) {
            return new Target[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TargetData> getData() {
        return data;
    }

    public void setData(List<TargetData> data) {
        this.data = data;
    }
}