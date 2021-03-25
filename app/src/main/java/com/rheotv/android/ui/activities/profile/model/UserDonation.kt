package com.rheotv.android.ui.activities.profile.model

import com.google.gson.annotations.SerializedName

data class UserDonation (
        @field:SerializedName("link")
        var link: String? = null,

        @field:SerializedName("title")
        var title: String? = null
)