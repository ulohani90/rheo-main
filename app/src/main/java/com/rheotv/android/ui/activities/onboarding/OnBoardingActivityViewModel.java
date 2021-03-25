package com.rheotv.android.ui.activities.onboarding;

import android.util.Log;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.onboarding.OnBoardingResponse;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnBoardingActivityViewModel extends BaseViewModel<OnBoardingActivityNavigator> {


    public OnBoardingActivityViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }


    public void fetchOnBoardingData() {
        getDataManager().fetchOnBoardingData().enqueue(new Callback<OnBoardingResponse>() {
            @Override
            public void onResponse(Call<OnBoardingResponse> call, Response<OnBoardingResponse> response) {
                if (response != null && response.body() != null && getNavigator() != null) {
                    getNavigator().setOnBoardingData(response.body());
                }
            }

            @Override
            public void onFailure(Call<OnBoardingResponse> call, Throwable t) {
                Log.i(OnBoardingActivityViewModel.class.getCanonicalName(), "Failed");
            }
        });
    }

    public void updateLanguage(List<String> languageId) {

        getDataManager().setUserLanguage(languageId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null && response.body() != null) {
                    getNavigator().showToast("Language preference saved successfully");
                    getNavigator().closeOnBoarding();
                } else {
                    getNavigator().showToast("Failure");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getNavigator().showToast("Failure" + t.getLocalizedMessage());
            }
        });
    }

    public void getCompetitionData(String competitionId) {
        getDataManager().getCompetitionPage(competitionId).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (getNavigator() != null)
                    getNavigator().showCompetionPage(response.body());
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {

            }
        });
    }
}
