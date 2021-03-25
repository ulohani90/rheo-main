package com.rheotv.android.ui.activities.tabcontainer.profile.videos;

public interface VideosFragmentNavigator {
    void handleError(Throwable throwable);

    void showNullView();

    void showReportPostSuccessToast();

    void onDeleteVideoSuccess(int position);

    void onDeleteVideoFailure();

    void handleLogin();
}
