package com.rheotv.android.ui.activities.profile.viewprofile.viewmodel

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.useProfile.responses.WalletDetail
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.Resource
import com.rheotv.android.utils.ServerFileDownloader
import com.rheotv.android.utils.rx.SchedulerProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UserWalletViewModel constructor(
        dataManager: DataManager?,
        schedulerProvider: SchedulerProvider?
) : BaseViewModel<Any>(dataManager, schedulerProvider) {
    var wallet: MutableLiveData<Resource<WalletDetail>> = MutableLiveData()
    lateinit var username: String


init {

    Log.i(javaClass.simpleName, "loadWallet_0")
}
    fun loadWallet() {
        Log.i(javaClass.simpleName, "loadWallet")
        wallet.value = Resource.loading()
        dataManager.getUserWallet("me").enqueue(object : Callback<WalletDetail> {
            override fun onFailure(call: Call<WalletDetail>, t: Throwable) {
                wallet.value = Resource.error(t.message, -1)
            }

            override fun onResponse(call: Call<WalletDetail>, response: Response<WalletDetail>) {
                if (response.isSuccessful)
                    wallet.value = Resource.success(response.body())
                else
                    wallet.value = Resource.error(response.errorBody()?.string(), response.code())
            }
        })
    }

    fun downloadStatement() {
        setIsLoading(true)
        compositeDisposable
                .add(dataManager
                        .downloadStatement()
                        .concatMap<File> { response ->
                            ServerFileDownloader().downloadFile(response, File(RheoTvApp.getNonUiContext().filesDir.absolutePath + "abc.pdf").absolutePath)
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { response ->
                                    if (response != null) {
                                        setIsLoading(false)
                                        val intent = Intent(Intent.ACTION_SEND)
                                        intent.type = "application/pdf"
                                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(response.absolutePath))
                                        RheoTvApp.getNonUiContext().startActivity(intent)
                                    }
                                },
                                { throwable -> throwable.printStackTrace() }))
    }
}