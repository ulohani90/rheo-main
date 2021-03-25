package com.rheotv.android.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.objects.SearchSuggestionObject;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;


public class SearchSuggestionsAdapter extends ArrayAdapter<SearchSuggestionObject> implements Filterable {
    private List<SearchSuggestionObject> mlistData;
    private Context mContext;

    public SearchSuggestionsAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mlistData = new ArrayList<>();
        this.mContext = context;
    }

    public void setData(List<SearchSuggestionObject> list) {
        mlistData.clear();
        mlistData.addAll(list);
    }

    @Override
    public int getCount() {
        return mlistData.size();
    }

    @Nullable
    @Override
    public SearchSuggestionObject getItem(int position) {
        return mlistData.get(position);
    }

    /**
     * Used to Return the full object directly from adapter.
     *
     * @param position
     * @return
     */
    public SearchSuggestionObject getObject(int position) {
        return mlistData.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TrendingSearchItemHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.trending_search_item_layout, parent, false);
            holder = new TrendingSearchItemHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (TrendingSearchItemHolder) convertView.getTag();
        }
        holder.suggestionTextView.setText(mlistData.get(position).getTitle());
        if (mlistData.get(position).getType() == AppConstants.SEARCH_ITEM_TYPE_TRENDING) {
            holder.suggestionImageView.setImageResource(R.drawable.ic_trending_up_black_24dp);
        } else if (mlistData.get(position).getType() == AppConstants.SEARCH_ITEM_TYPE_RECENT_SEARCHES) {
            holder.suggestionImageView.setImageResource(R.drawable.ic_history_black_24dp);
        } else if (mlistData.get(position).getType() == AppConstants.SEARCH_ITEM_TYPE_STREAMER) {
            BindingUtils.setProfileImageUrlRounded(holder.suggestionImageView, mlistData.get(position).getImageUrl(), 24, 24);
        } else {
            holder.suggestionImageView.setImageResource(R.drawable.ic_search_grey_24_dp);
        }
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter dataFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    filterResults.values = mlistData;
                    filterResults.count = mlistData.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && (results.count > 0)) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return dataFilter;
    }

    public class TrendingSearchItemHolder {

        TextView suggestionTextView;
        ImageView suggestionImageView;

        public TrendingSearchItemHolder(View itemView) {
            suggestionTextView = (TextView) itemView.findViewById(R.id.suggestion_text);
            suggestionImageView = (ImageView) itemView.findViewById(R.id.suggestion_icon);
        }
    }
}