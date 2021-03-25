package com.rheotv.android.ui.activities.profile.editprofile;

import com.rheotv.android.data.network.models.postlisting.responses.User;

import java.util.List;

public interface EditProfileNavigator {

    void openGallery(String imageType);

    void onHeaderBackPressed();

    void handleLoaderState(String type, int state);

    void showToastMessage(String type);

    User getNewUserObjectFromView();

    void showLoader();

    void hideLoader();

    String getBio();

    void showMessage(String s);

    void finishActivityOnSuccess();

    void updateUserName(String previousUsername);

    String getUsername();

    boolean isUsernameEdited();

    List<String> getSelectedLanguageId();
}
