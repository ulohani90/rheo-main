package com.rheotv.android.data.network.models.postlisting.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchApiResponse {
    @SerializedName("results")
    @Expose
    List<SearchResponse> searchResponse;

    public List<SearchResponse> getSearchResponse() {
        return searchResponse;
    }

    public void setSearchResponse(List<SearchResponse> searchResponse) {
        this.searchResponse = searchResponse;
    }
}
