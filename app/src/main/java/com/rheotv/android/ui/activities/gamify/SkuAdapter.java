package com.rheotv.android.ui.activities.gamify;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.gamify.RedeemPrice;
import com.rheotv.android.data.network.models.gamify.SkusItem;
import com.rheotv.android.databinding.ListItemGameRewardBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.RewardManager;

import java.util.HashMap;
import java.util.List;

public class SkuAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<SkusItem> mList;
    private int checkedPosition = -1;
    private SkuSelectionListener listener;
    private long totalCoins = RewardManager.getInstance().getTotalCoin();
    private long selectedCoin = 0;
    private HashMap<String, RedeemPrice> map = new HashMap<>();

    public SkuAdapter(List<SkusItem> mList) {
        this.mList = mList;
    }

    public void setListener(SkuSelectionListener listener) {
        this.listener = listener;
    }

    public void addSku(List<SkusItem> list) {
        if (list == null) return;
        this.mList.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemGameRewardBinding binding = ListItemGameRewardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SkuViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class SkuViewHolder extends BaseViewHolder {
        private ListItemGameRewardBinding binding;

        SkuViewHolder(@NonNull ListItemGameRewardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            SkusItem item = mList.get(position);
            binding.setSku(item);
            if (checkedPosition == position) {
                binding.setChecked(true);
                binding.skuContainerLayout.setSelected(true);
            } else {
                binding.setChecked(false);
                binding.skuContainerLayout.setSelected(false);
            }

            binding.getRoot().setOnClickListener(view -> {
                if (checkedPosition == position) {
                    binding.setChecked(false);
                    binding.skuContainerLayout.setSelected(false);
                    notifyDataSetChanged();
                } else {
                    if (item.getPrice().getAmount() <= totalCoins) {
                        checkedPosition = position;
                        binding.setChecked(true);
                        binding.skuContainerLayout.setSelected(true);
                        if (listener != null)
                            listener.onSkuItemSelected(item.getSku(), item.getPrice().getAmount());
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(binding.getRoot().getContext(), "You don't have enough Rheo coins", Toast.LENGTH_SHORT).show();
                    }
                }

//                if (map.containsKey(item.getSku())) {
//                    selectedCoin -= map.get(item.getSku()).getAmount();
//                    binding.setChecked(false);
//                    binding.getRoot().setSelected(false);
//                    map.remove(item.getSku());
//                } else {
//                    if (selectedCoin + item.getPrice().getAmount() <= totalCoins) {
//                        map.put(item.getSku(), item.getPrice());
//                        selectedCoin += selectedCoin + item.getPrice().getAmount();
//                        binding.setChecked(true);
//                        binding.getRoot().setSelected(true);
//                    }
//                }
                Log.i(getClass().getSimpleName(), "on reward selected: " + selectedCoin + " and " + map.entrySet());

            });

            binding.executePendingBindings();
        }
    }

    interface SkuSelectionListener {
        void onSkuItemSelected(String skus, int coinsUsed);
    }
}
