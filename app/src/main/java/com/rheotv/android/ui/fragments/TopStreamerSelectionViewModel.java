package com.rheotv.android.ui.fragments;

import android.os.AsyncTask;
import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.TopStreamerObject;
import com.rheotv.android.data.network.models.TopStreamersResponse;
import com.rheotv.android.data.network.models.onboarding.LanguageObject;
import com.rheotv.android.data.network.models.onboarding.OnBoardingResponse;
import com.rheotv.android.db.AppDatabase;
import com.rheotv.android.db.UserFollowDao;
import com.rheotv.android.db.UserFollowItem;
import com.rheotv.android.ui.activities.onboarding.OnBoardingActivityViewModel;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopStreamerSelectionViewModel extends BaseViewModel<TopStreamerFragmentNavigator> {

    Set<String> selectedLanguages = new HashSet<>();

    MutableLiveData<List<TopStreamerObject>> result = new MutableLiveData<>();
    MutableLiveData<List<LanguageObject>> languagesLiveData = new MutableLiveData<>();
    public ObservableField<Status> viewStatus = new ObservableField<>();

    private String nextUrl;

    public String getNextUrl() {
        return nextUrl;
    }

    public TopStreamerSelectionViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void loadTopStreamersData(boolean refreshList) {
        viewStatus.set(Status.LOADING);
        if (refreshList) {
            nextUrl = null;
        }
        getDataManager().fetchTopStreamers(nextUrl, AppUtilsKt.INSTANCE.getCollectionToArrayOfString(selectedLanguages)).enqueue(new Callback<TopStreamersResponse>() {
            @Override
            public void onResponse(@NotNull Call<TopStreamersResponse> call, @NotNull Response<TopStreamersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    nextUrl = response.body().getData().getNext();
                    if (result.getValue() != null && result.getValue().size() == 0)
                        result.getValue().clear();
                    result.setValue(response.body().getData().getResults());
                    viewStatus.set(Status.SUCCESS);
                } else {
                    viewStatus.set(Status.ERROR);
                }
            }

            @Override
            public void onFailure(@NotNull Call<TopStreamersResponse> call, @NotNull Throwable t) {
                t.printStackTrace();
                viewStatus.set(Status.ERROR);
            }
        });
    }

    void fetchLanguage() {
        viewStatus.set(Status.LOADING);
        getDataManager().fetchOnBoardingData().enqueue(new Callback<OnBoardingResponse>() {
            @Override
            public void onResponse(@NotNull Call<OnBoardingResponse> call, @NotNull Response<OnBoardingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    languagesLiveData.setValue(response.body().getLanguageObjects());
                    viewStatus.set(Status.SUCCESS);
                } else {
                    viewStatus.set(Status.ERROR);
                }
            }

            @Override
            public void onFailure(@NotNull Call<OnBoardingResponse> call, @NotNull Throwable t) {
                Log.i(OnBoardingActivityViewModel.class.getCanonicalName(), "Failed");
                viewStatus.set(Status.ERROR);
            }
        });
    }

    void followSelectedUsers(HashMap<Integer, TopStreamerObject> usersToBeFollowed) {
        viewStatus.set(Status.LOADING);
        new FollowUserAsyncTask(getDataManager(), usersToBeFollowed).execute();
    }

    private static class FollowUserAsyncTask extends AsyncTask<Void, Void, Void> {

        private DataManager mDataManager;
        private HashMap<Integer, TopStreamerObject> mUsersToBeFollowed;
        private UserFollowDao dao = AppDatabase.Companion.getInstance(RheoTvApp.getNonUiContext()).userFollowDao();

        public FollowUserAsyncTask(DataManager dataManager, HashMap<Integer, TopStreamerObject> usersToBeFollowed) {
            mDataManager = dataManager;
            mUsersToBeFollowed = usersToBeFollowed;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<UserFollowItem> list = AppUtilsKt.INSTANCE.getTypedList(new ArrayList<>(mUsersToBeFollowed.values()), new Function1<TopStreamerObject, UserFollowItem>() {
                @Override
                public UserFollowItem invoke(TopStreamerObject topStreamerObject) {
                    return new UserFollowItem(topStreamerObject.getUser().getId(), topStreamerObject.getUser().getUsername(), true);
                }
            });
            List<Long> addedIds = dao.updateMultipleUserEntry(list);
            for (Map.Entry<Integer, TopStreamerObject> streamerObject : mUsersToBeFollowed.entrySet()) {
                try {
                    mDataManager.followAuthor(streamerObject.getKey().toString()).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
