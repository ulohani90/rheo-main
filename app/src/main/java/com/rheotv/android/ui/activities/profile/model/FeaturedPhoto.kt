package com.rheotv.android.ui.activities.profile.model

import com.google.gson.annotations.SerializedName

data class FeaturedPhoto(

        @field:SerializedName("id")
        var id: String? = null,

        @field:SerializedName("image_url")
        var pictureUrl: String? = null,
        var isDelete: Boolean = false


)
