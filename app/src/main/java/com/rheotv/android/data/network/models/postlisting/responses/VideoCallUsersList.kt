package com.rheotv.android.data.network.models.postlisting.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


data class VideoCallUsersList(
        @SerializedName("data")
        val data: VideoCallUsersListData?,
        @SerializedName("sorted_position")
        val sortedPos: Int?,
        @SerializedName("state")
        var state: String?,
        @SerializedName("call_request_coin_fee")
        var callRequestCoinFee: Int?,
        @SerializedName("final_call_request_coin_fee")
        var finalCallRequestCoinFee: Int?,
        @SerializedName("discount_text")
        var discountText: String?,
)

data class VideoCallUsersListData(
        @SerializedName("count")
        val count: Int?,

        @SerializedName("next")
        val next: String?,

        @SerializedName("previous")
        val previous: String?,

        @SerializedName("results")
        val users: List<VideoCallUsersListObject>?
)

@Parcelize
data class VideoCallUsersListObject(
        @SerializedName("state")
        var state: String?,

        @SerializedName("post_id")
        val postId: String?,

        @SerializedName("channel_id")
        val channelId: String?,

        @SerializedName("user_profile")
        val userProfile: UserObject?


) : Parcelable

@Parcelize
data class UserObject(
        @SerializedName("user")
        val user: User?,
        @SerializedName("profile_pic")
        val profilePic: String?,
) : Parcelable