package com.rheotv.android.ui.activities.profile.viewprofile.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.gamify.DailyRewardsResponse
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat
import com.rheotv.android.data.network.models.postlisting.responses.Comments
import com.rheotv.android.data.network.models.stickers.Sticker
import com.rheotv.android.data.network.models.streamUpdates.StreamEventResponse
import com.rheotv.android.data.network.models.useProfile.responses.ChatGroupDetails
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult
import com.rheotv.android.db.AppDatabase
import com.rheotv.android.db.UserFollowDao
import com.rheotv.android.db.UserFollowItem
import com.rheotv.android.helpers.grpc.GrpcConnectionManager
import com.rheotv.android.helpers.grpc.IncomingChatListener
import com.rheotv.android.ui.activities.player.activity.*
import com.rheotv.android.ui.activities.profile.viewprofile.utils.CommentAction
import com.rheotv.android.ui.activities.profile.viewprofile.utils.CommentPublisher
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.*
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import goChat.Services
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class UserChatViewModel constructor(
        dataManager: DataManager?,
        schedulerProvider: SchedulerProvider?
) : BaseViewModel<Any>(dataManager, schedulerProvider) {
    var canChat = false
    var chatCriteriaMessage: String? = null
    val analyticsProperties: MutableMap<String, Any?> = hashMapOf()
    var connectionId: String? = null
    var postId: String? = null
    var username: String? = null
    lateinit var source: String
    var nextCommentUrl: String? = null
    var comments = MutableLiveData<List<CommentChat>>()
    var incomingComment = MutableLiveData<CommentChat>()
    var pinnedComment = ObservableField<CommentChat>()
    var removeComment = MutableLiveData<CommentChat>()
    var canComment: ObservableField<Boolean>? = ObservableField(true)
    var onAskUserLogin: ObservableField<Any> = ObservableField()
    var currentComment = ObservableField<String>()
    var suggestions = MutableLiveData<MutableList<String>>()
    var askToBuyCoins = MutableLiveData<Long>()
    val dao: UserFollowDao = AppDatabase.getInstance(RheoTvApp.getNonUiContext()).userFollowDao()
    val gson = Gson()
    var unreadChatCount = ObservableField(0)
    var updateCheckViews = MutableLiveData<Long>()
    var connectionAction = MutableLiveData<CommentAction>()

    protected val chatHelper by lazy { GrpcConnectionManager() }
    protected val commentPublisher by lazy {
        CommentPublisher {
            incomingComment.value = it
            updateCheckViews.value = System.currentTimeMillis()
        }
    }

    protected val stickerSelection = object : StickerGridRecyclerAdapter.StickerSelectionListener {
        override fun onStickerSelected(sticker: Sticker?) {
            sticker ?: return
            if (CommonUtils.isUserLoggedin()) {
                if (sticker.value <= RewardManager.getInstance().totalCoin) {
                    sendSticker(sticker)
                } else {
                    askToBuyCoins.value = System.currentTimeMillis()
                }
            } else
                askLogin()
        }

        override fun onStickerSelected(sticker: Sticker?, message: String?) {

        }

        override fun onBottomSheetClose() {

        }
    }

    fun showHint(): Boolean = CommonUtils.getUserName() != username

    protected val chatListener = object : IncomingChatListener() {
        override fun onReceived(chatMessage: Services.ChatMessage, isMine: Boolean) {
            this@UserChatViewModel.onReceived(chatMessage, isMine)
        }

        override fun onDelete(chatMessage: Services.ChatMessage) {
            this@UserChatViewModel.onDelete(chatMessage)
        }

        override fun onUserBlocked(chatMessage: Services.ChatMessage) {
            this@UserChatViewModel.onUserBlocked(chatMessage)
        }

        override fun onDynamicAction(chatMessage: Services.ChatMessage) {
            try {
                val response: StreamEventResponse = gson.fromJson(chatMessage.message, StreamEventResponse::class.java)
                onDynamicAction(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onPinned(chatMessage: Services.ChatMessage?) {
            this@UserChatViewModel.onPinned(chatMessage)
        }

        override fun waitAndReconnect() {
            Handler(Looper.getMainLooper()).postDelayed({
                Log.i(javaClass.simpleName, "Reconnecting chat client")
                connectionAction.value = CommentAction.Connect
//                connectChat()
            }, 2000)
        }

        override fun onConnectionComplete() {
            Handler(Looper.getMainLooper()).postDelayed({
                Log.i(javaClass.simpleName, "Reconnecting chat client")
                connectionAction.value = CommentAction.Connect
//                connectChat()
            }, 2000)
        }

        override fun updateGroupTotalCount(count: Double) {
            this@UserChatViewModel.updateGroupTotalCount(count)
        }

        override fun retryGroupConnect() {
            this@UserChatViewModel.retryGroupConnect()
        }
    }

    open fun onDynamicAction(response: StreamEventResponse) = Unit
    open fun retryGroupConnect() = Unit
    open fun updateGroupTotalCount(count: Double) = Unit
    open fun onReceived(chatMessage: Services.ChatMessage, isMine: Boolean) {
        if (isMine) {
            incomingComment.value = chatMessage.toCommentChat()
            updateCheckViews.value = System.currentTimeMillis()
        } else
            commentPublisher.add(chatMessage.toCommentChat())
    }

    open fun onDelete(chatMessage: Services.ChatMessage) {
        removeComment.value = chatMessage.toCommentChat()
    }

    open fun onUserBlocked(chatMessage: Services.ChatMessage) {
        removeComment.value = chatMessage.toCommentChat()
        if (CommonUtils.isUserLoggedin() && CommonUtils.getUserName().equals(chatMessage.sender, ignoreCase = true))
            canComment?.set(false)
    }

    open fun onPinned(chatMessage: Services.ChatMessage?) {
        pinnedComment.set(chatMessage?.toCommentChat())
    }

    open fun connectChat() {
        if (!connectionId.isNullOrEmpty())
            chatHelper.connectToGroup(connectionId, chatListener)
    }

    fun disconnectChat() {
        chatHelper.closeGroupConnection()
    }

    open fun sendActionMessage(commentChat: CommentChat) {
        chatHelper.sendMessage(connectionId, commentChat, chatListener)
    }

    open fun onGiftClick(view: View) {
        if (CommonUtils.isUserLoggedin()) {
            val stickerBottomSheet = StickerBottomSheet.newInstance(postId, "", false, stickerSelection)
            ((view.context) as? FragmentActivity)?.supportFragmentManager?.let { stickerBottomSheet.show(it, StickerBottomSheet.TAG) }
        } else {
            askLogin()
        }
    }

    fun sendMessage() {
        sendComment()
    }

    fun sendComment(message: String) {
        currentComment.set(message)
        sendComment()
    }

    open fun sendComment(message: String = currentComment.get() ?: "", messageType: String = AppConstants.MSG_TYPE_TEXT) {
        if (CommonUtils.isUserLoggedin()) {
            if (canComment?.get() == true && canChat) {
                val comment = CommentChat("", message, CommonUtils.getUserName(), CommonUtils.getUserProfilePic(), messageType)
                incomingComment.value = comment
                currentComment.set("")
                chatHelper.sendMessage(connectionId, comment, chatListener)
            } else {
                RheoTvApp.getNonUiContext().showToast(if (!chatCriteriaMessage.isNullOrEmpty()) chatCriteriaMessage
                else "You are not allowed to send message to $username")
            }
        }
    }

    open fun getGroupId() {
        dataManager.getConnectionDetails(username).enqueue(object : Callback<ChatGroupDetails> {
            override fun onFailure(call: Call<ChatGroupDetails>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ChatGroupDetails>, response: Response<ChatGroupDetails>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        connectionId = it.connectionId
                        postId = it.postId
                        if (it.pinnedComment?.message != null) pinnedComment.set(it.pinnedComment)
                        suggestions.value = it.messageSuggestion
                        if (!chatHelper.isConnectChatRequestMade.get()) {
                            connectChat()
                        }
                    }
                }
            }
        })
    }

    protected var isFirstCommentLoaded = false

    open fun loadComments() {
        isLoading.set(true)
        dataManager.getUserComments(username, nextCommentUrl).enqueue(object : Callback<Comments> {
            override fun onResponse(call: Call<Comments>, response: Response<Comments>) {
                response.body()?.let {
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

    open fun onCommentAction(comment: CommentChat, action: CommentAction) {
        dataManager.onChatAction(postId, comment.username, comment.message, action.path).enqueue(object : Callback<ResponseBody> {
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

    fun pinComment(commentChat: CommentChat) {
        togglePinComment(commentChat, true)
    }

    fun unPinComment() {
        pinnedComment.get()?.let {
            togglePinComment(it, false)
        }
    }

    open fun togglePinComment(commentChat: CommentChat, isPinned: Boolean) {
        commentChat.apply { messageType = AppConstants.MSG_PIN }
        val map = HashMap<String, Any?>(analyticsProperties).apply {
            "is_pinned" to isPinned
        }

        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_COMMENT_PINNED_IN_CHATROOM, map)
        dataManager
                .pinComment(postId, commentChat.username, if (isPinned) commentChat.message else "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableObserver<ResponseBody>() {
                    override fun onNext(responseBody: ResponseBody) {
                        try {
                            if (responseBody.string().contains("success")) {
                                pinnedComment.set(if (isPinned) commentChat else null)
                                chatHelper.sendMessage(connectionId,
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

    fun getChatOptionMenuBottomSheetData(username: String?, profilePic: String?): ChatMenuOptionData {
        return ChatMenuOptionData(
                username, profilePic, this.username,
                { followUserName, listener, followStatusListener ->
                    getUserDetails(followUserName, listener, followStatusListener)
                },
                { followState, followUserId, followUserName, listener ->
                    val map = HashMap<String, Any>()
                    map["is_first"] = CommonUtils.isFirstTimeFollow()
                    map["author"] = this.username ?: ""
                    map["source"] = SegmentConstants.SCREEN_NAME_VIDEO_PLAYER_CHAT
                    SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map)
                    CommonUtils.setFirstTimeFollow()
                    onFollowButtonClick(followState, followUserId, followUserName, listener)
                },
                {
                    askLogin()
                }, null)
    }

    protected fun askLogin() {
        onAskUserLogin.set("")
    }

    protected fun onFollowButtonClick(followState: String?, userId: Int, username: String?, listener: FollowStatusCompleteListener?) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateUserEntry(UserFollowItem(userId, username, followState == "follow"))
            withContext(Dispatchers.Main) { listener?.success() }
        }
        if (CommonUtils.isUserLoggedin()) {
            dataManager.toggleFollow(followState, userId.toString()).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (listener != null) {
//                        if (response.isSuccessful) {
//                            listener.success()
//                        } else {
//                            listener.error()
//                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }
            })
        } else {
            askLogin()
        }
    }

    protected fun getUserDetails(username: String?, callback: ApiCompleteListener?, followStatusListener: FollowStatusListener?) {
        viewModelScope.launch(Dispatchers.IO) {
            username?.let {
                dao.checkIfIsFollowedWithUsername(it)?.also { userFollowItem ->
                    viewModelScope.launch(Dispatchers.Main) {
                        followStatusListener?.followStatus(userFollowItem.isFollowed)
                    }
                }
            }
        }
        dataManager.getProfile(username).enqueue(object : Callback<ProfileResult> {
            override fun onResponse(call: Call<ProfileResult>, response: Response<ProfileResult>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        viewModelScope.launch(Dispatchers.IO) {
                            dao.insertUserWithIgnore(UserFollowItem(it.user.id, it.user.username, it.user.isFollowed))
                        }
                        callback?.updateProfileDataForBottomSheet(FollowResult.Success(it))
                    }
                }
            }

            override fun onFailure(call: Call<ProfileResult>, t: Throwable) {
                callback?.updateProfileDataForBottomSheet(FollowResult.Error(t))
            }
        })
    }

    open fun sendSticker(sticker: Sticker) {
        if (sticker.value > RewardManager.getInstance().totalCoin) {
            RheoTvApp.getNonUiContext().showToast("You don't have enough coin to send this sticker.")
            return
        }

        compositeDisposable.add(dataManager
                .buySticker(sticker.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ next: ResponseBody? ->
                    if (next != null) {
                        try {
                            val jsonObject = JSONObject(next.string())
                            if (jsonObject.has("success") && jsonObject.getBoolean("success")) {
                                currentComment.set(sticker.stickerUrl)
                                sendComment(messageType = AppConstants.MSG_TYPE_STICKER)
                                RewardManager.getInstance().reduceCoin(sticker.value)
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }) { throwable: Throwable -> Log.e(javaClass.simpleName, "Error in api sendSticker --> " + throwable.message) })
    }

    fun loadDailyRewards() {
        if (!CommonUtils.isUserLoggedin()) return
        dataManager.dailyRewards.enqueue(object : Callback<DailyRewardsResponse> {
            override fun onResponse(call: Call<DailyRewardsResponse>, response: Response<DailyRewardsResponse>) {
                try {
                    RewardManager.getInstance().dailyRewards = response.body()?.results
                    RewardManager.getInstance().totalCoins = response.body()?.totalCoins
                    RewardManager.getInstance().setShouldAskRating(response.body()?.canGiveFeedback
                            ?: false)
                    RewardManager.getInstance().isCodaEnabled = response.body()?.isCodaEnabled
                            ?: true
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<DailyRewardsResponse>, t: Throwable) {
                if (t != null) Log.i(javaClass.name, "loadDailyRewards: " + t.message)
            }
        })
    }

}