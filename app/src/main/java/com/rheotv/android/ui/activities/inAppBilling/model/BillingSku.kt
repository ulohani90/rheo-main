package com.rheotv.android.ui.activities.inAppBilling.model

import com.android.billingclient.api.SkuDetails
import com.google.gson.annotations.SerializedName

data class BillingResponse(
        @field:SerializedName("data")
        var sku: MutableList<BillingSku>? = null
)

data class BillingSku(
        @field:SerializedName("product_id")
        var productId: String? = null,

        @field:SerializedName("name")
        var name: String? = null,

        @field:SerializedName("description")
        var description: String? = null,

        @field:SerializedName("product_value")
        var productValue: String? = null,

        @field:SerializedName("product_unit")
        var productUnit: String? = null
)

data class PurchaseDetail (
        @field:SerializedName("purchase_details")
        var purchase: BillingPurchase? = null
)

data class BillingPurchase(
        @field:SerializedName("product_id")
        var productId: String? = null,

        @field:SerializedName("order_id")
        var orderId: String? = null,

        @field:SerializedName("purchase_time")
        var purchaseTime: Long? = null,

        @field:SerializedName("out_token")
        var outToken: String? = null
)

data class BillingSkuWrapper (
        var skuDetails: SkuDetails? = null,

        var billingSku: BillingSku? = null
)