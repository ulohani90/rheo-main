package com.rheotv.android.ui.activities.moderators;

import com.google.gson.Gson;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddModeratorsViewModel extends BaseViewModel<AddModeratorsNavigator> {
    public AddModeratorsViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void postModeratorsData(String moderators) {
        getDataManager().postModeratorsData(moderators).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null && response.body() != null) {
                    getNavigator().onRequestSuccess();
                } else {
                    if (response != null && response.errorBody() != null) {
                        try {
                            getNavigator().showToast(response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getNavigator().showToast(t.toString());
            }
        });
    }
}
