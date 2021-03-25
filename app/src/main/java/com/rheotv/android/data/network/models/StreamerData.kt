package com.rheotv.android.data.network.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.rheotv.android.data.network.models.useProfile.responses.AnalyticsGraphObject

class StreamerData {
    @SerializedName("total_view")
    @Expose
    val totalViews: Int = 0

    @SerializedName("total_followers")
    @Expose
    val totalFollowers: Int = 0

    @SerializedName("total_hours_streamed")
    @Expose
    val totalHoursStreamed: Int = 0

    @SerializedName("total_watch_time")
    @Expose
    val totalWatchTime: Float = 0.toFloat()

    @SerializedName("sort_type")
    @Expose
    val type: String? = null

    @SerializedName("daily_views")
    @Expose
    val dailyViews: List<GraphDataObject>? = null

    @SerializedName("graphs_data")
    var graphObjects: List<AnalyticsGraphObject>? = null

    @SerializedName("daily_watch_time")
    @Expose
    val dailyWatchTime: List<GraphDataObject>? = null

    val userMatrix: String
        get() = "$totalViews Views\n$totalFollowers Followers\n$totalHoursStreamed Hrs Streamed\n$totalWatchTime Hrs Watched"
}
