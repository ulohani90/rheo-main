package com.rheotv.story.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class StoryCTA implements Parcelable {

    @SerializedName("type")
    String ctaType;

    @SerializedName("data")
    private StoryCTAData storyCTAData;

    public StoryCTA(String ctaType) {
        this.ctaType = ctaType;
    }

    public StoryCTA(String ctaType, StoryCTAData storyCTAData) {
        this.ctaType = ctaType;
        this.storyCTAData = storyCTAData;
    }

    protected StoryCTA(Parcel in) {
        ctaType = in.readString();
        storyCTAData = in.readParcelable(StoryCTAData.class.getClassLoader());
    }

    public static final Creator<StoryCTA> CREATOR = new Creator<StoryCTA>() {
        @Override
        public StoryCTA createFromParcel(Parcel in) {
            return new StoryCTA(in);
        }

        @Override
        public StoryCTA[] newArray(int size) {
            return new StoryCTA[size];
        }
    };

    public String getCtaType() {
        return ctaType;
    }

    public void setCtaType(String ctaType) {
        this.ctaType = ctaType;
    }

    public StoryCTAData getStoryCTAData() {
        return storyCTAData;
    }

    public void setStoryCTAData(StoryCTAData storyCTAData) {
        this.storyCTAData = storyCTAData;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ctaType);
        dest.writeParcelable(storyCTAData, flags);
    }
}
