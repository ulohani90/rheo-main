package com.rheotv.android.ui.activities.search.fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.postlisting.responses.SearchItem;
import com.rheotv.android.databinding.SearchItemAuthosViewBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;

import java.util.List;

public class ImageTextItemViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<SearchItem> searchItems;
    private SearchFragmentAdapter.SearchItemSnippetClickListener searchItemSnippetClickListener;

    public ImageTextItemViewAdapter(List<SearchItem> mPostList, SearchFragmentAdapter.SearchItemSnippetClickListener searchItemSnippetClickListener) {
        this.searchItems = mPostList;
        this.searchItemSnippetClickListener = searchItemSnippetClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return AppConstants.TYPE_HORIZONTAL_IMAGE_TEXT_ITEMS;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        SearchItemAuthosViewBinding searchItemAuthosViewBinding = SearchItemAuthosViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()),
                viewGroup, false);
        return new ImageTextItemViewAdapter.ImageTextItemViewHolder(searchItemAuthosViewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int i) {
        baseViewHolder.onBind(i);
    }

    @Override
    public int getItemCount() {
        return searchItems == null ? 0 : searchItems.size();
    }

    public class ImageTextItemViewHolder extends BaseViewHolder {

        private SearchItemAuthosViewBinding mBinding;

        public ImageTextItemViewHolder(SearchItemAuthosViewBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @Override
        public void onBind(int position) {
            SearchItem searchItem = searchItems.get(position);
            BindingUtils.setImageUrlUsingCache(mBinding.authorImage, searchItem.getUrl(), true);
            mBinding.authorTitle.setText(searchItem.getTitle());
            mBinding.subtitle.setText(searchItem.getSubtitle());
            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchItemSnippetClickListener.onItemClicked(searchItem, AppConstants.TYPE_HORIZONTAL_IMAGE_TEXT_ITEMS);
                }
            });
        }
    }
}
