package com.rheotv.android.ui.activities.gamify;

import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.gamify.BaseTransactionResponse;
import com.rheotv.android.data.network.models.gamify.CodaShopGame;
import com.rheotv.android.data.network.models.gamify.CodaShopValidationResponse;
import com.rheotv.android.data.network.models.gamify.SkuResponse;
import com.rheotv.android.data.network.models.gamify.SkusItem;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class RedeemDetailViewModel extends BaseViewModel {
    private final MutableLiveData<List<SkusItem>> skusListResult = new MutableLiveData<>();
    private ObservableField<CharSequence> playerId = new ObservableField<>();
    private ObservableField<CharSequence> userName = new ObservableField<>();
    private ObservableField<CharSequence> emailAddress = new ObservableField<>();
    private ObservableField<Status> playerSearchStatus = new ObservableField<>();
    private ObservableField<Boolean> canContinue = new ObservableField<>(false);
    private ObservableField<Status> placeOrderStatus = new ObservableField<>();
    private ObservableField<String> rheoCoins = new ObservableField<>(RewardManager.getInstance().getTotalCoins());
    private ObservableField<CodaShopGame> gameDetails = new ObservableField<>(new CodaShopGame("", "", "", "", 0, false, "", 0, ""));
    private ArrayList<String> selectedSku = new ArrayList<>();
    private int coinUsed = 0;
    private String orderId;
    private String userAccount;
    private String transactionId;
    private String voucherCodes;
    private String redeemUrl;
    public ObservableField<Status> skuStatus = new ObservableField<>();

    public ObservableField<CharSequence> getPlayerId() {
        return playerId;
    }

    public ObservableField<CharSequence> getUserName() {
        return userName;
    }

    public void setUserName(ObservableField<CharSequence> userName) {
        this.userName = userName;
    }

    public ObservableField<CharSequence> getEmailAddress() {
        return emailAddress;
    }

    public ObservableField<Status> getPlayerSearchStatus() {
        return playerSearchStatus;
    }

    public ObservableField<Boolean> getCanContinue() {
        return canContinue;
    }

    public ObservableField<Status> getPlaceOrderStatus() {
        return placeOrderStatus;
    }

    public HashMap<String, Object> baseProperties = new HashMap<>();

    public void setSelectedSku(String selectedSku, int coinUsed) {
        this.selectedSku.clear();
        if (selectedSku == null) {
            this.coinUsed = 0;
            this.rheoCoins.set((RewardManager.getInstance().getTotalCoin() + coinUsed) + "");
        } else {
            this.selectedSku.add(0, selectedSku);
            this.coinUsed = coinUsed;
            this.rheoCoins.set((RewardManager.getInstance().getTotalCoin() - coinUsed) + "");
        }
    }

    public ObservableField<String> getRheoCoins() {
        return rheoCoins;
    }

    public ObservableField<CodaShopGame> getGameDetails() {
        return gameDetails;
    }

    public void setGameDetails(CodaShopGame gameDetails) {
        this.gameDetails.set(gameDetails);
    }

    public MutableLiveData<List<SkusItem>> getSkusListResult() {
        return skusListResult;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getCoinUsed() {
        return coinUsed;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getVoucherCode() {
        return voucherCodes;
    }

    public RedeemDetailViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void loadSku() {
        skuStatus.set(Status.LOADING);
        String gameId = gameDetails.get() != null ? gameDetails.get().getId() : null;
        getDataManager().getCodeShopSku(gameId).enqueue(new Callback<SkuResponse>() {
            @Override
            public void onResponse(Call<SkuResponse> call, Response<SkuResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    skusListResult.setValue(response.body().getResult().getSkus());
                    skuStatus.set(Status.SUCCESS);
                } else {
                    skuStatus.set(Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<SkuResponse> call, Throwable t) {
                skuStatus.set(Status.ERROR);
            }
        });
    }

    public boolean isTopup() {
        return Objects.requireNonNull(gameDetails.get()).getCodaShopInterface().equalsIgnoreCase("topup");
    }

    @Nullable
    public String redeemType() {
        return Objects.requireNonNull(gameDetails.get()).getCodaShopInterface();
    }

    public String redeemThumbnail() {
        return skusListResult.getValue() == null || skusListResult.getValue().isEmpty() ? "" : skusListResult.getValue().get(0).getThumbnail();
    }

    public String redeemCurrency() {
        return skusListResult.getValue() == null || skusListResult.getValue().isEmpty() ? "" : skusListResult.getValue().get(0).getDescription();
    }

    public String getRedeemUrl() {
        return redeemUrl;
    }

    public void onContinueClick(View view) {
        if (isTopup()) {
//            if(emailAddress.get() == null || Objects.requireNonNull(emailAddress.get()).toString().isEmpty()) {
//                Toast.makeText(view.getContext(), "Please enter an email address", Toast.LENGTH_SHORT).show();
//                return;
//            } else if (!CommonUtils.isEmailValid(Objects.requireNonNull(emailAddress.get()).toString())) {
//                Toast.makeText(view.getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
//                return;
//            }
            if (getPlayerId().get() == null || getPlayerId().get().toString().isEmpty()) {
                Toast.makeText(view.getContext(), "Please enter Player ID", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (selectedSku == null || selectedSku.isEmpty() || coinUsed == 0) {
            Toast.makeText(view.getContext(), "Please select a reward", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isTopup()) {
            validate();
        } else {
            voucherTransaction();
        }
    }

    private void validate() {
        playerSearchStatus.set(Status.LOADING);
        String gameId = gameDetails.get() != null ? gameDetails.get().getId() : null;
        String playerId = getPlayerId().get() != null ? getPlayerId().get().toString() : null;
        getDataManager().validateCodaShopUser(gameId, playerId, selectedSku).enqueue(new Callback<CodaShopValidationResponse>() {
            @Override
            public void onResponse(Call<CodaShopValidationResponse> call, Response<CodaShopValidationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getResult() != null) {
                        userName.set(response.body().getResult().getMessage().getUsername());
                        orderId = response.body().getResult().getOrderId();
                        userAccount = response.body().getResult().getMessage().getUserAccount();
                        playerSearchStatus.set(Status.SUCCESS);
                    } else if (response.body().getError() != null) {
                        Toast.makeText(getNonUiContext(), response.body().getError().getMessage(), Toast.LENGTH_SHORT).show();
                        playerSearchStatus.set(Status.EMPTY);
                    } else {
                        playerSearchStatus.set(Status.ERROR);
                    }
                } else {
                    playerSearchStatus.set(Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<CodaShopValidationResponse> call, Throwable t) {
                playerSearchStatus.set(Status.ERROR);
            }
        });
    }

    void topUpTransaction() {
        recordInitRedeemEvent();
        placeOrderStatus.set(Status.LOADING);
        String gameId = gameDetails.get() != null ? gameDetails.get().getId() : null;
        getDataManager().codaShopTopupTransaction(orderId, selectedSku, userAccount, gameId, coinUsed, redeemType()).enqueue(new Callback<BaseTransactionResponse>() {
            @Override
            public void onResponse(Call<BaseTransactionResponse> call, Response<BaseTransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    transactionId = response.body().getResult().getOrderId();
                    placeOrderStatus.set(Status.SUCCESS);
                } else
                    placeOrderStatus.set(Status.ERROR);
            }

            @Override
            public void onFailure(Call<BaseTransactionResponse> call, Throwable t) {
                placeOrderStatus.set(Status.ERROR);
            }
        });
    }

    private void voucherTransaction() {
        recordInitRedeemEvent();
        placeOrderStatus.set(Status.LOADING);
        String gameId = gameDetails.get() != null ? gameDetails.get().getId() : null;
        getDataManager().codaShopVoucherTransaction(selectedSku, gameId, coinUsed, redeemType()).enqueue(new Callback<BaseTransactionResponse>() {
            @Override
            public void onResponse(Call<BaseTransactionResponse> call, Response<BaseTransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    transactionId = response.body().getResult().getOrderId();
                    voucherCodes = response.body().getResult().getItems().get(0).getCodes();
                    redeemUrl = response.body().getResult().getRedeemUrl();
                    placeOrderStatus.set(Status.SUCCESS);
                } else
                    placeOrderStatus.set(Status.ERROR);
            }

            @Override
            public void onFailure(Call<BaseTransactionResponse> call, Throwable t) {
                placeOrderStatus.set(Status.ERROR);
            }
        });
    }

    private void recordInitRedeemEvent() {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("userName", userName.get() + "");
        properties.put("game", Objects.requireNonNull(gameDetails.get()).getName());
        SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.REWARD_REDEEM_INFO_VALIDATED, properties);
    }
}
