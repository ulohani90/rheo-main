package com.rheotv.android.ui.activities.player.activity.newPlayer

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.postlisting.responses.TopFans
import com.rheotv.android.data.network.models.postlisting.responses.TopFansResponse
import com.rheotv.android.db.AppDatabase
import com.rheotv.android.db.UserFollowDao
import com.rheotv.android.db.UserFollowItem
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.Status
import com.rheotv.android.utils.rx.SchedulerProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopFansViewModel(dataManager: DataManager?, schedulerProvider: SchedulerProvider?) : BaseViewModel<Any>(dataManager, schedulerProvider) {

    val baseProperties: MutableMap<String, Any> = hashMapOf()
    var username: String? = null
    val fansLiveData: MutableLiveData<List<TopFans>?> = MutableLiveData()
    val viewStatus: ObservableField<Status> = ObservableField()
    val dao: UserFollowDao = AppDatabase.getInstance(RheoTvApp.getNonUiContext()).userFollowDao()

    fun followUser(topFan: TopFans, updateCallback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = dao.updateUserEntry(UserFollowItem(topFan.user?.id ?: 0,
                    topFan.user?.username, topFan.isFollowed == false))
            withContext(Dispatchers.Main) {
                updateCallback()
                dataManager.followAuthor(topFan.user?.id?.toString()).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        Log.i("TopFansViewModel", "Response : ${response.isSuccessful}")
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
            }
        }
    }

    fun fetchTopFans() {
        viewStatus.set(Status.LOADING)
        dataManager.fetchTopFans(username).enqueue(object : Callback<TopFansResponse> {
            override fun onResponse(call: Call<TopFansResponse>, response: Response<TopFansResponse>) {
                if (response.isSuccessful) {
                    val serverList = response.body()?.data?.toMutableList() ?: mutableListOf()
                    viewModelScope.launch(Dispatchers.IO) {
                        val databaseList = dao.fetchFollowerList(serverList.map {
                            it.user?.username ?: ""
                        }.distinct())
                        val mapData = databaseList.associateBy { it.userName }
                        serverList.mapNotNull { remoteUser ->
                            mapData[remoteUser.user?.username]?.let { localUser ->
                                remoteUser.isFollowed = localUser.isFollowed
                            }
                        }
                        withContext(Dispatchers.Main) {
                            fansLiveData.value = serverList.filterNot { topFans -> topFans.user?.username?.equals(username, ignoreCase = true) == true }
                            viewStatus.set(Status.SUCCESS)
                        }
                        dao.insertMultipleUserWithIgnore(serverList.map {
                            UserFollowItem(it.user?.id ?: 0,
                                    it.user?.username ?: "",
                                    it.isFollowed ?: false)
                        })
                    }
                    try {
                        return
                    } finally {
                        print(10)
                    }
                } else {
                    viewStatus.set(Status.ERROR)
                }
            }

            override fun onFailure(call: Call<TopFansResponse>, t: Throwable) {
                t.printStackTrace()
                viewStatus.set(Status.ERROR)
            }
        })
    }
}