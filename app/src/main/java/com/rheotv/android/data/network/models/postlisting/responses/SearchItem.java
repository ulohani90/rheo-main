package com.rheotv.android.data.network.models.postlisting.responses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.objects.AuthorObject;
import com.rheotv.android.data.network.models.objects.GameObject;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.LinkHandler;

public class SearchItem implements Parcelable {
    @SerializedName("title")
    @Expose
    String title;
    @SerializedName("subtitle")
    @Expose
    String subtitle;
    @SerializedName("tag")
    @Expose
    String tag;
    @SerializedName("thumbnail")
    @Expose
    String url;
    @SerializedName("is_live")
    @Expose
    boolean isLive;
    @SerializedName("tag_background_color")
    @Expose
    String tagBackgroundColor;
    @SerializedName("permalink")
    @Expose
    String permalink;

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("cover_pic")
    @Expose
    String coverPic;

    @SerializedName("user_profile_pic")
    @Expose
    String userProfilePic;

    @SerializedName("profile_id")
    @Expose
    String profileId;

    @SerializedName("total_views")
    @Expose
    String totalViews;

    protected SearchItem(Parcel in) {
        title = in.readString();
        subtitle = in.readString();
        tag = in.readString();
        url = in.readString();
        isLive = in.readByte() != 0;
        tagBackgroundColor = in.readString();
        permalink = in.readString();
        id = in.readString();
        name = in.readString();
        coverPic = in.readString();
        userProfilePic = in.readString();
        totalViews = in.readString();
        profileId = in.readString();
    }

    public static final Creator<SearchItem> CREATOR = new Creator<SearchItem>() {
        @Override
        public SearchItem createFromParcel(Parcel in) {
            return new SearchItem(in);
        }

        @Override
        public SearchItem[] newArray(int size) {
            return new SearchItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(subtitle);
        parcel.writeString(tag);
        parcel.writeString(url);
        parcel.writeByte((byte) (isLive ? 1 : 0));
        parcel.writeString(tagBackgroundColor);
        parcel.writeString(permalink);
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(coverPic);
        parcel.writeString(userProfilePic);
        parcel.writeString(totalViews);
        parcel.writeString(profileId);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public String getTagBackgroundColor() {
        return tagBackgroundColor;
    }

    public void setTagBackgroundColor(String tagBackgroundColor) {
        this.tagBackgroundColor = tagBackgroundColor;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverPic() {
        return coverPic;
    }

    public void setCoverPic(String coverPic) {
        this.coverPic = coverPic;
    }

    public String getUserProfilePic() {
        return userProfilePic;
    }

    public void setUserProfilePic(String userProfilePic) {
        this.userProfilePic = userProfilePic;
    }

    public String getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(String totalViews) {
        this.totalViews = totalViews;
    }

    public String getProfileId() {
        return profileId;
    }

    public static Creator<SearchItem> getCREATOR() {
        return CREATOR;
    }

    public PostObject toPostObject() {
        return new PostObject(LinkHandler.getPostId(getPermalink()), getCoverPic(), getTotalViews() != null ? CommonUtils.getNumberFromFormat(getTotalViews()) : 0, getSubtitle(), new GameObject(getTag()), new AuthorObject(getUserProfilePic(), new User(getName())), isLive());
    }
}

