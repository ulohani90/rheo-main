package com.rheotv.android.ui.activities.profile.editprofile.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.ui.activities.profile.model.UserDonation
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.Status
import com.rheotv.android.utils.isNullOrEmptyOrBlank
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.showToast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DonationEditViewModel constructor(
        dataManager: DataManager,
        schedulerProvider: SchedulerProvider
) : BaseViewModel<Any>(dataManager, schedulerProvider) {
    var username: String? = null
    val donation = ObservableField<UserDonation>()
    val updateState = MutableLiveData<Status>()

    fun loadUserDonation() {
        dataManager.getUserDonation(username).enqueue(object : Callback<UserDonation>{
            override fun onFailure(call: Call<UserDonation>, t: Throwable) {

            }

            override fun onResponse(call: Call<UserDonation>, response: Response<UserDonation>) {
                if (response.isSuccessful)
                    donation.set(response.body())
            }
        })
    }

    private fun updateUserDonation() {
        updateState.value = Status.LOADING
        dataManager.updateUserDonation(donation.get()).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                updateState.value = Status.SUCCESS
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful)
                    updateState.value = Status.SUCCESS
            }
        })
    }

    fun onAddButtonClick() {
        when {
            donation.get()?.link.isNullOrEmptyOrBlank() -> RheoTvApp.getNonUiContext().showToast("Please enter donation link")
            donation.get()?.title.isNullOrEmptyOrBlank() -> RheoTvApp.getNonUiContext().showToast("Please enter donation title")
            else -> updateUserDonation()
        }
    }
}