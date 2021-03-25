package com.rheotv.android.ui.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.useProfile.responses.TargetData;
import com.rheotv.android.databinding.ListItemRankCriteriaBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.List;
import java.util.Random;

public class TargetListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<TargetData> mTargetItems;

    private int[] colorMap = new int[]{R.color.color_target_first, R.color.color_target_second, R.color.color_target_third, R.color.color_target_forth};

    TargetListAdapter(List<TargetData> targetItems) {
        this.mTargetItems = targetItems;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemRankCriteriaBinding binding = ListItemRankCriteriaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TargetListItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return mTargetItems.size();
    }

    public class TargetListItemHolder extends BaseViewHolder {

        ListItemRankCriteriaBinding mBinding;

        public TargetListItemHolder(ListItemRankCriteriaBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            Context context = mBinding.getRoot().getContext();
            TargetData result = mTargetItems.get(position);
            double targetValue = result.getTargetValue();
            double achievedValue = result.getAchievedValue();
            if (targetValue == -1) {
                targetValue = 1;
            }
            if (achievedValue == -1) {
                achievedValue = 1;
            }

            String progressTitle = "";
            if (result.getType() != null) {
                switch (result.getType().toLowerCase()) {
                    case "days":
                        progressTitle = "Valid Days";
                        break;
                    case "hours":
                        progressTitle = "Streaming Hours";
                        break;
                    case "time":
                        progressTitle = "Watch Time";
                        break;
                    case "followers":
                        progressTitle = "Followers";
                        break;
                    default:
                        progressTitle = result.getType();
                }
            }

            int progress = (int) ((int) (achievedValue * 100) / targetValue);

            LayerDrawable layerDrawable = (LayerDrawable) mBinding.streakProgressBar.getProgressDrawable();
            Drawable progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress);
            if (getItemCount() > 1)
                progressDrawable.setColorFilter(ContextCompat.getColor(mBinding.getRoot().getContext(), colorMap[position % colorMap.length]), PorterDuff.Mode.SRC_IN);
            else
                progressDrawable.setColorFilter(ContextCompat.getColor(mBinding.getRoot().getContext(), colorMap[new Random().nextInt(colorMap.length)]), PorterDuff.Mode.SRC_IN);

            if (progressTitle == null || progressTitle.isEmpty()) {
                mBinding.achievementTypeTextView.setVisibility(View.GONE);
            } else {
                mBinding.achievementTypeTextView.setVisibility(View.VISIBLE);
            }
            mBinding.achievementTypeTextView.setText(progressTitle);
            if (result.getTitle() == null || result.getTitle().isEmpty()) {
                mBinding.titleTextView.setVisibility(View.GONE);
            } else {
                mBinding.titleTextView.setVisibility(View.VISIBLE);
            }
            mBinding.titleTextView.setText(result.getTitle());
            if (result.getTargetValue() != -1 || result.getAchievedValue() != -1)
                mBinding.progressTextView.setText(String.format("%d/%d", (int) achievedValue, (int) targetValue));

            AnimatorSet set = new AnimatorSet();
            ObjectAnimator animation = ObjectAnimator.ofInt(mBinding.streakProgressBar, "progress", progress);
            animation.setDuration(1350);
            animation.setInterpolator(new DecelerateInterpolator());

            ValueAnimator animator = ValueAnimator.ofInt(0, (int) targetValue);
            animator.setDuration(1350);
            animator.setInterpolator(new DecelerateInterpolator());

            set.playTogether(animation, animator);
            set.start();
        }
    }
}
