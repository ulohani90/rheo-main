package com.rheotv.android.ui.activities.tabcontainer.clips;

import com.rheotv.android.data.network.models.postlisting.responses.Result;

import java.util.List;

public interface ClipsFragmentNavigator {

    void setClipsData(List<Result> clips);

    void setLoadMoreAllowed(boolean isLoadMoreAllowed);

    void openLoginFlow();

    void showToast();

    void showLoading();

    void hideLoading();

    void showError();

    void startFetchingClips();

    void showSuccessToast(String message);

    void showErrorToast(String message);

    void onBottomSheetDismiss();
}
