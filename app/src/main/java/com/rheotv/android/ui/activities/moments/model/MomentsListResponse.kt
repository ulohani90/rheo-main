package com.rheotv.android.ui.activities.moments.model


import com.google.gson.annotations.SerializedName

data class MomentsListResponse(
        @SerializedName("is_content_moderator")
        val isContentModerator: Boolean? = false,
        @SerializedName("results")
        val data: List<MomentsListItem>? = mutableListOf(),
        @SerializedName("next")
        val next: String? = null,
        @SerializedName("previous")
        val previous: String? = null,
        @SerializedName("page")
        val page: Int? = 0
)