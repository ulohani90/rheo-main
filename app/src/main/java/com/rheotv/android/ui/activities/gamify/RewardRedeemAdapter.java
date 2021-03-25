package com.rheotv.android.ui.activities.gamify;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.rheotv.android.data.network.models.gamify.CodaShopGame;
import com.rheotv.android.databinding.ListItemRewardRedeemBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

public class RewardRedeemAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<CodaShopGame> mList = new ArrayList<>();
    private RedeemAdapterInteraction listener;

    public void submitList(List<CodaShopGame> list) {
//        Log.i(getClass().getSimpleName(), "loadGames " + new Gson().toJson(list));
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setListener(RedeemAdapterInteraction listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemRewardRedeemBinding binding = ListItemRewardRedeemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RewardRedeemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class RewardRedeemViewHolder extends BaseViewHolder {
        private ListItemRewardRedeemBinding binding;

        RewardRedeemViewHolder(ListItemRewardRedeemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            boolean shouldProceedRedeem;
            if (mList.get(position).getCodaShopProductName() == null || mList.get(position).getCodaShopProductName().isEmpty()) {
                mList.get(position).setName(mList.get(position).getName() + " (Coming Soon)");
                shouldProceedRedeem = false;
            } else {
                shouldProceedRedeem = true;
            }
            binding.setGame(mList.get(position));
            binding.getRoot().setOnClickListener(view -> {
                if (listener != null && shouldProceedRedeem)
                    listener.onGameItemClick(mList.get(position));
            });
            binding.executePendingBindings();
        }
    }

    public interface RedeemAdapterInteraction {
        void onGameItemClick(CodaShopGame game);
    }
}
