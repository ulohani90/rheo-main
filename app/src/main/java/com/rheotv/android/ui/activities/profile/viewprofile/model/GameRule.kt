package com.rheotv.android.ui.activities.profile.viewprofile.model

import com.google.gson.annotations.SerializedName

data class GameRule(
        @field:SerializedName("id")
        var id: String? = null,

        @field:SerializedName("rule")
        var rule: String? = null
)