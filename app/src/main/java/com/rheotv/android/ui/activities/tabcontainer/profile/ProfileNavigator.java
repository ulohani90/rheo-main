package com.rheotv.android.ui.activities.tabcontainer.profile;

import android.content.Context;

import com.rheotv.android.data.network.models.postlisting.responses.User;
import com.rheotv.android.data.network.models.useProfile.responses.ButtonData;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;

public interface ProfileNavigator {
    void handleError(Throwable throwable);

    void editProfile();

    void setupViewsForLoggedinUser();

    void setupViewsForNonLoggedinUser();

    void handleLogin();

    void setUpLayoutForAuthor();

    void openGallery(String type);

    User getNewUserObjectFromView();

    void showLoader(boolean show);

    void editUserName();

    Context getContextInstance();

    void showToast(String message);

    void updateUserName(String username);

    void openPartnerFlow(ButtonData buttonData);

    void updateUI(ProfileResult body);

    void navigateToHome();

    void setBio(ProfileResult result);

    void startEditProfileActivity();

    void startUploadActivity();

    void setUpRheoProgressView();

    void updateProfileViewModelData();

    void hideProgressBar();

    String getAuthorUsername();

    void setUpTabs();

    void setUpFloatinActionButton();

    void onMedalViewClick();

    void onContentModeratorVoted();
}
