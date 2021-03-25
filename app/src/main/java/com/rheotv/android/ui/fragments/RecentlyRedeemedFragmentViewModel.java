package com.rheotv.android.ui.fragments;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.RecentlyRedeemedObject;
import com.rheotv.android.data.network.models.RecentlyRedeemedResponse;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecentlyRedeemedFragmentViewModel extends BaseViewModel<RecentlyRedeemedNavigator> {

    public ObservableField<Boolean> showLoading = new ObservableField<>(false);

    public ObservableField<Boolean> showEmptyText = new ObservableField<>(false);

    public ObservableField<Boolean> showList = new ObservableField<>(false);

    MutableLiveData<List<RecentlyRedeemedObject>> results = new MutableLiveData<>();

    public RecentlyRedeemedFragmentViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void loadRecentlyRedeemedList() {
        showLoading.set(true);
        showEmptyText.set(false);
        getDataManager().getRecentlyRedeemedList().enqueue(new Callback<RecentlyRedeemedResponse>() {
            @Override
            public void onResponse(Call<RecentlyRedeemedResponse> call, Response<RecentlyRedeemedResponse> response) {
                if (response != null && response.body() != null && response.body().getResults() != null) {
                    if (response.body().getResults().size() > 0) {
                        results.setValue(response.body().getResults());
                        showList.set(true);
                    } else {
                        showEmptyText.set(true);
                    }
                }
                showLoading.set(false);
            }

            @Override
            public void onFailure(Call<RecentlyRedeemedResponse> call, Throwable t) {
                showEmptyText.set(true);
                showLoading.set(false);

            }
        });
    }
}
