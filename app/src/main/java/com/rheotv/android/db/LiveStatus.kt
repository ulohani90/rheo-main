package com.rheotv.android.db

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LiveStatus (
        @SerializedName("is_live")
        @Expose
        var isLive: Boolean = false,
        @SerializedName("live_post_id")
        @Expose
        var livePostId: String? = null
)