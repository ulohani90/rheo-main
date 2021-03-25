package com.rheotv.android.data.network.models.share

import com.google.gson.annotations.SerializedName

data class GiphyImages(
        @field:SerializedName("480w_still")
        val still: GiphyStill? = null
)