package com.rheotv.android.ui.activities.scoreboard.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.score.TeamsListItem;
import com.rheotv.android.databinding.ListItemScoreboardTeamBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardTeamAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<TeamsListItem> list = new ArrayList<>();
    private String scoreUnit = "Kills";

    public ScoreboardTeamAdapter(ArrayList<TeamsListItem> list) {
        this.list = list;
    }

    public void addItems(List<TeamsListItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setScoreUnit(String scoreUnit) {
        this.scoreUnit = scoreUnit;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TeamViewHolder(ListItemScoreboardTeamBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class TeamViewHolder extends BaseViewHolder {
        ListItemScoreboardTeamBinding binding;

        public TeamViewHolder(ListItemScoreboardTeamBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            binding.setUnit(scoreUnit);
            binding.setShowUnit(position == 0);

            binding.teamTextView.setText((list.get(position).getTeam() != null && list.get(position).getTeam().getName() != null) ? list.get(position).getTeam().getName() : "Team " + position);
            binding.setPosition(String.valueOf(position + 1));
            ScoreboardPlayerAdapter adapter = new ScoreboardPlayerAdapter(list.get(position).getPlayersList());
            binding.rvList.setAdapter(adapter);
        }
    }
}
