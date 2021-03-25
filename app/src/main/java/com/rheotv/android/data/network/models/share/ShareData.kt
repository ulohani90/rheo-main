package com.rheotv.android.data.network.models.share

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShareData(

        @SerializedName("thumbnails")
        val thumbnails: List<String>? = null,

        @SerializedName("clip")
        val clip: List<String>? = null,

        @SerializedName("giphy")
        val giphy: List<String>? = null
): Parcelable