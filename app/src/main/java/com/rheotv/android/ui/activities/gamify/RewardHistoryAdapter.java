package com.rheotv.android.ui.activities.gamify;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.gamify.RewardHistoryItem;
import com.rheotv.android.data.network.models.gamify.RewardMeta;
import com.rheotv.android.databinding.FooterLoadingLayoutBinding;
import com.rheotv.android.databinding.ListItemRewardHistoryBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;

import java.util.List;

public class RewardHistoryAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<RewardHistoryItem> mHistoryList;
    private boolean isLoading = false;
    private RewardHistoryCallback mListener;

    RewardHistoryAdapter(List<RewardHistoryItem> postList) {
        this.mHistoryList = postList;
    }

    public void setListener(RewardHistoryCallback listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AppConstants.VIEW_TYPE_LOADING_FOOTER) {
            FooterLoadingLayoutBinding footerLoadingLayoutBinding = FooterLoadingLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new FooterLoadingViewHolder(footerLoadingLayoutBinding);
        }

        ListItemRewardHistoryBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_item_reward_history, parent, false);
        return new RewardHistoryAdapter.RewardHistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mHistoryList.size())
            return AppConstants.VIEW_TYPE_LOADING_FOOTER;
        return AppConstants.VIEW_TYPE_REWARD;
    }

    @Override
    public int getItemCount() {
        if (mHistoryList == null) return 0;
        return isLoading ? mHistoryList.size() + 1 : mHistoryList.size();
    }

    public void addItems(List<RewardHistoryItem> rewards) {
        if (rewards == null || rewards.isEmpty()) return;

        int oldCount = mHistoryList.size();
        this.mHistoryList.addAll(rewards);
        notifyItemRangeInserted(oldCount, mHistoryList.size());
    }

    public void showLoading(boolean flag) {
        this.isLoading = flag;
        if (flag) {
            notifyItemInserted(mHistoryList.size());
        } else {
            notifyItemRemoved(mHistoryList.size());
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    public class RewardHistoryViewHolder extends BaseViewHolder {
        private ListItemRewardHistoryBinding binding;

        RewardHistoryViewHolder(ListItemRewardHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            RewardHistoryItem item = mHistoryList.get(position);
            binding.setReward(item);
            binding.executePendingBindings();
            binding.getRoot().setOnClickListener(v -> {
               if (item.getRewardMeta() != null) {
                   RewardMeta meta = item.getRewardMeta();
                   meta.setDate(item.getCompletedOn());
                   meta.setCoins(item.getCoins());
                   mListener.onHistoryItemClick(meta);
               }
            });
        }
    }

    public class FooterLoadingViewHolder extends BaseViewHolder {

        FooterLoadingViewHolder(FooterLoadingLayoutBinding binding) {
            super(binding.getRoot());
        }

        @Override
        public void onBind(int position) {
            Log.i(getClass().getName(), "loading rewards");
        }
    }

    interface RewardHistoryCallback {
        void onHistoryItemClick(RewardMeta meta);
    }
}
