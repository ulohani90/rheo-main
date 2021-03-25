package com.rheotv.android.db

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ClipResponse(

        @field:SerializedName("results")
        val result: List<ClipItem> = ArrayList(),

        @field:SerializedName("count")
        @Expose
        var count: Int = 0,

        @field:SerializedName("next")
        @Expose
        var next: String? = null,

        @field:SerializedName("previous")
        @Expose
        var previous: String? = null

)