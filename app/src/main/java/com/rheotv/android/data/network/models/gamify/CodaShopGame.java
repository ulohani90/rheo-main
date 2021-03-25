package com.rheotv.android.data.network.models.gamify;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CodaShopGame implements Parcelable {

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("codashop_product_name")
    private String codaShopProductName;

    @SerializedName("cover_pic")
    private String coverPic;

    @SerializedName("name")
    private String name;

    @SerializedName("total_views")
    private int totalViews;

    @SerializedName("accept_play_request")
    private boolean acceptPlayRequest;

    @SerializedName("id")
    private String id;

    @SerializedName("total_videos")
    private int totalVideos;

    @SerializedName("codashop_interface")
    private String codaShopInterface;

    public CodaShopGame(String thumbnail, String codaShopProductName, String coverPic, String name, int totalViews, boolean acceptPlayRequest, String id, int totalVideos, String codaShopInterface) {
        this.thumbnail = thumbnail;
        this.codaShopProductName = codaShopProductName;
        this.coverPic = coverPic;
        this.name = name;
        this.totalViews = totalViews;
        this.acceptPlayRequest = acceptPlayRequest;
        this.id = id;
        this.totalVideos = totalVideos;
        this.codaShopInterface = codaShopInterface;
    }

    protected CodaShopGame(Parcel in) {
        thumbnail = in.readString();
        codaShopProductName = in.readString();
        coverPic = in.readString();
        name = in.readString();
        totalViews = in.readInt();
        acceptPlayRequest = in.readByte() != 0;
        id = in.readString();
        totalVideos = in.readInt();
        codaShopInterface = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(thumbnail);
        dest.writeString(codaShopProductName);
        dest.writeString(coverPic);
        dest.writeString(name);
        dest.writeInt(totalViews);
        dest.writeByte((byte) (acceptPlayRequest ? 1 : 0));
        dest.writeString(id);
        dest.writeInt(totalVideos);
        dest.writeString(codaShopInterface);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CodaShopGame> CREATOR = new Creator<CodaShopGame>() {
        @Override
        public CodaShopGame createFromParcel(Parcel in) {
            return new CodaShopGame(in);
        }

        @Override
        public CodaShopGame[] newArray(int size) {
            return new CodaShopGame[size];
        }
    };

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setCodaShopProductName(String codaShopProductName) {
        this.codaShopProductName = codaShopProductName;
    }

    public String getCodaShopProductName() {
        return codaShopProductName;
    }

    public void setCoverPic(String coverPic) {
        this.coverPic = coverPic;
    }

    public String getCoverPic() {
        return coverPic;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTotalViews(int totalViews) {
        this.totalViews = totalViews;
    }

    public int getTotalViews() {
        return totalViews;
    }

    public void setAcceptPlayRequest(boolean acceptPlayRequest) {
        this.acceptPlayRequest = acceptPlayRequest;
    }

    public boolean isAcceptPlayRequest() {
        return acceptPlayRequest;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTotalVideos(int totalVideos) {
        this.totalVideos = totalVideos;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public void setCodaShopInterface(String codaShopInterface) {
        this.codaShopInterface = codaShopInterface;
    }

    public String getCodaShopInterface() {
        return codaShopInterface;
    }

    @Override
    public String toString() {
        return "ResultsItem{" +
                "thumbnail = '" + thumbnail + '\'' +
                ",codashop_product_name = '" + codaShopProductName + '\'' +
                ",cover_pic = '" + coverPic + '\'' +
                ",name = '" + name + '\'' +
                ",total_views = '" + totalViews + '\'' +
                ",accept_play_request = '" + acceptPlayRequest + '\'' +
                ",id = '" + id + '\'' +
                ",total_videos = '" + totalVideos + '\'' +
                ",codashop_interface = '" + codaShopInterface + '\'' +
                "}";
    }
}