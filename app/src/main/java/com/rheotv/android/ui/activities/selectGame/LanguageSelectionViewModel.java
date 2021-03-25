package com.rheotv.android.ui.activities.selectGame;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.onboarding.OnBoardingResponse;
import com.rheotv.android.ui.activities.onboarding.OnBoardingActivityViewModel;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class LanguageSelectionViewModel extends BaseViewModel {
    MutableLiveData<OnBoardingResponse> boardingResponse = new MutableLiveData<>();
    public MutableLiveData<Status> loadingLanguage = new MutableLiveData<>();
    public MutableLiveData<Status> updatingLanguage = new MutableLiveData<>();
    List<String> selectedIds = new ArrayList<>();
    Map<String, String> selectedLanguage = new HashMap<>();

    public LanguageSelectionViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    void fetchLanguage() {
        loadingLanguage.setValue(Status.LOADING);
        getDataManager().fetchOnBoardingData().enqueue(new Callback<OnBoardingResponse>() {
            @Override
            public void onResponse(Call<OnBoardingResponse> call, Response<OnBoardingResponse> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    boardingResponse.setValue(response.body());
                    loadingLanguage.setValue(Status.SUCCESS);
                } else {
                    loadingLanguage.setValue(Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<OnBoardingResponse> call, Throwable t) {
                Log.i(OnBoardingActivityViewModel.class.getCanonicalName(), "Failed");
                loadingLanguage.setValue(Status.ERROR);
            }
        });
    }

    public void updateLanguage(View view) {
        if (selectedIds.isEmpty()) {
            Toast.makeText(view.getContext(), "Please select a language", Toast.LENGTH_SHORT).show();
            return;
        }

//        Log.i(getClass().getSimpleName(), "updateLanguage: " + new Gson().toJson(selectedIds));

        updatingLanguage.setValue(Status.LOADING);
        getDataManager().setUserLanguage(selectedIds).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    try {
                        CommonUtils.setUserLanguage(getNonUiContext(), new ArrayList<>(selectedLanguage.values()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    updatingLanguage.setValue(Status.SUCCESS);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                updatingLanguage.setValue(Status.ERROR);
            }
        });
    }
}
