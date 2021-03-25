package com.rheotv.android.ui.activities.scoreboard.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.score.PlayersListItem;
import com.rheotv.android.databinding.ListItemScoreboardPlayerBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardPlayerAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<PlayersListItem> list = new ArrayList<>();

    public ScoreboardPlayerAdapter(List<PlayersListItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayerViewHolder(ListItemScoreboardPlayerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class PlayerViewHolder extends BaseViewHolder {
        ListItemScoreboardPlayerBinding binding;

        public PlayerViewHolder(ListItemScoreboardPlayerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            binding.setPlayerItem(list.get(position));
        }
    }
}
