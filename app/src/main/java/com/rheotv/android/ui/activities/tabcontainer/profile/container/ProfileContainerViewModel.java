package com.rheotv.android.ui.activities.tabcontainer.profile.container;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.login.UserNameResult;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.helpers.JsonParseHelper;
import com.rheotv.android.helpers.ShareTaskHelper;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.activities.tabcontainer.profile.ProfileNavigator;
import com.rheotv.android.ui.activities.tabcontainer.profile.wallet.WalletViewState;
import com.rheotv.android.ui.adapters.LevelType;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AnalyticsConstants;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.ServerFileDownloader;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileContainerViewModel extends BaseViewModel<ProfileNavigator> {

    public final ObservableField<ProfileResult> authorProfileData = new ObservableField<>();
    public final ObservableList<Result> blogObservableArrayList = new ObservableArrayList<>();
    private final MutableLiveData<ProfileResult> mAuthorProfileData;
    private final MutableLiveData<List<Result>> blogListLiveData;
    public ObservableBoolean isFollowed = new ObservableBoolean();
    public ObservableField<String> followButton = new ObservableField<>();
    private List<Result> postList = new ArrayList<>();
    private JsonParseHelper jsonParseHelper = new JsonParseHelper();
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    public final ObservableBoolean isSetUpForSelf = new ObservableBoolean(false);
    private ObservableBoolean isBadgeVisible = new ObservableBoolean(CommonUtils.isUserLoggedin());

    public ObservableField<Boolean> isBronze = new ObservableField<>(false);
    public ObservableField<Boolean> isSilver = new ObservableField<>(false);
    public ObservableField<Boolean> isGold = new ObservableField<>(false);
    public ObservableField<String> redeemAmount = new ObservableField<>("0");
    public ObservableField<String> redeemDate = new ObservableField<>("");
    public int selectedTab = 0;
    private boolean firstApiCalled = false;

    public boolean isFirstApiCalled() {
        return firstApiCalled;
    }

    private String authorName = "me";
    public ObservableField<Status> profileStatus = new ObservableField<>();

    public MutableLiveData<WalletViewState> walletViewStateMutableLiveData = new MutableLiveData<>();

    public ProfileContainerViewModel(DataManager dataManager,
                                     SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mAuthorProfileData = new MutableLiveData<>();
        blogListLiveData = new MutableLiveData<>();
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setUpForSelf() {
        if (CommonUtils.isUserLoggedin()) {
            isSetUpForSelf.set(true);
            getNavigator().setupViewsForLoggedinUser();
        } else {
            isSetUpForSelf.set(false);
            getNavigator().setupViewsForNonLoggedinUser();
        }
    }

    public String getLabel1Data() {
        if (authorProfileData.get() != null) {
            return authorProfileData.get().getProgressData().getLabel1() + authorProfileData.get().getProgressData().getLabel1();
        }
        return "";
    }

    public void onEditUsernameClicked() {
        getNavigator().editUserName();
    }

    public void onMoreInfoPartnerClicked() {
        if (authorProfileData.get().getButtonData() != null) {
            getNavigator().openPartnerFlow(authorProfileData.get().getButtonData());
        }
    }

    public void toggleFollow() {
        if (!CommonUtils.isUserLoggedin()) {
            getNavigator().handleLogin();
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("is_first", CommonUtils.isFirstTimeFollow());
        map.put("author", authorProfileData.get().getUser().getUsername());
        map.put("source", SegmentConstants.SCREEN_NAME_PROFILE_SELF);
        map.put("userId", authorProfileData.get().getUser().getId());
        map.put("followAction", !isFollowed.get());
        map.put("from", "userProfile");
        SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
        CommonUtils.setFirstTimeFollow();
        if (authorProfileData.get() != null && authorProfileData.get().getUser() != null && authorProfileData.get().getUser().getId() != null) {
            if (isFollowed.get()) {
                getDataManager().unFollowAuthor(String.valueOf(authorProfileData.get().getUser().getId())).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                            isFollowed.set(false);
                            followButton.set("Follow");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
            } else {
                getDataManager().followAuthor(String.valueOf(authorProfileData.get().getUser().getId())).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                            isFollowed.set(true);
                            followButton.set("Following");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
            }
        } else {
            return;
        }

    }

    void checkUsernameAndSignup(String username) {
        getDataManager().checkUsernameAndSave(username).enqueue(new Callback<UserNameResult>() {
            @Override
            public void onResponse(Call<UserNameResult> call, Response<UserNameResult> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().isUserCreated()) {
                            sharedPrefsUtils.setStringPreference(
                                    RheoTvApp.getNonUiContext(),
                                    SharedPrefsUtils.USER_NAME,
                                    response.body().getUserName());
                            getNavigator().updateUserName(response.body().getUserName());
                        } else {
                            getNavigator().updateUserName(authorProfileData.get().getUser().getUsername());
                            String message = response.body().getUserName() + " is not available. Please try something else.";
                            getNavigator().showToast(message);
                        }
                    }
                } else {
                    getNavigator().updateUserName(authorProfileData.get().getUser().getUsername());
                    String message = "Username is not available. Please try something else.";
                    getNavigator().showToast(message);
                }
            }

            @Override
            public void onFailure(Call<UserNameResult> call, Throwable t) {
                Log.d("test", "test");
            }
        });
    }

    public void fetchProfile(String authorUserName) {
        firstApiCalled = true;
        setUpAccordingToUserType(authorUserName);
        setIsLoading(true);
        profileStatus.set(Status.LOADING);
        setAuthorName(authorUserName);
        getDataManager().getProfile(authorUserName).enqueue(new Callback<ProfileResult>() {
            @Override
            public void onResponse(Call<ProfileResult> call, Response<ProfileResult> response) {
                if (response.body() != null && getNavigator() != null) {
                    profileStatus.set(Status.SUCCESS);
                    getNavigator().showLoader(false);
                    getNavigator().setBio(response.body());

                    CommonUtils.setPaymentModel(response.body().getPaymentModel());
                    CommonUtils.setLevelType(response.body().getLevelType());
                    if (authorUserName.equalsIgnoreCase("me")) {
                        setIsBadgeVisible(CommonUtils.isUserLoggedin());
                        //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        //String username = sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_NAME);
                        if (CommonUtils.isUserLoggedin()) {
                            mAuthorProfileData.setValue(response.body());
                            setIsBadgeVisible(CommonUtils.isUserLoggedin());
                            if (getNavigator() != null)
                                getNavigator().updateProfileViewModelData();
                            postList.clear();
                            //isFollowed.set(response.body().getFollowed());
                            sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.AUTHOR_TOP_POSTS, "");
                            if (response.body().getTopVideos() != null) {
                                postList.addAll(response.body().getTopVideos());
                                blogListLiveData.setValue(postList);
                                addBlogItemsToList(postList);
                                jsonParseHelper.saveAuthorResponseJson(response.body());
                            }
                            if (getNavigator() != null) {
                                getNavigator().setupViewsForLoggedinUser();
                                getNavigator().setUpRheoProgressView();
                            }
                        } else {
                            setIsBadgeVisible(CommonUtils.isUserLoggedin());
                            if (getNavigator() != null) {
                                getNavigator().setupViewsForNonLoggedinUser();
                            }
                        }
                        if (getNavigator() != null)
                            getNavigator().updateUI(response.body());

                    } else {
                        mAuthorProfileData.setValue(response.body());
                        setIsBadgeVisible(true);
                        if (getNavigator() != null)
                            getNavigator().updateProfileViewModelData();
                        postList.clear();

                        isFollowed.set(response.body().getFollowed());
                        if (isFollowed.get()) {
                            followButton.set("Following");
                        } else {
                            followButton.set("Follow");
                        }
                        if (getNavigator() != null) {
                            getNavigator().showLoader(false);
                        }
                        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.AUTHOR_TOP_POSTS, "");
                        if (response.body().getTopVideos() != null) {
                            postList.addAll(response.body().getTopVideos());
                            blogListLiveData.setValue(postList);
                            addBlogItemsToList(postList);
                            jsonParseHelper.saveAuthorResponseJson(response.body());
                        }

                    }
                    if (getNavigator() != null) {
                        getNavigator().setUpTabs();
                        getNavigator().setUpFloatinActionButton();
                    }

                } else {
                    profileStatus.set(Status.ERROR);
                }

                setIsLoading(false);
            }

            @Override
            public void onFailure(Call<ProfileResult> call, Throwable t) {
                t.printStackTrace();
                Log.d("mirage", "fetching profile failed. Probably not loggedIn " + t.getMessage());
                if (NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext()))
                    profileStatus.set(Status.ERROR);
                else
                    profileStatus.set(Status.OFFLINE);
                setIsLoading(false);
            }
        });
    }

    public void setUserFollowStatus(boolean serverFollowStatus) {
        if (serverFollowStatus) {
            isFollowed.set(true);
        } else {

        }
    }

    private void setUpAccordingToUserType(String authorUserName) {
        setAuthorName(authorUserName);
        if (authorUserName.equalsIgnoreCase("me") || (authorUserName.equalsIgnoreCase(CommonUtils.getUserName(getNavigator().getContextInstance())))) {
            setUpForSelf();
        } else {
            //do nothing
            setUpForAuthor();
        }
    }

    private void setUpForAuthor() {
        getNavigator().setUpLayoutForAuthor();
    }

    public void updateProfileData(ProfileResult data) {
        authorProfileData.set(data);
    }

    public MutableLiveData<ProfileResult> getProfileData() {
        return mAuthorProfileData;
    }

    public void setProfileData(ProfileResult result) {
        mAuthorProfileData.setValue(result);
    }

    public void addBlogItemsToList(List<Result> blogs) {
        blogObservableArrayList.clear();
        blogObservableArrayList.addAll(blogs);
    }

    public void updateSingleItemInList(Result post) {
        int position = blogObservableArrayList.indexOf(post);

        if (post.getIsLiked()) {
            post.setTotalLikes(Integer.valueOf(post.getTotalLikes()) - 1);
        } else {
            post.setTotalLikes(Integer.valueOf(post.getTotalLikes()) + 1);
        }
        post.setIsLiked(!post.getIsLiked());

        blogObservableArrayList.set(position, post);
        blogListLiveData.getValue().set(position, post);
    }

    public void onLikeItemClicked(String body, Result post) {

        updateSingleItemInList(post);
        getDataManager()
                .postLikeToggle(body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        //do nothing
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        //do nothing
                    }
                });

        //analytics call for click via home screen
        AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext())
                .sendLike(post.getAuthor().getUser().getUsername()
                        , String.valueOf(post.getAuthor().getUser().getId())
                        , post.getId()
                        , post.getTitle()
                        , AnalyticsConstants.SOURCE_AUTHOR_SCREEN);

    }

    public void onShareSelfClicked(View view) {
        if (authorProfileData.get() != null) {
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("username", authorProfileData.get().getUser().getUsername());
            SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_SELF_PROFILE_SHARE_CLICK, properties);

            HashMap<String, String> map = new HashMap<>();
            map.put(AppConstants.BRANCH_PROFILE_URL_SHARE, authorProfileData.get().getShareUrl());
            map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_PROFILE);
            FirebaseDynamicLinkUtils.share(getNavigator().getContextInstance(), authorProfileData.get().getCampaignInfo(), "self_profile_share", "Join me on Rheo TV", "Hey Mate!!!! I have started streaming live on Rheo TV. Check this out and do not forget to follow me.", authorProfileData.get().getProfilePic(), map, authorProfileData.get().getShareUrl(), true);


            AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext()).sendClick("journalist_share");
            //String text = "Hey Mate!!!! I have started streaming live on Rheo TV. Check this out and do not forget to follow me.\n" + authorProfileData.get().getShareUrl();
            //new ShareTaskHelper().share(view.getContext(), text, ShareTaskHelper.ShareTarget.Others);
        }
    }

    public void onShareClicked(View view) {
        if (authorProfileData.get() != null) {
            //share profile
            AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext()).sendClick("journalist_share");
            String text = "Watch " + ((authorProfileData != null && authorProfileData.get() != null && authorProfileData.get().getUser() != null) ? authorProfileData.get().getUser().getUserFullName() : "gamer") + " streaming live on Rheo TV!\n" + authorProfileData.get().getShareUrl();
            new ShareTaskHelper().share(view.getContext(), text, ShareTaskHelper.ShareTarget.Others);
        }
    }

    public void editProfileButton() {
        //show edit profile layout and hide others
        getNavigator().startEditProfileActivity();
    }

    public void uploadVideoButton() {
        //show edit profile layout and hide others
        getNavigator().startUploadActivity();
    }

    public void logOutClicked() {
        FirebaseAuth.getInstance().signOut();

        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_NAME, null);
        setUpAccordingToUserType("me");
    }

    public void onShareItemClicked(String body, Result post, Context context) {
        getDataManager()
                .postShare(body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        //do nothing
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        //do nothing
                    }
                });

        //analytics for click via the home screen
        AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext())
                .sendPostShareClick(post.getAuthor().getUser().getUsername()
                        , String.valueOf(post.getAuthor().getUser().getId())
                        , post.getId()
                        , post.getTitle()
                        , AnalyticsConstants.SOURCE_AUTHOR_SCREEN);

        //call the sharing method
        ShareTaskHelper.getNewInstance(context).downloadAndSharePostOnWhatsApp(post);
    }

    public void pickImageFromGallery(String type) {
        if (authorProfileData.get() != null && authorProfileData.get().getUser() != null && authorProfileData.get().getUser().getId() != null) {
            if (CommonUtils.getUserName(getNavigator().getContextInstance()).equalsIgnoreCase(authorProfileData.get().getUser().getUsername())) {
                getNavigator().openGallery(type);
            }
        }
    }

    public void initiateLogin() {
        getNavigator().handleLogin();
    }

    public void saveAuthorProfile() {
        getDataManager().uploadUserInfo(getNavigator().getNewUserObjectFromView()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //handle
                getNavigator().setupViewsForLoggedinUser();
                fetchProfile("me");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public MutableLiveData<List<Result>> getBlogListLiveData() {
        return blogListLiveData;
    }

    public ObservableList<Result> getBlogObservableList() {
        return blogObservableArrayList;
    }

    public void onMedalClick() {
        if (getNavigator() != null)
            getNavigator().onMedalViewClick();
    }

    public ObservableBoolean getIsBadgeVisible() {
        return isBadgeVisible;
    }

    public void setIsBadgeVisible(boolean flag) {
        this.isBadgeVisible.set((authorName == null || !authorName.equalsIgnoreCase("me") || authorProfileData.get() == null || authorProfileData.get().getPaymentModel() == 1) && flag);
    }

    private WalletViewState mLastWalletViewState;

    public WalletViewState getLastWalletViewState() {
        return mLastWalletViewState;
    }

    public void setLastWalletViewState(WalletViewState mLastWalletViewState) {
        this.mLastWalletViewState = mLastWalletViewState;
    }

    public void setWalletViewState(WalletViewState walletViewState) {
        walletViewStateMutableLiveData.setValue(walletViewState);
    }

    public void setBronzePaymentBadge() {
        isBronze.set(true);
        isSilver.set(false);
        isGold.set(false);
    }

    public void setSilverPaymentBadge() {
        isBronze.set(true);
        isSilver.set(true);
        isGold.set(false);
    }

    public void setGoldPaymentBadge() {
        isBronze.set(true);
        isSilver.set(true);
        isGold.set(true);
    }

    public void setNoLevelAssigned() {
        isBronze.set(false);
        isSilver.set(false);
        isGold.set(false);
    }

    public void uploadImage(MultipartBody.Part part, String type) {
        getDataManager().uploadImage(part, type).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                boolean isSuccessFul = response.isSuccessful();
                Log.d(RheoTvApp.TAG, "onResponse isSuccessful : " + isSuccessFul);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(RheoTvApp.TAG, "on failure ");
                t.printStackTrace();
            }
        });
    }

    public void requestPayout() {
        getDataManager().requestPayout().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (getNavigator() != null) {
                    getNavigator().hideProgressBar();
                    getNavigator().showToast("Request for Payout has been successfully sent. Our support team with get in touch with you soon.");
                    fetchProfile(getNavigator().getAuthorUsername());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (getNavigator() != null) {
                    getNavigator().showToast("Some problem occurred. Please try again.");
                }
            }
        });
    }

    public void updateWalletWithResult(ProfileResult profileResult, String authorUserName) {
        setUpAccordingToUserType(authorUserName);
        getNavigator().setBio(profileResult);
        setAuthorName(authorUserName);

        if (authorUserName.equalsIgnoreCase("me")) {
            setIsBadgeVisible(CommonUtils.isUserLoggedin());
            if (CommonUtils.isUserLoggedin()) {
                mAuthorProfileData.setValue(profileResult);
                if (getNavigator() != null)
                    getNavigator().updateProfileViewModelData();
                postList.clear();
                //isFollowed.set(response.body().getFollowed());
                sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.AUTHOR_TOP_POSTS, "");
                if (profileResult.getTopVideos() != null) {
                    postList.addAll(profileResult.getTopVideos());
                    blogListLiveData.setValue(postList);
                    addBlogItemsToList(postList);
                    jsonParseHelper.saveAuthorResponseJson(profileResult);
                }
                if (getNavigator() != null) {
                    getNavigator().setupViewsForLoggedinUser();
                    getNavigator().setUpRheoProgressView();
                }
            } else {
                if (getNavigator() != null) {
                    getNavigator().setupViewsForNonLoggedinUser();
                }
            }
            if (getNavigator() != null)
                getNavigator().updateUI(profileResult);

        } else {
            mAuthorProfileData.setValue(profileResult);
            setIsBadgeVisible(true);
            if (getNavigator() != null)
                getNavigator().updateProfileViewModelData();
            postList.clear();
            isFollowed.set(profileResult.getFollowed());
            if (isFollowed.get()) {
                followButton.set("Following");
            } else {
                followButton.set("Follow");
            }
            if (getNavigator() != null) {
                getNavigator().showLoader(false);
            }
            sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.AUTHOR_TOP_POSTS, "");
            if (profileResult.getTopVideos() != null) {
                postList.addAll(profileResult.getTopVideos());
                blogListLiveData.setValue(postList);
                addBlogItemsToList(postList);
                jsonParseHelper.saveAuthorResponseJson(profileResult);
            }
        }
    }

    public void downloadStatement(Context context) {
        setIsLoading(true);
        getCompositeDisposable()
                .add(getDataManager()
                        .downloadStatement()
                        .concatMap(response -> new ServerFileDownloader().downloadFile(response, new File(context.getFilesDir().getAbsolutePath() + "abc.pdf").getAbsolutePath()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response != null) {
                                        setIsLoading(false);
                                        Intent intent = new Intent(Intent.ACTION_SEND);
                                        intent.setType("application/pdf");
                                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(response.getAbsolutePath()));
                                        context.startActivity(intent);
                                    }
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                }));
    }

    void requestForContentModerator() {
        if (getNavigator() != null) getNavigator().showLoader(true);
        getDataManager().voteAsContentModerator(String.valueOf(authorProfileData.get().getUser().getId())).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (getNavigator() != null) {
                    getNavigator().showLoader(false);
                    if (response.isSuccessful()) {
                        getNavigator().onContentModeratorVoted();
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable throwable) {
                if (getNavigator() != null) {
                    getNavigator().showLoader(false);
                    getNavigator().handleError(throwable);
                }
            }
        });
    }

    public LevelType getLevel() {
        return mAuthorProfileData.getValue() != null ? mAuthorProfileData.getValue().getLevelType() : LevelType.Unassigned.INSTANCE;
    }
}

