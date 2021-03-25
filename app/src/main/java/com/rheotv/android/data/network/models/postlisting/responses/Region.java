package com.rheotv.android.data.network.models.postlisting.responses;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.districtlisting.DistrictResult;

public class Region {
    @SerializedName("Region")
    @Expose
    private List<DistrictResult> districtResults = null;

    public List<DistrictResult> getDistrictResults() {
        return districtResults;
    }

    public void setDistrictResults(List<DistrictResult> districtResults) {
        this.districtResults = districtResults;
    }

}
