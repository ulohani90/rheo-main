package com.rheotv.android.ui.activities.profile.editprofile.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.general.GameDetails
import com.rheotv.android.ui.activities.profile.viewprofile.utils.UserAction
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.showToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameWiseUserViewModel constructor(
        dataManager: DataManager,
        schedulerProvider: SchedulerProvider
) : BaseViewModel<Any>(dataManager, schedulerProvider) {
    var username: String? = null
    val games = MutableLiveData<List<GameDetails>>()
    val selectedGames = MutableLiveData<MutableList<GameDetails>>()
    val userAction = MutableLiveData<Pair<UserAction, GameDetails>>()
    val gameUsername = ObservableField<String>()
    var currentGame: GameDetails? = null
    var inEditMode = ObservableField<Boolean>(false)

    fun onEditButtonClick() {
        inEditMode.set(!inEditMode.get()!!)
    }

    fun loadGames() {
        dataManager.gameDetails.enqueue(object : Callback<List<GameDetails>> {
            override fun onResponse(call: Call<List<GameDetails>>, response: Response<List<GameDetails>>) {
                if (response.isSuccessful)
                    games.value = response.body()
            }

            override fun onFailure(call: Call<List<GameDetails>>, t: Throwable) {

            }
        })
    }

    fun loadSelectedGame() {
        dataManager.getUserSelectedGames(username).enqueue(object : Callback<MutableList<GameDetails>> {
            override fun onResponse(call: Call<MutableList<GameDetails>>, response: Response<MutableList<GameDetails>>) {
                if (response.isSuccessful)
                    selectedGames.value = response.body()
            }

            override fun onFailure(call: Call<MutableList<GameDetails>>, t: Throwable) {

            }
        })
    }

    fun onAddGameUser() {
        when {
            currentGame == null && currentGame?.id.isNullOrEmpty() -> RheoTvApp.getNonUiContext().showToast("Please select a game")
            gameUsername.get().isNullOrEmpty() -> RheoTvApp.getNonUiContext().showToast("Please enter username")
            else -> {
                currentGame?.gameUsername = gameUsername.get()
                updateUserGame(UserAction.Add)
            }
        }
    }

    private fun updateUserGame(action: UserAction) {
//        dataManager.onUserGameAction(currentGame, action).enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful)
//                    userAction.value = Pair(action, currentGame!!)
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//
//            }
//        })
    }
}