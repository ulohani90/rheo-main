package com.rheotv.android.ui.activities.tabcontainer.profile.wallet

import androidx.databinding.ObservableField
import com.rheotv.android.data.DataManager
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class WalletDetailsInputActivityViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider)
    : BaseViewModel<WalletDetailsInputActivityNavigator>(dataManager, schedulerProvider) {

    val loaderObservable: ObservableField<Boolean> = ObservableField(false)

    fun requestRedeem(upiId: String?, mobileNo: String?, amount: Int) {
        compositeDisposable
                .add(dataManager.redeemRequest(upiId, mobileNo, amount)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ success ->
                            if (success != null && navigator != null) {
                                navigator.onPaymentSuccess(amount)
                            }
                        }, { error ->
                            if (error != null && navigator != null) navigator.onPaymentFailure(error.localizedMessage)
                        }))
    }

}

interface WalletDetailsInputActivityNavigator {
    fun onPaymentSuccess(amount: Int)
    fun onPaymentFailure(errorMessage: String)
}