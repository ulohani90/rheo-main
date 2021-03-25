package com.rheotv.android.ui.activities.player.activity;

import android.content.Context;
import android.util.Log;

import androidx.databinding.ObservableField;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.postlisting.responses.Comments;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatViewModel extends BaseViewModel<ChatNavigator> {
    public String commentNextUrl = null;
    public List<String> slangs = new ArrayList<>();
    public boolean canComment;
    public String stickersNextUrl = "";
    public int localHeartCounter = 0;
    public ObservableField<String> totalHeartCount = new ObservableField<>();
    public int heartCount = 0;

    public HashMap<String, Object> baseProperties = new HashMap<>();

    public void updateHeartCount(@Nullable String count) {
        if (count != null) {
            localHeartCounter = Integer.parseInt(totalHeartCount.get() == null ? "0" : totalHeartCount.get()) + Integer.parseInt(Objects.requireNonNull(count));
            totalHeartCount.set((localHeartCounter / 1000 >= 1) ? (localHeartCounter / 1000) + "." + ((localHeartCounter % 1000) / 100) + "K" : localHeartCounter + "");
        }
    }

    public void updateHeartCount() {
        if (totalHeartCount.get() == null) return;
        localHeartCounter = localHeartCounter + 1;
        totalHeartCount.set((localHeartCounter / 1000 >= 1) ? (localHeartCounter / 1000) + "." + ((localHeartCounter % 1000) / 100) + "K" : localHeartCounter + "");
    }

    public ChatViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void fetchComments(String id) {
        getDataManager().getComments(id).enqueue(new Callback<Comments>() {
            @Override
            public void onResponse(Call<Comments> call, Response<Comments> response) {
                if (response != null && response.body() != null && getNavigator() != null) {
                    getNavigator().addItemsInChat(id, response.body().getResults());
                    commentNextUrl = response.body().getNext();
                    try {
                        if (slangs.size() == 0)
                            slangs.addAll(response.body().getSlangs());
                        canComment = response.body().isCanComment();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<Comments> call, Throwable t) {

            }

        });
    }

    public void fetchCommentsFromUrl(String id) {
        if (commentNextUrl == null) {
            return;
        }
        getDataManager().getPagedCommentsFromUrl(commentNextUrl).enqueue(new Callback<Comments>() {
            @Override
            public void onResponse(Call<Comments> call, Response<Comments> response) {
                if (response != null && response.body() != null && response.body().getResults() != null && getNavigator() != null) {
                    getNavigator().addItemsInChat(id, response.body().getResults());
                    commentNextUrl = response.body().getNext();
                }
            }

            @Override
            public void onFailure(Call<Comments> call, Throwable t) {

            }

        });
    }

    public void reportComment(String postId, String username, String comment, boolean isActionDelete) {
        getDataManager().reportComment(postId, username, comment).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (getNavigator() != null) {
                    if (isActionDelete)
                        getNavigator().showDeleteSuccessToast();
                    else
                        getNavigator().showReportPostSuccessToast();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void blockUser(String postId, String username, String comment) {
        getDataManager().blockUser(postId, username, comment).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                getNavigator().onBlockUserSuccess();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /*public void loadStickers(String postId) {
        getDataManager().loadStickers(postId, stickersNextUrl).enqueue(new Callback<StickersResponse>() {
            @Override
            public void onResponse(Call<StickersResponse> call, Response<StickersResponse> response) {
                if (response.body() != null && getNavigator() != null) {
                    getNavigator().onStickersLoadComplete(response.body().getResults());
                    stickersNextUrl = response.body().getNext();
                }
            }

            @Override
            public void onFailure(Call<StickersResponse> call, Throwable t) {
                if (t != null)
                    Log.i(PlayerViewModel.class.getCanonicalName(), t.getLocalizedMessage());
            }
        });
    }*/

    private boolean isInitialHeartSent = false;

    public void addHeart(@Nullable String url, String postId, String userName, String streamerName, Context context) {
        getDataManager().postHeart(postId, url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(PlayerViewModel.class.getName(), "Heart Post Success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(PlayerViewModel.class.getName(), "Heart Post Fail");
            }
        });

        HashMap<String, Object> property = baseProperties;
        property.put("postId", postId);
        property.put("selfUserName", userName);
        if (url != null)
            property.put("url", url);
        property.put("author", streamerName);

        if (!isInitialHeartSent) {
            isInitialHeartSent = true;
            SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_HEART_FIRST_CLICK, property);
        }

//        localHeartCounter = localHeartCounter + 1;
//        totalHeartCount.set((localHeartCounter / 1000 >= 1) ? (localHeartCounter / 1000) + "." + ((localHeartCounter % 1000) / 100) + "K" : localHeartCounter + "");
        getNavigator().onHeartUpdate(localHeartCounter);
    }
}
