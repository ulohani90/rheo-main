package com.rheotv.android.data.network.models.gamify;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.utils.AppConstants;

public class Reward implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("plan_name")
    @Expose
    private String rewardType;

    @SerializedName("coins")
    @Expose
    private String coinWon;

    @SerializedName("rewardDate")
    @Expose
    private String rewardDate;

    @SerializedName("state")
    @Expose
    private String rewardStatus;

    @SerializedName("plan_title")
    @Expose
    private String rewardTitle;

    @SerializedName("plan_subtitle")
    @Expose
    private String rewardSubtitle;

    @SerializedName("total_days")
    @Expose
    private String totalDays = "0";

    @SerializedName("days_completed")
    @Expose
    private String daysCompleted = "0";

    @SerializedName("days_left")
    @Expose
    private String daysLeft = "0";

    @SerializedName("completed_on")
    @Expose
    private String completedOn;

    @SerializedName("next_milestone")
    @Expose
    private String nextMilestone;

    @SerializedName("activated")
    @Expose
    private boolean active;

    @SerializedName("plan_waiting_time")
    @Expose
    private long rewardActivateAfter;

    @SerializedName("plan_description")
    @Expose
    private String description;

    public Reward() {
    }

    public Reward(String rewardType, String coinWon, String totalDays, String daysCompleted) {
        this.rewardType = rewardType;
        this.coinWon = coinWon;
        this.totalDays = totalDays;
        this.daysCompleted = daysCompleted;
    }

    public Reward(String rewardType, String coinWon, String totalDays, String daysCompleted, String status) {
        this.rewardType = rewardType;
        this.coinWon = coinWon;
        this.totalDays = totalDays;
        this.daysCompleted = daysCompleted;
        this.rewardStatus = status;
    }

    public Reward(String rewardType, String coinWon, String rewardDate) {
        this.rewardType = rewardType;
        this.coinWon = coinWon;
        this.rewardDate = rewardDate;
    }

    public Reward(String id, String rewardType, String coinWon, String rewardDate, String rewardStatus, String rewardTitle, String rewardSubtitle, String totalDays, String daysCompleted, String daysLeft, String completedOn, String nextMilestone, boolean active, String description) {
        this.id = id;
        this.rewardType = rewardType;
        this.coinWon = coinWon;
        this.rewardDate = rewardDate;
        this.rewardStatus = rewardStatus;
        this.rewardTitle = rewardTitle;
        this.rewardSubtitle = rewardSubtitle;
        this.totalDays = totalDays;
        this.daysCompleted = daysCompleted;
        this.daysLeft = daysLeft;
        this.completedOn = completedOn;
        this.nextMilestone = nextMilestone;
        this.active = active;
        this.description = description;
    }

    protected Reward(Parcel in) {
        id = in.readString();
        rewardType = in.readString();
        coinWon = in.readString();
        rewardDate = in.readString();
        rewardStatus = in.readString();
        rewardTitle = in.readString();
        rewardSubtitle = in.readString();
        totalDays = in.readString();
        daysCompleted = in.readString();
        daysLeft = in.readString();
        completedOn = in.readString();
        nextMilestone = in.readString();
        active = in.readByte() != 0;
        rewardActivateAfter = in.readLong();
        description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(rewardType);
        dest.writeString(coinWon);
        dest.writeString(rewardDate);
        dest.writeString(rewardStatus);
        dest.writeString(rewardTitle);
        dest.writeString(rewardSubtitle);
        dest.writeString(totalDays);
        dest.writeString(daysCompleted);
        dest.writeString(daysLeft);
        dest.writeString(completedOn);
        dest.writeString(nextMilestone);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeLong(rewardActivateAfter);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Reward> CREATOR = new Creator<Reward>() {
        @Override
        public Reward createFromParcel(Parcel in) {
            return new Reward(in);
        }

        @Override
        public Reward[] newArray(int size) {
            return new Reward[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public String getCoinWon() {
        return coinWon;
    }

    public void setCoinWon(String coinWon) {
        this.coinWon = coinWon;
    }

    public String getRewardDate() {
        return rewardDate;
    }

    public void setRewardDate(String rewardDate) {
        this.rewardDate = rewardDate;
    }

    public String getRewardStatus() {
        return rewardStatus;
    }

    public void setRewardStatus(String rewardStatus) {
        this.rewardStatus = rewardStatus;
    }

    public String getRewardTitle() {
        return rewardTitle;
    }

    public void setRewardTitle(String rewardTitle) {
        this.rewardTitle = rewardTitle;
    }

    public String getRewardSubtitle() {
        return rewardSubtitle;
    }

    public void setRewardSubtitle(String rewardSubtitle) {
        this.rewardSubtitle = rewardSubtitle;
    }

    public String getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(String totalDays) {
        this.totalDays = totalDays;
    }

    public String getDaysCompleted() {
        return daysCompleted;
    }

    public void setDaysCompleted(String daysCompleted) {
        this.daysCompleted = daysCompleted;
    }

    public String getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(String daysLeft) {
        this.daysLeft = daysLeft;
    }

    public String getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(String completedOn) {
        this.completedOn = completedOn;
    }

    public String getNextMilestone() {
        return nextMilestone;
    }

    public void setNextMilestone(String nextMilestone) {
        this.nextMilestone = nextMilestone;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    private boolean isActive() {
        return active;
    }

    public long getRewardActivateAfter() {
        return rewardActivateAfter;
    }

    public void setRewardActivateAfter(long rewardActivateAfter) {
        this.rewardActivateAfter = rewardActivateAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int progressPercent() {
        double m = (double) Integer.parseInt(daysCompleted) / Integer.parseInt(totalDays);
        double p = m * 100;
        return (int) p;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Reward)) return false;
        boolean isEqual = false;
        Reward newObject = (Reward) obj;
        isEqual = id != null && id.equalsIgnoreCase(newObject.getId());
        isEqual = isEqual && rewardType != null && rewardType.equalsIgnoreCase(newObject.getRewardType());
        isEqual = isEqual && coinWon != null && coinWon.equalsIgnoreCase(newObject.getCoinWon());
        isEqual = isEqual && rewardDate != null && rewardDate.equalsIgnoreCase(newObject.getRewardDate());
        isEqual = isEqual && rewardStatus != null && rewardStatus.equalsIgnoreCase(newObject.getRewardStatus());
        isEqual = isEqual && rewardTitle != null && rewardTitle.equalsIgnoreCase(newObject.getRewardTitle());
        isEqual = isEqual && rewardSubtitle != null && rewardSubtitle.equalsIgnoreCase(newObject.getRewardSubtitle());
        isEqual = isEqual && totalDays != null && totalDays.equalsIgnoreCase(newObject.getTotalDays());
        isEqual = isEqual && daysCompleted != null && daysCompleted.equalsIgnoreCase(newObject.getDaysCompleted());
        isEqual = isEqual && daysLeft != null && daysLeft.equalsIgnoreCase(newObject.getDaysLeft());
        isEqual = isEqual && completedOn != null && completedOn.equalsIgnoreCase(newObject.getCompletedOn());
        isEqual = isEqual && nextMilestone != null && nextMilestone.equalsIgnoreCase(newObject.getNextMilestone());
        isEqual = isEqual && active == newObject.isActive();
        isEqual = isEqual && description != null && description.equalsIgnoreCase(newObject.getDescription());
        return isEqual;
    }
}