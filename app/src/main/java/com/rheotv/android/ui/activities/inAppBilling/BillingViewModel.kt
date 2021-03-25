package com.rheotv.android.ui.activities.inAppBilling

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.ui.activities.inAppBilling.model.BillingPurchase
import com.rheotv.android.ui.activities.inAppBilling.model.BillingResponse
import com.rheotv.android.ui.activities.inAppBilling.model.BillingSku
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.Status
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.showToast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BillingViewModel(
        dataManager: DataManager,
        schedulerProvider: SchedulerProvider
) : BaseViewModel<Any>(dataManager, schedulerProvider) {
    private val TAG = javaClass.simpleName
    val skuList = MutableLiveData<MutableList<BillingSku>>()
    val purchaseStatus = ObservableField<Status>()

    fun loadSku() {
        isLoading.set(true)
        dataManager.billingSkus.enqueue(object : Callback<BillingResponse> {
            override fun onFailure(call: Call<BillingResponse>, t: Throwable) {
                isLoading.set(false)
            }

            override fun onResponse(call: Call<BillingResponse>, response: Response<BillingResponse>) {
                if (response.isSuccessful) {
                    response.body()?.sku.let { skus -> skuList.value = skus}
                }
                isLoading.set(false)
            }
        })
    }

    fun buyProduct(purchase: BillingPurchase) {
        dataManager.buyProduct(purchase).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "buyProduct: ${t.localizedMessage}")
                purchaseStatus.set(Status.ERROR)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    purchaseStatus.set(Status.SUCCESS)
                }
            }
        })
    }
}