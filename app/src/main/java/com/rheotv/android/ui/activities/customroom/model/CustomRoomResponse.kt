package com.rheotv.android.ui.activities.customroom.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class CustomRoomResponse(
        @SerializedName("count")
        val count: Int = 0,
        @SerializedName("next")
        val next: String?,
        @SerializedName("previous")
        val previous: String?,
        @SerializedName("requested_in")
        val requestedInList: MutableList<String>? = mutableListOf(),
        @SerializedName("can_request")
        val canRequest: Boolean? = false,
        @SerializedName("results")
        val data: MutableList<CustomRoomDetail>? = mutableListOf()
) {
    fun transformedData(isStreamer: Boolean): List<CustomRoomDetail>? {
        data?.forEach {
            it.setupViewType(isStreamer)
        }
        return data
    }
}

data class CustomRoomDetail constructor(
        @SerializedName("id")
        var id: String?,
        @SerializedName("post_id")
        val postId: String?,
        @SerializedName("game")
        var game: String?,
        @SerializedName("winner")
        var winner: CustomRoomPlayer?,
        @SerializedName("state")
        var state: String?,
        @SerializedName("room_id")
        var customRoomId: String?,
        @SerializedName("room_password")
        var customRoomPassword: String?,
        @SerializedName("start_time")
        var startTime: String?,
        @SerializedName("end_time")
        var endTime: String?,
        @SerializedName("is_full")
        var isFull: Boolean = false,
        @SerializedName("can_refund")
        var canRefund: Boolean = false,
        @SerializedName("streamer_cut_given")
        var isStreamerCutGiven: Boolean = false,
        @SerializedName("entry_coins")
        var entryCoins: Int = 0,
        @SerializedName("users_count")
        var currentPlayerCount: Int = 0,
        @SerializedName("max_allowed_users")
        var maxPlayerCount: Int = 0,
        var gameUserName: String? = null
) {
    constructor(postId: String) : this(null, postId, null, null, null, null, null, null, null)

    var viewType: CustomRoomDetailViewType? = CustomRoomDetailViewType.Requested
        get() = if (field == null) {
            CustomRoomDetailViewType.Requested
        } else field

    var dataViewType: CustomRoomViewType? = CustomRoomViewType.CustomRoomCreated
        get() = field ?: CustomRoomViewType.CustomRoomCreated

    fun deepCopy(): CustomRoomDetail = copy().also {
        it.viewType = viewType
        it.dataViewType = dataViewType
    }

    fun setupViewType(isStreamer: Boolean) {
        dataViewType = CustomRoomViewType.getViewType(state?.toLowerCase(Locale.getDefault()))
        viewType = if (dataViewType == CustomRoomViewType.CustomRoomCreated) {
            if (isStreamer) {
                if (customRoomId.isNullOrBlank()) {
                    CustomRoomDetailViewType.RequestRoomIdAndPassword
                } else {
                    CustomRoomDetailViewType.ShowRoomIdAndPassword
                }
            } else {
                CustomRoomDetailViewType.GameUserInput
            }
        } else {
            CustomRoomDetailViewType.ShowRoomIdAndPassword
        }
    }
}