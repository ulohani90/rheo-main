package com.rheotv.android.ui.activities.gamify;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.objects.FeedListingObject;
import com.rheotv.android.data.network.models.objects.FeedObject;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardGiveawayViewModel extends BaseViewModel {

    private static final String TAG = "RewardGiveawayViewModel";

    public RewardGiveawayViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    private MutableLiveData<List<FeedObject>> mGiveawayResult = new MutableLiveData<>();
    private String mGiveAwayUrl = null;

    MutableLiveData<List<FeedObject>> getGiveawayResult() {
        return mGiveawayResult;
    }

    public String getNextUrl() {
        return mGiveAwayUrl;
    }

    void loadGiveaway() {
        getCompositeDisposable().add(getDataManager()
                .getGiveawayVideos(null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onDataReceived, this::onError));
    }

    void loadMoreGiveaway() {
        if (mGiveAwayUrl == null) return;
        getCompositeDisposable().add(getDataManager()
                .getGiveawayVideos(mGiveAwayUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onDataReceived, this::onError));
    }

    private void onDataReceived(Response<FeedListingObject> response) {
        if (response != null && response.isSuccessful() && response.body() != null) {
            FeedListingObject result = response.body();
            mGiveAwayUrl = result.getNext();
            mGiveawayResult.setValue(result.getResults());
        }
    }

    private void onError(Throwable error) {
        Log.e(TAG, "Error ---> " + error.getMessage());
    }

    public void setNextUrl(String nextUrl) {
        mGiveAwayUrl = nextUrl;
    }

    public void reportPost(String postId) {
        //getNavigator().showProgressBarLoading("Reporting post. Please wait..");
        getDataManager().postReport(postId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //getNavigator().hideProgressBarLoading();
//                getNavigator().showToast();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //getNavigator().hideProgressBarLoading();
            }
        });
    }
}
