package com.rheotv.android.utils

import com.rheotv.android.data.network.models.objects.PostObject
import com.rheotv.android.ui.activities.moments.model.MomentsListItem
import com.rheotv.android.ui.activities.player.activity.newPlayer.StreamPlayerFragmentV2
import org.greenrobot.eventbus.EventBus


sealed class EventBusModel {
    object LoginSuccess : EventBusModel()
    object UpdateCoin : EventBusModel()
    object ShowTags : EventBusModel()
    object RefreshProfile : EventBusModel()
    object LogoutSuccess : EventBusModel()
    object RemoveMomentsView : EventBusModel()
    data class UpdateMomentData(val moment: MomentsListItem?) : EventBusModel()
    data class UpdateBackPress(val value: Boolean = false) : EventBusModel()
    object StartStreamService : EventBusModel()
    data class UpdateStreamFragment(val streamPlayerFragment: StreamPlayerFragmentV2) : EventBusModel()
    data class OpenPostWitId(
            val postId: String,
            val isFromDeepLink: Boolean = false,
            val isForCustomRoom: Boolean = false,
            val loadMore: Boolean = false
    ) : EventBusModel()

    object RefreshAudioGroupList : EventBusModel()

    object RemoveChatroomController : EventBusModel()

    object FetchLastAudioRoomState : EventBusModel()

    data class AudioRoomConnected(val id: String?) : EventBusModel()

    data class AudioRoomDisconnected(val id: String?) : EventBusModel()

    data class LoadIntroAndGameRules(
            val introVideoUrl: String,
            val gameRulesVideoUrl: String,
            val gameName: String,
            val authorName: String
    ) : EventBusModel()


    data class Next(val id: String) : EventBusModel()

    data class Previous(val id: String) : EventBusModel()

    data class End(val id: String) : EventBusModel()

}