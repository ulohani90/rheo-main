package com.rheotv.android.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.BottomSheetListItemPlayerBinding;
import com.rheotv.android.ui.activities.player.activity.PlayerItemViewModel;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.BindingUtils;

import java.util.List;

public class PlayerListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<Result> mPostList;

    private PlayerAdapterListener mListener;

    private boolean isLoading = true;

    public PlayerListAdapter(List<Result> mPostList) {
        this.mPostList = mPostList;
    }

    public List<Result> getList() {
        return mPostList;
    }

    @Override
    public int getItemCount() {
        if (mPostList != null && mPostList.size() > 0) {
            Log.d("videoLISTADAPTER", mPostList.size() + " size ki list");
            return mPostList.size();
        } else {
            Log.d("videoLISTADAPTER", "1 size ki list");
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BottomSheetListItemPlayerBinding blogViewBinding = BottomSheetListItemPlayerBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new PlayerListViewHolder(blogViewBinding);
    }

    public void addItems(List<Result> mPostList) {
        this.isLoading = mPostList != null && mPostList.size() > 0;
        this.mPostList.addAll(mPostList);
        notifyDataSetChanged();
    }

    public void notifyPlayingItemChange(String id) {
        notifyDataSetChanged();
//        notifyItemChanged();
    }

    public void clearItems() {
        mPostList.clear();
    }

    public void setListener(PlayerAdapterListener listener) {
        this.mListener = listener;
    }

    public interface PlayerAdapterListener {
        void onItemClick(String id);
    }

    public class PlayerListViewHolder extends BaseViewHolder implements PlayerItemViewModel.PlayerItemViewModelListener {
        private BottomSheetListItemPlayerBinding mBinding;

        private PlayerItemViewModel mPlayerItemViewModel;

        public PlayerListViewHolder(BottomSheetListItemPlayerBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            Log.d("videoLISTADAPTER", " bind called");

            final Result result = mPostList.get(position);
            mPlayerItemViewModel = new PlayerItemViewModel(result, this);
            mBinding.setViewModel(mPlayerItemViewModel);
            BindingUtils.setProfileImageUrlFromCache(mBinding.profileIV, result.getThumbnail(), true);
            // Immediate Binding
            // When a variable or observable changes, the binding will be scheduled to change before
            // the next frame. There are times, however, when binding must be executed immediately.
            // To force execution, use the executePendingBindings() method.
            mBinding.executePendingBindings();
        }

        @Override
        public void onItemClick(String id) {
            mListener.onItemClick(id);
        }
    }

    private void getItemAtPositionForId(String id) {

    }

}
