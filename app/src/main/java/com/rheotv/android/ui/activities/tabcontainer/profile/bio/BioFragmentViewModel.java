package com.rheotv.android.ui.activities.tabcontainer.profile.bio;


import android.text.Editable;
import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.useProfile.responses.BioResponse;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BioFragmentViewModel extends BaseViewModel<BioFragmentNavigator> {

    public final ObservableField<ProfileResult> authorProfileData = new ObservableField<>();
    private final MutableLiveData<ProfileResult> mAuthorProfileData;

    public final ObservableBoolean isSetUpForSelf = new ObservableBoolean(false);


    public BioFragmentViewModel(DataManager dataManager,
                                SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mAuthorProfileData = new MutableLiveData<>();
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

    public void getBio(String authorUserName) {
        setUpAccordingToUserType(authorUserName);
        setIsLoading(true);
        getDataManager().getProfileBio(authorUserName).enqueue(new Callback<BioResponse>() {
            @Override
            public void onResponse(Call<BioResponse> call, Response<BioResponse> response) {
                if (response.body() != null && getNavigator() != null) {
                    getNavigator().showLoader(false);
                    getNavigator().setBio(response.body().getText());
                }
            }

            @Override
            public void onFailure(Call<BioResponse> call, Throwable t) {
                if (getNavigator() != null) {
                    Log.d("mirage", "fetching profile failed. Probably not loggedIn");
                    getNavigator().showLoader(false);
                }
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
//        getNavigator().setUpLayoutForAuthor();
    }

    public void updateProfileData(ProfileResult data) {
        authorProfileData.set(data);
    }

    public MutableLiveData<ProfileResult> getProfileData() {
        return mAuthorProfileData;
    }

    public void setBio(Editable text) {
        getDataManager().setUserBio(text.toString()).enqueue(new Callback<BioResponse>() {
            @Override
            public void onResponse(Call<BioResponse> call, Response<BioResponse> response) {
                if (response.body() != null && getNavigator() != null) {
                    getNavigator().showLoader(false);
                    getNavigator().setBio(response.body().getText());
                }
            }

            @Override
            public void onFailure(Call<BioResponse> call, Throwable t) {
                Log.d("mirage", "fetching profile failed. Probably not loggedIn");
                getNavigator().showLoader(false);
            }
        });
    }
}

