package com.rheotv.android.data.network.models.postlisting.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.objects.PostObject;

import java.util.List;

public class UniservalListingApiResponse {
    @SerializedName("results")
    @Expose
    private List<PostObject> resultList;

    @SerializedName("image_url")
    @Expose
    private String imageURl;


    @SerializedName("total_views")
    @Expose
    private int totalViews;

    @SerializedName("total_videos")
    @Expose
    private int totalVideos;

    @SerializedName("cover_pic")
    @Expose
    private String coverPic;

    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;

    public List<PostObject> getResultList() {
        return resultList;
    }

    public void setResultList(List<PostObject> resultList) {
        this.resultList = resultList;
    }

    public String getImageURl() {
        return imageURl;
    }

    public void setImageURl(String imageURl) {
        this.imageURl = imageURl;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    @SerializedName("previous")
    private String previous;

    @SerializedName("next")
    private String next;

    @SerializedName("count")
    private int count;

    public List<PostObject> getResults() {
        return resultList;
    }

    public void setResults(List<PostObject> results) {
        this.resultList = results;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCoverPic() {
        return coverPic;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public int getTotalViews() {
        return totalViews;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setCoverPic(String coverPic) {
        this.coverPic = coverPic;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setTotalVideos(int totalVideos) {
        this.totalVideos = totalVideos;
    }

    public void setTotalViews(int totalViews) {
        this.totalViews = totalViews;
    }
}
