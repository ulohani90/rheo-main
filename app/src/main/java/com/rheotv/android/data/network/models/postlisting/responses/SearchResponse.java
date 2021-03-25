package com.rheotv.android.data.network.models.postlisting.responses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SearchResponse implements Parcelable {
    @SerializedName("item_type")
    @Expose
    int itemType;
    @SerializedName("item")
    @Expose
    SearchItemsResponse searchItemsResponse;

    protected SearchResponse(Parcel in) {
        itemType = in.readInt();
        searchItemsResponse = in.readParcelable(SearchItemsResponse.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(itemType);
        dest.writeParcelable(searchItemsResponse, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SearchResponse> CREATOR = new Creator<SearchResponse>() {
        @Override
        public SearchResponse createFromParcel(Parcel in) {
            return new SearchResponse(in);
        }

        @Override
        public SearchResponse[] newArray(int size) {
            return new SearchResponse[size];
        }
    };

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public SearchItemsResponse getSearchItemsResponse() {
        return searchItemsResponse;
    }

    public void setSearchItemsResponse(SearchItemsResponse searchItemsResponse) {
        this.searchItemsResponse = searchItemsResponse;
    }
}
