package com.rheotv.android.ui.activities.scoreboard.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.databinding.ListItemPastMatchTeamBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;

public class PastMatchTeamAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private ArrayList<String> list = new ArrayList<>();

    public PastMatchTeamAdapter(ArrayList<String> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PastMatchTeamViewHolder(ListItemPastMatchTeamBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class PastMatchTeamViewHolder extends BaseViewHolder {
        ListItemPastMatchTeamBinding binding;

        public PastMatchTeamViewHolder(ListItemPastMatchTeamBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {

        }
    }
}
