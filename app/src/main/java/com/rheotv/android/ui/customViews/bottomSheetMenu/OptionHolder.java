package com.rheotv.android.ui.customViews.bottomSheetMenu;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Holds either the resource options or the custom option
 */
public class OptionHolder implements Parcelable {
    private Integer resource;
    private OptionRequest optionRequest;

    public OptionHolder(Integer resource, OptionRequest optionRequest) {
        this.resource = resource;
        this.optionRequest = optionRequest;
    }

    protected OptionHolder(Parcel in) {
        resource = in.readInt();
    }

    public static final Creator<OptionHolder> CREATOR = new Creator<OptionHolder>() {
        @Override
        public OptionHolder createFromParcel(Parcel in) {
            return new OptionHolder(in);
        }

        @Override
        public OptionHolder[] newArray(int size) {
            return new OptionHolder[size];
        }
    };

    public Integer getResource() {
        return resource;
    }

    public OptionRequest getOptionRequest() {
        return optionRequest;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (resource != null)
            parcel.writeInt(resource);
    }
}

