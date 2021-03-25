package com.rheotv.android.ui.activities.moderators;

public interface AddModeratorsNavigator {

    void onRequestSuccess();

    void onRequestFailed();

    void showToast(String message);
}
