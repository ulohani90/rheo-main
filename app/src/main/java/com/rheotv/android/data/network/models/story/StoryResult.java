package com.rheotv.android.data.network.models.story;

import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;

import java.util.List;

public class StoryResult {
    private List<com.rheotv.story.model.Story> userStories;
    private ProfileResult userProfile;

    public StoryResult() {
    }

    public StoryResult(List<com.rheotv.story.model.Story> userStories, ProfileResult userProfile) {
        this.userStories = userStories;
        this.userProfile = userProfile;
    }

    public List<com.rheotv.story.model.Story> getUserStories() {
        return userStories;
    }

    public void setUserStories(List<com.rheotv.story.model.Story> userStories) {
        this.userStories = userStories;
    }

    public ProfileResult getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(ProfileResult userProfile) {
        this.userProfile = userProfile;
    }

}
