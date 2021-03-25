package com.rheotv.android.ui.activities.player.activity;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.stickers.Sticker;
import com.rheotv.android.data.network.models.stickers.StickersResponse;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StickerBottomSheetViewModel extends BaseViewModel {

    String postId;
    private String nextStickerUrl;
    private MutableLiveData<List<Sticker>> mStickerList = new MutableLiveData<>();

    MutableLiveData<List<Sticker>> getStickerList() {
        return mStickerList;
    }

    String getNextStickerUrl() {
        return nextStickerUrl;
    }

    public StickerBottomSheetViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    void loadStickers() {
        getDataManager().loadStickers(postId, nextStickerUrl).enqueue(new Callback<StickersResponse>() {
            @Override
            public void onResponse(Call<StickersResponse> call, Response<StickersResponse> response) {
                if (response.body() != null) {
                    mStickerList.setValue(response.body().getResults());
                    nextStickerUrl = response.body().getNext();
                }
            }

            @Override
            public void onFailure(Call<StickersResponse> call, Throwable t) {
                if (t != null)
                    Log.i(PlayerViewModel.class.getCanonicalName(), "loadStickers error: " + t.getLocalizedMessage());
            }
        });
    }
}
