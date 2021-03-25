package com.rheotv.android.ui.activities.audioroom.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.rheotv.android.ui.activities.audioroom.viewmodel.AudioChatRoomActivityViewModel
import com.rheotv.android.utils.CommonUtils
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AudioRoom(
        @SerializedName("group_details")
        var groupDetails: AudioGroup?,

        @SerializedName("last_commented")
        var lastComment: AudioRoomLastComment?,

        @SerializedName("chatroom_details")
        var activeChatRooms: AudioChatRoom?,

        @SerializedName("languages")
        val language: List<String>?,

        var isConnected: Boolean = false
) : Parcelable {
    fun onlineParticipant(): String = "${CommonUtils.formatValue(activeChatRooms?.totalActiveUsers?.toDouble() ?: 0.toDouble())} Online"
}

@Parcelize
data class AudioGroup(
        @SerializedName("id")
        val id: String?,
        @SerializedName("name")
        var name: String?,
        @SerializedName("logo_url")
        val logoUrl: String?,
        @SerializedName("owner_details")
        val ownerDetails: OwnerDetail?,
) : Parcelable

@Parcelize
data class OwnerDetail(
        @SerializedName("id")
        val id: Int?,

        @SerializedName("first_name")
        val firstName: String? = null,

        @SerializedName("last_name")
        val lastName: String? = null,

        @SerializedName("username")
        var username: String?,

        @SerializedName("intro")
        val intro: String?,

        @SerializedName("name")
        val name: String? = null,

        @SerializedName("profile_pic")
        var profileImageUrl: String? = null,

        @SerializedName("is_muted")
        var isMuted: Boolean? = false,

        @SerializedName("can_mute_unmute_participants")
        var canUpdateUser: Boolean? = false,

        @SerializedName("is_duplex")
        var isDuplex: Boolean? = false,

        @SerializedName("game_name")
        var gameName: String? = "",

        @SerializedName("is_live")
        var isLive: Boolean? = false,

        @SerializedName("post_id")
        var livePostId: String? = null,

        var isSpeaking: Boolean = false
) : Parcelable

@Parcelize
data class AudioChatRoom(
        @SerializedName("total_active_users")
        var totalActiveUsers: Int,
        @SerializedName("chatrooms")
        val chatRoomList: MutableList<String>?,
        @SerializedName("is_public")
        val isPublic: Boolean
) : Parcelable

@Parcelize
data class AudioRoomLastComment(
        @SerializedName("username")
        val username: String?,
        @SerializedName("created_at")
        val createdAt: String?,
        @SerializedName("text")
        val text: String?
) : Parcelable

data class AudioRoomResponse(
        @SerializedName("count")
        val count: Int?,
        @SerializedName("next")
        val next: String?,
        @SerializedName("previous")
        val previous: String?,
        @SerializedName("results")
        val results: MutableList<AudioRoom>?,
        @SerializedName("featured_chatroom")
        val featureChatRoom: CreateAudioRoomResponse
)

data class Participant(
        @SerializedName("participant_details")
        val participantDetails: OwnerDetail?
)

sealed class AudioConnection : Parcelable {

    @Parcelize
    object CallConnected : AudioConnection()

    @Parcelize
    object CallDisconnected : AudioConnection()

    @Parcelize
    data class UserCountUpdate(val count: Int) : AudioConnection()

    @Parcelize
    data class CallLeft(val uid: Int) : AudioConnection()

    @Parcelize
    data class UserJoined(val uid: Int) : AudioConnection()

    @Parcelize
    object FirstUser : AudioConnection()

    @Parcelize
    data class SelfMute(val isMuted: Boolean) : AudioConnection()

    @Parcelize
    data class SpeakerIndicate(val speaks: List<Int>?) : AudioConnection()

    @Parcelize
    data class UserJoinRoom(val user: List<OwnerDetail>?) : AudioConnection()

    @Parcelize
    data class UserLeaveRoom(val action: AudioChatRoomActivityViewModel.AudioRoomAction, val data: AudioChatRoomActivityViewModel.UpdateData?) : AudioConnection()

    @Parcelize
    data class ExitRoom(val reason: String? = null) : AudioConnection()
}

@Parcelize
data class AudioRoomDetail(
        var isMicMuted: Boolean = false,
        var isVolumeEnabled: Boolean = true,
        var audioGroup: AudioGroup? = null,
        var onlineMemberCount: Int = 1,
        var chatRoomId: String? = null,
        var groupId: String? = null,
        var thumbnail: String? = null,
        var text: String? = null,
        var authToken: String? = null,
        var channelId: String? = null,
        var grpcConnectionId: String? = null
) : Parcelable