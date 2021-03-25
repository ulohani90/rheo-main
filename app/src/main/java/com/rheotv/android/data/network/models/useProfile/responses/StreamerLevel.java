package com.rheotv.android.data.network.models.useProfile.responses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.ui.adapters.LevelType;

import java.util.List;

public class StreamerLevel implements Parcelable {
    @SerializedName("state")
    @Expose
    String state;

    @SerializedName("level")
    @Expose
    String level;

    @SerializedName("targets")
    @Expose
    List<Target> targets;

    @SerializedName("criteria")
    @Expose
    List<Target> criteria;

    protected StreamerLevel(Parcel in) {
        state = in.readString();
        level = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(state);
        dest.writeString(level);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StreamerLevel> CREATOR = new Creator<StreamerLevel>() {
        @Override
        public StreamerLevel createFromParcel(Parcel in) {
            return new StreamerLevel(in);
        }

        @Override
        public StreamerLevel[] newArray(int size) {
            return new StreamerLevel[size];
        }
    };

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLevel() {
        return level;
    }

    public LevelType getLevelType() {
        if (level != null) {
            if (level.equalsIgnoreCase("bronze")) {
                return LevelType.Bronze.INSTANCE;
            } else if (level.equalsIgnoreCase("silver")) {
                return LevelType.Silver.INSTANCE;
            } else if (level.equalsIgnoreCase("gold")) {
                return LevelType.Gold.INSTANCE;
            }
        }
        return LevelType.Unassigned.INSTANCE;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    public List<Target> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<Target> criteria) {
        this.criteria = criteria;
    }
}