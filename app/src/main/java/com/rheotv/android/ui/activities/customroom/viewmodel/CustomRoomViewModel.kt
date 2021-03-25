package com.rheotv.android.ui.activities.customroom.viewmodel

import androidx.lifecycle.MutableLiveData
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.postlisting.responses.Result
import com.rheotv.android.ui.activities.customroom.model.*
import com.rheotv.android.ui.activities.customroom.model.CustomRoomViewType
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.showToast
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomRoomViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider)
    : BaseViewModel<Unit>(dataManager, schedulerProvider) {

    var post: Result? = null
    var canRequest = false
        private set
    var playerNextUrl: String? = null
    var roomPageRefresh = false
    val customRoomUserAction: MutableLiveData<CustomRoomUserAction> = MutableLiveData()
    val customRoomLiveData: MutableLiveData<List<CustomRoomDetail>> = MutableLiveData()
    val customRoomDetailLiveData: MutableLiveData<List<CustomRoomDetail>> = MutableLiveData()
    val progressLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val customRoomPlayerLiveData: MutableLiveData<List<CustomRoomPlayer>> = MutableLiveData()
    val requestedCustomRooms = HashSet<String>()
    var selectedRoomId: String? = null

    fun fetchCustomRoom() {
        if (post?.id.isNullOrBlank()) {
            setLoading(false)
            return
        }

        setLoading(true)
        dataManager.fetchCustomRooms(post?.id).enqueue(object : Callback<CustomRoomResponse> {
            override fun onFailure(call: Call<CustomRoomResponse>, t: Throwable) {
                setLoading(false)
                t.printStackTrace()
            }

            override fun onResponse(call: Call<CustomRoomResponse>, response: Response<CustomRoomResponse>) {
                if (response.isSuccessful) {
                    canRequest = response.body()?.canRequest ?: false
                    response.body()?.requestedInList?.let {
                        requestedCustomRooms.clear()
                        requestedCustomRooms.addAll(it)
                    }
                    customRoomLiveData.value = response.body()
                            ?.transformedData(post?.isStreamer ?: false)
                } else {
                    showError(response.errorBody())
                    customRoomUserAction.value = CustomRoomUserAction.RefreshCustomRoom
                }
                setLoading(false)
            }
        })
    }

    fun createCustomRoom(customRoomDetail: CustomRoomDetail) {
        if (post?.id.isNullOrBlank()) {
            setLoading(false)
            return
        }
        setLoading(true)
        dataManager.createCustomRoom(post?.id,
                customRoomDetail.startTime,
                customRoomDetail.entryCoins,
                customRoomDetail.maxPlayerCount)
                .enqueue(object : Callback<CustomRoomDetailResponse> {
                    override fun onFailure(call: Call<CustomRoomDetailResponse>, t: Throwable) {
                        setLoading(false)
                        t.printStackTrace()
                    }

                    override fun onResponse(call: Call<CustomRoomDetailResponse>, response: Response<CustomRoomDetailResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.customRoom?.let {
                                it.setupViewType(post?.isStreamer == true)
                                selectedRoomId = it.id
                                customRoomLiveData.value = listOf(it)
                                customRoomDetailLiveData.value = listOf(it)
                                (customRoomDetail.viewType as? CustomRoomDetailViewType.CreateCustomRoom)?.let { event ->
                                    if (event.customRoomCount != -1)
                                        customRoomUserAction.value = CustomRoomUserAction.CreateCustomRoomClick
                                                .also { action -> action.headerText = "Custom Room ${event.customRoomCount}" }
                                }

                            }
                        } else {

                            showError(response.errorBody())
                        }
                        setLoading(false)
                    }
                })
    }

    private fun showError(errorBody: ResponseBody?) {
        try {
            errorBody?.string()?.let {
                val json = JSONObject(it)
                if (json.has("message")) {
                    RheoTvApp.getNonUiContext()?.showToast(json.getString("message"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun submitRoomIdAndPassword(customRoomDetail: CustomRoomDetail) {
        if (customRoomDetail.id.isNullOrBlank()) {
            setLoading(false)
            return
        }

        setLoading(true)
        dataManager.addCustomRoomIdAndPassword(customRoomDetail.id, customRoomDetail.customRoomId, customRoomDetail.customRoomPassword)
                .enqueue(object : Callback<CustomRoomDetailResponse> {
                    override fun onFailure(call: Call<CustomRoomDetailResponse>, t: Throwable) {
                        setLoading(false)
                        t.printStackTrace()
                    }

                    override fun onResponse(call: Call<CustomRoomDetailResponse>, response: Response<CustomRoomDetailResponse>) {
                        if (response.isSuccessful) {
                            customRoomDetail.setupViewType(post?.isStreamer == true)
                            customRoomDetailLiveData.value = listOf(customRoomDetail)
                        } else {
                            showError(response.errorBody())
                            customRoomDetail.customRoomId = null
                            customRoomDetail.customRoomPassword = null
                        }
                        customRoomUserAction.value = CustomRoomUserAction.RefreshCustomRoom
                        setLoading(false)
                    }
                })
    }

    fun refundCustomRoom(customRoomDetail: CustomRoomDetail) {
        if (customRoomDetail.id.isNullOrBlank()) {
            setLoading(false)
            return
        }

        setLoading(true)
        dataManager.refundCustomRoom(customRoomDetail.id).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                setLoading(false)
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    customRoomDetail.state = CustomRoomViewType.CustomRoomRefunded.name
                    customRoomDetail.dataViewType = CustomRoomViewType.CustomRoomRefunded
                    customRoomDetail.viewType = CustomRoomDetailViewType.ShowRoomIdAndPassword
                    customRoomDetail.canRefund = false
                    customRoomDetail.id?.let { requestedCustomRooms.remove(it) }
                    customRoomUserAction.value = CustomRoomUserAction.RefreshCustomRoom
                } else {
                    showError(response.errorBody())
                }
                setLoading(false)
            }
        })
    }

    fun fetchCustomRoomPlayers(customRoomDetail: CustomRoomDetail) {
        if (customRoomDetail.id.isNullOrBlank()) {
            setLoading(false)
            return
        }

        dataManager.fetchCustomRoomPlayers(customRoomDetail.id, playerNextUrl)
                .enqueue(object : Callback<CustomRoomPlayerResponse> {
                    override fun onFailure(call: Call<CustomRoomPlayerResponse>, t: Throwable) {
                        t.printStackTrace()
                        setLoading(false)
                    }

                    override fun onResponse(call: Call<CustomRoomPlayerResponse>, response: Response<CustomRoomPlayerResponse>) {
                        if (response.isSuccessful) {
                            playerNextUrl = response.body()?.next
                            val list = response.body()?.results
                            customRoomPlayerLiveData.value = list
                        } else {
                            showError(response.errorBody())
                        }
                        setLoading(false)
                    }
                })
    }

    fun onAddCustomRoomClick(customRoomCount: Int) {
        customRoomDetailLiveData.value = listOf(CustomRoomDetail(post?.id ?: return).apply {
            viewType = CustomRoomDetailViewType.CreateCustomRoom.also { it.customRoomCount = customRoomCount + 1 }
        })
        customRoomUserAction.value = CustomRoomUserAction.AddCustomRoomClick
    }

    fun openCustomRoom(customRoomDetail: CustomRoomDetail, itemPosition: Int) {
        if (post?.id.isNullOrBlank()) {
            setLoading(false)
            return
        }

        selectedRoomId = customRoomDetail.id
        customRoomDetailLiveData.value = listOf(customRoomDetail)
        customRoomUserAction.value = CustomRoomUserAction.CustomRoomViewClick.also { it.headerText = "Custom Room $itemPosition" }
    }

    fun requestToCustomRoom(customRoomDetail: CustomRoomDetail) {
        if (customRoomDetail.id.isNullOrBlank()) {
            setLoading(false)
            return
        }

        setLoading(true)
        dataManager.requestToCustomRoom(customRoomDetail.id, customRoomDetail.gameUserName)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        setLoading(false)
                        t.printStackTrace()
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            customRoomDetail.id?.let { requestedCustomRooms.add(it) }
                            customRoomUserAction.value = CustomRoomUserAction.DetailPageBackClick
                            canRequest = false
                        } else {
                            showError(response.errorBody())
                        }
                        setLoading(false)
                    }
                })
    }

    fun updateStartTime(customRoomDetail: CustomRoomDetail) {
        if (customRoomDetail.id.isNullOrBlank()) {
            setLoading(false)
            return
        }

        setLoading(true)
        dataManager.updateCustomRoomStartTime(customRoomDetail.id, customRoomDetail.startTime)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        t.printStackTrace()
                        setLoading(false)
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            customRoomUserAction.value = CustomRoomUserAction.RefreshCustomRoom
                        } else {
                            showError(response.errorBody())
                        }
                    }
                })
    }

    fun setLoading(loading: Boolean) {
        progressLiveData.value = loading
    }

    fun searchPlayer(customRoomDetail: CustomRoomDetail, searchQuery: String) {
        if (customRoomDetail.id.isNullOrBlank()) {
            setLoading(false)
            return
        }

        dataManager.searchCustomRoomPlayer(customRoomDetail.id, searchQuery)
                .enqueue(object : Callback<CustomRoomPlayerResponse> {
                    override fun onFailure(call: Call<CustomRoomPlayerResponse>, t: Throwable) {
                        t.printStackTrace()
                        setLoading(false)
                    }

                    override fun onResponse(call: Call<CustomRoomPlayerResponse>, response: Response<CustomRoomPlayerResponse>) {
                        if (response.isSuccessful) {
                            customRoomPlayerLiveData.value = response.body()?.results
                        } else {
                            showError(response.errorBody())
                        }
                        setLoading(false)
                    }
                })
    }

    fun markWinner(customRoomDetail: CustomRoomDetail?) {
        if (customRoomDetail?.id.isNullOrBlank()) {
            setLoading(false)
            return
        }

        setLoading(true)
        dataManager.markCustomRoomWinner(customRoomDetail?.id, customRoomDetail?.winner?.id)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        setLoading(false)
                        t.printStackTrace()
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            customRoomUserAction.value = CustomRoomUserAction.RefreshPlayerList
                        } else {
                            showError(response.errorBody())
                            customRoomDetail?.winner = null
                        }
                        setLoading(false)
                    }
                })
    }
}