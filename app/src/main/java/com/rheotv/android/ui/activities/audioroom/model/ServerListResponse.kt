package com.rheotv.android.ui.activities.audioroom.model

import com.google.gson.annotations.SerializedName

data class ServerListResponse<T>(
        @SerializedName("count")
        val count: Int?,
        @SerializedName("next")
        val next: String?,
        @SerializedName("previous")
        val previous: String?,
        @SerializedName("results")
        val results: MutableList<T>?
)