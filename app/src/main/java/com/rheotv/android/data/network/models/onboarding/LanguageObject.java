package com.rheotv.android.data.network.models.onboarding;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.ui.activities.profile.model.Selectable;

import org.jetbrains.annotations.Nullable;

public class LanguageObject extends Selectable implements Parcelable {
    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("display_name")
    @Expose
    String displayName;

    @SerializedName("language_image")
    @Expose
    String thumbnail;

    @SerializedName("selected")
    @Expose
    boolean isLanguageSelected;

    protected LanguageObject(Parcel in) {
        id = in.readString();
        name = in.readString();
        displayName = in.readString();
        thumbnail = in.readString();
        isLanguageSelected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(displayName);
        dest.writeString(thumbnail);
        dest.writeByte((byte) (isLanguageSelected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LanguageObject> CREATOR = new Creator<LanguageObject>() {
        @Override
        public LanguageObject createFromParcel(Parcel in) {
            return new LanguageObject(in);
        }

        @Override
        public LanguageObject[] newArray(int size) {
            return new LanguageObject[size];
        }
    };

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isLanguageSelected() {
        return isLanguageSelected;
    }

    public void setLanguageSelected(boolean selected) {
        this.isLanguageSelected = selected;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Nullable
    @Override
    public CharSequence getText() {
        return name;
    }

    @Nullable
    @Override
    public CharSequence getTag() {
        return getId();
    }

    @Override
    public void setTag(@Nullable CharSequence charSequence) {
        if (charSequence != null)
            this.id = charSequence.toString();
    }

    @Override
    public boolean isSelected() {
        return isLanguageSelected;
    }

    @Override
    public void setSelected(boolean b) {
        this.isLanguageSelected = b;
    }
}
