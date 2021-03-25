package com.rheotv.android.ui.activities.audioroom.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.streamUpdates.StreamEventResponse
import com.rheotv.android.helpers.grpc.GrpcConnectionManager
import com.rheotv.android.helpers.grpc.IncomingChatListener
import com.rheotv.android.services.AudioRoomService
import com.rheotv.android.ui.activities.audioroom.model.*
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.*
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import goChat.Services
import io.agora.rtc.RtcEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AudioRoomViewModel constructor(
        dataManager: DataManager,
        schedulerProvider: SchedulerProvider
) : BaseViewModel<Any>(dataManager, schedulerProvider) {
    private val TAG = javaClass.simpleName
    val audioRoomListLiveData = MutableLiveData<List<AudioRoom>?>()
    val featuredRoomParticipant = MutableLiveData<List<OwnerDetail>?>()
    val suggestions = MutableLiveData<List<AudioRoom>>()
    var nextUrl: String? = null
    val isSearchVisible = ObservableField(false)
    val searchQuery = ObservableField<String>()
    val isRefreshing = ObservableField<Boolean>()
    var featuredChatRoomId: String? = null
    var currentChatRoomId: String? = null
    var featuredChatRoomJoinUserCount: Int? = null
    var userNextUrl: String? = null
    val analyticsProperties: MutableMap<String, Any?> = hashMapOf()
    val userMutableLiveData = MutableLiveData<Pair<AudioChatRoomActivityViewModel.AudioRoomAction, AudioChatRoomActivityViewModel.UpdateData?>>()
    val connectAudioLiveData = MutableLiveData(false)
    var audioGroup = ObservableField<AudioGroup?>()
    var isMuted = ObservableField(true)
    var isSelfMuted: Boolean = true
    var isVisible = false
    var isAllowedToSpeak: Boolean = true
    var grpcConnectionId: String? = null
    var mAgoraChannelId = ""
    var mAgoraAccessToken = ""
    val featureRoomAuthor: String
        get() = audioGroup.get()?.ownerDetails?.name ?: ""
    val gson by lazy { Gson() }
    val roomDetails: AudioRoomDetail?
        get() = AudioRoomDetail(
                isSelfMuted,
                false,
                audioGroup.get(),
                featuredChatRoomJoinUserCount ?: 0,
                featuredChatRoomId,
                audioGroup.get()?.id,
                if (!audioGroup.get()?.logoUrl.isNullOrEmpty()) audioGroup.get()?.logoUrl else audioGroup.get()?.ownerDetails?.profileImageUrl,
                audioGroup.get()?.name ?: audioGroup.get()?.ownerDetails?.username,
                mAgoraAccessToken,
                mAgoraChannelId,
                grpcConnectionId
        )

    val agoraConnectionUtils by lazy { AgoraConnectionUtils() }
    private val chatHelper by lazy { GrpcConnectionManager() }
    private val chatListener = object : IncomingChatListener() {
        override fun waitAndReconnect() {
            CoroutineScope(Dispatchers.Main).doAfter(2000) {
                if (isVisible) chatHelper.connectToGroup(grpcConnectionId, this)
            }
        }

        override fun onConnectionComplete() {
            CoroutineScope(Dispatchers.Main).doAfter(2000) {
                if (isVisible) chatHelper.connectToGroup(grpcConnectionId, this)
            }
        }

        override fun onDynamicAction(chatMessage: Services.ChatMessage) {
            val response: StreamEventResponse = gson.fromJson(chatMessage.message, StreamEventResponse::class.java)
            if (response.type == AppConstants.MSG_TYPE_AUDIO_ROOM) {
                when (response.action) {
                    AppConstants.STATUS_MUTE ->
                        userMutableLiveData.postValue(AudioChatRoomActivityViewModel.AudioRoomAction.UpdateUser to AudioChatRoomActivityViewModel.UpdateData(actionUserName = response.actionUserName, ownerDetail = response.participant?.participantDetails?.also {
                            it.isDuplex = true
                        }))
                    AppConstants.STATUS_UNMUTE ->
                        userMutableLiveData.postValue(AudioChatRoomActivityViewModel.AudioRoomAction.UpdateUser to AudioChatRoomActivityViewModel.UpdateData(actionUserName = response.actionUserName, ownerDetail = response.participant?.participantDetails?.also {
                            it.isDuplex = true
                        }))
                    AppConstants.STATUS_JOINED -> {
                        userMutableLiveData.postValue(AudioChatRoomActivityViewModel.AudioRoomAction.AddUser to AudioChatRoomActivityViewModel.UpdateData(ownerDetail = response.participant?.participantDetails?.also { it.isDuplex = true }))
                    }
                    AppConstants.STATUS_LEFT -> {
                        userMutableLiveData.postValue(AudioChatRoomActivityViewModel.AudioRoomAction.DeleteUser to AudioChatRoomActivityViewModel.UpdateData(ownerDetail = response.participant?.participantDetails?.also { it.isDuplex = true }))
                    }
                    AppConstants.STATUS_CHATROOM_ACTIVATED -> {
                    }

                    AppConstants.STATUS_CHATROOM_ENDED -> userMutableLiveData.postValue(AudioChatRoomActivityViewModel.AudioRoomAction.FinishRoom to null)
                }
            }
        }
    }

    fun refresh() {
        if (CommonUtils.isFeaturedRoomEnabled()) {
            grpcConnectionId = null
            audioGroup.set(null)
            featuredChatRoomId = null
            grpcConnectionId = null
            isAllowedToSpeak = true
            isMuted.set(true)
            isSelfMuted = true
        }
        nextUrl = null
        isRefreshing.set(true)
        fetchAudioRoomList()
    }

    fun connectGrpc() {
        if (!grpcConnectionId.isNullOrEmpty())
            chatHelper.connectToGroup(grpcConnectionId, chatListener)
    }

    fun disconnectGrpc() {
        chatHelper.closeGroupConnection()
    }

    fun updateCurrentRoomId() {
        currentChatRoomId = featuredChatRoomId
        connectGrpc()
    }

    fun enterRoom() {
        if (!CommonUtils.isFeaturedRoomEnabled() || grpcConnectionId.isNullOrEmptyOrBlank() || AudioRoomService.isRunning) return
        Log.i(TAG, "connectAudio: startCall : $grpcConnectionId and ${AudioRoomService.isRunning}")
        connectGrpc()
        joinChatRoom()
    }

    fun leaveRoom() {
        if (!CommonUtils.isFeaturedRoomEnabled()) return
        disconnectGrpc()
        if (AudioRoomService.connectedRoomId != featuredChatRoomId) {
            leaveChatRoom()
        }
    }

    private fun userMuteState() {
        if (!isAllowedToSpeak || isMuted.get() == true)
            agoraConnectionUtils.muteLocalAudio()
        else
            agoraConnectionUtils.unMuteLocalAudio()
    }

    fun toggleMute() {
        isMuted.set(!(isMuted.get() ?: false))
//        userMuteState()
        muteUnMuteParticipant(CommonUtils.getUserName(), CommonUtils.getUserID(), if (isMuted.get() == true) "mute" else "unmute")
    }

    override fun onCleared() {
        super.onCleared()
//        if (CommonUtils.isFeaturedRoomEnabled() && !AudioRoomService.isRunning)
//            agoraConnectionUtils.releaseAgoraEngine()
    }

    fun fetchAudioRoomList() {
        setIsLoading(true)
        dataManager?.fetchAudioRoomList(nextUrl)?.enqueue(object : Callback<AudioRoomResponse> {
            override fun onResponse(call: Call<AudioRoomResponse>, response: Response<AudioRoomResponse>) {
                if (response.isSuccessful) {
                    audioRoomListLiveData.postValue(response.body()?.results)
                    // feature chat room params
                    if (nextUrl == null && CommonUtils.isFeaturedRoomEnabled()) {
                        response.body()?.featureChatRoom?.let {
                            audioGroup.set(it.chatRoomDetails?.groupDetails)
                            featuredChatRoomId = it.chatRoomDetails?.id
                            grpcConnectionId = it.chatRoomDetails?.grpcConnectionId
                            featuredChatRoomJoinUserCount = it.currentlyJoined
                            mAgoraChannelId = it.streamingService?.channelId ?: ""
                            mAgoraAccessToken = it.streamingService?.authToken ?: ""

                            if ((it.currentlyJoined ?: 0) < (it.maxConnections ?: 0)) {
                                isAllowedToSpeak = (it.currentlyJoined
                                        ?: 0) < (it.maxDuplexConnections ?: 0)
                                connectAudioLiveData.postValue(true)
                                loadConnectedUsers()
                            }
                        }
                    }
                    nextUrl = response.body()?.next
                }
                setIsLoading(false)
                isRefreshing.set(false)
            }

            override fun onFailure(call: Call<AudioRoomResponse>, t: Throwable) {
                t.printStackTrace()
                setIsLoading(false)
                isRefreshing.set(false)
            }
        })
    }

    fun searchResults() {
        if (searchQuery.get().isNullOrEmptyOrBlank()) return
        val q = searchQuery.get()
        dataManager?.searchRoom(searchQuery.get())?.enqueue(object : Callback<AudioRoomResponse> {
            override fun onResponse(call: Call<AudioRoomResponse>, response: Response<AudioRoomResponse>) {
                if (response.isSuccessful) {
                    if (q == searchQuery.get())
                        suggestions.postValue(response.body()?.results)
                }
            }

            override fun onFailure(call: Call<AudioRoomResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun loadConnectedUsers() {
        if (featuredChatRoomId.isNullOrEmpty()) return
        dataManager?.fetchAudioRoomConnectedUsers(featuredChatRoomId, userNextUrl)?.enqueue(object : Callback<ServerListResponse<OwnerDetail>> {
            override fun onResponse(call: Call<ServerListResponse<OwnerDetail>>, response: Response<ServerListResponse<OwnerDetail>>) {
                // here 404 means no connected user found
                if (response.isSuccessful) {
                    userNextUrl = response.body()?.next
                    response.body()?.results?.forEach {
                        it.isDuplex = true
                    }
                    featuredRoomParticipant.value = response.body()?.results
                }
            }

            override fun onFailure(call: Call<ServerListResponse<OwnerDetail>>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun joinChatRoom() {
        Log.i(TAG, "connectAudio: joinChatRoom : ${audioGroup.get()?.id}")
        if (audioGroup.get()?.id.isNullOrEmptyOrBlank()) return
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_JOIN_CHANNEL, analyticsProperties)
        dataManager?.joinChatRoom(audioGroup.get()?.id, featuredChatRoomId)
                ?.enqueue(object : Callback<ChatRoomActionResponse> {
                    override fun onResponse(call: Call<ChatRoomActionResponse>, response: Response<ChatRoomActionResponse>) {
                        if (response.isSuccessful) {
                            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_JOIN_CHANNEL_SUCCESS, analyticsProperties)
                            response.body()?.ownerDetail?.let {
                                featuredRoomParticipant.value = listOf(it)
                            }
                        } else {
                            SegmentTracker.getInstance()
                                    .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_JOIN_CHANNEL_FAILURE,
                                            HashMap(analyticsProperties).also {
                                                it["error"] = response.errorBody()?.string()
                                            })
                            try {
                                val error = Gson().fromJson(response.errorBody()?.string(), ChatRoomActionResponse::class.java)
                                Log.i(TAG, "Error --> ${error?.error}")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ChatRoomActionResponse>, t: Throwable) {
                        SegmentTracker.getInstance()
                                .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_JOIN_CHANNEL_FAILURE,
                                        HashMap(analyticsProperties).also {
                                            it["error"] = t.message
                                        })
                        t.printStackTrace()
                    }
                })
    }

    private fun leaveChatRoom() {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_LEAVE_CHANNEL, analyticsProperties)
        dataManager?.leaveChatRoom(featuredChatRoomId)?.enqueue(object : Callback<ChatRoomActionResponse> {
            override fun onResponse(call: Call<ChatRoomActionResponse>, response: Response<ChatRoomActionResponse>) {
                if (response.isSuccessful) {
                    SegmentTracker.getInstance()
                            .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_LEAVE_CHANNEL_SUCCESS,
                                    analyticsProperties)
                    userMutableLiveData.postValue(AudioChatRoomActivityViewModel.AudioRoomAction.AddUser to AudioChatRoomActivityViewModel.UpdateData(ownerDetail = OwnerDetail(
                            id = CommonUtils.getUserID(),
                            username = CommonUtils.getUserName(),
                            profileImageUrl = CommonUtils.getUserProfilePic(),
                            intro = ""
                    )))
                } else {
                    SegmentTracker.getInstance()
                            .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_LEAVE_CHANNEL_FAILURE,
                                    HashMap(analyticsProperties).also {
                                        it["error"] = response.errorBody()?.string()
                                    })
                    Log.i(TAG, "Error --> ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ChatRoomActionResponse>, t: Throwable) {
                SegmentTracker.getInstance()
                        .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_LEAVE_CHANNEL_FAILURE,
                                HashMap(analyticsProperties).also {
                                    it["error"] = t.message
                                })
                t.printStackTrace()
            }
        })
    }

    fun muteUnMuteParticipant(username: String?, userId: Int, action: String?) {
        dataManager?.muteUnMuteParticipant(
                audioGroup.get()?.id,
                featuredChatRoomId,
                username,
                userId.toString(),
                action
        )?.enqueue(object : Callback<ChatRoomActionResponse> {
            override fun onResponse(call: Call<ChatRoomActionResponse>, response: Response<ChatRoomActionResponse>) {
                if (response.isSuccessful)
                    Log.i(TAG, "Response -- > ${response.body()?.message}")
                else
                    Log.i(TAG, "Error --> ${response.errorBody()?.string()}")
            }

            override fun onFailure(call: Call<ChatRoomActionResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}