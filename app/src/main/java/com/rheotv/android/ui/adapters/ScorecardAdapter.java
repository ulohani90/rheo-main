package com.rheotv.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.score.TeamsListItem;
import com.rheotv.android.databinding.ListItemScorecardBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ScorecardAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<TeamsListItem> list = new ArrayList<>();

    public ScorecardAdapter(ArrayList<TeamsListItem> list) {
        this.list = list;
    }

    public void addItems(List<TeamsListItem> teams) {
        this.list.clear();
        this.list.addAll(teams);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScorecardViewHolder(ListItemScorecardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ScorecardViewHolder extends BaseViewHolder {
        ListItemScorecardBinding binding;

        ScorecardViewHolder(ListItemScorecardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            binding.setTeamItem(list.get(position));
        }
    }
}
