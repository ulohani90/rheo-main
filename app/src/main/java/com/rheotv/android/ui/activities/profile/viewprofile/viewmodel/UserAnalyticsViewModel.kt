package com.rheotv.android.ui.activities.profile.viewprofile.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.StreamerData
import com.rheotv.android.data.network.models.useProfile.responses.AnalyticsDataResponse
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserAnalyticsViewModel constructor(
        dataManager: DataManager?,
        schedulerProvider: SchedulerProvider?
) : BaseViewModel<Any>(dataManager, schedulerProvider) {
    val analyticsList: MutableLiveData<ArrayList<StreamerData>> = MutableLiveData()

    fun loadStreamerAnalytics() {
        setIsLoading(true)
        Log.i(javaClass.name, "getStreamerData 1")
        dataManager.getStreamAnalytics("me").enqueue(object : Callback<AnalyticsDataResponse> {
            override fun onResponse(call: Call<AnalyticsDataResponse>, response: Response<AnalyticsDataResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    analyticsList.value = response.body()?.data
                }
            }

            override fun onFailure(call: Call<AnalyticsDataResponse>, t: Throwable) {
                Log.i(javaClass.name, "getStreamerData fail " + t.message)
            }
        })
    }
}
