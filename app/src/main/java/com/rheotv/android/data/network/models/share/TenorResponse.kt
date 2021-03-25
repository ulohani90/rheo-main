package com.rheotv.android.data.network.models.share

import com.google.gson.annotations.SerializedName

data class TenorResponse(
        @SerializedName("next")
        val next: String?,
        @SerializedName("results")
        val results: List<TenorGIF> = listOf(),
        @SerializedName("code")
        val code: Int? = 0,
        @SerializedName("error")
        val error: String?
)

data class TenorGIF(
        @SerializedName("id")
        val id: String?,
        @SerializedName("media")
        val media: List<TenorGifFormat> = listOf()
)

data class TenorGifFormat(
        @SerializedName("tinygif")
        val tinyGif: TenorMedia,
        @SerializedName("mediumgif")
        val mediumGif: TenorMedia,
        @SerializedName("mp4")
        val mp4: TenorMedia
)

data class TenorMedia(
        @SerializedName("url")
        val url: String?,
        @SerializedName("dims")
        val dims: Array<Int> = arrayOf(),
        @SerializedName("preview")
        val preview: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TenorMedia

        if (url != other.url) return false
        if (!dims.contentEquals(other.dims)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url?.hashCode() ?: 0
        result = 31 * result + dims.contentHashCode()
        return result
    }
}