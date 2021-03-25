package com.rheotv.android.data.network.models.useProfile.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.rheotv.android.data.network.models.StreamerData

import java.util.ArrayList

class AnalyticsDataResponse {

    @SerializedName("data")
    @Expose
    val data: ArrayList<StreamerData>? = null
}
