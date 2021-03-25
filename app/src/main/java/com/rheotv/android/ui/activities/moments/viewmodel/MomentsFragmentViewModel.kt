package com.rheotv.android.ui.activities.moments.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.databinding.ObservableField
import androidx.databinding.library.baseAdapters.BR
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat
import com.rheotv.android.data.network.models.postlisting.responses.Comments
import com.rheotv.android.data.network.models.postlisting.responses.Result
import com.rheotv.android.ui.activities.moments.model.MomentsListItem
import com.rheotv.android.ui.activities.player.activity.ViewPagerMediator
import com.rheotv.android.ui.activities.player.activity.newPlayer.StreamPlayerViewModelV2
import com.rheotv.android.utils.Status
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.showToast
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.pow

class MomentsFragmentViewModel(dataManager: DataManager?, schedulerProvider: SchedulerProvider?) :
        StreamPlayerViewModelV2(dataManager, schedulerProvider) {

    private val TAG = javaClass.simpleName
    val moments = ObservableField<MomentsListItem>()
    val isBlockedLoading = ObservableField(false)

    override fun loadInitialComments(previousComments: MutableList<CommentChat>?) {
        if (moments.get()?.isContentModerator == true) return
        loadComments()
    }

    override fun loadComments() {
        if (moments.get()?.isContentModerator == true) return
        dataManager?.fetchMomentComments(postId, moments.get()?.postCreatedAtTimestamp,
                moments.get()?.seekStartedAt?.toLong() ?: 0L,
                moments.get()?.seekEndedAt?.toLong()
                        ?: 0L, commentNextUrl)?.enqueue(object : Callback<Comments> {
            override fun onResponse(call: Call<Comments>, response: Response<Comments>) {
                if (messageHandler != null) {
                    messageHandler.post {
                        response.body()?.let {
                            if (!it.results.isNullOrEmpty()) {
                                if (commentNextUrl == null) {
                                    // todo
                                    if (streamMessageHandler != null) streamMessageHandler.addList(it.results.reversed())
                                } else {
                                    comments.setValue(it.results)
                                }
                            }
                            commentNextUrl = it.next
                        }

                    }
                }
                isLoading.set(false)
            }

            override fun onFailure(call: Call<Comments>, t: Throwable) {
                t.printStackTrace()
                isLoading.set(false)
                if (commentRetryCount < 3) Handler().postDelayed({ loadInitialComments(null) }, (2.0.pow(commentRetryCount.toDouble()) * 1000).toLong())
            }
        })
    }

    fun loadPost() {
        postRetryCount++
        loadPostStatus.value = Status.LOADING
        loadInitialComments(null)
        dataManager.getSpecificPostWithUid(postId).enqueue(object : Callback<Result?> {
            override fun onResponse(call: Call<Result?>, response: Response<Result?>) {
                if (response.body() != null) {
                    totalHeartCount.value = response.body()?.hearts
                    /*if (response.body()?.isLive == true) viewCount.set(response.body()?.totalViews.toString() + " Views") else if (!isConnectChatRequestMade.get()) {
                        viewCount.set(response.body()?.watchingCount.toString() + " Watching")
                    }*/
                    viewCount.set(response.body()?.totalViews.toString() + " Views")
                    populatePost(response.body())
                    response.body()?.author?.user?.let {
                        loadStreamerFollowState(it.id, it.username)
                    }
                    currentPost.set(response.body())
                    isCustomRoomEnabled.set(response.body()?.isCustomRoomEnabled)
                    isVideoCallEnabled.set(response.body()?.isVideoCallEnabled)
                    isRewardIconEnabled.set(response.body()?.isRewardIconEnabled)
                    isPlayRequestEnabled.set(response.body()?.canRequestPlay())
                    live.set(response.body()?.isLive)
                    if (isPageSelected && response.body()?.isLive == true && getChatHelper() != null) {
                        getChatHelper().getTotal(postId, chatHelperCallback)
                    }
                    setPinnedComment(response.body()?.pinnedComment)
                    if (response.body()?.commentSuggestions != null && !response.body()?.commentSuggestions.isNullOrEmpty()) commentSuggestion.value = response.body()?.commentSuggestions
                    setGreeting(response.body()?.postGifts)
                    loadPostStatus.value = Status.SUCCESS
                    if (response.body()?.isShareDataGenerated == true) {
                        Handler(Looper.getMainLooper()).postDelayed({ loadSharableContent() }, 10000)
                    }
                } else {
                    loadPostStatus.value = Status.ERROR
                    if (response.errorBody() != null) {
                        Log.d(TAG, "Error : " + response.errorBody().toString())
                    } else {
                        Log.d(TAG, "Message : ")
                    }
                }
                notifyPropertyChanged(BR.followCount)
            }

            override fun onFailure(call: Call<Result?>, t: Throwable) {
                Log.d(TAG, "Message : ")
                loadPostStatus.value = Status.ERROR
                if (postRetryCount < 3) Handler().postDelayed({ loadPost(false) }, (2.0.pow(postRetryCount.toDouble()) * 1000).toLong())
                if (commentRetryCount <= 3) Handler().postDelayed({ loadComments() }, (2.0.pow(commentRetryCount.toDouble()) * 1000).toLong())
            }
        })
    }

    fun acceptMoment(startTime: Long, endTime: Long) {
        if (moments.get()?.id.isNullOrEmpty()) return
        isBlockedLoading.set(true)
        dataManager?.updateMomentState(moments.get()?.id, startTime, endTime)?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {

                    RheoTvApp.getNonUiContext()?.showToast((if (startTime == -1L && endTime == -1L) "Reject" else "Accept") + " Success")
                    EventBus.getDefault().post(ViewPagerMediator.PageChange.NEXT)
                    isBlockedLoading.set(false)
                } else {
                    RheoTvApp.getNonUiContext()?.showToast("Error :\n ${response.errorBody()?.string()}")
                    isBlockedLoading.set(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                RheoTvApp.getNonUiContext()?.showToast("Exception :\n ${t.message}")
                isBlockedLoading.set(false)
                t.printStackTrace()
            }
        })
    }

    fun rejectMoment() {
        acceptMoment(-1, -1)
    }
}