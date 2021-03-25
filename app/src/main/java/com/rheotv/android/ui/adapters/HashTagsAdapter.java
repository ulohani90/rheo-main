package com.rheotv.android.ui.adapters;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.databinding.ItemTagsViewBinding;
import com.rheotv.android.ui.activities.tabcontainer.posts.TagItemViewModel;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.List;

public class HashTagsAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<String> mPostList;

    private PostListAdapter.BlogAdapterListener mListener;

    private boolean isLoading = true;

    public HashTagsAdapter(List<String> mPostList) {
        this.mPostList = mPostList;
    }

    @Override
    public int getItemCount() {
        Log.d("TAGSLISTADAPTER", mPostList.size() + " size ki list");
        return mPostList.size();
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemTagsViewBinding tagsViewBinding = ItemTagsViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new TagsViewHolder(tagsViewBinding);
    }

    public void addTagItems(List<String> mPostList) {
        this.isLoading = mPostList != null && mPostList.size() > 0;
        this.mPostList.addAll(mPostList);
        notifyDataSetChanged();
    }

    public void clearItems() {
        mPostList.clear();
    }

    public class TagsViewHolder extends BaseViewHolder {

        private ItemTagsViewBinding mBinding;

        private TagItemViewModel mTagItemViewModel;

        public TagsViewHolder(ItemTagsViewBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            final String hashTag =  mPostList.get(position);
            Log.d("KKKK", "hashtag set : " + hashTag);
            mTagItemViewModel = new TagItemViewModel(hashTag);
            mBinding.setViewModel(mTagItemViewModel);

            // Immediate Binding
            // When a variable or observable changes, the binding will be scheduled to change before
            // the next frame. There are times, however, when binding must be executed immediately.
            // To force execution, use the executePendingBindings() method.
            mBinding.executePendingBindings();
        }
    }
}

