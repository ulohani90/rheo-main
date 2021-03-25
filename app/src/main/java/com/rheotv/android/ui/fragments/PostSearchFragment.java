package com.rheotv.android.ui.fragments;


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
import com.rheotv.android.databinding.FragmentPostSearchBinding;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.search.fragment.SearchFragmentAdapter;
import com.rheotv.android.ui.activities.search.fragment.SearchItemViewAdapter;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.SearchItemLinearDecorator;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostSearchFragment extends Fragment implements SearchFragmentAdapter.SearchItemSnippetClickListener {
    private FragmentPostSearchBinding mBinding;
    private SearchItemViewAdapter adapter;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static PostSearchFragment getInstance(ArrayList<SearchItem> posts, String source) {
        PostSearchFragment fragment = new PostSearchFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("posts", posts);
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_post_search, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<SearchItem> posts = new ArrayList<>();
        if (getArguments() != null) {
            if (getArguments().containsKey("posts"))
                posts = getArguments().getParcelableArrayList("posts");
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        }

        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_SEARCH_POST);
        baseProperties.put("isTop", false);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_SEARCH_POST, baseProperties);

        adapter = new SearchItemViewAdapter(posts, this);
        int spacingInPixels = mBinding.rvPost.getContext().getResources().getDimensionPixelSize(R.dimen.margin_16);
        mBinding.rvPost.addItemDecoration(new SearchItemLinearDecorator(spacingInPixels));
        mBinding.rvPost.setAdapter(adapter);
    }

    public void updateContent(List<SearchItem> topGames) {
        adapter.submitItems(topGames);
        if (topGames.isEmpty()) {
            mBinding.errorText.setVisibility(View.VISIBLE);
            mBinding.rvPost.setVisibility(View.GONE);
        } else {
            mBinding.errorText.setVisibility(View.GONE);
            mBinding.rvPost.setVisibility(View.VISIBLE);
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
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_SEARCH_POST_CLICKED, properties);
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        if (getContext() == null) return;
        StreamPlayerActivity.Companion.startActivity(getContext(),
                new StreamPlayerContainerFragment.Builder()
                        .addPost(searchItem.toPostObject())
                        .addSourceScreenName(SegmentConstants.SCREEN_NAME_SEARCH_POST)
                        .buildExtras());

    }

    @Override
    public void onGameCardClicked(String gameName, String gameId) {

    }
}
