package com.rheotv.android.ui.activities.home.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.AnalyticsEventsResponse
import com.rheotv.android.data.network.models.gamify.DailyRewardsResponse
import com.rheotv.android.data.network.models.gamify.RewardTakenResponse
import com.rheotv.android.data.network.models.general.AppVersionResponse
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult
import com.rheotv.android.utils.EventBusModel
import com.rheotv.android.ui.activities.home.view.HomeActivity
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.RewardManager
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class HomeActivityViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider) :
        BaseViewModel<Any>(dataManager, schedulerProvider) {

    val properties: MutableMap<String, Any> = hashMapOf()
    val actionsLiveData = MutableLiveData<HomeActivity.Action>()

    fun updateScratchCard(rewardId: String) {
        dataManager.updateDailyScratchCard(rewardId).enqueue(object : Callback<RewardTakenResponse> {
            override fun onResponse(call: Call<RewardTakenResponse>, response: Response<RewardTakenResponse>) {
                if (response.isSuccessful && response.body()!!.isSuccessful) {
                    // update reward manager
                    loadDailyRewards()
                }
            }

            override fun onFailure(call: Call<RewardTakenResponse>, t: Throwable) {
                Log.i(javaClass.name, "dummyLoadDailyRewards " + t.message)
            }
        })
        val properties: MutableMap<String, Any> = HashMap(this.properties)
        properties["rewardId"] = rewardId
        SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_SCRATCH_CARD, properties)
    }

    fun rateApp(rating: Int, feedback: String?) {
        dataManager.rateApp(rating, feedback).enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {}
            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.i(javaClass.name, "rate App " + t.message)
            }
        })
        SegmentTracker.getInstance(RheoTvApp.getNonUiContext())
                .trackEvent(if (rating > 0) SegmentConstants.EVENT_RATING_GIVEN else SegmentConstants.EVENT_RATING_CANCELLED,
                        HashMap(properties).apply {
                            if (rating > 0) {
                                "rating" to rating
                                "feedback" to feedback
                            }
                        })
    }

    fun fetchProfile(authorUserName: String?) {
        dataManager.getProfile(authorUserName).enqueue(object : Callback<ProfileResult?> {
            override fun onResponse(call: Call<ProfileResult?>, response: Response<ProfileResult?>) {
                if (response.body() != null) {
                    response.body()?.contentModerator?.let { CommonUtils.setIsUserContentModerator(it) }
                    response.body()?.paymentModel?.let { CommonUtils.setPaymentModel(it) }
                    response.body()?.levelType?.let { CommonUtils.setLevelType(it) }
                }
            }

            override fun onFailure(call: Call<ProfileResult?>, t: Throwable) {
                t.printStackTrace()
                Log.d("mirage", "fetching profile failed. Probably not loggedIn " + t.message)
            }
        })
    }

    fun getAnalyticsEventsList() {
        dataManager.analyticsEventsList.enqueue(object : Callback<AnalyticsEventsResponse?> {
            override fun onResponse(call: Call<AnalyticsEventsResponse?>, response: Response<AnalyticsEventsResponse?>) {
                if (response.body() != null) {
                    SegmentTracker.getInstance(RheoTvApp.getNonUiContext())
                            .setAnalyticsEvents(response.body()?.events ?: listOf(),response.body()?.moengageEvents?: listOf())
                }
            }

            override fun onFailure(call: Call<AnalyticsEventsResponse?>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun updateScratchCardStatusShown(rewardId: String?) {
        dataManager.updateScratchCardStatusShown(rewardId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableObserver<Response<RewardTakenResponse?>?>() {
                    override fun onNext(rewardTakenResponse: Response<RewardTakenResponse?>) {
                        if (rewardTakenResponse.isSuccessful)
                            loadDailyRewards()
                    }

                    override fun onError(throwable: Throwable) {
                        Log.i(javaClass.name, "updateScratchCardStatusShown " + throwable.message)
                    }

                    override fun onComplete() {}
                })
    }

    fun loadDailyRewards() {
        if (!CommonUtils.isUserLoggedin()) {
            actionsLiveData.value = HomeActivity.Action.RewardUpdate
            return
        }
        dataManager.dailyRewards.enqueue(object : Callback<DailyRewardsResponse?> {
            override fun onResponse(call: Call<DailyRewardsResponse?>, response: Response<DailyRewardsResponse?>) {
                try {
                    if (response.body() != null) {
                        response.body()?.results?.let { RewardManager.getInstance().dailyRewards = it }
                        response.body()?.totalCoins?.let { RewardManager.getInstance().totalCoins = it }
                        response.body()?.isCodaEnabled?.let { RewardManager.getInstance().isCodaEnabled = it }
                        actionsLiveData.value = HomeActivity.Action.RewardUpdate
                        actionsLiveData.value = HomeActivity.Action.ShowLoginStreak
                        EventBus.getDefault().post(EventBusModel.UpdateCoin)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<DailyRewardsResponse?>, t: Throwable) {
                Log.i(javaClass.name, "loadDailyRewards: " + t.message)
            }
        })
    }

    fun checkVersionSupport() {
        if (CommonUtils.getDeviceId(RheoTvApp.getNonUiContext()) == null) return
        Log.i(javaClass.simpleName, "checkVersionSupport")
        dataManager.checkVersionSupport(CommonUtils.getBranchExtraInfo(RheoTvApp.getNonUiContext())).enqueue(object : Callback<AppVersionResponse?> {
            override fun onResponse(call: Call<AppVersionResponse?>, response: Response<AppVersionResponse?>) {
                if (navigator != null) {
                    if (response.isSuccessful && response.body() != null) {
                        if (response.body()?.supported == false) {
                            if (response.body()?.strict == true) {
                                actionsLiveData.value = HomeActivity.Action.ForceUpdate
                            } else {
                                actionsLiveData.value = HomeActivity.Action.Update
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<AppVersionResponse?>, t: Throwable) {}
        })
    }
}