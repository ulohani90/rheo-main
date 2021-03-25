package com.rheotv.android.ui.activities.gamify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.gamify.Reward;
import com.rheotv.android.databinding.ListItemRewardVoucherBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.ViewUtils;

import java.util.Objects;
import java.util.Random;

public class RewardVoucherAdapter extends ListAdapter<Reward, BaseViewHolder> {

    private RewardVoucherInteraction mListener;
    private int mCellHeight = ViewGroup.LayoutParams.MATCH_PARENT;

    RewardVoucherAdapter(Context context, VoucherDiffUtil voucherDiffUtil) {
        super(voucherDiffUtil);
        mCellHeight = (int) ((ViewUtils.getScreenWidthInPx(context) - ViewUtils.dpToPx(54)) / 3);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemRewardVoucherBinding binding = ListItemRewardVoucherBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RewardVoucherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    public void setListener(RewardVoucherInteraction listener) {
        mListener = listener;
    }

    class RewardVoucherViewHolder extends BaseViewHolder {

        private ListItemRewardVoucherBinding mBinding;

        public RewardVoucherViewHolder(ListItemRewardVoucherBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            mBinding.setReward(getItem(position));
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mCellHeight);
            }
            layoutParams.height = mCellHeight;
            int scratchCardImage = R.drawable.ic_scratch_card_1;
            switch (new Random().nextInt(3)) {
                case 0:
                    scratchCardImage = (R.drawable.ic_scratch_card_1);
                    break;
                case 1:
                    scratchCardImage = (R.drawable.ic_scratch_card_2);
                    break;
                case 2:
                    scratchCardImage = (R.drawable.ic_scratch_card_3);
                    break;
            }
            mBinding.fireworkImageView.setImageResource(scratchCardImage);
            int finalScratchCardImage = scratchCardImage;
            mBinding.fireworkImageView.setOnClickListener(view -> {
                if (mListener != null) {
                    mListener.onScratchCardClick(mBinding.getReward(), finalScratchCardImage);
                }
            });
        }
    }

    public interface RewardVoucherInteraction {
        void onScratchCardClick(Reward card, int scratchCardImage);
    }

    public static class VoucherDiffUtil extends DiffUtil.ItemCallback<Reward> {

        @Override
        public boolean areItemsTheSame(@NonNull Reward oldItem, @NonNull Reward newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Reward oldItem, @NonNull Reward newItem) {
            return oldItem.equals(newItem);
        }
    }
}