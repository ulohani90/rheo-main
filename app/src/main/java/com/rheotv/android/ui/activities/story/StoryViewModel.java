package com.rheotv.android.ui.activities.story;

import android.util.Log;
import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.story.StoryAuthorResponse;
import com.rheotv.android.data.network.models.story.StoryResponse;
import com.rheotv.android.data.network.models.story.StoryResult;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.story.model.Story;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class StoryViewModel extends BaseViewModel {
    MutableLiveData<StoryResult> stories = new MutableLiveData<>();
    ObservableField<String> shouldResume = new ObservableField<>();
    List<ProfileResult> profiles = new ArrayList<>();
    public String nextStoryAuthorUrl = null;
    MutableLiveData<List<ProfileResult>> newStories = new MutableLiveData<>();
    String singleAuthorId;

    MutableLiveData<StoryAction> storyAction = new MutableLiveData<>();

    public int index = 0;
    public String storyId;
    boolean isFromDeeplink = false;
    MutableLiveData<Status> loadingStatus = new MutableLiveData<>();

    MutableLiveData<Boolean> followStatus = new MutableLiveData<>();
    ObservableField<String> interestedStatus = new ObservableField<>();

    public void loadNext() {
        if (index < profiles.size()) {
            Log.i(getClass().getSimpleName(), "loadNext_story: " + index);
            index++;
//            profile = profiles.get(index);
//            authorId.setValue(profile.getId());
        } else {
            Log.i(getClass().getSimpleName(), "Stories Ended");
        }
    }

    public void loadPrevious() {
        Log.i(getClass().getSimpleName(), "loadPrevious_story: " + index);
        index--;
        if (index >= 0) {
//            profile = profiles.get(index);
//            authorId.setValue(profile.getId());
        } else {
            index = 0;
        }
    }

    public StoryViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void loadStories(String authorId) {
        loadingStatus.setValue(Status.LOADING);

        getDataManager().loadUserStories(authorId, storyId).enqueue(new Callback<StoryResponse>() {
            @Override
            public void onResponse(Call<StoryResponse> call, Response<StoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    stories.setValue(new StoryResult(response.body().getStories(), response.body().getProfileResult()));

                } else {
                    loadingStatus.setValue(Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<StoryResponse> call, Throwable t) {
                loadingStatus.setValue(Status.ERROR);
            }
        });
    }

    public void loadStoryAuthor() {
        if (nextStoryAuthorUrl == null) return;
        String userId = CommonUtils.getUserName();
        if (!CommonUtils.isUserLoggedin())
            userId = "notloggedin";

        getDataManager().loadStoryAuthors(userId, nextStoryAuthorUrl).enqueue(new Callback<StoryAuthorResponse>() {
            @Override
            public void onResponse(Call<StoryAuthorResponse> call, Response<StoryAuthorResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getResult() != null) {
                        if (response.body().getSelfId() != null)
                            CommonUtils.setAuthorId(response.body().getSelfId());
                        if (response.body().getProfilePic() != null)
                            CommonUtils.setProfileImageUrl(response.body().getProfilePic());


                        newStories.setValue(response.body().getResult());
                        nextStoryAuthorUrl = response.body().getNext();
                    } else {
                        newStories.setValue(new ArrayList<>());
                    }
                }
            }

            @Override
            public void onFailure(Call<StoryAuthorResponse> call, Throwable t) {

            }
        });

    }

    public void markStoryWatched(Story story) {
        if (story == null || story.getId() == null) return;
        Log.i(getClass().getSimpleName(), "markStoryWatched: " + story.getId());
        getDataManager().watchedStory(story.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null)
                    story.setWatched(true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void reportStory(Story story) {
        getDataManager().reportStory(story.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getNonUiContext(), "Thanks for your feedback.", Toast.LENGTH_SHORT).show();
                    shouldResume.set(story.getId() + System.currentTimeMillis());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void followUnFollow(String userId, boolean isFollowing) {
        if (isFollowing) {
            getDataManager()
                    .unFollowAuthor(userId)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                                followStatus.setValue(false);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(getClass().getSimpleName(), "followUnFollow_error: " + t.getMessage());
                        }
                    });
        } else {
            getDataManager()
                    .followAuthor(userId)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                                followStatus.setValue(true);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(getClass().getSimpleName(), "followUnFollow_error: " + t.getMessage());
                        }
                    });
        }
    }

    public void interestedStory(Story story) {
        getDataManager().interestedStory(story.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    interestedStatus.set(story.getId() + System.currentTimeMillis());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(getClass().getSimpleName(), "interestedStory_fail");
            }
        });
    }

    public void markCurrentProfileAsViewed() {
        try {
            profiles.get(index).setStoryViewed(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateStoryAction(StoryAction action) {
        storyAction.postValue(action);
    }
}
