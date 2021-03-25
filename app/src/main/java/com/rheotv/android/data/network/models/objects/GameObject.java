package com.rheotv.android.data.network.models.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GameObject implements Parcelable {

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("thumbnail")
    @Expose
    String thumbnail;

    @SerializedName("cover_pic")
    @Expose
    String coverPic;

    @SerializedName("total_views")
    @Expose
    private int totalViews;

    @SerializedName("total_videos")
    @Expose
    private int totalVideos;

    @SerializedName("accept_play_request")
    @Expose
    private boolean acceptPlayRequest;

    public GameObject(String name) {
        this.name = name;
    }

    protected GameObject(Parcel in) {
        id = in.readString();
        name = in.readString();
        thumbnail = in.readString();
        coverPic = in.readString();
        totalViews = in.readInt();
        totalVideos = in.readInt();
        acceptPlayRequest = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(thumbnail);
        dest.writeString(coverPic);
        dest.writeInt(totalViews);
        dest.writeInt(totalVideos);
        dest.writeByte((byte) (acceptPlayRequest ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GameObject> CREATOR = new Creator<GameObject>() {
        @Override
        public GameObject createFromParcel(Parcel in) {
            return new GameObject(in);
        }

        @Override
        public GameObject[] newArray(int size) {
            return new GameObject[size];
        }
    };

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

    public int getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(int totalViews) {
        this.totalViews = totalViews;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public void setTotalVideos(int totalVideos) {
        this.totalVideos = totalVideos;
    }

    public boolean getAcceptPlayRequest() {
        return acceptPlayRequest;
    }

    public void setAcceptPlayRequest(boolean acceptPlayRequest) {
        this.acceptPlayRequest = acceptPlayRequest;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
