package com.rheotv.android.ui.activities.player.activity;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;
import com.rheotv.android.data.network.models.postlisting.responses.Comments;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreamChatViewModel extends BaseViewModel {
    public String postId;
    public boolean isModerator = false;
    public String commentNextUrl;
    public ObservableField<Integer> unreadChatCount = new ObservableField<>(0);
    ObservableField<Boolean> isLoading = new ObservableField<>(false);
    public MutableLiveData<List<CommentChat>> comments = new MutableLiveData<>();

    MutableLiveData<Status> blockUserStatus = new MutableLiveData<>();
    MutableLiveData<Status> reportComment = new MutableLiveData<>();
    MutableLiveData<Status> deleteComment = new MutableLiveData<>();

    public StreamChatViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void loadComments() {
        getDataManager().getStreamComments(postId, commentNextUrl).enqueue(new Callback<Comments>() {
            @Override
            public void onResponse(Call<Comments> call, Response<Comments> response) {
                if (response != null && response.body() != null) {
                    comments.setValue(response.body().getResults());
                    commentNextUrl = response.body().getNext();
                    isLoading.set(false);
                }
            }

            @Override
            public void onFailure(Call<Comments> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }

    public void reportComment(String username, String comment, boolean isActionDelete) {
        updateCommentStatus(isActionDelete, Status.LOADING);
        getDataManager().reportComment(postId, username, comment).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful())
                        updateCommentStatus(isActionDelete, Status.SUCCESS);
                    else
                        updateCommentStatus(isActionDelete, Status.ERROR);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                updateCommentStatus(isActionDelete, Status.ERROR);
            }
        });
    }

    private void updateCommentStatus(boolean isActionDelete, Status status) {
        if (isActionDelete)
            deleteComment.setValue(status);
        else
            reportComment.setValue(status);
    }

    public void blockUser(String username, String comment) {
        blockUserStatus.setValue(Status.LOADING);
        getDataManager().blockUser(postId, username, comment).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful())
                    blockUserStatus.setValue(Status.SUCCESS);
                else
                    blockUserStatus.setValue(Status.ERROR);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                blockUserStatus.setValue(Status.ERROR);
            }
        });
    }
}
