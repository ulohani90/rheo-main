package com.rheotv.android.data.network.models.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.postlisting.responses.User;

public class AuthorObject implements Parcelable {

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("profile_pic")
    @Expose
    String profilePic;

    @SerializedName("user")
    @Expose
    User user;

    @SerializedName("campaign_info")
    @Expose
    private String campaignInfo;

    public AuthorObject(String profilePic, User user) {
        this.profilePic = profilePic;
        this.user = user;
    }

    public AuthorObject(User user) {
        this.user = user;
    }

    protected AuthorObject(Parcel in) {
        id = in.readString();
        profilePic = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
        campaignInfo = in.readString();
    }

    public static final Creator<AuthorObject> CREATOR = new Creator<AuthorObject>() {
        @Override
        public AuthorObject createFromParcel(Parcel in) {
            return new AuthorObject(in);
        }

        @Override
        public AuthorObject[] newArray(int size) {
            return new AuthorObject[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCampaignInfo() {
        return campaignInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(profilePic);
        parcel.writeParcelable(user, i);
        parcel.writeString(campaignInfo);
    }
}
