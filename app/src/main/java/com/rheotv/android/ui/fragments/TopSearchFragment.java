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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.SearchItem;
import com.rheotv.android.data.network.models.postlisting.responses.SearchResponse;
import com.rheotv.android.databinding.FragmentTopSearchBinding;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.search.fragment.SearchFragmentAdapter;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.SearchItemDecorator;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopSearchFragment extends Fragment implements SearchFragmentAdapter.SearchItemSnippetClickListener {

    private SearchFragmentAdapter searchFragmentAdapter;
    private FragmentTopSearchBinding mBinding;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static TopSearchFragment getInstance(ArrayList<SearchResponse> topSearch, String source) {
        TopSearchFragment fragment = new TopSearchFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("topSearch", topSearch);
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_top_search, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<SearchResponse> topSearch = new ArrayList<>();
        if (getArguments() != null && getArguments().containsKey("topSearch")) {
            topSearch = getArguments().getParcelableArrayList("topSearch");
        }

        if (getArguments() != null)
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_TOP_SEARCH);
        baseProperties.put("isTop", true);

        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_TOP_SEARCH, baseProperties);

        searchFragmentAdapter = new SearchFragmentAdapter(topSearch, this);
        LinearLayoutManager mLayoutManager = (LinearLayoutManager) mBinding.rvTopSearch.getLayoutManager();
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.rvTopSearch.setLayoutManager(mLayoutManager);
        mBinding.rvTopSearch.setItemAnimator(new DefaultItemAnimator());
        mBinding.rvTopSearch.setAdapter(searchFragmentAdapter);
        mBinding.rvTopSearch.addItemDecoration(new SearchItemDecorator(getActivity().getResources().getDimensionPixelOffset(R.dimen.margin_16)));
    }

    public void updateContent(List<SearchResponse> topSearch) {
        searchFragmentAdapter.clearItems();
        searchFragmentAdapter.addItems(topSearch);
    }

    @Override
    public void headerActionClicked(SearchResponse searchResponse, String actionType, int position) {

    }

    @Override
    public void onItemClicked(SearchItem searchItem, int type) {
        switch (type) {
            case AppConstants.TYPE_VIDEO_SNIPPETS:
                handleVideoSnippetClicked(searchItem);
                break;
            case AppConstants.TYPE_HORIZONTAL_IMAGE_TEXT_ITEMS:
                handleImageTextItemClicked(searchItem);
                break;
        }
    }

    @Override
    public void onGameCardClicked(String gameName, String gameId) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("game", gameName);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_GAME_TAG_CLICKED, properties);
        Intent intent = new Intent(getActivity(), UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, gameName);
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, gameId);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TOP_SEARCH);
        startActivity(intent);
    }

    private void handleImageTextItemClicked(SearchItem searchItem) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("name", searchItem.getName());
        properties.put("id", searchItem.getId());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_SEARCH_STREAMER_CLICKED, properties);
        String userName = searchItem.getTitle();
        Intent intent = ProfileActivity.getCallingIntent(getContext());
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TOP_SEARCH);
        intent.putExtra("author_name", userName);
        startActivity(intent);
    }

    private void handleVideoSnippetClicked(SearchItem searchItem) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("name", searchItem.getName());
        properties.put("id", searchItem.getId());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_SEARCH_POST_CLICKED, properties);
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        if (getContext() != null) {
            StreamPlayerActivity.Companion.startActivity(getContext(),
                    new StreamPlayerContainerFragment.Builder()
                            .addPost(searchItem.toPostObject())
                            .addSourceScreenName(SegmentConstants.SCREEN_NAME_SEARCH_POST)
                            .buildExtras());
        }
    }
}
