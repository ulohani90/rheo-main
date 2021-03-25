package com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel

import android.os.Handler
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.postlisting.responses.*
import com.rheotv.android.ui.base.BaseViewModel

import com.rheotv.android.utils.rx.SchedulerProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class RequestVideoCallViewModel(dataManager: DataManager?, schedulerProvider: SchedulerProvider?) : BaseViewModel<Any>(dataManager, schedulerProvider) {

    var coinValue: Int = -1

    var finalCoinValue: Int = -1

    var discountText: String? = null

    var postId: String? = null

    var authorName: String? = null

    var isAuthor: Boolean = false

    var nextUrl: String? = null

    var isLoadingData: Boolean = false

    var isFirstRequest: Boolean = false

    var unreadChatCount = ObservableField(0)

    var chatListShown = ObservableField(false)

    var commentNextUrl: String? = null

    var comments = MutableLiveData<List<CommentChat>>()

    var isLoadingComments = ObservableField(false);

    var screenSource: String = ""


    fun manageVideoCall(channelId: String?, userId: Int, action: VideoCallAction, onDataReceived: ((String?, Boolean, String?, String?, Int?) -> Unit)? = null) {

        dataManager.manageVideoCalls(channelId, userId, postId, action.name)?.enqueue(object : Callback<VideoCallResponse> {
            override fun onResponse(call: Call<VideoCallResponse>, response: Response<VideoCallResponse>) {
                if (response.isSuccessful) {
                    onDataReceived?.invoke(null, true, response.body()?.results?.channelId, response.body()?.results?.streamerAgoraToken, response.body()?.results?.sortedPosition)
                } else {
                    onDataReceived?.invoke(response.errorBody()?.string(), false, null, null, -1)
                }
            }

            override fun onFailure(call: Call<VideoCallResponse>, t: Throwable) {
                t.printStackTrace()
                onDataReceived?.invoke(t.message, false, null, null, -1)
            }
        })
    }

    fun getVideoRequestUsers(postId: String?, onDataReceived: ((List<VideoCallUsersListObject>?, Int?, String?, String?) -> Unit)?) {
        isFirstRequest = nextUrl == null
        isLoadingData = true
        dataManager.getVideoCallRequestedUsersList(postId, nextUrl)?.enqueue(object : Callback<VideoCallUsersList> {
            override fun onResponse(call: Call<VideoCallUsersList>, response: Response<VideoCallUsersList>) {
                if (response.isSuccessful && response.body() != null && response?.body()?.data != null) {
                    nextUrl = response.body()?.data?.next
                    response.body()?.callRequestCoinFee?.let { coinValue = it }
                    response.body()?.finalCallRequestCoinFee?.let { finalCoinValue = it }
                    discountText = response.body()?.discountText
                    onDataReceived?.invoke(response.body()?.data?.users, response.body()?.sortedPos, null, response.body()?.state)
                } else {
                    onDataReceived?.invoke(null, -1, response.errorBody()?.string(), null)
                }
            }

            override fun onFailure(call: Call<VideoCallUsersList>, t: Throwable) {
                onDataReceived?.invoke(null, -1, t.message, null)
            }
        })
    }

    fun loadComments() {
        dataManager.getStreamComments(postId, commentNextUrl).enqueue(object : Callback<Comments?> {
            override fun onResponse(call: Call<Comments?>, response: Response<Comments?>) {
                if (response.body() != null) {
                    if (response.body()!!.results != null && !response.body()!!.results.isEmpty()) {
                        val list = response.body()!!.results
                        Collections.reverse(list)
                        comments.value = list
                    }
                    commentNextUrl = response.body()!!.next
                    isLoadingComments.set(false)

                }
            }

            override fun onFailure(call: Call<Comments?>, t: Throwable) {
                isLoadingComments.set(false)
            }
        })
    }
}

sealed class VideoCallAction {
    object Request : VideoCallAction() {
        override val name = "request"
    }

    object End : VideoCallAction() {
        override val name = "end"
    }

    object Accept : VideoCallAction() {
        override val name = "accept"
    }

    object Start : VideoCallAction() {
        override val name = "start"
    }

    object Deny : VideoCallAction() {
        override val name = "deny"
    }

    object List : VideoCallAction() {
        override val name = "list"
    }

    object Refund : VideoCallAction() {
        override val name = "refund"
    }

    abstract val name: String
}