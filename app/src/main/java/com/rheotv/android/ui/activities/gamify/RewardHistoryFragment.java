package com.rheotv.android.ui.activities.gamify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.gamify.RewardHistoryItem;
import com.rheotv.android.data.network.models.gamify.RewardMeta;
import com.rheotv.android.databinding.RewardHistoryFragmentBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.recyclerdecorators.LinearItemDecoration;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class RewardHistoryFragment extends BaseFragment<RewardHistoryFragmentBinding, RewardHistoryViewModel>
        implements RewardHistoryNavigator, RewardHistoryAdapter.RewardHistoryCallback {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    RewardHistoryAdapter historyAdapter;

    private RewardHistoryFragmentBinding mBinding;
    private RewardHistoryViewModel mViewModel;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static RewardHistoryFragment newInstance(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        RewardHistoryFragment fragment = new RewardHistoryFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_REWARD_HISTORY);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_REWARD_HISTORY, baseProperties);

        setupViews();
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.reward_history_fragment;
    }

    @Override
    public RewardHistoryViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(RewardHistoryViewModel.class);
        mViewModel.setNavigator(this);
        return mViewModel;
    }

    private void setupViews() {
        int spacingInPixels = mBinding.historyRecyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.margin_8);
        historyAdapter.setListener(this);
        LinearLayoutManager layoutManager = (LinearLayoutManager) mBinding.historyRecyclerView.getLayoutManager();
        mBinding.historyRecyclerView.addItemDecoration(new LinearItemDecoration(spacingInPixels));
        mBinding.historyRecyclerView.setAdapter(historyAdapter);
        mBinding.historyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!historyAdapter.isLoading() && mViewModel.rewardNextUrl != null && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    historyAdapter.showLoading(true);
                    mViewModel.loadRewardTransactionHistory();
                }
            }
        });
        mViewModel.loadRewardTransactionHistory();
    }

    @Override
    public void addItemInRewards(List<RewardHistoryItem> rewards) {
//        Log.i(getClass().getSimpleName(), " addItemInRewards " + new Gson().toJson(rewards));
        historyAdapter.showLoading(false);
        historyAdapter.addItems(rewards);
    }

    @Override
    public void handleError(String error) {
        historyAdapter.showLoading(false);
    }

    @Override
    public void onHistoryItemClick(RewardMeta meta) {
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_REWARD_HISTORY_ITEM_CLICK, baseProperties);
        Intent intent = new Intent(getContext(), RedeemActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppConstants.ARG_REWARD_META, meta);
        intent.putExtras(bundle);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_REWARD_HISTORY);
        startActivity(intent);
    }
}
