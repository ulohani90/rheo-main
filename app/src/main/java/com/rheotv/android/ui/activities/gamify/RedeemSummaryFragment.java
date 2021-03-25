package com.rheotv.android.ui.activities.gamify;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.gamify.RewardMeta;
import com.rheotv.android.databinding.FragmentRedeemSummaryBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.customViews.bottomSheetMenu.BottomSheetMenuDialog;
import com.rheotv.android.ui.customViews.bottomSheetMenu.Option;
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtils;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import static com.rheotv.android.utils.AppConstants.ARG_REWARD_META;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RedeemSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RedeemSummaryFragment extends BaseFragment<FragmentRedeemSummaryBinding, RedeemSummaryViewModel> {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private FragmentRedeemSummaryBinding mBinding;
    private RedeemSummaryViewModel mViewModel;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    private BottomSheetMenuDialog.Builder shareSheet;
    private boolean isAfterShareAction = false;

    public static RedeemSummaryFragment newInstance(RewardMeta rewardMeta, String source) {
        RedeemSummaryFragment fragment = new RedeemSummaryFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_REWARD_META, rewardMeta);
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupActionbar();
        buildShareSheet();
    }

    private void setupViews() {
        mBinding = getViewDataBinding();

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_REDEEM_SUMMARY);
        baseProperties.put("username", CommonUtils.getUserName(getContext()));
        baseProperties.put("game", Objects.requireNonNull(mViewModel.meta.get()).getGame());
        baseProperties.put("coins", Objects.requireNonNull(mViewModel.meta.get()).getCoins());
        baseProperties.put("rewardType", Objects.requireNonNull(mViewModel.meta.get()).getRewardType());

        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_REDEEM_SUMMARY, baseProperties);
        mViewModel.baseProperties = baseProperties;
        mBinding.setViewModel(mViewModel);
        mBinding.doneButton.setOnClickListener(v -> finishSummary());
        mBinding.revealButton.setOnClickListener(v -> shareSheet.show(getChildFragmentManager(), "BottomSheetMenuDialog"));
    }

    private void setupActionbar() {
        getBaseActivity().setSupportActionBar(mBinding.toolbar);
        if (getBaseActivity().getSupportActionBar() == null) return;
        getBaseActivity().getSupportActionBar().setTitle(getString(R.string.summary));
        mBinding.toolbar.setNavigationOnClickListener(v -> finishSummary());
    }

    private void finishSummary() {
        Intent intent = new Intent(getActivity(), RewardsActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_REDEEM_SUMMARY);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).finish();
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_redeem_summary;
    }

    @Override
    public RedeemSummaryViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(RedeemSummaryViewModel.class);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.getParcelable(ARG_REWARD_META) != null) {
            mViewModel.meta.set(bundle.getParcelable(ARG_REWARD_META));
        }

        return mViewModel;
    }

    private void buildShareSheet() {
        shareSheet = new BottomSheetMenuDialog.Builder()
                .header("Share via")
                .columns(3)
                .setAdjustWindow(false)
                .setListener(this::onShareItemClick);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        List<ResolveInfo> resolveInfoList = getBaseActivity().getPackageManager().queryIntentActivities(intent, 0);
        ArrayList<OptionRequest> optionRequestList = new ArrayList<>();
        for (ResolveInfo res : resolveInfoList) {
            OptionRequest request = new OptionRequest(
                    res.labelRes,
                    res.loadLabel(getBaseActivity().getPackageManager()).toString(),
                    res.loadIcon(getBaseActivity().getPackageManager()),
                    res.activityInfo.packageName);
            optionRequestList.add(request);
        }

        Comparator<OptionRequest> comparator = (optionRequest, t1) -> {
            if (optionRequest.getTag() != null && t1.getTag() != null) {
                if (optionRequest.getTag().equalsIgnoreCase(AppConstants.WHATSAPP_PACKAGE)) {
                    return -1;
                } else if (optionRequest.getTag().equalsIgnoreCase(AppConstants.FACEBOOK_PACKAGE)) {
                    return -1;
                } else if (optionRequest.getTag().equalsIgnoreCase(AppConstants.FACEBOOK_LITE_PACKAGE)) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return 0;
        };

        try {
            Collections.sort(optionRequestList, comparator);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        shareSheet.addAll(optionRequestList);
        shareSheet.build();
    }

    private void onShareItemClick(String s, Option option) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("platform", option.getTitle());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_REDEEM_SHARE_PLATFORM_SELECTED, properties);
        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.BRANCH_REDEEM_URL_SHARE, AppUtils.getUserRedeemUrl());
        map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_REDEEM);
        FirebaseDynamicLinkUtils.share(getContext(),
                CommonUtils.getBranchExtraInfo(getContext()),
                "redeem_share",
                shareTitle(),
                "Download the Rheo App fast for such exciting game accessories. -\n".concat(AppUtils.getUserRedeemUrl()),
                CommonUtils.getUserProfilePic(),
                map,
                AppUtils.getUserRedeemUrl(),
                option.getTag());
        isAfterShareAction = true;
    }

    private String shareTitle() {
        return "Hey, I have redeemed game currency on Rheo. Get yours now\n\n".
                replace("game", mViewModel.getGameName()).
                replace("currency", mViewModel.getGameCurrency());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAfterShareAction && mViewModel != null)
            mViewModel.isContentShared.set(true);
    }
}
