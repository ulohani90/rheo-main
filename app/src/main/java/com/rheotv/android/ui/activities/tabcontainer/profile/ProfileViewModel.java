package com.rheotv.android.ui.activities.tabcontainer.profile;


import android.content.Context;
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
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AnalyticsConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.branch.referral.Branch;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends BaseViewModel<ProfileNavigator> {

    public final ObservableField<ProfileResult> authorProfileData = new ObservableField<>();
    public final ObservableList<Result> blogObservableArrayList = new ObservableArrayList<>();
    private final MutableLiveData<ProfileResult> mAuthorProfileData;
    private final MutableLiveData<List<Result>> blogListLiveData;
    public ObservableBoolean isFollowed = new ObservableBoolean();
    private List<Result> postList = new ArrayList<>();
    private JsonParseHelper jsonParseHelper = new JsonParseHelper();
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    public final ObservableBoolean isSetUpForSelf = new ObservableBoolean(false);

    public ProfileViewModel(DataManager dataManager,
                            SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mAuthorProfileData = new MutableLiveData<>();
        blogListLiveData = new MutableLiveData<>();
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
        map.put("userId", authorProfileData.get().getUser().getId());
        map.put("followAction", !isFollowed.get());
        map.put("source", SegmentConstants.SCREEN_NAME_SELF_PROFILE_DETAILS);
        SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
        if (authorProfileData.get() != null && authorProfileData.get().getUser() != null && authorProfileData.get().getUser().getId() != null) {
            if (isFollowed.get()) {
                getDataManager().unFollowAuthor(String.valueOf(authorProfileData.get().getUser().getId())).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                            isFollowed.set(false);
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
        setUpAccordingToUserType(authorUserName);
        setIsLoading(true);
        getDataManager().getProfile(authorUserName).enqueue(new Callback<ProfileResult>() {
            @Override
            public void onResponse(Call<ProfileResult> call, Response<ProfileResult> response) {
                if (response.body() != null && getNavigator() != null && getNavigator().getContextInstance() != null) {
                    getNavigator().showLoader(false);
                    CommonUtils.setPaymentModel(response.body().getPaymentModel());
                    CommonUtils.setLevelType(response.body().getLevelType());
                    if (authorUserName.equalsIgnoreCase("me") || authorUserName.equalsIgnoreCase(CommonUtils.getUserName(getNavigator().getContextInstance()))) {
                        //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        //String username = sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_NAME);
                        if (CommonUtils.isUserLoggedin()) {
                            mAuthorProfileData.setValue(response.body());
                            postList.clear();
                            //isFollowed.set(response.body().getFollowed());
                            sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.AUTHOR_TOP_POSTS, "");
                            if (response.body().getTopVideos() != null) {
                                postList.addAll(response.body().getTopVideos());
                                blogListLiveData.setValue(postList);
                                addBlogItemsToList(postList);
                                jsonParseHelper.saveAuthorResponseJson(response.body());
                            }
                            if (getNavigator() != null)
                                getNavigator().setupViewsForLoggedinUser();
                        } else {
                            if (getNavigator() != null)
                                getNavigator().setupViewsForNonLoggedinUser();
                        }
                        if (getNavigator() != null)
                            getNavigator().updateUI(response.body());
                    } else {
                        mAuthorProfileData.setValue(response.body());
                        postList.clear();
                        isFollowed.set(response.body().getFollowed());
                        if (getNavigator() != null)
                            getNavigator().showLoader(false);
                        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.AUTHOR_TOP_POSTS, "");
                        if (response.body().getTopVideos() != null) {
                            postList.addAll(response.body().getTopVideos());
                            blogListLiveData.setValue(postList);
                            addBlogItemsToList(postList);
                            jsonParseHelper.saveAuthorResponseJson(response.body());
                        }

                    }

                }
            }

            @Override
            public void onFailure(Call<ProfileResult> call, Throwable t) {
                Log.d("mirage", "fetching profile failed. Probably not loggedIn");

            }
        });
    }

    private void setUpAccordingToUserType(String authorUserName) {
        if (authorUserName.equalsIgnoreCase("me")) {
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

    public void setAuthorProfile(ProfileResult result, String authorUserName) {
        setUpAccordingToUserType(authorUserName);
        if (authorUserName.equalsIgnoreCase("me") || authorUserName.equalsIgnoreCase(CommonUtils.getUserName(getNavigator().getContextInstance()))) {
            if (CommonUtils.isUserLoggedin()) {
                mAuthorProfileData.setValue(result);
                authorProfileData.set(result);
                postList.clear();
                //isFollowed.set(response.body().getFollowed());
                sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.AUTHOR_TOP_POSTS, "");
                if (result.getTopVideos() != null) {
                    postList.addAll(result.getTopVideos());
                    blogListLiveData.setValue(postList);
                    addBlogItemsToList(postList);
                    jsonParseHelper.saveAuthorResponseJson(result);
                }
                if (getNavigator() != null)
                    getNavigator().setupViewsForLoggedinUser();
            } else {
                if (getNavigator() != null)
                    getNavigator().setupViewsForNonLoggedinUser();
            }
            if (getNavigator() != null)
                getNavigator().updateUI(result);
        } else {
            mAuthorProfileData.setValue(result);
            authorProfileData.set(result);
            postList.clear();
            isFollowed.set(result.getFollowed());
            if (getNavigator() != null)
                getNavigator().showLoader(false);
            sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.AUTHOR_TOP_POSTS, "");
            if (result.getTopVideos() != null) {
                postList.addAll(result.getTopVideos());
                blogListLiveData.setValue(postList);
                addBlogItemsToList(postList);
                jsonParseHelper.saveAuthorResponseJson(result);
            }
        }
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

    public void onShareClicked(View view) {
        //share profile
        AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext()).sendClick("journalist_share");
        String text = "Watch " + authorProfileData.get().getUser().getUserFullName() + " streaming live on Rheo TV!\n" + authorProfileData.get().getShareUrl();
        new ShareTaskHelper().share(view.getContext(), text, ShareTaskHelper.ShareTarget.Others);
    }

    public void editProfileButton() {
        //show edit profile layout and hide others
    }

    public void logOutClicked() {
        Branch.getInstance().logout();
        FirebaseAuth.getInstance().signOut();
        sharedPrefsUtils.setBooleanPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.IS_LOGGED_IN, false);
        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_NAME, null);
        setUpAccordingToUserType("me");
        getNavigator().navigateToHome();
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
}

