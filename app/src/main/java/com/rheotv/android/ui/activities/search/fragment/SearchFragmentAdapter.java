package com.rheotv.android.ui.activities.search.fragment;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.SearchItem;
import com.rheotv.android.data.network.models.postlisting.responses.SearchItemsResponse;
import com.rheotv.android.data.network.models.postlisting.responses.SearchResponse;
import com.rheotv.android.databinding.ItemPostEmptyBinding;
import com.rheotv.android.databinding.ItemPostTopGamesLayoutBinding;
import com.rheotv.android.databinding.SearchItemTypeRectTagsBinding;
import com.rheotv.android.ui.adapters.SearchStreamersAdapter;
import com.rheotv.android.ui.adapters.TopGamesListAdapter;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.SearchItemDecorator;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.HashMap;
import java.util.List;

public class SearchFragmentAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<SearchResponse> searchResponseList;

    private boolean isLoading = true;

    private SearchItemSnippetClickListener searchItemSnippetClickListener;

    public SearchFragmentAdapter(List<SearchResponse> mPostList) {
        this.searchResponseList = mPostList;
    }

    public SearchFragmentAdapter(List<SearchResponse> mPostList, SearchItemSnippetClickListener searchItemSnippetClickListener) {
        this.searchResponseList = mPostList;
        this.searchItemSnippetClickListener = searchItemSnippetClickListener;
    }

    @Override
    public int getItemCount() {
        if (searchResponseList != null && searchResponseList.size() > 0) {
            Log.d("POSTLISTADAPTER", searchResponseList.size() + " size ki list");
            return searchResponseList.size();
        } else {
            return 0;
        }
    }

    public void addItems(List<SearchResponse> searchResponses) {
        this.isLoading = searchResponses != null && searchResponses.size() > 0;
        this.searchResponseList.addAll(searchResponses);
        notifyDataSetChanged();
    }

    public void clearItems() {
        searchResponseList.clear();
    }

    @Override
    public int getItemViewType(int position) {
        if (searchResponseList != null && !searchResponseList.isEmpty()) {
            if (searchResponseList.get(position).getItemType() == AppConstants.TYPE_HORIZONTAL_TAGS)
                return AppConstants.TYPE_HORIZONTAL_TAGS;
            else if (searchResponseList.get(position).getItemType() == (AppConstants.TYPE_HORIZONTAL_IMAGE_TEXT_ITEMS)) {
                return AppConstants.TYPE_HORIZONTAL_IMAGE_TEXT_ITEMS;
            } else if (searchResponseList.get(position).getItemType() == (AppConstants.TYPE_VIDEO_SNIPPETS)) {
                return AppConstants.TYPE_VIDEO_SNIPPETS;
            } else if (searchResponseList.get(position).getItemType() == (AppConstants.TYPE_TOP_GAMES)) {
                return AppConstants.TYPE_TOP_GAMES;
            } else {
                return AppConstants.VIEW_TYPE_NORMAL;
            }
        } else {
            return AppConstants.VIEW_TYPE_EMPTY;
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SearchItemTypeRectTagsBinding searchItemTypeRectTagsBinding = SearchItemTypeRectTagsBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        switch (viewType) {
            case AppConstants.TYPE_HORIZONTAL_TAGS:
                return new SearchFragmentAdapter.HorizontalTagsViewHolder(searchItemTypeRectTagsBinding, searchItemSnippetClickListener);

            case AppConstants.TYPE_HORIZONTAL_IMAGE_TEXT_ITEMS:
                return new SearchFragmentAdapter.HorizontalImageTextsViewHolder(searchItemTypeRectTagsBinding, searchItemSnippetClickListener);

            case AppConstants.TYPE_VIDEO_SNIPPETS:
                return new SearchFragmentAdapter.SearchItemViewsViewHolder(searchItemTypeRectTagsBinding, searchItemSnippetClickListener);

            case AppConstants.TYPE_TOP_GAMES:
                ItemPostTopGamesLayoutBinding binding = ItemPostTopGamesLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new TopGamesViewHolder(binding);

            default:
                ItemPostEmptyBinding emptyBinding = ItemPostEmptyBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new SearchFragmentAdapter.EmptyViewHolder(emptyBinding);
        }
    }

    public void removeItemFromList(int position) {
        searchResponseList.remove(position);
    }

    public class HorizontalTagsViewHolder extends BaseViewHolder {
        SearchItemTypeRectTagsBinding searchItemTypeRectTagsBinding;
        SearchItemSnippetClickListener searchItemSnippetClickListener;

        public HorizontalTagsViewHolder(SearchItemTypeRectTagsBinding searchItemTypeRectTagsBinding, SearchItemSnippetClickListener searchItemSnippetClickListener) {
            super(searchItemTypeRectTagsBinding.getRoot());
            this.searchItemTypeRectTagsBinding = searchItemTypeRectTagsBinding;
            this.searchItemSnippetClickListener = searchItemSnippetClickListener;
        }

        @Override
        public void onBind(int position) {
            SearchResponse searchResponse = searchResponseList.get(position);
            SearchItemsResponse searchItemsResponse = searchResponse.getSearchItemsResponse();
            if (searchItemsResponse.getTitle() != null) {
                searchItemTypeRectTagsBinding.title.setVisibility(View.VISIBLE);
                searchItemTypeRectTagsBinding.title.setText(searchItemsResponse.getTitle());
            } else {
                searchItemTypeRectTagsBinding.title.setVisibility(View.GONE);
            }

            if (searchItemsResponse.getActionItem() != null) {
                searchItemTypeRectTagsBinding.action.setVisibility(View.VISIBLE);
                searchItemTypeRectTagsBinding.action.setText(searchItemsResponse.getActionItem().getActionTitle());
                searchItemTypeRectTagsBinding.action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchItemSnippetClickListener.headerActionClicked(searchResponse, searchItemsResponse.getActionItem().getActionType(), getAdapterPosition());
                    }
                });
            } else {
                searchItemTypeRectTagsBinding.action.setVisibility(View.GONE);
            }

            if (searchItemsResponse.getSearchItems() != null && searchItemsResponse.getSearchItems().size() > 0) {
                searchItemTypeRectTagsBinding.tagContainer.setVisibility(View.VISIBLE);
                searchItemTypeRectTagsBinding.errorView.setVisibility(View.GONE);
                HorizontalTagsAdapter horizontalTagsAdapter = new HorizontalTagsAdapter(searchItemsResponse.getSearchItems(), searchItemSnippetClickListener);
                FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(searchItemTypeRectTagsBinding.getRoot().getContext());
                flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
                flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START);
                searchItemTypeRectTagsBinding.tagContainer.setLayoutManager(flexboxLayoutManager);
                searchItemTypeRectTagsBinding.tagContainer.setAdapter(horizontalTagsAdapter);
            } else {
                searchItemTypeRectTagsBinding.tagContainer.setVisibility(View.GONE);
                searchItemTypeRectTagsBinding.errorView.setVisibility(View.VISIBLE);
            }
        }
    }

    public class SearchItemViewsViewHolder extends BaseViewHolder {
        SearchItemTypeRectTagsBinding searchItemTypeRectTagsBinding;
        SearchItemSnippetClickListener searchItemSnippetClickListener;
        boolean isItemDecorationAdded = false;

        public SearchItemViewsViewHolder(SearchItemTypeRectTagsBinding searchItemTypeRectTagsBinding, SearchItemSnippetClickListener searchItemSnippetClickListener) {
            super(searchItemTypeRectTagsBinding.getRoot());
            this.searchItemTypeRectTagsBinding = searchItemTypeRectTagsBinding;
            this.searchItemSnippetClickListener = searchItemSnippetClickListener;
        }

        @Override
        public void onBind(int position) {
            SearchResponse searchResponse = searchResponseList.get(position);
            SearchItemsResponse searchItemsResponse = searchResponse.getSearchItemsResponse();
            if (searchItemsResponse.getTitle() != null) {
                searchItemTypeRectTagsBinding.title.setVisibility(View.VISIBLE);
                searchItemTypeRectTagsBinding.title.setText(searchItemsResponse.getTitle());
            } else {
                searchItemTypeRectTagsBinding.title.setVisibility(View.GONE);
            }

            if (searchItemsResponse.getActionItem() != null) {
                searchItemTypeRectTagsBinding.action.setVisibility(View.VISIBLE);
                searchItemTypeRectTagsBinding.action.setText(searchItemsResponse.getActionItem().getActionTitle());
                searchItemTypeRectTagsBinding.action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchItemSnippetClickListener.headerActionClicked(searchResponse, searchItemsResponse.getActionItem().getActionType(), getAdapterPosition());
                    }
                });
            } else {
                searchItemTypeRectTagsBinding.action.setVisibility(View.GONE);
            }
            if (searchItemsResponse.getSearchItems() != null && searchItemsResponse.getSearchItems().size() > 0) {
                searchItemTypeRectTagsBinding.tagContainer.setVisibility(View.VISIBLE);
                searchItemTypeRectTagsBinding.errorView.setVisibility(View.GONE);
                int spacingInPixels = searchItemTypeRectTagsBinding.tagContainer.getContext().getResources().getDimensionPixelSize(R.dimen.margin_12);
                if (!isItemDecorationAdded) {
                    isItemDecorationAdded = true;
                    searchItemTypeRectTagsBinding.tagContainer.addItemDecoration(new SearchItemDecorator(spacingInPixels));
                }
                SearchItemViewAdapter adapter = new SearchItemViewAdapter(searchItemsResponse.getSearchItems(), true, searchItemSnippetClickListener);
                LinearLayoutManager manager = new LinearLayoutManager(searchItemTypeRectTagsBinding.getRoot().getContext());
                searchItemTypeRectTagsBinding.tagContainer.setLayoutManager(manager);
                searchItemTypeRectTagsBinding.tagContainer.setAdapter(adapter);
            } else {
                searchItemTypeRectTagsBinding.tagContainer.setVisibility(View.GONE);
                searchItemTypeRectTagsBinding.errorView.setVisibility(View.VISIBLE);
            }
        }
    }

    public class HorizontalImageTextsViewHolder extends BaseViewHolder {
        SearchItemTypeRectTagsBinding searchItemTypeRectTagsBinding;
        SearchItemSnippetClickListener searchItemSnippetClickListener;

        private boolean isItemDecorationAdded;

        public HorizontalImageTextsViewHolder(SearchItemTypeRectTagsBinding searchItemTypeRectTagsBinding, SearchItemSnippetClickListener searchItemSnippetClickListener) {
            super(searchItemTypeRectTagsBinding.getRoot());
            this.searchItemTypeRectTagsBinding = searchItemTypeRectTagsBinding;
            this.searchItemSnippetClickListener = searchItemSnippetClickListener;
        }

        @Override
        public void onBind(int position) {
            SearchResponse searchResponse = searchResponseList.get(position);
            SearchItemsResponse searchItemsResponse = searchResponse.getSearchItemsResponse();
            if (searchItemsResponse.getTitle() != null) {
                searchItemTypeRectTagsBinding.title.setVisibility(View.VISIBLE);
                searchItemTypeRectTagsBinding.title.setText(searchItemsResponse.getTitle());
            } else {
                searchItemTypeRectTagsBinding.title.setVisibility(View.GONE);
            }

            if (searchItemsResponse.getActionItem() != null) {
                searchItemTypeRectTagsBinding.action.setVisibility(View.VISIBLE);
                searchItemTypeRectTagsBinding.action.setText(searchItemsResponse.getActionItem().getActionTitle());
                searchItemTypeRectTagsBinding.action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchItemSnippetClickListener.headerActionClicked(searchResponse, searchItemsResponse.getActionItem().getActionType(), getAdapterPosition());
                    }
                });
            } else {
                searchItemTypeRectTagsBinding.action.setVisibility(View.GONE);
            }

            if (searchItemsResponse.getSearchItems() != null && searchItemsResponse.getSearchItems().size() > 0) {
                searchItemTypeRectTagsBinding.tagContainer.setVisibility(View.VISIBLE);
                searchItemTypeRectTagsBinding.errorView.setVisibility(View.GONE);

                SearchStreamersAdapter imageTextItemAdapter = new SearchStreamersAdapter(searchItemsResponse.getSearchItems(), searchItemSnippetClickListener, true);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(searchItemTypeRectTagsBinding.getRoot().getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

                int spacingInPixels = searchItemTypeRectTagsBinding.tagContainer.getContext().getResources().getDimensionPixelSize(R.dimen.margin_12);
                if (!isItemDecorationAdded) {
                    isItemDecorationAdded = true;
                    searchItemTypeRectTagsBinding.tagContainer.addItemDecoration(new SearchItemDecorator(spacingInPixels));
                }

                searchItemTypeRectTagsBinding.tagContainer.setLayoutManager(linearLayoutManager);
                searchItemTypeRectTagsBinding.tagContainer.setAdapter(imageTextItemAdapter);

            } else {
                searchItemTypeRectTagsBinding.tagContainer.setVisibility(View.GONE);
                searchItemTypeRectTagsBinding.errorView.setVisibility(View.VISIBLE);
            }
        }
    }

    public class EmptyViewHolder extends BaseViewHolder {
        ItemPostEmptyBinding emptyBinding;

        public EmptyViewHolder(ItemPostEmptyBinding emptyBinding) {
            super(emptyBinding.getRoot());
        }

        @Override
        public void onBind(int position) {

        }
    }

    public class TopGamesViewHolder extends BaseViewHolder implements TopGamesListAdapter.OnTopGamesCardClick {

        ItemPostTopGamesLayoutBinding mBinding;

        private boolean isGameRVItemDecorationAdded;

        public TopGamesViewHolder(ItemPostTopGamesLayoutBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            SearchResponse result = searchResponseList.get(position);
            mBinding.title.setVisibility(View.GONE);
            mBinding.title1.setVisibility(View.VISIBLE);
            mBinding.title1.setText(result.getSearchItemsResponse().getTitle());

            LinearLayoutManager layoutManager = new LinearLayoutManager(mBinding.topGamesRv.getContext(), LinearLayoutManager.VERTICAL, false);
            mBinding.topGamesRv.setLayoutManager(layoutManager);

            int spacingInPixels = mBinding.topGamesRv.getContext().getResources().getDimensionPixelSize(R.dimen.margin_12);
            if (!isGameRVItemDecorationAdded) {
                isGameRVItemDecorationAdded = true;
                mBinding.topGamesRv.addItemDecoration(new SearchItemDecorator(spacingInPixels));
            }
            TopGamesListAdapter adapter = new TopGamesListAdapter(result.getSearchItemsResponse().getSearchItems(), true);
            adapter.setListener(this);
            mBinding.topGamesRv.setAdapter(adapter);
        }

        @Override
        public void onTopGameCardClick(String gameName, String gameId) {
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("game", gameName);
            SegmentTracker.getInstance(mBinding.topGamesRv.getContext()).trackEvent(SegmentConstants.EVENT_SEARCH_TOP_GAME_CLICK, properties);
            searchItemSnippetClickListener.onGameCardClicked(gameName, gameId);
        }
    }

    public interface SearchItemSnippetClickListener {
        void headerActionClicked(SearchResponse searchResponse, String actionType, int position);

        void onItemClicked(SearchItem seachItem, int type);

        void onGameCardClicked(String gameName, String gameId);
    }
}