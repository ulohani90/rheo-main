package com.rheotv.android.ui.activities.tabcontainer.posts;



import androidx.databinding.ObservableField;

import com.rheotv.android.data.network.models.postlisting.responses.Result;

public class AlertItemViewModel {

    public ObservableField<String> imageUrl;

    AlertItemViewModelListener mListener;

    Result mResult;

    public AlertItemViewModel(AlertItemViewModelListener alertItemViewModelListener) {
        this.mListener = alertItemViewModelListener;
    }

    public void setData(Result result) {
        this.mResult = result;
        this.imageUrl = new ObservableField<String>(mResult.getBannerImageUrl());
    }

    public void onCardClicked() {
        mListener.onCardClicked(mResult);
    }

    public interface AlertItemViewModelListener {
        void onCardClicked(Result result);
    }

}
