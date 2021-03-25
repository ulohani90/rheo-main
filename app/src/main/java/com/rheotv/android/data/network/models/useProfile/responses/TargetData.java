package com.rheotv.android.data.network.models.useProfile.responses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TargetData implements Parcelable {
    @SerializedName("title")
    @Expose
    String title;

    @SerializedName("target")
    @Expose
    double targetValue;

    @SerializedName("value")
    @Expose
    double achievedValue;

    @SerializedName("type")
    @Expose
    String type;

    @SerializedName("completed")
    @Expose
    boolean completed;

    @SerializedName("text")
    @Expose
    String dataMetrics;

    protected TargetData(Parcel in) {
        title = in.readString();
        targetValue = in.readInt();
        achievedValue = in.readInt();
        type = in.readString();
        dataMetrics = in.readString();
    }

    public TargetData() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeDouble(targetValue);
        dest.writeDouble(achievedValue);
        dest.writeString(type);
        dest.writeString(dataMetrics);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TargetData> CREATOR = new Creator<TargetData>() {
        @Override
        public TargetData createFromParcel(Parcel in) {
            return new TargetData(in);
        }

        @Override
        public TargetData[] newArray(int size) {
            return new TargetData[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(double targetValue) {
        this.targetValue = targetValue;
    }

    public double getAchievedValue() {
        return achievedValue;
    }

    public void setAchievedValue(double achievedValue) {
        this.achievedValue = achievedValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getDataMetrics() {
        return dataMetrics;
    }

    public void setDataMetrics(String dataMetrics) {
        this.dataMetrics = dataMetrics;
    }
}