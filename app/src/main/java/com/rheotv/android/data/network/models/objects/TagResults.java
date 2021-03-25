package com.rheotv.android.data.network.models.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TagResults {

    @SerializedName("tag_name")
    @Expose
    String tagName;

    @SerializedName("slug")
    @Expose
    String slug;

    @SerializedName("image_url")
    @Expose
    String imageUrl;

    @SerializedName("post_count")
    @Expose
    int postCounts;

    public TagResults() {
    }

    public TagResults(String tagName, int postCounts) {
        this.tagName = tagName;
        this.postCounts = postCounts;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public int getPostCounts() {
        return postCounts;
    }

    public void setPostCounts(int postCounts) {
        this.postCounts = postCounts;
    }

    public String getSlug() {
        return slug;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
