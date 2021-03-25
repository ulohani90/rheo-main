package com.rheotv.android.ui.activities.profile.model

import com.google.gson.annotations.SerializedName

data class SocialMedia (
        @field:SerializedName("id")
        var id: String? = null,

        @field:SerializedName("link")
        var link: String? = null,

        @field:SerializedName("logo")
        var logo: String? = null,

        @field:SerializedName("name")
        var name: String? = null,

        @field:SerializedName("text")
        var text: String? = null
)