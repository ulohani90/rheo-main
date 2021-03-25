package com.rheotv.android.data.network.models.useProfile.responses

import com.google.gson.annotations.SerializedName

data class GameWiseUser(
        @field:SerializedName("game__thumbnail")
        var thumbnail: String? = null,

        @field:SerializedName("game__name")
        var name: String? = null,

        @field:SerializedName("id")
        var id: String? = null,

        @field:SerializedName("game_username")
        var gameUsername: String? = null
)