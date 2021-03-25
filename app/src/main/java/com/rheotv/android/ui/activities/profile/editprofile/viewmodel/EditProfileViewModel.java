package com.rheotv.android.ui.activities.profile.editprofile.viewmodel;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.login.UserNameResult;
import com.rheotv.android.data.network.models.onboarding.LanguageObject;
import com.rheotv.android.data.network.models.postlisting.responses.User;
import com.rheotv.android.data.network.models.useProfile.responses.BioResponse;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class EditProfileViewModel extends BaseViewModel {
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    public ObservableField<User> user = new ObservableField<>(new User("", "", ""));

    public ObservableField<String> avatar = new ObservableField<>();
    public ObservableField<String> backdrop = new ObservableField<>();
    public ObservableField<String> userName = new ObservableField<>();
    public ObservableField<String> bio = new ObservableField<>();
    public MutableLiveData<ArrayList<LanguageObject>> languages = new MutableLiveData<>();
    public HashMap<String, String> languageMap = new HashMap<>();

    public ObservableField<Boolean> coverPicLoader = new ObservableField<>();
    public ObservableField<Boolean> profilePicLoader = new ObservableField<>();
    public ObservableField<Status> saving = new ObservableField<>();

    private String initialUserName;

    public EditProfileViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void setFields(Bundle bundle) {
        user.set(new User(bundle.getString(AppConstants.ARG_FIRST_NAME),
                bundle.getString(AppConstants.ARG_LAST_NAME),
                bundle.getString(AppConstants.ARG_DESCRIPTION)));

        avatar.set(bundle.getString(AppConstants.ARG_PROFILE_AVATAR));
        backdrop.set(bundle.getString(AppConstants.ARG_PROFILE_COVER_PIC));
        userName.set(bundle.getString(AppConstants.ARG_USERNAME));
        bio.set(bundle.getString(AppConstants.ARG_BIO));
        languages.setValue(bundle.getParcelableArrayList(AppConstants.ARG_LANGUAGE));
        initialUserName = bundle.getString(AppConstants.ARG_USERNAME);
    }

    public String getFirstName() {
        return Objects.requireNonNull(user.get()).getFirstName().trim();
    }

    public String getLastName() {
        return Objects.requireNonNull(user.get()).getLastName().trim();
    }

    public String getUserNameValue() {
        if (userName != null) {
            return Objects.requireNonNull(userName.get()).trim();
        }
        return null;
    }

    public String getDescription() {
        return Objects.requireNonNull(user.get()).getDescription();
    }

    public String getBioValue() {
        if (bio != null)
            return bio.get();
        return null;
    }

    public void uploadImage(MultipartBody.Part part, String type) {
        handleLoaderState(type, true);
        getDataManager().uploadImage(part, type).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                boolean isSuccessFul = response.isSuccessful();
                Log.d(getClass().getSimpleName(), "onResponse isSuccessful : " + isSuccessFul);
                handleLoaderState(type, false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(RheoTvApp.TAG, "on failure ");
                handleLoaderState(type, false);
                t.printStackTrace();
            }
        });
    }

    public void saveAuthorProfile() {
        saving.set(Status.LOADING);
        getDataManager().uploadUserInfo(user.get()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isUsernameEdited()) {
                    checkUsernameAndSignup(userName.get());
                } else {
                    updateLanguage(getSelectedLanguage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showToast("Error updating profile.");
                saving.set(Status.ERROR);
            }
        });
    }

    private void updateLanguage(List<String> languageId) {
        getDataManager().setUserLanguage(languageId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setBio();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showToast("Error updating profile.");
                saving.set(Status.ERROR);
            }
        });
    }

    public void setBio() {
        getDataManager().setUserBio(bio.get()).enqueue(new Callback<BioResponse>() {
            @Override
            public void onResponse(Call<BioResponse> call, Response<BioResponse> response) {
                if (response.body() != null) {
                    showToast("Profile updated successfully.");
                    saving.set(Status.SUCCESS);
                }
            }

            @Override
            public void onFailure(Call<BioResponse> call, Throwable t) {
                Log.d(getClass().getSimpleName(), "fetching profile failed. Probably not loggedIn");
                showToast("Error updating profile.");
                saving.set(Status.ERROR);
            }
        });
    }

    private void checkUsernameAndSignup(String username) {
        getDataManager().checkUsernameAndSave(username).enqueue(new Callback<UserNameResult>() {
            @Override
            public void onResponse(Call<UserNameResult> call, Response<UserNameResult> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().isUserCreated()) {
                            sharedPrefsUtils.setStringPreference(
                                    getNonUiContext(),
                                    SharedPrefsUtils.USER_NAME,
                                    response.body().getUserName());
                            setBio();
                        } else {
                            String message = response.body().getUserName() + " is not available. Please try something else.";
                            showToast(message);
                            saving.set(Status.ERROR);
                        }
                    }
                } else {
                    String message = "Username is not available. Please try something else.";
                    showToast(message);
                    saving.set(Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<UserNameResult> call, Throwable t) {
                Log.d("test", "test");
            }
        });
    }

    private boolean isUsernameEdited() {
        return initialUserName.equals(userName.get() != null ? userName.get().trim() : "");
    }

    private ArrayList<String> getSelectedLanguage() {
        return new ArrayList<>(languageMap.keySet());
    }

    private void showToast(String message) {
        Toast.makeText(getNonUiContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void handleLoaderState(String type, boolean state) {
        if (type != null && type.contentEquals("edit_cover")) {
            coverPicLoader.set(state);
        } else {
            profilePicLoader.set(state);
        }
    }
}
