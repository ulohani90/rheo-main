package com.rheotv.android.data.network.models.postlisting.responses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchItemsResponse implements Parcelable {
    @SerializedName("title")
    @Expose
    String title;
    @SerializedName("action")
    @Expose
    ActionItem actionItem;

    @SerializedName("items")
    @Expose
    List<SearchItem> searchItems;

    protected SearchItemsResponse(Parcel in) {
        title = in.readString();
        searchItems = in.createTypedArrayList(SearchItem.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeTypedList(searchItems);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SearchItemsResponse> CREATOR = new Creator<SearchItemsResponse>() {
        @Override
        public SearchItemsResponse createFromParcel(Parcel in) {
            return new SearchItemsResponse(in);
        }

        @Override
        public SearchItemsResponse[] newArray(int size) {
            return new SearchItemsResponse[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ActionItem getActionItem() {
        return actionItem;
    }

    public void setActionItem(ActionItem actionItem) {
        this.actionItem = actionItem;
    }

    public List<SearchItem> getSearchItems() {
        return searchItems;
    }

    public void setSearchItems(List<SearchItem> searchItems) {
        this.searchItems = searchItems;
    }
}
