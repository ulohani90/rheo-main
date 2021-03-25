package com.rheotv.android.db

import com.google.gson.annotations.SerializedName


data class Author(

	@field:SerializedName("is_top_streamer")
	var isTopStreamer: Boolean? = null,

	@field:SerializedName("cover_pic")
	var coverPic: String? = null,

	@field:SerializedName("followers_count")
	var followersCount: Int? = null,

	@field:SerializedName("profile_pic")
	var profilePic: String? = null,

	@field:SerializedName("is_followed")
	var isFollowed: Boolean? = null,

	@field:SerializedName("bio")
	var bio: Any? = null,

	@field:SerializedName("total_views")
	var totalViews: Int? = null,

	@field:SerializedName("id")
	var id: String? = null,

	@field:SerializedName("user")
	var user: User? = null,

	@field:SerializedName("is_verified")
	var isVerified: Boolean? = null,

	@field:SerializedName("is_prime")
	var isPrime: Boolean? = null,

	@field:SerializedName("campaign_info")
	var campaignInfo: String = "" // todo add in api
)