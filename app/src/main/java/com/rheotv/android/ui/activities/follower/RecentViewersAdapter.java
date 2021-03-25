package com.rheotv.android.ui.activities.follower;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.useProfile.responses.RecentViewer;
import com.rheotv.android.databinding.ItemRecentViewerLayoutBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.NumberUtils;

import java.util.List;

public class RecentViewersAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    public RecentViewersAdapter(OnItemClickedListener listener) {
        this.mListener = listener;
    }

    List<RecentViewer> results;

    OnItemClickedListener mListener;

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecentViewerLayoutBinding binding = ItemRecentViewerLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemRecentViewerViewHolder(binding);
    }

    public void setResults(List<RecentViewer> results) {
        this.results = results;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    public class ItemRecentViewerViewHolder extends BaseViewHolder {
        ItemRecentViewerLayoutBinding mBinding;

        public ItemRecentViewerViewHolder(ItemRecentViewerLayoutBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            RecentViewer recentViewer = results.get(position);
            BindingUtils.setImageUrlCircular(mBinding.authorID2, recentViewer.getProfilePic(), 54, 54);
            mBinding.nameL.setText(recentViewer.getUsername());
            mBinding.followerCountView.setText(NumberUtils.getFormattedCount(recentViewer.getFollowersCount()) + " Followers");
            mBinding.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onUserClicked(recentViewer.getUsername());
                    }
                }
            });

        }
    }

    public interface OnItemClickedListener {
        void onUserClicked(String username);
    }


}
