package com.rheotv.android.ui.activities.player.activity;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.streamUpdates.StreamEvent;
import com.rheotv.android.databinding.ListItemRecentlyFollowedBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

public class StreamRecentFollowerAdapter extends RecyclerView.Adapter<BaseViewHolder>  {
    private List<StreamEvent> list = new ArrayList<>();
    private OnRecentFollowCallback callback;

    public StreamRecentFollowerAdapter(OnRecentFollowCallback callback) {
        this.callback = callback;
    }

    public void submitList(List<StreamEvent> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemRecentlyFollowedBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_item_recently_followed, parent, false);
        return new RecentFollowerHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class RecentFollowerHolder extends BaseViewHolder {
        ListItemRecentlyFollowedBinding binding;

        RecentFollowerHolder(ListItemRecentlyFollowedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            StreamEvent event = list.get(position);
            binding.setFollower(event);
            binding.getRoot().setOnClickListener(v -> {
                if (callback != null)
                    callback.onFollowerClick(event.getUsername(), event.getProfilePic());
            });
            binding.executePendingBindings();
        }
    }

    public interface OnRecentFollowCallback {
        void onFollowerClick(String username, String profilePic);
    }
}
