package com.rheotv.android.data.network.models.share

import com.google.gson.annotations.SerializedName

data class Data(

	@field:SerializedName("fixed_width_downsampled_url")
	val fixedWidthDownsampledUrl: String? = null,

	@field:SerializedName("fixed_height_downsampled_url")
	val fixedHeightDownsampledUrl: String? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("fixed_width_small_still_url")
	val fixedWidthSmallStillUrl: String? = null,

	@field:SerializedName("fixed_height_small_still_url")
	val fixedHeightSmallStillUrl: String? = null,

	@field:SerializedName("caption")
	val caption: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("fixed_height_small_url")
	val fixedHeightSmallUrl: String? = null,

	@field:SerializedName("image_original_url")
	val imageOriginalUrl: String? = null,

	@field:SerializedName("fixed_width_small_url")
	val fixedWidthSmallUrl: String? = null,

	@field:SerializedName("images")
	val images: GiphyImages? = null


)