package com.rheotv.android.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.UserPermissionsResponse;
import com.rheotv.android.data.network.models.general.GameDetails;
import com.rheotv.android.data.network.models.general.RTMPDetails;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class LiveStreamViewModel extends BaseViewModel {
    private MutableLiveData<List<GameDetails>> gameResults = new MutableLiveData<>();
    private MutableLiveData<RTMPDetails> rtmDetail = new MutableLiveData<>();
    private ObservableField<String> livePostTitle = new ObservableField<>();
    private ObservableField<Boolean> takeGameRequest = new ObservableField<>(false);

    private ObservableField<Boolean> enableCoHostRequest = new ObservableField<>(false);
    private ObservableField<Boolean> termsCondition = new ObservableField<>(false);
    private ObservableField<Boolean> submitted = new ObservableField<>(false);
    private ObservableField<Boolean> platformSelected = new ObservableField<>(false);
    private ObservableField<String> rtmUrl = new ObservableField<>();
    private ObservableField<String> rtmKey = new ObservableField<>();
    private ObservableField<Boolean> showCanTakeRequest = new ObservableField<>(false);
    public HashMap<String, Object> baseProperties = new HashMap<>();
    public ObservableField<Boolean> mobileSelected = new ObservableField<>();
    public ObservableField<String> rheoCoinCount = new ObservableField<>();
    public ObservableField<Boolean> allowCustomRoom = new ObservableField<>();
    public ObservableField<Boolean> isVideoCallingFeatureAllowed = new ObservableField<>(false);

    public MutableLiveData<List<GameDetails>> getGameResults() {
        return gameResults;
    }

    public MutableLiveData<RTMPDetails> getRtmDetail() {
        return rtmDetail;
    }

    public ObservableField<String> getLivePostTitle() {
        return livePostTitle;
    }

    public ObservableField<String> getRheoCoinCount() {
        return rheoCoinCount;
    }

    public void setAllowCustomRoom(ObservableField<Boolean> allowCustomRoom) {
        this.allowCustomRoom = allowCustomRoom;
    }

    public void setLivePostTitle(ObservableField<String> livePostTitle) {
        this.livePostTitle = livePostTitle;
    }

    public ObservableField<Boolean> getEnableCoHostRequest() {
        return enableCoHostRequest;
    }

    public void setEnableCoHostRequest(ObservableField<Boolean> enableCoHostRequest) {
        this.enableCoHostRequest = enableCoHostRequest;
    }

    public ObservableField<Boolean> getTakeGameRequest() {
        return takeGameRequest;
    }

    public void setTakeGameRequest(ObservableField<Boolean> takeGameRequest) {
        this.takeGameRequest = takeGameRequest;
    }

    public ObservableField<Boolean> getShowCanTakeRequest() {
        return showCanTakeRequest;
    }

    public void setShowCanTakeRequest(ObservableField<Boolean> showCanTakeRequest) {
        this.showCanTakeRequest = showCanTakeRequest;
    }

    public void setPlatformSelected(boolean platformSelected) {
        this.platformSelected.set(platformSelected);
    }

    public void setAllowCustomRoom(boolean allowCustomRoom) {
        this.allowCustomRoom.set(allowCustomRoom);
    }

    public ObservableField<Boolean> getPlatformSelected() {
        return platformSelected;
    }

    public void setShowTakeRequest(boolean flag) {
        this.showCanTakeRequest.set(flag);
    }

    public ObservableField<Boolean> getTermsCondition() {
        return termsCondition;
    }

    public void setTermsCondition(ObservableField<Boolean> termsCondition) {
        this.termsCondition = termsCondition;
    }

    public ObservableField<Boolean> getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean flag) {
        this.submitted.set(flag);
    }

    public ObservableField<String> getRtmUrl() {
        return rtmUrl;
    }

    public void setRtmUrl(ObservableField<String> rtmUrl) {
        this.rtmUrl = rtmUrl;
    }

    public ObservableField<String> getRtmKey() {
        return rtmKey;
    }

    public void setRtmKey(ObservableField<String> rtmKey) {
        this.rtmKey = rtmKey;
    }

    public LiveStreamViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void loadGameDetails() {
        getDataManager()
                .getGameDetails()
                .enqueue(new Callback<List<GameDetails>>() {
                    @Override
                    public void onResponse(Call<List<GameDetails>> call, Response<List<GameDetails>> response) {
                        if (response.isSuccessful()) {
                            gameResults.setValue(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GameDetails>> call, Throwable t) {
                        Log.e(getClass().getSimpleName(), "failed to get games");
                    }
                });
    }

    public void createLivePostAndGetRTMPURL(String name, String gameId, String gameName, MultipartBody.Part part, int entryCoins) {
        Log.i(getClass().getSimpleName(), "createLivePostAndGetRTMPURL: canRequestPlay " + takeGameRequest);
        getDataManager()
                .createLivePost(name, gameId, takeGameRequest.get(), part, mobileSelected.get() ? true : false, (allowCustomRoom.get() && takeGameRequest.get()), enableCoHostRequest.get(), entryCoins)
                .enqueue(new Callback<RTMPDetails>() {
                    @Override
                    public void onResponse(Call<RTMPDetails> rtmpDetails, Response<RTMPDetails> response) {
                        rtmDetail.setValue(response.body());
                        if (response.body() != null) {
                            rtmKey.set(response.body().getKey());
                            rtmUrl.set(response.body().getBase_url());
                            baseProperties.put("game_name", gameName);
                            if (CommonUtils.isFirstGoLiveGenerateKeyNotTracked()) {
                                CommonUtils.setFirstGoLiveGenerateKeyEventTracked();
                                SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_FIRST_LIVE_STREAM_KEY_CREATED, baseProperties);
                            }
                            Map<String, Object> map = new HashMap<>(baseProperties);
                            map.put("is_cohost_feature_enabled", enableCoHostRequest.get());
                            map.put("is_first", CommonUtils.isFirstTimeLiveStreamKeyCreated());
                            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_LIVE_STREAM_KEY_CREATED, map);
                            CommonUtils.setFirstTimeLiveStreamKeyCreated();
                        }
                    }

                    @Override
                    public void onFailure(Call<RTMPDetails> call, Throwable t) {
                        Log.e(getClass().getSimpleName(), "failed to create post");
                    }
                });
    }

    public void checkFeaturesEnabledPermission() {
        getDataManager().getPermissionsResponse().enqueue(new Callback<UserPermissionsResponse>() {
            @Override
            public void onResponse(@NotNull Call<UserPermissionsResponse> call, @NotNull Response<UserPermissionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isVideoCallingFeatureAllowed.set(true);
                }
            }

            @Override
            public void onFailure(@NotNull Call<UserPermissionsResponse> call, @NotNull Throwable t) {
                Log.i(LiveStreamViewModel.class.getCanonicalName(), t.getLocalizedMessage());
            }
        });
    }

    public void onCopyLinkClick(View view) {
        ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("rtmp_url", rtmUrl.get());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(view.getContext(), "URL is copied, use it to stream.", Toast.LENGTH_SHORT).show();
    }

    public void onCopyKeyClick(View view) {
        ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("rtmp_key", rtmKey.get());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(view.getContext(), "Key is copied, use it to stream.", Toast.LENGTH_SHORT).show();
    }

}
