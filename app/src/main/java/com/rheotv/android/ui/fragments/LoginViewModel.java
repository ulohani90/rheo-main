package com.rheotv.android.ui.fragments;

import android.net.Uri;
import android.util.Log;

import com.moe.pushlibrary.MoEHelper;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.login.LoginUserRequest;
import com.rheotv.android.data.network.models.login.LoginUserResponse;
import com.rheotv.android.data.network.models.login.UserNameResult;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class LoginViewModel extends BaseViewModel<LoginNavigator> {

    public SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    protected String name;
    public String originalUserName;

    protected String photoUrl;
    public HashMap<String, Object> baseProperties = new HashMap<>();

    public LoginViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void sendAuthenticatedUserToServer(String name, String phone, String email, Uri photoUrl, String uid) {
        if (photoUrl != null)
            this.photoUrl = photoUrl.toString();
        else
            this.photoUrl = "";
        this.name = name;
        LoginUserRequest user = new LoginUserRequest(name, phone, email, photoUrl, uid);

        getDataManager().authorizeLogin(user).enqueue(new Callback<LoginUserResponse>() {
            @Override
            public void onResponse(@NotNull Call<LoginUserResponse> call, @NotNull Response<LoginUserResponse> response) {
                if (response.isSuccessful()) {
                    AnalyticsHelper.getInstance(getNonUiContext()).sendSuccess("login");

                    SegmentTracker.getInstance(getNonUiContext()).setIdentityUsername(email, name);
                    MoEHelper.getInstance(getNonUiContext()).setAlias(email);
                    if (response.body() != null) {
                        sharedPrefsUtils.setBooleanPreference(getNonUiContext(), SharedPrefsUtils.IS_LOGGED_IN, true);
                        sharedPrefsUtils.setStringPreference(getNonUiContext(), SharedPrefsUtils.USER_NAME, response.body().getUserName());
                        sharedPrefsUtils.setIntegerPreference(getNonUiContext(), SharedPrefsUtils.USER_ID, response.body().getUserId());
                        sharedPrefsUtils.setStringPreference(getNonUiContext(), SharedPrefsUtils.AUTH_TOKEN, response.body().getAccessToken());
                        CommonUtils.setHideSyncContacts(true);
                        CommonUtils.setProfileImageUrl(response.body().getProfileUrl());
                        CommonUtils.setAuthorId(response.body().getAuthorId());
                        CommonUtils.setIsUSerStreamer(response.body().getIsStreamer());
                        CommonUtils.setIsUserContentModerator(response.body().isContentModerator());
                        CommonUtils.setBranchExtraInfo(getNonUiContext(), response.body().getExtraInfo());
                        CommonUtils.setUserLanguage(getNonUiContext(), response.body().getLanguages());
                        CommonUtils.setNewUser(response.body().getIsNew());
                        if (getNavigator() != null) {
                            if (response.body().getIsNew()) {
                                originalUserName = response.body().getUserName();
                                getNavigator().askUsername("", LoginViewModel.this.name, LoginViewModel.this.photoUrl.toString());
                            } else {
                                SegmentTracker.getInstance(getNonUiContext()).setIdentityUsername(response.body().getUserName());
                                getNavigator().handleBackendLoginResponse(true);

                            }
                        }
                    }
                } else {
                    Log.d(RheoTvApp.TAG, "something went from server");

                }
            }

            @Override
            public void onFailure(@NotNull Call<LoginUserResponse> call, @NotNull Throwable t) {
//                getNavigator().setLoginSuccessful(false);
            }
        });

    }

    public void checkUsernameAndSignup(String username) {
        HashMap<String, Object> userProperties = new HashMap<>(baseProperties);
        getDataManager().checkUsernameAndSave(username).enqueue(new Callback<UserNameResult>() {
            @Override
            public void onResponse(@NotNull Call<UserNameResult> call, @NotNull Response<UserNameResult> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().isUserCreated()) {
                            SegmentTracker.getInstance(getNonUiContext()).setIdentityUsername(response.body().getUserName());
                            sharedPrefsUtils.setStringPreference(getNonUiContext(), SharedPrefsUtils.USER_NAME, response.body().getUserName());
                            userProperties.put("userName", username);
                            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_LOGIN_USERNAME_ACCEPTED, userProperties);
                            if (getNavigator() != null)
                                getNavigator().handleBackendLoginResponse(true);

                        } else {
                            String message = response.body().getUserName() + " is not available. Please try something else.";
                            if (getNavigator() != null)
                                getNavigator().askUsername(message, name, photoUrl);
                            userProperties.put("userName", username);
                            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_LOGIN_USERNAME_EXIST, userProperties);
                        }
                    }
                } else {
                    String message = "Username is not available. Please try something else.";
                    if (getNavigator() != null)
                        getNavigator().handleFailure(message);
                }
            }

            @Override
            public void onFailure(@NotNull Call<UserNameResult> call, @NotNull Throwable t) {
                t.printStackTrace();
//                getNavigator().setLoginSuccessful(false);
            }
        });
    }

    public void loanReward() {
        getCompositeDisposable().add(getDataManager().loadAvailableScratchCards()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dailyRewardsResponse -> {
                    RewardManager.getInstance().updateData(dailyRewardsResponse);
                    if (getNavigator() != null) {
                        getNavigator().handleLoginSuccess();
                    }
                }, throwable -> {
                    if (getNavigator() != null) {
                        getNavigator().handleLoginSuccess();
                    }
                }));
    }
}
