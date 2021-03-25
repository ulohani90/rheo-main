package com.rheotv.android.ui.activities.search.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.postlisting.responses.SearchItem;
import com.rheotv.android.databinding.SearchItemViewsBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;

import java.util.List;

public class SearchItemViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<SearchItem> searchItems;
    private SearchFragmentAdapter.SearchItemSnippetClickListener searchItemSnippetClickListener;

    boolean isTopPageAdapter;

    public void setSearchItemSnippetClickListener(SearchFragmentAdapter.SearchItemSnippetClickListener searchItemSnippetClickListener) {
        this.searchItemSnippetClickListener = searchItemSnippetClickListener;
    }

    public SearchItemViewAdapter(List<SearchItem> searchItems) {
        this.searchItems = searchItems;
    }

    public void submitItems(List<SearchItem> searchItems) {
        this.searchItems = searchItems;
        notifyDataSetChanged();
    }

    public SearchItemViewAdapter(List<SearchItem> mPostList, SearchFragmentAdapter.SearchItemSnippetClickListener searchItemSnippetClickListener) {
        this.searchItems = mPostList;
        this.searchItemSnippetClickListener = searchItemSnippetClickListener;
    }

    public SearchItemViewAdapter(List<SearchItem> mPostList, boolean isTopPageAdapter, SearchFragmentAdapter.SearchItemSnippetClickListener searchItemSnippetClickListener) {
        this.searchItems = mPostList;
        this.isTopPageAdapter = isTopPageAdapter;
        this.searchItemSnippetClickListener = searchItemSnippetClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return AppConstants.TYPE_VIDEO_SNIPPETS;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        SearchItemViewsBinding searchItemViewsBinding = SearchItemViewsBinding.inflate(LayoutInflater.from(viewGroup.getContext()),
                viewGroup, false);
        return new SearchItemViewAdapter.SearchItemViewViewHolder(searchItemViewsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int i) {
        baseViewHolder.onBind(i);
    }

    @Override
    public int getItemCount() {
        return searchItems == null ? 0 : ((isTopPageAdapter && searchItems.size() > 3) ? 3 : searchItems.size());
    }

    public class SearchItemViewViewHolder extends BaseViewHolder {

        private SearchItemViewsBinding mBinding;

        SearchItemViewViewHolder(SearchItemViewsBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @Override
        public void onBind(int position) {
            SearchItem searchItem = searchItems.get(position);
            BindingUtils.setImageUrlUsingCache(mBinding.vidThumbnail, searchItem.getUrl(), true);
            BindingUtils.setImageUrlUsingCache(mBinding.userProfilePic, searchItem.getUserProfilePic(), true);

            mBinding.title.setText(searchItem.getSubtitle());
            mBinding.subtitle.setText(searchItem.getTitle());
            mBinding.tag.setText(searchItem.getTag());
            if (searchItem.isLive()) {
                mBinding.liveTag.setVisibility(View.VISIBLE);
            } else {
                mBinding.liveTag.setVisibility(View.GONE);
            }
            mBinding.viewCount.setText(searchItem.getTotalViews());
            /*if (searchItem.getTagBackgroundColor() != null) {
                mBinding.tag.setBackgroundColor(Color.parseColor(searchItem.getTagBackgroundColor()));
            } else {
                mBinding.tag.setBackgroundColor(Color.parseColor("#7600a9"));
            }*/
            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchItemSnippetClickListener.onItemClicked(searchItem, AppConstants.TYPE_VIDEO_SNIPPETS);
                }
            });
        }
    }
}
