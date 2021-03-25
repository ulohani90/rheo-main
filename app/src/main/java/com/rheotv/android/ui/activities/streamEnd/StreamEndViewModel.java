package com.rheotv.android.ui.activities.streamEnd;

import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.R;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.objects.VideoListingResponse;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class StreamEndViewModel extends BaseViewModel {
    public final MutableLiveData<List<PostObject>> blogListLiveData = new MutableLiveData<>();
    public final ObservableField<Status> loadingStatus = new ObservableField<>();
    private String nextUrl;

    public StreamEndViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void loadSimilarPost() {
        loadingStatus.set(Status.LOADING);
        getDataManager().loadSimilarPosts().enqueue(new Callback<VideoListingResponse>() {
            @Override
            public void onResponse(Call<VideoListingResponse> call, Response<VideoListingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    blogListLiveData.setValue(response.body().getResults());
                    nextUrl = response.body().getNext();
                    loadingStatus.set(Status.SUCCESS);
                }
            }

            @Override
            public void onFailure(Call<VideoListingResponse> call, Throwable t) {
                loadingStatus.set(Status.ERROR);
            }
        });
    }

    public void reportPost(String postId) {
        getDataManager().postReport(postId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(getNonUiContext(), getNonUiContext().getString(R.string.post_report_success), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
