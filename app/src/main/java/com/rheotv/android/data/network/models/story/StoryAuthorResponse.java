package com.rheotv.android.data.network.models.story;

import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;

import java.util.ArrayList;

public class StoryAuthorResponse {

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("user_profile_id")
    private String profileId;

    @SerializedName("user_profile_pic")
    private String profilePic;

    @SerializedName("results")
    private ArrayList<ProfileResult> result;

    public ArrayList<ProfileResult> getResult() {
        return result;
    }

    public String getSelfId() {
        return profileId;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public String getNext() {
        return next;
    }
}
