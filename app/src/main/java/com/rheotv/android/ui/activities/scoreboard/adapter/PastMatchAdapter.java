package com.rheotv.android.ui.activities.scoreboard.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.databinding.ListItemPastMatchBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;

public class PastMatchAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private ArrayList<String> list = new ArrayList<>();

    public PastMatchAdapter(ArrayList<String> list) {
        this.list = list;
    }

    public void addItems(ArrayList<String> list) {
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PastMatchViewHolder(ListItemPastMatchBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class PastMatchViewHolder extends BaseViewHolder {
        ListItemPastMatchBinding binding;

        public PastMatchViewHolder(ListItemPastMatchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            PastMatchTeamAdapter adapter = new PastMatchTeamAdapter(list);
            binding.rvList.setAdapter(adapter);
        }
    }
}
