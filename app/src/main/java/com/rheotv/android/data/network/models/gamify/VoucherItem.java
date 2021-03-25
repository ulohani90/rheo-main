package com.rheotv.android.data.network.models.gamify;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class VoucherItem implements Parcelable {

    @SerializedName("codes")
    private String codes;

    @SerializedName("sku")
    private String sku;

    public VoucherItem(String codes, String sku) {
        this.codes = codes;
        this.sku = sku;
    }

    protected VoucherItem(Parcel in) {
        codes = in.readString();
        sku = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(codes);
        dest.writeString(sku);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VoucherItem> CREATOR = new Creator<VoucherItem>() {
        @Override
        public VoucherItem createFromParcel(Parcel in) {
            return new VoucherItem(in);
        }

        @Override
        public VoucherItem[] newArray(int size) {
            return new VoucherItem[size];
        }
    };

    public void setCodes(String codes) {
        this.codes = codes;
    }

    public String getCodes() {
        return codes;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }

    @Override
    public String toString() {
        return "ItemsItem{" +
                "codes = '" + codes + '\'' +
                ",sku = '" + sku + '\'' +
                "}";
    }
}