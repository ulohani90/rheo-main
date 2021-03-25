package com.rheotv.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.postlisting.responses.SearchItem;
import com.rheotv.android.databinding.ListItemTopGamesBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.CommonUtils;

import java.util.List;

public class TopGamesListAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<SearchItem> mTopGames;
    TopGamesListAdapter.OnTopGamesCardClick mListener;
    private boolean isInTopSearch = false;

    public TopGamesListAdapter(List<SearchItem> topGames) {
        this.mTopGames = topGames;
    }

    public TopGamesListAdapter(List<SearchItem> topGames, boolean isInTopSearch) {
        this.mTopGames = topGames;
        this.isInTopSearch = isInTopSearch;

    }

    public void setListener(TopGamesListAdapter.OnTopGamesCardClick mListener) {
        this.mListener = mListener;
    }

    public void submitItems(List<SearchItem> topGames) {
        this.mTopGames = topGames;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ListItemTopGamesBinding itemTopGamesLayoutBinding = ListItemTopGamesBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new TopGamesListAdapter.ItemTopGameCardViewHolder(itemTopGamesLayoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int position) {
        baseViewHolder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return mTopGames == null ? 0 : ((isInTopSearch && mTopGames.size() > 3) ? 3 : mTopGames.size());
    }

    public class ItemTopGameCardViewHolder extends BaseViewHolder {

        ListItemTopGamesBinding mBinding;

        public ItemTopGameCardViewHolder(ListItemTopGamesBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            SearchItem result = mTopGames.get(position);
            mBinding.setGame(result);
            BindingUtils.setImageUrlUsingCache(mBinding.gameThumbnailImageView, result.getUrl(), true);
            int count = 0;
            try {
                count = Integer.parseInt(result.getTotalViews());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            String formatValue = CommonUtils.formatValue(count);
            mBinding.totalViewsTextView.setText(CommonUtils.getPlural("View", count, formatValue));
            mBinding.getRoot().setOnClickListener(view -> mListener.onTopGameCardClick(result.getName(), result.getId()));
        }
    }

    public interface OnTopGamesCardClick {
        void onTopGameCardClick(String gameName, String gameId);
    }
}
