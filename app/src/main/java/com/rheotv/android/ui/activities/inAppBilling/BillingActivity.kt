package com.rheotv.android.ui.activities.inAppBilling

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.Observable
import androidx.lifecycle.Observer
import com.android.billingclient.api.*
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.ActivityBillingBinding
import com.rheotv.android.ui.activities.inAppBilling.model.BillingPurchase
import com.rheotv.android.ui.activities.inAppBilling.model.BillingSku
import com.rheotv.android.ui.activities.inAppBilling.model.BillingSkuWrapper
import com.rheotv.android.ui.base.BaseActivity
import com.rheotv.android.utils.AppConstants.*
import com.rheotv.android.utils.RewardManager
import com.rheotv.android.utils.Status
import com.rheotv.android.utils.doAfter
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentConstants.*
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.rheotv.android.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class BillingActivity : BaseActivity<ActivityBillingBinding, BillingViewModel>(), PurchasesUpdatedListener {
    private val TAG = javaClass.simpleName

    @Inject
    lateinit var mViewModel: BillingViewModel

    private lateinit var billingClient: BillingClient

    //    private val skus = mutableListOf("test_product_one", "test_product_two", "test_product_third", "test_product_four")
    private var adapter: ProductSkuAdapter? = null

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.activity_billing

    override fun getViewModel() = mViewModel

    val analyticsProperties: MutableMap<String, Any> = hashMapOf()
    private var currentProductValue: String = "0"
    private var currentProductUnit: String = "coins"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpAnalyticsProperties()
        setUpViews()
        setupBillingClient()
        try {
            intent?.apply {
                getIntExtra("featureCoins", -1)?.let {
                    if (it == -1) return@let
                    analyticsProperties["featureCoins"] = it
                }
                getIntExtra("requiredCoins", -1)?.let {
                    if (it == -1) return@let
                    analyticsProperties["requiredCoins"] = it
                }
                getIntExtra("coinsLeft", -1)?.let {
                    if (it == -1) return@let
                    analyticsProperties["coinsLeft"] = it
                }
                getStringExtra("coinSource")?.let {
                    analyticsProperties["coinSource"] = it
                }
            }
            analyticsProperties["coinsLeft"] = RewardManager.getInstance().totalCoins
            trackEvent(EVENT_BUY_COINS_PAGE_SHOWED)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpAnalyticsProperties() {
        analyticsProperties[SCREEN_NAME] = SCREEN_BUY_COINS
        intent ?: return
        analyticsProperties[SCREEN_SOURCE] = if (intent.hasExtra(SCREEN_SOURCE) && intent.getStringExtra(SCREEN_SOURCE) != null) intent.getStringExtra(SCREEN_SOURCE) else SCREEN_BUY_COINS
        analyticsProperties[USER_NAME] = if (intent.hasExtra(USER_NAME) && intent.getStringExtra(USER_NAME) != null) intent.getStringExtra(USER_NAME) else ""
        analyticsProperties[KEY_POST_ID] = if (intent.hasExtra(KEY_POST_ID) && intent.getStringExtra(KEY_POST_ID) != null) intent.getStringExtra(KEY_POST_ID) else ""
    }

    private fun setUpViews() {
        with(viewDataBinding) {
            adapter = ProductSkuAdapter { launchBilling(it) }
            recyclerView.adapter = adapter
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }

        with(viewModel) {
            skuList.observe(this@BillingActivity, Observer {
                loadAllSKUs(it)
            })

            purchaseStatus.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    when (purchaseStatus.get()) {
                        Status.SUCCESS -> {
                            showToast("Transaction successful and $currentProductValue $currentProductUnit added to your account")
                            trackEvent(EVENT_BUY_COIN_SKU_PURCHASE_COMPLETE)
                            CoroutineScope(Dispatchers.IO).doAfter(1000) {
                                val intent = Intent()
                                setResult(RESULT_OK, intent)
                                finish()
                            }
                        }

                        Status.ERROR -> {
                            trackEvent(EVENT_BUY_COIN_ERROR_ON_PURCHASE)
                            this@BillingActivity.showToast("Transaction fail")
                        }

                        else -> {
                            Log.i(TAG, "processing transaction")
                        }
                    }
                }
            })
        }
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this)
                .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is setup successfully
                    Log.i(TAG, "Setup Billing Done")
                    viewModel.loadSku()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.i(TAG, "Setup Billing Failed")
            }
        })
    }

    private fun loadAllSKUs(skus: MutableList<BillingSku>) = if (billingClient.isReady) {
        val params = SkuDetailsParams
                .newBuilder()
                .setSkusList(skus.map { it.productId })
                .setType(BillingClient.SkuType.INAPP)
                .build()
        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            // Process the result.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList?.isNotEmpty() == true) {
                // this will return both the SKUs from Google Play Console
                val productById: Map<String?, BillingSku> = skus.associateBy { it.productId }
                val result = skuDetailsList.mapNotNull { product ->
                    productById[product.sku]?.let { bill -> BillingSkuWrapper(product, bill) }
                }

/*                val res = skuDetailsList.zip(skus) { a, b ->
                    if (a.sku == b.productId)
                        BillingSkuWrapper(a, b)
                    else null
                }.filterNotNull()*/

                adapter?.submitList(result)
//                for (skuDetails in skuDetailsList) {
//
//                    if (skuDetails.sku == "test_product_one")
//                        buttonBuyProduct.setOnClickListener {
//
//                        }
//                }
            }
        }

    } else {
        println("Billing Client not ready")
    }

    private fun launchBilling(billing: BillingSkuWrapper?) {
        //viewModel.buyProduct(BillingPurchase(billing?.billingSku?.productId, "", System.currentTimeMillis(), ""))
        billing?.skuDetails ?: return
        analyticsProperties["product_id"] = billing.skuDetails?.sku ?: ""
        analyticsProperties["product_value"] = billing.billingSku?.productValue ?: ""
        analyticsProperties["product_unit"] = billing.billingSku?.productUnit ?: ""
        analyticsProperties["price"] = billing.skuDetails?.price ?: ""
        trackEvent(EVENT_BUY_COIN_SKU_CLICKED)
        currentProductValue = billing.billingSku?.productValue ?: ""
        currentProductUnit = billing.billingSku?.productUnit ?: "coins"

        val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(billing.skuDetails!!)
                .build()
        billingClient.launchBillingFlow(this, billingFlowParams)
    }

    override fun onPurchasesUpdated(
            billingResult: BillingResult,
            purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            viewModel.purchaseStatus.set(Status.LOADING)
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.i(TAG, "onPurchasesUpdated: ${billingResult.debugMessage}")
            trackEvent(EVENT_BUY_COIN_USER_CANCEL)
        } else {
            // Handle any other error codes.
            Log.i(TAG, "onPurchasesUpdated: ${billingResult.debugMessage}")
        }
    }

    /**
     *  This method also enables your app to make the one-time product available for purchase again.
     **/
    private fun handlePurchase(purchase: Purchase) {
        // Purchase retrieved from BillingClient#queryPurchases or your PurchasesUpdatedListener.
        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.
        val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

        billingClient.consumeAsync(consumeParams) { billingResult, outToken ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    // Handle the success of the consume operation.
                    Log.i(TAG, "onConsumeProduct: ${billingResult.debugMessage}")
                    viewModel.buyProduct(BillingPurchase(purchase.sku, purchase.orderId, purchase.purchaseTime, outToken))
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    // Handle an error caused by a user cancelling the purchase flow.
                    Log.i(TAG, "onConsumeProduct: ${billingResult.debugMessage}")
                }
                else -> {
                    // Handle any other error codes.
                    Log.i(TAG, "onConsumeProduct: ${billingResult.debugMessage}")
                }
            }
        }
    }

    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
        }
    }

    private fun trackEvent(event: String) {
        SegmentTracker.getInstance().trackEvent(event, analyticsProperties)
    }
}