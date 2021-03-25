package com.rheotv.android.ui.activities.tabcontainer.videoUpload;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.R;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.general.GameDetails;
import com.rheotv.android.data.network.models.general.SignedUrlResponse;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class VideoUploadViewModel extends BaseViewModel {
    private MutableLiveData<List<GameDetails>> gameResults = new MutableLiveData<>();
    private ObservableField<String> postTitle = new ObservableField<>();
    private ObservableField<Boolean> submitted = new ObservableField<>(false);
    private ObservableField<Boolean> termsCondition = new ObservableField<>(true);
    private MutableLiveData<String> signedUrl = new MutableLiveData<>();
    private ObservableField<Boolean> clipAdded = new ObservableField<>(false);
    private ObservableField<Integer> uploadProgress = new ObservableField<>(-1);
    private String videoMode = "landscape";
    private int videoDuration = 0;
    private String gameId;
    private boolean isSignedUrlApiCalled = false;
    public HashMap<String, Object> baseProperties = new HashMap<>();

    public MutableLiveData<List<GameDetails>> getGameResults() {
        return gameResults;
    }

    public ObservableField<String> getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(ObservableField<String> postTitle) {
        this.postTitle = postTitle;
    }

    public ObservableField<Boolean> getSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean isSubmitted) {
        this.submitted.set(isSubmitted);
    }

    public ObservableField<Boolean> getTermsCondition() {
        return termsCondition;
    }

    public void setTermsCondition(ObservableField<Boolean> termsCondition) {
        this.termsCondition = termsCondition;
    }

    public ObservableField<Boolean> getClipAdded() {
        return clipAdded;
    }

    public void setClipAdded(boolean clipAdded) {
        this.clipAdded.set(clipAdded);
    }

    public void setVideoMode(String videoMode) {
        this.videoMode = videoMode;
    }

    public void setVideoDuration(int videoDuration) {
        this.videoDuration = videoDuration;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }

    public MutableLiveData<String> getSignedUrl() {
        return signedUrl;
    }

    public boolean isSignedUrlApiCalled() {
        return isSignedUrlApiCalled;
    }

    public void setSignedUrlApiCalled(boolean signedUrlApiCalled) {
        isSignedUrlApiCalled = signedUrlApiCalled;
    }

    public ObservableField<Integer> getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(Integer uploadProgress) {
        this.uploadProgress.set(uploadProgress);
    }

    public void updateProgress(int progress) {
        this.uploadProgress.set(progress);
    }

    public VideoUploadViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    void buildSignedUrl() {
        setSignedUrlApiCalled(true);
        getDataManager().getSignedUrl(videoDuration).enqueue(new Callback<SignedUrlResponse>() {
            @Override
            public void onResponse(Call<SignedUrlResponse> call, Response<SignedUrlResponse> response) {
                if (response.body() != null) {
                    signedUrl.setValue(response.body().getUploadUrl());
                    setSignedUrlApiCalled(false);
                }
            }

            @Override
            public void onFailure(Call<SignedUrlResponse> call, Throwable t) {
                setSignedUrlApiCalled(false);
                Toast.makeText(getNonUiContext(), R.string.internet_not_working, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void loadGameDetails() {
        getDataManager().getGameDetails().enqueue(new Callback<List<GameDetails>>() {
            @Override
            public void onResponse(Call<List<GameDetails>> call, Response<List<GameDetails>> response) {
                if (response.isSuccessful()) {
                    gameResults.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<GameDetails>> call, Throwable t) {

            }
        });
    }

    void createStory() {
        Log.i(getClass().getSimpleName(), "createStory_called");
        String videoFileUrl = "";
        String uploadUrl = signedUrl.getValue();
        uploadUrl = uploadUrl == null ? "" : uploadUrl.substring(0, uploadUrl.indexOf("?"));
        getDataManager().createStory(postTitle.get(), gameId, uploadUrl, videoFileUrl, videoDuration, videoMode).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    HashMap<String, Object> properties = new HashMap<>(baseProperties);
                    properties.put("gameId", gameId);
                    properties.put("headline", postTitle.get());
                    SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_UPLOAD_VIDEO_CREATE_STORY, properties);
                    submitted.set(true);
                }

                uploadProgress.set(100);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getNonUiContext(), R.string.internet_not_working, Toast.LENGTH_SHORT).show();
                uploadProgress.set(100);
            }
        });
    }

}
