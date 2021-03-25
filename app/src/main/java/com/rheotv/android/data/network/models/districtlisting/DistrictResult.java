package com.rheotv.android.data.network.models.districtlisting;

import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.postlisting.responses.District;

import java.util.List;

public class DistrictResult {

    public DistrictResult(String id, String imageUrl, String title, List<District> districts) {
        this.id = id;
//        this.name = name;
        this.imageUrl = imageUrl;
        this.districtList = districts;
        this.title = title;
    }

    @SerializedName("id")
    private String id;

   /* @SerializedName("name")
    private String name;*/

    @SerializedName("title")
    private String title;

    @SerializedName("type")
    private int type;

    @SerializedName("results")
    private List<District> districtList;

    @SerializedName("imageUrl")
    private String imageUrl;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<District> getDistrictList() {
        return districtList;
    }

    public void setDistrictList(List<District> districtList) {
        this.districtList = districtList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }*/

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
