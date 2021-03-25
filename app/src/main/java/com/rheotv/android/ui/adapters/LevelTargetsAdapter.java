package com.rheotv.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.useProfile.responses.Target;
import com.rheotv.android.databinding.LevelTargetItemLayoutBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.recyclerdecorators.VerticalLinearItemDecorationV2;
import com.rheotv.android.utils.ViewUtils;

import java.util.List;

public class LevelTargetsAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<Target> mTargets;

    public LevelTargetsAdapter(List<Target> targets) {
        this.mTargets = targets;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LevelTargetItemLayoutBinding binding = LevelTargetItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LevelTargetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return mTargets.size();
    }

    public class LevelTargetViewHolder extends BaseViewHolder {
        LevelTargetItemLayoutBinding mBinding;

        LevelTargetViewHolder(LevelTargetItemLayoutBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            if (mTargets.get(position).getTitle() != null && !mTargets.get(position).getTitle().isEmpty()) {
                mBinding.title.setVisibility(View.VISIBLE);
                mBinding.title.setText(mTargets.get(position).getTitle());
            } else {
                mBinding.title.setVisibility(View.GONE);
            }
            if (mTargets.get(position).getData() != null && !mTargets.get(position).getData().isEmpty()) {
                mBinding.targetsDetailsRv.setVisibility(View.VISIBLE);
                TargetListAdapter adapter = new TargetListAdapter(mTargets.get(position).getData());
                mBinding.targetsDetailsRv.setLayoutManager(new LinearLayoutManager(mBinding.targetsDetailsRv.getContext(), LinearLayoutManager.VERTICAL, false));
                int spacing = ViewUtils.dpToPx(12);
                if (mTargets.get(position).getData().get(0).getTargetValue() == -1) {
                    spacing = ViewUtils.dpToPx(8);
                }
                mBinding.targetsDetailsRv.addItemDecoration(new VerticalLinearItemDecorationV2(spacing));
                mBinding.targetsDetailsRv.setAdapter(adapter);
            } else {
                mBinding.targetsDetailsRv.setVisibility(View.GONE);
            }
        }
    }


}
