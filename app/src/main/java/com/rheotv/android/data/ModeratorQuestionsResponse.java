package com.rheotv.android.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModeratorQuestionsResponse {

    @SerializedName("results")
    List<ModeratorQuestion> results;

    public List<ModeratorQuestion> getResults() {
        return results;
    }

    public void setResults(List<ModeratorQuestion> results) {
        this.results = results;
    }
}
