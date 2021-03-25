package com.rheotv.android.data.network.models.useProfile.responses

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.rheotv.android.utils.TimeUtils
import java.util.*

data class RedeemStatement(
        @SerializedName("amount")
        val amount: Int,
        @SerializedName("redeemed_on")
        val redeemedOn: String?,
        @SerializedName("redeemed_request_on")
        val redeemedRequestOn: String?,
        @SerializedName("state")
        val state: String?,
        @SerializedName("upi_id")
        val transferredTo: String?,
        @SerializedName("mobile_number")
        val mobileNo: String?,
        var transferVia: String? = "UPI"
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString() ?: "UPI")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(amount)
        parcel.writeString(redeemedOn)
        parcel.writeString(redeemedRequestOn)
        parcel.writeString(state)
        parcel.writeString(transferredTo)
        parcel.writeString(mobileNo)
        parcel.writeString(transferVia)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getFormattedDate(): String? {
        val date = TimeUtils.getDateFromString(redeemedOn
                ?: redeemedRequestOn, TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX)
        return TimeUtils.getFormattedDate(TimeUtils.DD_MMM_YYYY, date).replace("-", " ")
    }

    companion object CREATOR : Parcelable.Creator<RedeemStatement> {
        override fun createFromParcel(parcel: Parcel): RedeemStatement {
            return RedeemStatement(parcel)
        }

        override fun newArray(size: Int): Array<RedeemStatement?> {
            return arrayOfNulls(size)
        }
    }

}
