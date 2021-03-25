package com.rheotv.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.TopStreamerObject;
import com.rheotv.android.databinding.ListItemCardTopStreamersBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopStreamersRVAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<TopStreamerObject> topStreamerObjects;
    private OnItemSelectedListener itemSelectedListener;
    private boolean isPaginating;
    private HashMap<Integer, TopStreamerObject> selectedStreamers = new HashMap<>();

    public TopStreamersRVAdapter() {
        topStreamerObjects = new ArrayList<>();
    }

    public void addTopStreamers(List<TopStreamerObject> topStreamerObjects) {
        setPaginating(false);
        if (isRefreshing) {
            this.topStreamerObjects.clear();
            this.topStreamerObjects.addAll(topStreamerObjects);
            notifyDataSetChanged();
            isRefreshing = false;
            return;
        }
        int startPosition = this.topStreamerObjects.size();
        this.topStreamerObjects.addAll(topStreamerObjects);
        notifyItemRangeInserted(startPosition, topStreamerObjects.size());
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemCardTopStreamersBinding mBinding = ListItemCardTopStreamersBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TopStreamerItemViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return topStreamerObjects.size();
    }

    public boolean isPaginating() {
        return isPaginating;
    }

    public void setPaginating(boolean paginating) {
        isPaginating = paginating;
    }

    public void setItemSelectedListener(OnItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    public HashMap<Integer, TopStreamerObject> getSelectedStreamers() {
        return selectedStreamers;
    }

    public void setRefreshing(boolean refreshing) {
        isRefreshing = refreshing;
    }

    private boolean isRefreshing = false;

    public class TopStreamerItemViewHolder extends BaseViewHolder {
        ListItemCardTopStreamersBinding mBinding;

        public TopStreamerItemViewHolder(ListItemCardTopStreamersBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            TopStreamerObject topStreamerObject = topStreamerObjects.get(position);
            mBinding.setTopStreamerObject(topStreamerObject);
            mBinding.setIsSelected(selectedStreamers.containsKey(topStreamerObject.getUser().getId()));
            mBinding.getRoot().setOnClickListener(v -> {
                if (selectedStreamers.containsKey(topStreamerObject.getUser().getId())) {
                    selectedStreamers.remove(topStreamerObject.getUser().getId());
                    mBinding.setIsSelected(false);
                } else {
                    selectedStreamers.put(topStreamerObject.getUser().getId(), topStreamerObject);
                    mBinding.setIsSelected(true);
                }
                if (itemSelectedListener != null) {
                    itemSelectedListener.onItemSelected(topStreamerObject);
                }
            });
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(TopStreamerObject topStreamerObject);
    }
}
