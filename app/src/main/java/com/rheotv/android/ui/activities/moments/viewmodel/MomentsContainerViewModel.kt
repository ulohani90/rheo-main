package com.rheotv.android.ui.activities.moments.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rheotv.android.data.DataManager
import com.rheotv.android.ui.activities.moments.model.MomentsListItem
import com.rheotv.android.ui.activities.moments.model.MomentsListResponse
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MomentsContainerViewModel(dataManager: DataManager?, schedulerProvider: SchedulerProvider?) : BaseViewModel<Any>(dataManager, schedulerProvider) {

    public var mNextUrl: String? = null
    val momentListData: MutableLiveData<List<MomentsListItem>> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    fun setLoading(isLoading: Boolean) {
        loading.value = isLoading
    }

    private var mIsContendModerator: Boolean? = null;

    fun isContentModerator(): Boolean {
        return mIsContendModerator == true;
    }

    var authorUsername: String? = null


    fun fetchMoments(refresh: Boolean) {
        if (refresh) {
            mNextUrl = null;
        }
        setLoading(true)
        dataManager?.fetchMoments(mNextUrl, authorUsername)?.enqueue(object : Callback<MomentsListResponse> {
            override fun onResponse(call: Call<MomentsListResponse>, response: Response<MomentsListResponse>) {
                Log.i(TAG, "Success")
                if (response.isSuccessful) {
                    if (mIsContendModerator == null)
                        mIsContendModerator = response.body()?.isContentModerator
                    momentListData.value = response.body()?.data?.map { it.apply { isContentModerator = mIsContendModerator == true } }
                            ?: mutableListOf()
                    mNextUrl = response.body()?.next
                } else {
                    try {
                        JSONObject(response.errorBody()?.string() ?: "").apply {
                            has("err")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                setLoading(false)
            }

            override fun onFailure(call: Call<MomentsListResponse>, t: Throwable) {
                Log.i(TAG, "Error -> ${t.message}")
                setLoading(false)
            }
        })
    }

    companion object {
        private const val TAG = "MomentsContainerVM"
    }
}