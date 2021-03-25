package com.rheotv.android.data.network.models.postlisting.responses

import com.google.gson.annotations.SerializedName

data class TopFansResponse(
        @SerializedName("data")
        var data: List<TopFans>?
)

data class TopFans(
        @SerializedName("bio")
        var bio: String?,
        @SerializedName("cover_pic")
        var coverPic: String?,
        @SerializedName("followers_count")
        var followersCount: Int? = 0,
        @SerializedName("id")
        var id: String?,
        @SerializedName("is_followed")
        var isFollowed: Boolean? = false,
        @SerializedName("is_prime")
        var isPrime: Boolean? = false,
        @SerializedName("is_top_streamer")
        var isTopStreamer: Boolean? = false,
        @SerializedName("is_verified")
        var isVerified: Boolean? = false,
        @SerializedName("profile_pic")
        var profilePic: String?,
        @SerializedName("total_views")
        var totalViews: Int? = 0,
        @SerializedName("user")
        var user: User?
)