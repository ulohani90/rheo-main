package com.rheotv.android.ui.activities.search.fragment;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.postlisting.responses.SearchItem;
import com.rheotv.android.databinding.SearchTagItemBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;

import java.util.List;

public class HorizontalTagsAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<SearchItem> searchItems;
    private SearchFragmentAdapter.SearchItemSnippetClickListener searchItemSnippetClickListener;

    public HorizontalTagsAdapter(List<SearchItem> mPostList, SearchFragmentAdapter.SearchItemSnippetClickListener searchItemSnippetClickListener) {
        this.searchItems = mPostList;
        this.searchItemSnippetClickListener = searchItemSnippetClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return AppConstants.TYPE_HORIZONTAL_TAGS;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        SearchTagItemBinding searchTagItemBinding = SearchTagItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()),
                viewGroup, false);
        return new HorizontalTagsAdapter.HorizontalTagsViewHolder(searchTagItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int i) {
        baseViewHolder.onBind(i);
    }

    @Override
    public int getItemCount() {
        return searchItems == null ? 0 : searchItems.size();
    }

    public class HorizontalTagsViewHolder extends BaseViewHolder {

        private SearchTagItemBinding mBinding;

        public HorizontalTagsViewHolder(SearchTagItemBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @Override
        public void onBind(int position) {
            SearchItem searchItem = searchItems.get(position);
            mBinding.text.setText(searchItem.getTitle());

            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(10);
            shape.setColor(Color.parseColor(searchItem.getTagBackgroundColor()));
            mBinding.text.setBackground(shape);
            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchItemSnippetClickListener.onItemClicked(searchItem, AppConstants.TYPE_HORIZONTAL_TAGS);
                }
            });
        }
    }
}
