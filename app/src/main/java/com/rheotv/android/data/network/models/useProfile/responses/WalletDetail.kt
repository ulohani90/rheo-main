package com.rheotv.android.data.network.models.useProfile.responses

import com.google.gson.annotations.SerializedName
import com.rheotv.android.R
import com.rheotv.android.data.network.models.postlisting.responses.User
import com.rheotv.android.ui.adapters.LevelType
import com.rheotv.android.utils.Level

data class WalletDetail(

        @field:SerializedName("payment_model")
        val paymentModel: Int? = null,

        @field:SerializedName("can_redeem_available_balance")
        val canRedeemAvailableBalance: Boolean? = null,

        @field:SerializedName("level")
        val level: String? = null,

        @field:SerializedName("next_redeem_date")
        val nextRedeemDate: String? = null,

        @field:SerializedName("current_rheo_diamond_value")
        val currentRheoDiamondValue: Float? = null,

        @field:SerializedName("available_redeem_balance")
        val availableRedeemBalance: Int? = null,

        @field:SerializedName("is_level_assigned")
        val isLevelAssigned: Boolean? = null,

        @SerializedName("minimum_redeem_balance")
        var minimumRedeemBalance: Int = 0,

        @SerializedName("streaming_opening_statement")
        var streamingOpeningStatement: String? = null,

        @SerializedName("user")
        var user: User? = null,

        @SerializedName("redeem_statements")
        val redeemStatement: List<RedeemStatement> = ArrayList()
) {

    val redeemAmount: String
        get() = "$availableRedeemBalance Rheo Diamond${if (availableRedeemBalance ?: 0 <= 1) "" else "s"}"

    val conversion: String
        get() = "1 Rheo Diamond = ₹ $currentRheoDiamondValue"

    val isBronze: Boolean
        get() = if (level.isNullOrEmpty()) false else Level.valueOf(level.toLowerCase()) == Level.bronze

    val isSilver: Boolean
        get() = if (level.isNullOrEmpty()) false else Level.valueOf(level.toLowerCase()) == Level.silver

    val isGold: Boolean
        get() = if (level.isNullOrEmpty()) false else Level.valueOf(level.toLowerCase()) == Level.gold

    val stateColor: Int
        get() = if (canRedeemAvailableBalance == true)
            R.color.color_accent
        else
            R.color.color_remaining_target

    val toLevelType: LevelType
        get() = if (level != null) {
            if (level.equals("bronze", ignoreCase = true)) {
                if (isLevelAssigned == true)
                    LevelType.Bronze
                else
                    LevelType.Unassigned
            } else if (level.equals("silver", ignoreCase = true)) {
                LevelType.Silver
            } else if (level.equals("gold", ignoreCase = true)) {
                LevelType.Gold
            } else
                LevelType.Unassigned
        } else
            LevelType.Unassigned

    val canRedeem: Boolean
        get() = canRedeemAvailableBalance == true && (availableRedeemBalance ?: 0) >= minimumRedeemBalance
}