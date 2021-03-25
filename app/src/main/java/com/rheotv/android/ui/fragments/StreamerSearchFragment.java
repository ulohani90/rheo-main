package com.rheotv.android.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.SearchItem;
import com.rheotv.android.data.network.models.postlisting.responses.SearchResponse;
import com.rheotv.android.databinding.FragmentStreamerSearchBinding;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.search.fragment.SearchFragmentAdapter;
import com.rheotv.android.ui.adapters.SearchStreamersAdapter;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.SearchItemLinearDecorator;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class StreamerSearchFragment extends Fragment implements SearchFragmentAdapter.SearchItemSnippetClickListener {
    private SearchStreamersAdapter adapter;
    private FragmentStreamerSearchBinding mBinding;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static StreamerSearchFragment getInstance(ArrayList<SearchItem> streamers, String source) {
        StreamerSearchFragment fragment = new StreamerSearchFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("streamers", streamers);
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_streamer_search, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<SearchItem> streamers = new ArrayList<>();
        if (getArguments() != null) {
            if (getArguments().containsKey("streamers"))
                streamers = getArguments().getParcelableArrayList("streamers");
            if (getArguments().containsKey(AppConstants.SCREEN_SOURCE))
                baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        }

        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_SEARCH_STREAMER);
        baseProperties.put("isTop", false);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_SEARCH_STREAMER, baseProperties);

        adapter = new SearchStreamersAdapter(streamers);
        adapter.setListener(this);
        int spacingInPixels = mBinding.rvStreamer.getContext().getResources().getDimensionPixelSize(R.dimen.margin_16);
        mBinding.rvStreamer.addItemDecoration(new SearchItemLinearDecorator(spacingInPixels));
        mBinding.rvStreamer.setAdapter(adapter);
    }

    public void updateContent(List<SearchItem> topGames) {
        adapter.submitItems(topGames);
        if (topGames.isEmpty()) {
            mBinding.errorText.setVisibility(View.VISIBLE);
            mBinding.rvStreamer.setVisibility(View.GONE);
        } else {
            mBinding.errorText.setVisibility(View.GONE);
            mBinding.rvStreamer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void headerActionClicked(SearchResponse searchResponse, String actionType, int position) {

    }

    @Override
    public void onItemClicked(SearchItem searchItem, int type) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("name", searchItem.getName());
        properties.put("id", searchItem.getId());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_SEARCH_STREAMER_CLICKED, properties);
        String userName = searchItem.getTitle();
        Intent intent = ProfileActivity.getCallingIntent(getContext());
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SEARCH_STREAMER);
        intent.putExtra("author_name", userName);
        startActivity(intent);
    }

    @Override
    public void onGameCardClicked(String gameName, String gameId) {

    }
}
