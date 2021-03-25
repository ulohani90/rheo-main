package com.rheotv.android.ui.activities.onboarding;

import com.rheotv.android.data.network.models.onboarding.OnBoardingResponse;
import com.rheotv.android.data.network.models.postlisting.responses.Result;

public interface OnBoardingActivityNavigator {


    public void setOnBoardingData(OnBoardingResponse response);

    void showToast(String message);

    void closeOnBoarding();

    void showCompetionPage(Result body);
}
