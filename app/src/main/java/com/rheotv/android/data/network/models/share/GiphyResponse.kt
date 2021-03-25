package com.rheotv.android.data.network.models.share

import com.google.gson.annotations.SerializedName

data class GiphyResponse(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("meta")
	val meta: Meta? = null
)