package com.rheotv.android.ui.activities.audioroom.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.databinding.PropertyChangeRegistry
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.general.SignedUrlResponse
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat
import com.rheotv.android.data.network.models.postlisting.responses.Comments
import com.rheotv.android.data.network.models.stickers.Sticker
import com.rheotv.android.data.network.models.streamUpdates.StreamEventResponse
import com.rheotv.android.data.network.models.useProfile.responses.PictureUploadResult
import com.rheotv.android.ui.activities.audioroom.model.*
import com.rheotv.android.ui.activities.player.activity.FollowStatusListener
import com.rheotv.android.ui.activities.player.activity.StickerBottomSheet
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment
import com.rheotv.android.ui.activities.profile.model.FeaturedPhoto
import com.rheotv.android.ui.activities.profile.viewprofile.utils.CommentAction
import com.rheotv.android.ui.activities.profile.viewprofile.viewmodel.UserChatViewModel
import com.rheotv.android.utils.*
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import java.util.concurrent.CancellationException
import kotlin.collections.HashMap

const val COMMENT_AUDIO_MESSAGE = "audio_message"

class AudioChatRoomActivityViewModel(
        dataManager: DataManager?,
        schedulerProvider: SchedulerProvider?
) : UserChatViewModel(dataManager, schedulerProvider), Observable {

    private val callbacks = PropertyChangeRegistry()
    val TAG = "AudioChatRoomActVM"
    val agoraConnectionUtils = AgoraConnectionUtils()
    var audioGroup = ObservableField<AudioGroup?>()
    var audioGroupId: String? = null
    var onlineMemberCount = ObservableField<Double>()
    var uploadProgress = ObservableField<Int>()
    var chatRoomId: String? = null
    var userNextUrl: String? = null
    var isMicMuted: Boolean = false
    var isVolumeEnabled: Boolean = true
    var agoraChannelId: String = ""
    var agoraToken: String = ""
    val connectUserListMutableLiveData = MutableLiveData<List<OwnerDetail>?>()
    val userMutableLiveData = MutableLiveData<Pair<AudioRoomAction, UpdateData?>>()
    val connectAudioLiveData = MutableLiveData<Boolean?>(false)
    val isStreamerFollowed = MutableLiveData<Boolean>()
    val ownerAudioAction = MutableLiveData<CommentAction>()
    var chatRoomDetail: ChatRoomDetails? = null
    var onStreamerWentLive = MutableLiveData<Boolean>()
    var onFollowerStream = MutableLiveData<Boolean>()
    var grpcConnectionId: String? = null
    var isUserBlocked = MutableLiveData(false)
    var isAllowedToSpeak: Boolean = true
    var hasGameStarted = ObservableField(false)
    var roomActionLiveData = MutableLiveData<Pair<AudioRoomAction, Any?>>()

    var highlightedUser: OwnerDetail? = null

    var isStayingConnected: Boolean = true
    var currentMessageId: String = "${System.currentTimeMillis()}"

    var roomName: String
        @Bindable
        get() = audioGroup.get()?.name ?: ""
        set(value) {
            audioGroup.get()?.name = value
            notifyPropertyChanged(BR.roomName)
        }

    var isStreamLive: Boolean
        @Bindable
        get() = audioGroup.get()?.ownerDetails?.isLive ?: false
        set(value) {
            audioGroup.get()?.ownerDetails?.isLive = value
            notifyPropertyChanged(BR.streamLive)
        }

    var authorName: String
        @Bindable
        get() = audioGroup.get()?.ownerDetails?.username ?: ""
        set(value) {
            audioGroup.get()?.ownerDetails?.username = value
            notifyPropertyChanged(BR.authorName)
        }

    var gameName: String
        @Bindable
        get() = audioGroup.get()?.ownerDetails?.gameName ?: ""
        set(value) {
            audioGroup.get()?.ownerDetails?.gameName = value
            notifyPropertyChanged(BR.gameName)
        }

    var authorProfilePicture: String
        @Bindable
        get() = audioGroup.get()?.ownerDetails?.profileImageUrl ?: ""
        set(value) {
            audioGroup.get()?.ownerDetails?.profileImageUrl = value
            notifyPropertyChanged(BR.authorProfilePicture)
        }

    val roomDetails: AudioRoomDetail?
        get() = AudioRoomDetail(
                isMicMuted,
                isVolumeEnabled,
                audioGroup.get(),
                onlineMemberCount.get()?.toInt() ?: 0,
                chatRoomId,
                audioGroup.get()?.id ?: audioGroupId,
                if (!audioGroup.get()?.logoUrl.isNullOrEmpty()) audioGroup.get()?.logoUrl else audioGroup.get()?.ownerDetails?.profileImageUrl,
                audioGroup.get()?.name ?: audioGroup.get()?.ownerDetails?.username,
                agoraToken,
                agoraChannelId,
                grpcConnectionId
        )

    val isStreamer: Boolean
        get() = CommonUtils.getUserID() == audioGroup.get()?.ownerDetails?.id

    fun fetchAudioChatRoomDetail() {
        if (chatRoomId.isNullOrEmpty() || chatRoomId?.toLowerCase(Locale.getDefault()) == "null") return
        loadConnectedUsers()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = dataManager
                        ?.fetchAudioRoomDetail(audioGroup.get()?.id ?: audioGroupId, chatRoomId)
                        ?.execute()
                if (result?.isSuccessful == true) {
                    Log.e("universal", "${result.body()?.toString()}")
                    performSuccessAction(result.body())
                } else {
                    Log.e("universal", "${result?.errorBody()?.string()?.toString()}")
//                    Log.e(TAG, result?.errorBody()?.string())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, e.stackTraceToString())
            }

        }
    }

    fun loadConnectedUsers() {
        if (chatRoomId.isNullOrEmpty()) return
        dataManager?.fetchAudioRoomConnectedUsers(chatRoomId, userNextUrl)?.enqueue(object : Callback<ServerListResponse<OwnerDetail>> {
            override fun onResponse(call: Call<ServerListResponse<OwnerDetail>>, response: Response<ServerListResponse<OwnerDetail>>) {
                if (response.isSuccessful) {
                    Log.e("universal", "${response.body()?.toString()}")
                    userNextUrl = response.body()?.next
                    response.body()?.results?.forEach {
                        it.isDuplex = true
                    }
                    connectUserListMutableLiveData.value = response.body()?.results
                } else {
                    Log.e("universal", "${response.errorBody()?.string()?.toString()}")
                }
            }

            override fun onFailure(call: Call<ServerListResponse<OwnerDetail>>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private suspend fun performSuccessAction(result: CreateAudioRoomResponse?) {
        agoraConnectionUtils.agoraAccessToken = result?.streamingService?.authToken ?: ""
        agoraConnectionUtils.agoraChannelId = result?.streamingService?.channelId ?: ""
        agoraChannelId = result?.streamingService?.channelId ?: ""
        agoraToken = result?.streamingService?.authToken ?: ""
        chatRoomDetail = result?.chatRoomDetails
        grpcConnectionId = result?.chatRoomDetails?.grpcConnectionId
        connectionId = result?.chatRoomDetails?.grpcConnectionId
        highlightedUser = chatRoomDetail?.highlightedUser
        isUserBlocked.postValue(result?.isUserBlocked)
        val isFollowed = (dao.checkIfIsFollowedWithUserId(result?.chatRoomDetails?.groupDetails?.ownerDetails?.id
                ?: 0)?.isFollowed ?: false) or (result?.isStreamerFollowed == true)
        if (result?.isUserBlocked == true) {
            return
        }

        if (audioGroup.get() == null) {
            withContext(Dispatchers.Main) {
                audioGroup.set(result?.chatRoomDetails?.groupDetails)
                notifyPropertyChanged(BR.roomName)
            }
            analyticsProperties["chatroom_author_name"] = result?.chatRoomDetails?.groupDetails?.ownerDetails?.username
            username = result?.chatRoomDetails?.groupDetails?.ownerDetails?.username
            canChat = true
            onStreamerWentLive.postValue(result?.chatRoomDetails?.groupDetails?.ownerDetails?.isLive)
        }
        if ((result?.currentlyJoined ?: 0) < (result?.maxConnections
                        ?: 0) && (CommonUtils.getUserID() == result?.chatRoomDetails?.groupDetails?.ownerDetails?.id || isFollowed)) {
            isAllowedToSpeak = (result?.currentlyJoined ?: 0) < (result?.maxDuplexConnections ?: 0)
            connectAudioLiveData.postValue(true)
            isStreamerFollowed.postValue(CommonUtils.getUserID() == result?.chatRoomDetails?.groupDetails?.ownerDetails?.id || isFollowed)
        } else {
            isStreamerFollowed.postValue(CommonUtils.getUserID() == result?.chatRoomDetails?.groupDetails?.ownerDetails?.id)
        }


        withContext(Dispatchers.Main) {
            getGroupId()
            loadComments()
        }
        result?.chatRoomDetails?.pinnedComment?.also {
            pinnedComment.set(it)
        }
    }

    override fun getGroupId() {
        if (!chatHelper.isConnectChatRequestMade.get()) {
            connectChat()
        }
    }

    fun followOwner() {
        val map = HashMap<String, Any>(analyticsProperties)
        map["is_first"] = CommonUtils.isFirstTimeFollow()
        map["author"] = audioGroup?.get()?.ownerDetails?.username ?: ""
        map["source"] = SegmentConstants.SCREEN_NAME_AUDIO_CHAT_ROOM
        SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map)
        dataManager?.toggleFollowState(audioGroup.get()?.ownerDetails?.username
                ?: "", audioGroup.get()?.ownerDetails?.id
                ?: 0, true, false, object : FollowStatusListener {
            override fun followStatus(isFollowed: Boolean) {
                isStreamerFollowed.postValue(isFollowed)
                if (isFollowed) {
                    connectAudioLiveData.postValue(true)
                    // a dirty fallback hack, since backend server is not reliable when it comes to follow user state
                    viewModelScope.launch(Dispatchers.IO) {
                        delay(2 * 1000)
                        withContext(Dispatchers.Main) {
                            onFollowerStream.value = isFollowed
                        } // todo handle this in AudioRoomService
                    }
                }
            }
        })
    }

    fun changeGameState(action: String, gameId: String, successAction: (() -> Unit)? = null) {
        dataManager.startAudioRoomGame(action, chatRoomId, gameId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    successAction?.invoke()
                } else {
                    try {
                        RheoTvApp.getNonUiContext()
                                ?.showToast(JSONObject(response.errorBody()?.string()
                                        ?: return).getString("error"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                RheoTvApp.getNonUiContext()?.showToast(t.message)
            }
        })
    }

    fun highlightUser(action: String, userId: Int = -1, successAction: ((OwnerDetail?) -> Unit)? = null) {
        dataManager.highlightAudioRoomUser(chatRoomId, action, userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.e("universal", "${response.body()?.toString()}")
                    if (action == "pick_random") {
                        try {
                            val data = Gson().fromJson(response.body()?.string(), OwnerDetail::class.java)
                            successAction?.invoke(if (data?.id != null) data else null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            successAction?.invoke(null)
                        }
                    } else
                        successAction?.invoke(null)
                } else {
                    Log.e("universal", "${response.errorBody()?.string()?.toString()}")
                    try {
                        RheoTvApp.getNonUiContext()
                                ?.showToast(JSONObject(response.errorBody()?.string()
                                        ?: return).getString("error"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                RheoTvApp.getNonUiContext()?.showToast(t.message)
            }
        })
    }

    fun joinChatRoom() {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_JOIN_CHANNEL, analyticsProperties)
        dataManager?.joinChatRoom(audioGroup.get()?.id ?: audioGroupId, chatRoomId)
                ?.enqueue(object : Callback<ChatRoomActionResponse> {
                    override fun onResponse(call: Call<ChatRoomActionResponse>, response: Response<ChatRoomActionResponse>) {
                        if (response.isSuccessful) {
                            Log.e("universal", "${response.body()?.toString()}")
                            SegmentTracker.getInstance()
                                    .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_JOIN_CHANNEL_SUCCESS,
                                            analyticsProperties)
                            response.body()?.ownerDetail?.let {
                                connectUserListMutableLiveData.value = listOf(it)
                            }
                        } else {
                            Log.e("universal", "${response.errorBody()?.string()?.toString()}")
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

    fun leaveChatRoom() {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_LEAVE_CHANNEL, analyticsProperties)
        dataManager?.leaveChatRoom(chatRoomId)?.enqueue(object : Callback<ChatRoomActionResponse> {
            override fun onResponse(call: Call<ChatRoomActionResponse>, response: Response<ChatRoomActionResponse>) {
                if (response.isSuccessful) {
                    Log.e("universal", "${response.body()?.toString()}")
                    SegmentTracker.getInstance()
                            .trackEvent(SegmentConstants.EVENT_CHAT_ROOM_LEAVE_CHANNEL_SUCCESS,
                                    analyticsProperties)
                    userMutableLiveData.postValue(AudioRoomAction.AddUser to UpdateData(ownerDetail = OwnerDetail(
                            id = CommonUtils.getUserID(),
                            username = CommonUtils.getUserName(),
                            profileImageUrl = CommonUtils.getUserProfilePic(),
                            intro = ""
                    )))
                } else {
                    Log.e("universal", "${response.errorBody()?.string()?.toString()}")
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
        dataManager?.muteUnMuteParticipant(audioGroup.get()?.id, chatRoomId, username, userId.toString(), action)
                ?.enqueue(object : Callback<ChatRoomActionResponse> {
                    override fun onResponse(call: Call<ChatRoomActionResponse>, response: Response<ChatRoomActionResponse>) {
                        if (response.isSuccessful)
                            Log.i("universal", "Response -- > ${response.body()?.toString()}")
                        else
                            Log.i("universal", "Error --> ${response.errorBody()?.string()}")
                    }

                    override fun onFailure(call: Call<ChatRoomActionResponse>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
    }

    fun ownerAction(ownerDetail: OwnerDetail?, action: CommentAction) {
        /*if (action is CommentAction.Block) {
            val message = StreamEventResponse(chatRoomId, AppConstants.MSG_TYPE_BLOCK_FROM_AUDIO_ROOM, Participant(ownerDetail)).also {
                it.type = AppConstants.MSG_TYPE_AUDIO_ROOM
            }
            val comment = CommentChat().also {
                it.message = gson.toJson(message)
                it.id = ""
                it.profile_pic = CommonUtils.getUserProfilePic() ?: ""
                it.username = CommonUtils.getUserName() ?: ""
                it.messageType = AppConstants.MSG_DYNAMIC_PLAYER_ACTIONS
            }
            sendActionMessage(comment)
            onCommentAction(comment.apply {
                username = ownerDetail?.username ?: ""
                profile_pic = ownerDetail?.profileImageUrl ?: ""
            }, action)
        } else {
            val comment = CommentChat().also {
                it.message = COMMENT_AUDIO_MESSAGE
                it.id = ""
                it.profile_pic = ownerDetail?.profileImageUrl ?: ""
                it.username = ownerDetail?.username ?: ""
                it.messageType = COMMENT_AUDIO_MESSAGE
            }
            onCommentAction(comment, action)
        }*/
        val comment = CommentChat().also {
            it.message = COMMENT_AUDIO_MESSAGE
            it.id = ""
            it.profile_pic = ownerDetail?.profileImageUrl ?: ""
            it.username = ownerDetail?.username ?: ""
            it.messageType = COMMENT_AUDIO_MESSAGE
        }
        onCommentAction(comment, action)
    }

    fun updateGroupName(name: String) {
        if (!CommonUtils.isUserLoggedin()) return
        dataManager.updateAudioGroupName(audioGroup.get()?.id
                ?: audioGroupId, name).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.e("universal", "${response.body()?.toString()}")
                    roomName = name
                    RheoTvApp.getNonUiContext().showToast("Name Updated Successfully")
                } else {
                    Log.e("universal", "${response.errorBody()?.string()?.toString()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    override fun sendSticker(sticker: Sticker) {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_STICKER_SENT,
                HashMap(analyticsProperties ?: hashMapOf())?.apply {
                    put("coins_spent", sticker.value)
                })
        super.sendSticker(sticker)
    }

    override fun onDynamicAction(response: StreamEventResponse) {
        if (response.type == AppConstants.MSG_TYPE_AUDIO_ROOM) {
            when (response.action) {
                AppConstants.STATUS_MUTE ->
                    userMutableLiveData.postValue(AudioRoomAction.UpdateUser to UpdateData(actionUserName = response.actionUserName, ownerDetail = response.participant?.participantDetails?.also {
                        it.isMuted = true
                        it.isDuplex = true
                    }))
                AppConstants.STATUS_UNMUTE ->
                    userMutableLiveData.postValue(AudioRoomAction.UpdateUser to UpdateData(actionUserName = response.actionUserName, ownerDetail = response.participant?.participantDetails?.also {
                        it.isMuted = false
                        it.isDuplex = true
                    }))
                AppConstants.STATUS_JOINED -> {
                    userMutableLiveData.postValue(AudioRoomAction.AddUser to UpdateData(ownerDetail = response.participant?.participantDetails?.also { it.isDuplex = true }))
                }
                AppConstants.STATUS_LEFT -> {
                    userMutableLiveData.postValue(AudioRoomAction.DeleteUser to UpdateData(ownerDetail = response.participant?.participantDetails?.also { it.isDuplex = true }))
                }
                AppConstants.STATUS_CHATROOM_ACTIVATED -> {
                }
                AppConstants.STATUS_CHATROOM_ENDED ->
                    userMutableLiveData.postValue(AudioRoomAction.FinishRoom to null)
                AppConstants.STATUS_BLOCKED ->
                    ownerAudioAction.postValue(CommentAction.Block(response.participant?.participantDetails?.username
                            ?: ""))
                AppConstants.STATUS_STREAMER_WENT_LIVE -> {
                    isStreamLive = true
                    authorName = response.username
                    gameName = response.gameName
                    authorProfilePicture = response.userProfileUrl
                    onStreamerWentLive.postValue(true)
                    Handler(Looper.getMainLooper()).post {
                        audioGroup.get()?.ownerDetails?.livePostId = response.postId
                    }
                }
                AppConstants.STATUS_HIGHLIGHTED -> {
                    roomActionLiveData.postValue(AudioRoomAction.UserHighlighted to response.highlighterRoomUser)
                }
                AppConstants.STATUS_UN_HIGHLIGHTED -> {
                    roomActionLiveData.postValue(AudioRoomAction.UserUnHighlighted to response.highlighterRoomUser)
                }
                AppConstants.STATUS_GAME_STARTED -> {
                    roomActionLiveData.postValue(AudioRoomAction.GameStarted to response.socialGame)
                }
                AppConstants.STATUS_GAME_ENDED -> {
                    roomActionLiveData.postValue(AudioRoomAction.GameEnded to response.socialGame)
                }
            }
        }
    }

    override fun updateGroupTotalCount(count: Double) {
//        onlineMemberCount.set(count)
    }

    private fun connectTotalCount() {
//        if (!grpcConnectionId.isNullOrEmpty())
//            chatHelper?.getGroupTotalCount(grpcConnectionId, chatListener)
    }

    private var mJob: Job? = null
    override fun retryGroupConnect() {
        mJob?.cancel(CancellationException("New Job requested!"))
        mJob = viewModelScope.launch(Dispatchers.IO) {
            Log.i(TAG, "rety delayed")
            delay(2 * 1000)
            withContext(Dispatchers.Main) {
                connectTotalCount()
            }
        }
    }

    override fun onCleared() {
        mJob?.cancel(CancellationException("ViewModel destroyed!"))
        super.onCleared()
    }

    override fun connectChat() {
        if (!grpcConnectionId.isNullOrEmpty())
            chatHelper.connectToGroup(grpcConnectionId, chatListener)
        connectTotalCount()
    }

    override fun sendActionMessage(commentChat: CommentChat) {
        if (!grpcConnectionId.isNullOrEmpty())
            chatHelper.sendMessage(grpcConnectionId, commentChat, chatListener)
    }

    fun sendMedia(id: String = "", url: String?, mimeType: String?) {
        if (CommonUtils.isUserLoggedin()) {
            if (canComment?.get() == true && canChat) {
                val comment = CommentChat(id, url
                        ?: return, CommonUtils.getUserName(), CommonUtils.getUserProfilePic(), mimeType?.toUpperCase()
                        ?: return)
                incomingComment.value = comment
                chatHelper.sendMessage(grpcConnectionId, comment, chatListener)
            } else {
                RheoTvApp.getNonUiContext().showToast(
                        if (!chatCriteriaMessage.isNullOrEmpty())
                            chatCriteriaMessage
                        else
                            "You are not allowed to send message to $username"
                )
            }
        }
    }

    override fun togglePinComment(commentChat: CommentChat, isPinned: Boolean) {
        commentChat.apply { messageType = AppConstants.MSG_PIN }
        val map = HashMap<String, Any?>(analyticsProperties).apply {
            "is_pinned" to isPinned
        }

        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COMMENT_PINNED_IN_CHATROOM, map)
        dataManager
                .pinComment(grpcConnectionId, commentChat.username, if (isPinned) commentChat.message else "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableObserver<ResponseBody>() {
                    override fun onNext(responseBody: ResponseBody) {
                        try {
                            Log.e("universal", "${responseBody.string()}")
                            if (responseBody.string().contains("success")) {
                                pinnedComment.set(if (isPinned) commentChat else null)
                                chatHelper.sendMessage(grpcConnectionId,
                                        if (isPinned) commentChat else commentChat.apply { message = "" },
                                        chatListener, AppConstants.MSG_PIN)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    override fun onError(e: Throwable) {
                        Log.e(javaClass.simpleName, "Error in api pinComment --> " + e.message)
                    }

                    override fun onComplete() {

                    }
                })
    }

    override fun loadComments() {
        if (grpcConnectionId.isNullOrEmpty()) return
        isLoading.set(true)
        dataManager.getStreamComments(grpcConnectionId, nextCommentUrl).enqueue(object : Callback<Comments> {
            override fun onResponse(call: Call<Comments>, response: Response<Comments>) {
                response.body()?.let {
                    Log.e("universal", "${response.body()?.toString()}")
                    it.results ?: return
                    if (nextCommentUrl == null && !isFirstCommentLoaded) {
                        isFirstCommentLoaded = true
                        it.results.reverse()
                        commentPublisher.addList(it.results)
                    } else {
                        comments.setValue(it.results)
                    }
                    nextCommentUrl = it.next
                    isLoading.set(false)
                }
            }

            override fun onFailure(call: Call<Comments>, t: Throwable) {
                isLoading.set(false)
                t.printStackTrace()
            }
        })
    }

    sealed class AudioRoomAction : Parcelable {
        @Parcelize
        object AddUser : AudioRoomAction()

        @Parcelize
        object DeleteUser : AudioRoomAction()

        @Parcelize
        object UpdateUser : AudioRoomAction()

        @Parcelize
        object FinishRoom : AudioRoomAction()

        @Parcelize
        object UserHighlighted : AudioRoomAction()

        @Parcelize
        object UserUnHighlighted : AudioRoomAction()

        @Parcelize
        object GameStarted : AudioRoomAction()

        @Parcelize
        object GameEnded : AudioRoomAction()
    }

    @Parcelize
    data class UpdateData(
            val actionUserName: String? = null,
            val ownerDetail: OwnerDetail? = null
    ) : Parcelable

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.remove(callback)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }

    override fun onGiftClick(view: View) {
        if (CommonUtils.isUserLoggedin()) {
            val stickerBottomSheet = StickerBottomSheet.newInstance(grpcConnectionId, "", false, stickerSelection)
            ((view.context) as? FragmentActivity)?.supportFragmentManager?.let { stickerBottomSheet.show(it, StickerBottomSheet.TAG) }
        } else {
            askLogin()
        }
    }

    override fun onCommentAction(comment: CommentChat, action: CommentAction) {
        dataManager.onChatAction(grpcConnectionId, comment.username, comment.message, action.path).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful)
                    RheoTvApp.getNonUiContext()?.showToast(
                            when (action) {
                                CommentAction.Delete -> R.string.delete_comment_success
                                CommentAction.Report -> R.string.post_report_success
                                else -> R.string.user_block_message
                            }
                    )
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun compressFile(context: Context, fileUri: String, onCompress: ((String) -> Unit)) {
        viewModelScope.launch(Dispatchers.IO) {
            File(fileUri).compress(context, onCompress) {
                uploadProgress.set(it)
            }
        }
    }

    fun uploadFile(mimeType: String, fileUri: String, onFileUpload: ((String?, String?, Int, String?) -> Unit)) {
        dataManager.getSignedUrl(mimeType, grpcConnectionId, "s3")
                .enqueue(object : Callback<SignedUrlResponse> {
                    override fun onResponse(call: Call<SignedUrlResponse>, response: Response<SignedUrlResponse>) {
                        if (response.isSuccessful) {
                            onFileUpload.invoke(fileUri, response.body()?.uploadUrl, AppConstants.S3_STORAGE, mimeType)
                        }
                    }

                    override fun onFailure(call: Call<SignedUrlResponse>, t: Throwable) {
                        RheoTvApp.getNonUiContext().showToast(
                                if (NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext()))
                                    "Error While Uploading"
                                else
                                    "No Network"
                        )

                        t.printStackTrace()
                    }
                })
    }

    /**
     * Notifies observers that all properties of this instance have changed.
     */
    internal fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies observers that a specific property has changed. The getter for the
     * property that changes should be marked with the @Bindable annotation to
     * generate a field in the BR class to be used as the fieldId parameter.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    internal fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }
}