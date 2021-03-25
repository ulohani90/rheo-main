package com.rheotv.android.ui.fragments;

public interface LoginNavigator {
    void handleLoginSuccess();

    void askUsername(String message, String name, String photoUrl);

    void handleBackendLoginResponse(boolean isSuccessful);

    void handleFailure(String message);
}
