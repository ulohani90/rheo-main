package com.rheotv.android.ui.activities.gamify;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.gamify.CodaShopGame;
import com.rheotv.android.data.network.models.gamify.RewardMeta;
import com.rheotv.android.databinding.FragmentRedeemDetailBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import static com.rheotv.android.ui.activities.gamify.RewardRedeemFragment.ARG_GAME;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RedeemDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RedeemDetailFragment extends BaseFragment<FragmentRedeemDetailBinding, RedeemDetailViewModel>
        implements SkuAdapter.SkuSelectionListener, ConfirmationBottomSheetDialog.ItemClickListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    SkuAdapter adapter;

    private FragmentRedeemDetailBinding mBinding;
    private RedeemDetailViewModel mViewModel;
    private HashMap<String, Object> properties = new HashMap<>();

    public static RedeemDetailFragment newInstance(Bundle args) {
        RedeemDetailFragment fragment = new RedeemDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        setUpViews();
    }

    private void setUpViews() {
        adapter.setListener(this);
        mBinding.rewardRecyclerView.setAdapter(adapter);
        mBinding.setViewModel(mViewModel);
        setupActionbar(Objects.requireNonNull(mViewModel.getGameDetails().get()).getName());

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            properties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));

        properties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_REDEEM_DETAIL);
        properties.put("username", CommonUtils.getUserName(getContext()));
        properties.put("game", Objects.requireNonNull(mViewModel.getGameDetails().get()).getName());
        mViewModel.baseProperties = properties;

        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_REDEEM_DETAIL, properties);
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_redeem_detail;
    }

    @Override
    public RedeemDetailViewModel getViewModel() {
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(RedeemDetailViewModel.class);
        if (getArguments() != null) {
            CodaShopGame gameDetails = getArguments().getParcelable(ARG_GAME);
            if (gameDetails != null) {
                mViewModel.setGameDetails(gameDetails);
            }
        }
        mViewModel.loadSku();

        mViewModel.getPlayerSearchStatus().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mViewModel.getPlayerSearchStatus().get() == Status.SUCCESS) {
                    verificationAlert();
                }
            }
        });

        mViewModel.getPlaceOrderStatus().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                Log.i(getClass().getSimpleName(), "order_transaction_result" + mViewModel.getPlaceOrderStatus().get());
                if (mViewModel.getPlaceOrderStatus().get() == Status.SUCCESS) {
                    moveToTransactionDetail();

                } else if (mViewModel.getPlaceOrderStatus().get() == Status.ERROR) {
                    Toast.makeText(requireActivity(), "Transaction Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mViewModel.getSkusListResult().observe(this, list -> adapter.addSku(list));

        return mViewModel;
    }

    @Override
    public void onSkuItemSelected(String skus, int coinsUsed) {
        mViewModel.setSelectedSku(skus, coinsUsed);
    }

    private void setupActionbar(String title) {
        getBaseActivity().setSupportActionBar(mBinding.toolbar);
        if (getBaseActivity().getSupportActionBar() == null) return;
        getBaseActivity().getSupportActionBar().setTitle(title);
        mBinding.toolbar.setNavigationOnClickListener(v -> requireActivity().finish());
    }

    private void moveToTransactionDetail() {
        String totalCoins = RewardManager.getInstance().getTotalCoins();
        try {
            int coins = Integer.parseInt(totalCoins) - mViewModel.getCoinUsed();
            RewardManager.getInstance().setTotalCoins(coins + "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        RewardMeta meta = new RewardMeta(
                mViewModel.getPlayerId().get() == null ? null : mViewModel.getPlayerId().get().toString(),
                mViewModel.redeemType(),
                mViewModel.getTransactionId(),
                mViewModel.getVoucherCode(),
                mViewModel.redeemThumbnail(),
                mViewModel.getCoinUsed(),
                CommonUtils.getFormattedDate(System.currentTimeMillis()),
                Objects.requireNonNull(mViewModel.getGameDetails().get()).getName(),
                mViewModel.redeemCurrency(),
                mViewModel.getRedeemUrl()
        );

        RedeemSummaryFragment fragment = RedeemSummaryFragment.newInstance(meta, SegmentConstants.SCREEN_REDEEM_DETAIL);
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, getClass().getSimpleName()).commit();
        Map<String, Object> map = new HashMap<>(properties);
        map.put("coins_spent", mViewModel.getCoinUsed());
        map.put("game", Objects.requireNonNull(mViewModel.getGameDetails().get()).getName());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.REWARD_REDEEM_COMPLETED, map);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_UC_REDEEMED, map);
    }

    private void verificationAlert() {
        if (getFragmentManager() == null) return;
        ConfirmationBottomSheetDialog dialog = ConfirmationBottomSheetDialog.newInstance(RedeemDetailFragment.this, Objects.requireNonNull(mViewModel.getGameDetails().get()).getName(), Objects.requireNonNull(mViewModel.getUserName().get()).toString());
        dialog.show(getFragmentManager(), "AlertBottomSheetDialog");
    }

    @Override
    public void onPositiveButtonClick() {
        mViewModel.topUpTransaction();
    }

    @Override
    public void onNegativeButtonClick() {

    }
}
