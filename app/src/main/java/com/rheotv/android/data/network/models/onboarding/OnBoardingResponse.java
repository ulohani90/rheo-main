package com.rheotv.android.data.network.models.onboarding;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OnBoardingResponse {

    @SerializedName("languages")
    @Expose
    List<LanguageObject> languageObjects;

    @SerializedName("images")
    @Expose
    List<String> images;

    public List<LanguageObject> getLanguageObjects() {
        return languageObjects;
    }

    public void setLanguageObjects(List<LanguageObject> languageObjects) {
        this.languageObjects = languageObjects;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
