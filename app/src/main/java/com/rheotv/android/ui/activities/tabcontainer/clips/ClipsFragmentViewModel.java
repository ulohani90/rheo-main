package com.rheotv.android.ui.activities.tabcontainer.clips;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.ModeratorQuestionsResponse;
import com.rheotv.android.data.network.models.FollowResponse;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.db.AppDatabase;
import com.rheotv.android.db.ClipDao;
import com.rheotv.android.db.ClipItem;
import com.rheotv.android.db.ClipResponse;
import com.rheotv.android.db.UserFollowDao;
import com.rheotv.android.db.UserFollowItem;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.activities.player.activity.ApiCompleteListener;
import com.rheotv.android.ui.activities.player.activity.ChatMenuOptionData;
import com.rheotv.android.ui.activities.player.activity.FollowResult;
import com.rheotv.android.ui.activities.player.activity.FollowStatusCompleteListener;
import com.rheotv.android.ui.activities.player.activity.FollowStatusListener;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.ModeratorQuestions;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.Dispatchers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class ClipsFragmentViewModel extends BaseViewModel<ClipsFragmentNavigator> {

    ClipDao dao;

    String nextUrl = "";

    MutableLiveData<List<ClipItem>> clipsLiveList = new MutableLiveData<>();

    public ObservableField<Boolean> showLoading = new ObservableField<>();

    public ObservableField<Boolean> moderatorResponseSubmitted = new ObservableField<>();

    public ObservableField<Status> state = new ObservableField<>();
    private boolean firstApiCalled = false;

    private UserFollowDao userFollowDao = AppDatabase.Companion.getInstance(RheoTvApp.getNonUiContext()).userFollowDao();

    public boolean isFirstApiCalled() {
        return firstApiCalled;
    }

    List<ClipItem> clips = new ArrayList<>();
    public HashMap<String, Object> properties = new HashMap<>();

    public String getLastClipId() {
        return (clipsLiveList.getValue() != null && clipsLiveList.getValue().size() > 0) ? clipsLiveList.getValue().get(clipsLiveList.getValue().size() - 1).getId() : null;
    }

    public ClipsFragmentViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        if (dao == null)
            dao = AppDatabase.Companion.getInstance(RheoTvApp.getNonUiContext()).clipDao();
    }

    void loadUserFollowStatus(String username, ApiCompleteListener callback, FollowStatusListener followStatusListener) {
        AtomicBoolean isUserFound = new AtomicBoolean(false);
        AppUtilsKt.INSTANCE.runOnIO(this, () -> {
            UserFollowItem userFollowItem = userFollowDao.checkIfIsFollowedWithUsername(username);
            AppUtilsKt.INSTANCE.runOnMain(this, () -> {
                if (userFollowItem != null) {
                    isUserFound.set(true);
                    if (followStatusListener != null)
                        followStatusListener.followStatus(userFollowItem.isFollowed());
                }
                getDataManager().getProfile(username).enqueue(new Callback<ProfileResult>() {
                    @Override
                    public void onResponse(Call<ProfileResult> call, Response<ProfileResult> response) {
                        if (response != null && response.body() != null) {
                            insertFollowStatusInDB(response.body().getUser().getId(), response.body().getUser().getUsername(),
                                    response.body().getFollowed(), new ClipsFragment.OnFollowStatusUpdateListener() {
                                        @Override
                                        public void onFollowStatusUpdate(boolean isFollowed) {
                                            if (!isUserFound.get() && followStatusListener != null)
                                                followStatusListener.followStatus(isFollowed);
                                            if (callback != null) {
                                                callback.updateProfileDataForBottomSheet(new FollowResult.Success(response.body()));
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResult> call, Throwable t) {
                        AppUtilsKt.INSTANCE.runOnMain(ClipsFragmentViewModel.this, () -> {
                            if (callback != null) {
                                callback.updateProfileDataForBottomSheet(new FollowResult.Error(t));
                            }
                            return null;
                        });
                    }
                });
                return null;
            });
            return null;
        });
    }

    void loadUserFollowState(ClipItem clipItem, ClipsFragment.OnFollowStatusUpdateListener listener) {
        if (clipItem == null || clipItem.getAuthor() == null || clipItem.getAuthor().getUser() == null || clipItem.getAuthor().getUser().getId() <= 0)
            return;
        AtomicBoolean isUserFound = new AtomicBoolean(false);
        AppUtilsKt.INSTANCE.runOnIO(this, () -> {
            UserFollowItem userFollowItem = userFollowDao.checkIfIsFollowedWithUserId(clipItem.getAuthor().getUser().getId());
            AppUtilsKt.INSTANCE.runOnMain(this, () -> {
                if (userFollowItem != null) {
                    isUserFound.set(true);
                    if (listener != null)
                        listener.onFollowStatusUpdate(userFollowItem.isFollowed());
                }
                getDataManager().checkFollowAuthor(String.valueOf(clipItem.getAuthor().getUser().getId())).enqueue(new Callback<FollowResponse>() {
                    @Override
                    public void onResponse(Call<FollowResponse> call, Response<FollowResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            insertFollowStatusInDB(clipItem.getAuthor().getUser().getId(), clipItem.getAuthor().getUser().getUsername(),
                                    response.body().isFollow(), isFollowed -> {
                                        if (!isUserFound.get() && listener != null)
                                            listener.onFollowStatusUpdate(isFollowed);
                                    });
                        }
                    }

                    @Override
                    public void onFailure(Call<FollowResponse> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
                return null;
            });
            return null;
        });
    }

    private void insertFollowStatusInDB(int userId, String userName, boolean isFollow, ClipsFragment.OnFollowStatusUpdateListener listener) {
        AppUtilsKt.INSTANCE.runOnIO(this, () -> {
            userFollowDao.insertUserWithIgnore(new UserFollowItem(userId, userName, isFollow));
            UserFollowItem userFollowItem = userFollowDao.checkIfIsFollowedWithUserId(userId);
            if (userFollowItem != null)
                AppUtilsKt.INSTANCE.runOnMain(ClipsFragmentViewModel.this, () -> {
                    listener.onFollowStatusUpdate(userFollowItem.isFollowed());
                    return null;
                });
            return null;
        });
    }

    ChatMenuOptionData getClipOptionMenuBottomSheetData(ClipItem result) {
        return new ChatMenuOptionData(
                result.getAuthor() != null ? (result.getAuthor().getUser() != null ? result.getAuthor().getUser().getUsername() : "") : "",
                result.getAuthor() != null ? result.getAuthor().getProfilePic() : "", null,
                (followUserName, listener, followStatusListener) -> {
                    loadUserFollowStatus(followUserName, listener, followStatusListener);
                    return null;
                },
                (followState, followUserId, followUserName, listener) -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("is_first", CommonUtils.isFirstTimeFollow());
                    map.put("author", result.getAuthor().getUser().getUsername());
                    map.put("source", SegmentConstants.SCREEN_NAME_VIDEO_PLAYER_CHAT);
                    SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
                    CommonUtils.setFirstTimeFollow();
                    followClicked(result, "follow".equalsIgnoreCase(followState), listener);
                    return null;

                },
                () -> {
                    if (getNavigator() != null)
                        getNavigator().openLoginFlow();
                    return null;
                }, () -> {
            if (getNavigator() != null)
                getNavigator().onBottomSheetDismiss();
            return null;
        });
    }

    public void fetchClips(boolean isFreshLoad) {
        firstApiCalled = true;
        if (isFreshLoad) {
            getNavigator().showLoading();
        }
        if (nextUrl == null) {
            return;
        }
        getDataManager().getClips(nextUrl).enqueue(new Callback<ClipResponse>() {
            @Override
            public void onResponse(Call<ClipResponse> call, Response<ClipResponse> response) {
                if (getNavigator() != null) {
                    if (response != null && response.body() != null) {
                        nextUrl = response.body().getNext();
                        // getNavigator().setClipsData(response.body().getClipItems());
                        if (isFreshLoad) {
                            if (clipsLiveList != null && clipsLiveList.getValue() != null) {
                                clipsLiveList.getValue().clear();
                            }
                        }
                        clips.addAll(response.body().getResult());
                        clipsLiveList.setValue(response.body().getResult());

                        if (nextUrl == null) {
                            getNavigator().setLoadMoreAllowed(false);
                        }
                        if (isFreshLoad) {
                            getNavigator().hideLoading();
                        }
                    } else {
                        if (isFreshLoad) {
                            getNavigator().showError();
                        } else {

                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ClipResponse> call, Throwable t) {
                Log.i("Tag", "Error");
                if (isFreshLoad && getNavigator() != null) {
                    getNavigator().showError();
                }

            }
        });

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint({"CheckClipItem", "CheckResult"})
    public void loadOfflineData() {
        dao.getClips()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list ->
                {
                    nextUrl = null;
                    clipsLiveList.setValue(list);
                    //consume modelClasses here which is a list of ModelClass
                    System.out.println("RoomWithRx: " + list.size());

                }, e -> System.out.println("RoomWithRx: " + e.getMessage()));
    }

    public void fetchClip(String clipId) {
        getNavigator().showLoading();
        getDataManager().fetchClip(clipId).enqueue(new Callback<ClipItem>() {
            @Override
            public void onResponse(Call<ClipItem> call, Response<ClipItem> response) {
                if (response != null && response.body() != null) {
                    clips.clear();
                    clips.add(response.body());
                    clipsLiveList.setValue(clips);
                    getNavigator().hideLoading();
                    getNavigator().startFetchingClips();
                }
            }

            @Override
            public void onFailure(Call<ClipItem> call, Throwable t) {
                getNavigator().showError();
            }
        });
    }

    public void followClicked(int userId, boolean isFollowed) {
//        followClicked(userId, isFollowed, null);
    }

    public void followClicked(ClipItem clipItem, boolean isFollowed, FollowStatusCompleteListener listener) {
        if (clipItem == null || clipItem.getAuthor() == null || clipItem.getAuthor().getUser() == null || clipItem.getAuthor().getUser().getId() <= 0)
            return;
        if (!CommonUtils.isUserLoggedin()) {
            getNavigator().openLoginFlow();
            return;
        }
        properties.put("userId", clipItem.getAuthor().getUser().getId());
        properties.put("followAction", isFollowed);
        properties.put("from", "clips");
        SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, properties);
        updateFollowStatusInDB(clipItem.getAuthor().getUser().getId(), clipItem.getAuthor().getUser().getUsername(), isFollowed, followed -> {
            if (listener != null)
                listener.success();
            if (!isFollowed) {

                getDataManager()
                        .unFollowAuthor(String.valueOf(clipItem.getAuthor().getUser().getId()))
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                Log.i(ClipsFragmentViewModel.class.getCanonicalName(), "Success");
                                EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                                if (listener != null) {
//                                if (response != null && response.body() != null) {
//                                    listener.success();
//
//                                } else {
//                                    listener.error();
//                                }
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.i(ClipsFragmentViewModel.class.getCanonicalName(), "Failure");
                                if (listener != null)
                                    listener.error();
                            }
                        });
            } else {
                getDataManager()
                        .followAuthor(String.valueOf(clipItem.getAuthor().getUser().getId()))
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                Log.i(ClipsFragmentViewModel.class.getCanonicalName(), "Success");
                                EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                                if (listener != null) {
//                                if (response != null && response.body() != null) {
//                                    listener.success();
//                                } else {
//                                    listener.error();
//                                }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.i(ClipsFragmentViewModel.class.getCanonicalName(), "Failure");
                                if (listener != null)
                                    listener.error();
                            }
                        });
            }
        });
    }

    private void updateFollowStatusInDB(int userId, String username, boolean status, FollowStatusListener listener) {
        AppUtilsKt.INSTANCE.runOnIO(this, () -> {
            userFollowDao.updateUserEntry(new UserFollowItem(userId, username, status));
            AppUtilsKt.INSTANCE.runOnMain(this, () -> {
                if (listener != null)
                    listener.followStatus(status);
                return null;
            });
            return null;
        });
    }

    public void likeClicked(String postId) {
        getDataManager()
                .likeClip(postId)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.i(ClipsFragmentViewModel.class.getCanonicalName(), "Success");
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.i(ClipsFragmentViewModel.class.getCanonicalName(), "Failure");
                    }
                });
    }

    public void reportPost(String postId) {
        //getNavigator().showProgressBarLoading("Reporting post. Please wait..");
        getDataManager().postReport(postId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //getNavigator().hideProgressBarLoading();
                getNavigator().showToast();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //getNavigator().hideProgressBarLoading();
            }
        });
    }

    public void fetchModeratorQuestions() {
        getDataManager().loadModeratorQuestions("CLIP", "NEGATIVE").enqueue(new Callback<ModeratorQuestionsResponse>() {
            @Override
            public void onResponse(Call<ModeratorQuestionsResponse> call, Response<ModeratorQuestionsResponse> response) {
                if (response != null && response.body() != null) {
                    ModeratorQuestions.getInstance().setClipsQuestions(response.body().getResults());
                }
            }

            @Override
            public void onFailure(Call<ModeratorQuestionsResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void submitModeratorQuestionResponse(String postId, String questionId, List<String> selectedQuestionIds) {
        showLoading.set(true);
        getDataManager().submitModeratorQuestionResponse(postId, questionId, selectedQuestionIds).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null && response.body() != null) {
                    getNavigator().showSuccessToast("Response submitted successfully");
                } else {
                    getNavigator().showErrorToast("Error in submitting response");
                }
                showLoading.set(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showLoading.set(true);
                getNavigator().showErrorToast("Error in submitting response");
            }
        });
    }
}
