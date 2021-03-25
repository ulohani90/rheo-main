package com.rheotv.android.data.network.models.stickers;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class LanguagesSlang implements Parcelable {

    String language;

    List<String> slangs;


    protected LanguagesSlang(Parcel in) {
        language = in.readString();
        slangs = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(language);
        dest.writeStringList(slangs);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LanguagesSlang> CREATOR = new Creator<LanguagesSlang>() {
        @Override
        public LanguagesSlang createFromParcel(Parcel in) {
            return new LanguagesSlang(in);
        }

        @Override
        public LanguagesSlang[] newArray(int size) {
            return new LanguagesSlang[size];
        }
    };

    public List<String> getSlangs() {
        return slangs;
    }


    public String getLanguage() {
        return language;
    }
}
