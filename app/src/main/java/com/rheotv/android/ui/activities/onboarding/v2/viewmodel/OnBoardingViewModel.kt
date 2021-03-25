package com.rheotv.android.ui.activities.onboarding.v2.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.onboarding.LanguageObject
import com.rheotv.android.data.network.models.onboarding.OnBoardingResponse
import com.rheotv.android.ui.activities.onboarding.OnBoardingActivityViewModel
import com.rheotv.android.ui.activities.onboarding.v2.UserLanguage
import com.rheotv.android.ui.activities.onboarding.v2.model.ShowData
import com.rheotv.android.ui.activities.onboarding.v2.model.TopShowResponse
import com.rheotv.android.ui.fragments.LoginViewModel
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.Status
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.showToast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap

class OnBoardingViewModel(dataManager: DataManager?, schedulerProvider: SchedulerProvider?) : LoginViewModel(dataManager, schedulerProvider) {

    val languageLiveData: MutableLiveData<List<LanguageObject>?> = MutableLiveData()
    val viewState = ObservableField<Status>()
    val selectedLanguageIds = HashMap<String, LanguageObject>()
    val userActionLiveData: MutableLiveData<UserAction?> = MutableLiveData()
    val preferredLanguage = MutableLiveData<UserLanguage?>()
    val topShows = MutableLiveData<List<ShowData>?>()
    var hasTopShows = false

    fun fetchLanguage() {
        viewState.set(Status.LOADING)
        dataManager.fetchOnBoardingData().enqueue(object : Callback<OnBoardingResponse?> {
            override fun onResponse(call: Call<OnBoardingResponse?>, response: Response<OnBoardingResponse?>) {
                if (response.isSuccessful && response.body() != null) {
                    languageLiveData.value = response.body()?.languageObjects
                    viewState.set(Status.SUCCESS)
                } else {
                    viewState.set(Status.ERROR)
                }
            }

            override fun onFailure(call: Call<OnBoardingResponse?>, t: Throwable) {
                Log.i(OnBoardingActivityViewModel::class.java.canonicalName, "Failed")
                viewState.set(Status.ERROR)
            }
        })
    }

    fun updateLanguage() {
        if (selectedLanguageIds.isEmpty()) {
            Toast.makeText(RheoTvApp.getNonUiContext(), "Please select a language", Toast.LENGTH_SHORT).show()
            return
        }

        if (CommonUtils.isPreferredLanguageBoardingUser()) {
            preferredLanguage.value = try {
                UserLanguage.toUserLanguage(selectedLanguageIds.values.toList().first().name)
            } catch (e: Exception) {
                e.printStackTrace()
                UserLanguage.English
            }
        }

//        Log.i(getClass().getSimpleName(), "updateLanguage: " + new Gson().toJson(selectedIds));
        viewState.set(Status.LOADING)
        dataManager.setUserLanguage(selectedLanguageIds.keys.toList()).enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        CommonUtils.setUserLanguage(RheoTvApp.getNonUiContext(), selectedLanguageIds.values.map { it.name })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    viewState.set(Status.SUCCESS)
                    userActionLiveData.value = UserAction.LanguageUpdated
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                viewState.set(Status.ERROR)
            }
        })
    }

    fun updateNextButtonState(usernameText: CharSequence?) {
        userActionLiveData.value = if (usernameText.isNullOrEmpty())
            UserAction.DisableNextButton
        else
            UserAction.EnableNextButton
    }

    fun updateNextButtonState() {
        userActionLiveData.value = if (selectedLanguageIds.isEmpty())
            UserAction.DisableNextButton
        else
            UserAction.EnableNextButton

    }

    fun updateSelectedLanguage(item: LanguageObject) {
        if (item.isSelected)
            selectedLanguageIds[item.id] = item
        else
            selectedLanguageIds.remove(item.id)
        updateNextButtonState()
    }

    fun fetchTopShows() {
        if (selectedLanguageIds.keys.toList().isEmpty()) return
        dataManager.fetchTopShow(selectedLanguageIds.keys.toList().joinToString(",")).enqueue(object : Callback<TopShowResponse> {
            override fun onResponse(call: Call<TopShowResponse>, response: Response<TopShowResponse>) {
                if (response.isSuccessful) {
                    topShows.value = response.body()?.results
                    hasTopShows = !(response.body()?.results?.isNullOrEmpty() ?: true)
                }
            }

            override fun onFailure(call: Call<TopShowResponse>, t: Throwable) {

            }
        })
    }

    fun setShowReminder(list: List<String>?) {
        if (list.isNullOrEmpty()) RheoTvApp.getNonUiContext().showToast("Please select a show to continue")
        dataManager.setShowReminder(list, SegmentConstants.ONBOARDING_CARD).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful)
                    userActionLiveData.value = UserAction.TopShowSelection
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    sealed class UserAction {
        data class AskUsername(var message: String?, var name: String?, var photoUrl: String?) : UserAction()
        object UsernameAdded : UserAction()
        object SubmitLanguage : UserAction()
        object LanguageUpdated : UserAction()
        object Login : UserAction()
        object EnableNextButton : UserAction()
        object DisableNextButton : UserAction()
        object HideNextButton : UserAction()
        object HideBackButton : UserAction()
        object ShowNextButton : UserAction()
        object ShowBackButton : UserAction()
        object LoginSuccess : UserAction()
        object SubmitTopShow : UserAction()
        object TopShowSelection: UserAction()
    }
}