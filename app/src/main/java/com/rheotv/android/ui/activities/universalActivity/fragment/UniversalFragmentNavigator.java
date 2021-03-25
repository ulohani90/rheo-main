package com.rheotv.android.ui.activities.universalActivity.fragment;

public interface UniversalFragmentNavigator {

    void showToast(String message);

    void showReportPostSuccessToast();

    void handleError(Throwable throwable);

    void handleLogin();

}
