package com.rheotv.android.ui.activities.story;

import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.story.StoryResponse;
import com.rheotv.android.data.network.models.story.UploadStoryMediaResponse;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.rheotv.story.Constants;
import com.rheotv.story.model.Story;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class CreateStoryViewModel extends BaseViewModel {
    MutableLiveData<ArrayList<Story>> stories = new MutableLiveData<>();
    public ArrayList<Story> storyList = new ArrayList<>();
    public ObservableField<Boolean> hasStories = new ObservableField<>(false);
    public MutableLiveData<Story> deletedMedia = new MutableLiveData<>();
    public ObservableField<Status> publishedStatus = new ObservableField<>();
    public ObservableField<Status> deleteStatus = new ObservableField<>();
    public String authorId;
    public boolean isStoryAdded = false;
    public HashMap<String, Object> baseProperties = new HashMap<>();
    public ObservableField<Story> errorUploadStory = new ObservableField<>();
    private String storyId;

    public ObservableField<Boolean> showLoading = new ObservableField<>();


    public CreateStoryViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void loadStories() {
        if (authorId == null) return;
        getDataManager().loadUserStories(authorId, storyId).enqueue(new Callback<StoryResponse>() {
            @Override
            public void onResponse(Call<StoryResponse> call, Response<StoryResponse> response) {
                if (response.isSuccessful() && response.body() != null)
                    stories.setValue(response.body().getStories());
            }

            @Override
            public void onFailure(Call<StoryResponse> call, Throwable t) {

            }
        });
    }

    public void uploadMedia(Story story) {
        if (!isStoryAdded)
            isStoryAdded = true;
        errorUploadStory.set(null);
        showLoading.set(true);
        Call<UploadStoryMediaResponse> request = getDataManager().uploadStoryMedia(story.getUrl(), story.getType(), story.getMetaData());
        if (request != null) {
            request.enqueue(new Callback<UploadStoryMediaResponse>() {
                @Override
                public void onResponse(Call<UploadStoryMediaResponse> call, Response<UploadStoryMediaResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        story.setId(response.body().getStoryId());
                        story.setState(Constants.UPLOADED);
                        Toast.makeText(getNonUiContext(), "Media Uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        errorUploadStory.set(story);
                    }
                    showLoading.set(false);
                }

                @Override
                public void onFailure(Call<UploadStoryMediaResponse> call, Throwable t) {
                    errorUploadStory.set(story);
                    t.printStackTrace();
                    showLoading.set(false);
                }
            });
        } else {
            Toast.makeText(RheoTvApp.getNonUiContext(), "Please try again", Toast.LENGTH_LONG).show();
        }
    }

    public void publishAllStories() {
        if (!isStoryAdded) {
            Toast.makeText(getNonUiContext(), "Please add a Story first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (publishedStatus.get() != Status.LOADING) {
            publishedStatus.set(Status.LOADING);
            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_STORY_PUBLISH_CLICKED, baseProperties);
        }

        for (Story story : storyList) {
            if (Constants.UPLOADED.equalsIgnoreCase(story.getState())) {
                publishStory(story);
                return;
            }
        }
        publishedStatus.set(Status.SUCCESS);
        isStoryAdded = false;
    }

    public void publishStory(Story story) {
        getDataManager().publishStory(story).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    story.setState(Constants.PUBLISHED);
                    publishAllStories();
                } else {
                    publishedStatus.set(Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                publishedStatus.set(Status.ERROR);
                t.printStackTrace();
            }
        });
    }

    public void deleteStory(Story story) {
        deleteStatus.set(Status.LOADING);
        getDataManager().deleteStory(story.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    deletedMedia.setValue(story);
                    deleteStatus.set(Status.SUCCESS);
                } else {
                    deleteStatus.set(Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                deleteStatus.set(Status.ERROR);
            }
        });
    }
}
