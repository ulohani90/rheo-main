package com.rheotv.android.data.network.models.useProfile.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.GraphDataObject;

import java.util.List;

public class AnalyticsGraphObject {

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("data")
    @Expose
    private List<GraphDataObject> graphDataObjects;

    @SerializedName("should_format_date")
    @Expose
    boolean shouldFormatDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<GraphDataObject> getGraphDataObjects() {
        return graphDataObjects;
    }

    public void setGraphDataObjects(List<GraphDataObject> graphDataObjects) {
        this.graphDataObjects = graphDataObjects;
    }

    public boolean isShouldFormatDate() {
        return shouldFormatDate;
    }

    public void setShouldFormatDate(boolean shouldFormatDate) {
        this.shouldFormatDate = shouldFormatDate;
    }
}

