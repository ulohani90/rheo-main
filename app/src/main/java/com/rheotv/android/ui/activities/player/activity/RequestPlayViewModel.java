package com.rheotv.android.ui.activities.player.activity;

import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.play.RequestPlayResponse;
import com.rheotv.android.data.network.models.play.ResultsItem;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import org.jetbrains.annotations.NotNull;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestPlayViewModel extends BaseViewModel<RequestPlayNavigator> {
    public String postId = null;
    public ObservableField<Boolean> isCustomRoom = new ObservableField<>(false);

    public MutableLiveData<Boolean> showLoading = new MutableLiveData<>();

    public String customRoomWinnerUsername = null;
    private String next = null;

    private String currentRoomRheoCoin;

    public RequestPlayViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public String getNext() {
        return next;
    }

    public String getPollUrl() {
        return pollUrl;
    }

    public void refreshPlayRequest() {
        next = null;
        loadPlayRequest(true, false);
    }

    public String getCurrentRoomRheoCoin() {
        return currentRoomRheoCoin;
    }

    public void setCurrentRoomRheoCoin(String currentRoomRheoCoin) {
        this.currentRoomRheoCoin = currentRoomRheoCoin;
    }

    public void setLoading(boolean isLoading) {
        showLoading.setValue(isLoading);
    }

    public boolean getLoading() {
        return showLoading.getValue() != null && showLoading.getValue();
    }

    public String pollUrl = null;

    public void loadPlayRequest(boolean showRefresh, boolean updateWaitingNumber) {
        if (showRefresh) setLoading(true);
        System.out.println("current time -----> " + System.currentTimeMillis());
        getDataManager().getRequestPlayData(postId, updateWaitingNumber ? null : next).enqueue(new Callback<RequestPlayResponse>() {
            @Override
            public void onResponse(@NotNull Call<RequestPlayResponse> call, @NotNull Response<RequestPlayResponse> response) {
                try {
                    if (response.body() != null && getNavigator() != null) {
                        if (!updateWaitingNumber) {
                            if (next != null && response.body().getNext() == null) {
                                pollUrl = next;
                            }
                            next = response.body().getNext();
                            customRoomWinnerUsername = response.body().getCustomRoomWinnerUsername();
                            getNavigator().addPlayers(response.body());
                        } else {
                            getNavigator().updateWaitingNumber(response.body().getWaitingNumber());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setLoading(false);
                }
            }

            @Override
            public void onFailure(@NotNull Call<RequestPlayResponse> call, @NotNull Throwable t) {
                setLoading(false);
            }
        });
    }

    public void fetchPendingPlayRequest() {
        if (pollUrl == null) return;
        getDataManager().getRequestPlayData(postId, pollUrl).enqueue(new Callback<RequestPlayResponse>() {
            @Override
            public void onResponse(@NotNull Call<RequestPlayResponse> call, @NotNull Response<RequestPlayResponse> response) {
                if (getNavigator() != null && response.isSuccessful() && response.body() != null) {
                    next = response.body().getNext();
                    getNavigator().addPlayers(response.body());
                }
            }

            @Override
            public void onFailure(@NotNull Call<RequestPlayResponse> call, @NotNull Throwable throwable) {

            }
        });
    }

    public void requestToPlay(String gameUserName) {
        Log.i(getClass().getSimpleName(), "requestToPlay: " + gameUserName + " and " + postId);
        getDataManager().requestPlay(postId, gameUserName.trim()).enqueue(new Callback<ResultsItem>() {
            @Override
            public void onResponse(@NotNull Call<ResultsItem> call, @NotNull Response<ResultsItem> response) {
                try {
                    if (response.isSuccessful())
                        loadPlayRequest(false, false);
                    else if (getNavigator() != null)
                        getNavigator().handleErrorResponse();

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getNavigator() != null)
                        getNavigator().handleErrorResponse();
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResultsItem> call, @NotNull Throwable t) {
                if (getNavigator() != null)
                    getNavigator().handleErrorResponse();
            }
        });
    }

    public void setCustomRoomWinnerUsername(String customRoomWinnerUsername) {
        this.customRoomWinnerUsername = customRoomWinnerUsername;
    }

    public void requestAction(String requestId, String action) {
        setLoading(true);
        Log.i(getClass().getSimpleName(), "requestAction: " + requestId + " and " + action);
        getDataManager().requestPlayAction(requestId, action).enqueue(new Callback<ResultsItem>() {
            @Override
            public void onResponse(@NotNull Call<ResultsItem> call, @NotNull Response<ResultsItem> response) {
                try {
//                    Log.i(getClass().getName(), "requestAction " + new Gson().toJson(response));
                    if (getNavigator() != null) {
                        if (response.body() != null) {
                            getNavigator().handleActionSuccessResponse(requestId, action);
//                        loadPlayRequest(false, false);
                        } else {
                            getNavigator().handleErrorResponse();
                        }
                    }
                    setLoading(false);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getNavigator() != null)
                        getNavigator().handleErrorResponse();
                    setLoading(true);
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResultsItem> call, @NotNull Throwable t) {
                setLoading(false);
                if (getNavigator() != null)
                    getNavigator().handleErrorResponse();
            }
        });
    }

    public void submitCustomRoomDetails(String id, String roomId, String roomPass, boolean isEdit) {
        getDataManager().submitCustomRoomDetails(id, roomId, roomPass, isEdit).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (getNavigator() != null) {
                    if (response.body() != null) {
                        getNavigator().handleSubmitCustomRoomDetailsSuccess(roomId, roomPass);
                    } else if (response.errorBody() != null) {
                        getNavigator().handleSubmitCustomRoomDetailsError(response.errorBody().toString());
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                if (getNavigator() != null) {
                    getNavigator().handleSubmitCustomRoomDetailsError(t.getLocalizedMessage());
                }
            }
        });
    }

    public void submitCustomRoomWinner(String requestId) {
        setLoading(true);
        getDataManager().setCustomRoomWinner(requestId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    loadPlayRequest(false, false);
                }
                setLoading(false);
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, Throwable t) {
                setLoading(false);
            }
        });
    }
}
