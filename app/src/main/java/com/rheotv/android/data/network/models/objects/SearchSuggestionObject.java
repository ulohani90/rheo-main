package com.rheotv.android.data.network.models.objects;

public class SearchSuggestionObject {

    String title;

    String imageUrl;

    String postId;

    int type;

    public SearchSuggestionObject(String title, String imageUrl, int type, String postId) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.type = type;
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public int getType() {
        return type;
    }

    public String getPostId() {
        return postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
