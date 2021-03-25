package com.rheotv.android.ui.activities.profile.editprofile.viewmodel

import androidx.lifecycle.MutableLiveData
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.ui.activities.profile.viewprofile.model.GameRule
import com.rheotv.android.ui.activities.profile.viewprofile.utils.UserAction
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.isNullOrEmptyOrBlank
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.showToast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameRuleViewModel constructor(
        dataManager: DataManager,
        schedulerProvider: SchedulerProvider
) : BaseViewModel<Any>(dataManager, schedulerProvider) {
    val rules = MutableLiveData<MutableList<GameRule>>()
    val selectedRule = MutableLiveData<GameRule>()
    var username: String? = null
    var currentRule: String? = null

    fun onAddButtonClicked() {
        when {
            currentRule.isNullOrEmptyOrBlank() -> RheoTvApp.getNonUiContext().showToast("Please enter a rule")
            else -> updateGameRules(GameRule("0", currentRule))
        }
    }

    fun loadGameRule() {
        dataManager.getUserGameRules(username).enqueue(object : Callback<MutableList<GameRule>> {
            override fun onFailure(call: Call<MutableList<GameRule>>, t: Throwable) {

            }

            override fun onResponse(call: Call<MutableList<GameRule>>, response: Response<MutableList<GameRule>>) {
               if (response.isSuccessful)
                   rules.value = response.body()
            }
        })
    }

    private fun updateGameRules(gameRule: GameRule) {
        dataManager.updateGameRule(gameRule, UserAction.Add).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful)
                    selectedRule.value = gameRule
            }
        })
    }
}