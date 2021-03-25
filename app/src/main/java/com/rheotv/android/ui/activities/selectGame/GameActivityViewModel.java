package com.rheotv.android.ui.activities.selectGame;

import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.directVideo.VideoResponse;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class GameActivityViewModel extends BaseViewModel {
    MutableLiveData<Result> competition = new MutableLiveData<>();
    MutableLiveData<Status> updatingLanguage = new MutableLiveData<>();
    public ArrayList<PostObject> directPost = new ArrayList<>();

    public GameActivityViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        if (CommonUtils.isDirectVideoWatchUser())
            getDirectVideo();
    }

    public void getCompetitionData(String competitionId) {
        getDataManager().getCompetitionPage(competitionId).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                competition.setValue(response.body());
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {

            }
        });
    }

    public void updateLanguage(String langId) {
        ArrayList<String> selectedIds = new ArrayList<>();
        selectedIds.add(langId);
        if (selectedIds.isEmpty()) {
            Toast.makeText(getNonUiContext(), "Please select a language", Toast.LENGTH_SHORT).show();
            return;
        }

//        Log.i(getClass().getSimpleName(), "updateLanguage: " + new Gson().toJson(selectedIds));

        updatingLanguage.setValue(Status.LOADING);
        getDataManager().setUserLanguage(selectedIds).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    updatingLanguage.setValue(Status.SUCCESS);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                updatingLanguage.setValue(Status.ERROR);
            }
        });
    }

    public void getDirectVideo() {
        getDataManager().loadPost(AppConstants.GET_DIRECT_VIDEO).enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getResults().isEmpty())
                    directPost = new ArrayList<>(response.body().getResults().get(0).getPosts());
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {

            }
        });
    }
}
