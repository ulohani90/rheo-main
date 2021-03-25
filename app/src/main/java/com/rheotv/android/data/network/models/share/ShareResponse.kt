package com.rheotv.android.data.network.models.share

import com.google.gson.annotations.SerializedName

data class ShareResponse(

        @field:SerializedName("data")
        val shareData: ShareData? = null,

        @field:SerializedName("error")
        val error: Any? = null
)