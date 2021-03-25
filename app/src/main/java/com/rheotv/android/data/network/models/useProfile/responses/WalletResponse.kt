package com.rheotv.android.data.network.models.useProfile.responses

import com.google.gson.annotations.SerializedName

data class WalletResponse(

        @field:SerializedName("data")
        val data: WalletDetail? = null
)