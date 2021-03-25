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
import com.rheotv.android.databinding.FragmentGamesBinding;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.TopGamesListAdapter;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.SearchItemLinearDecorator;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GamesFragment extends Fragment implements TopGamesListAdapter.OnTopGamesCardClick {
    private FragmentGamesBinding mBinding;
    private TopGamesListAdapter adapter;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static GamesFragment getInstance(ArrayList<SearchItem> topGames, String source) {
        GamesFragment fragment = new GamesFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("games", topGames);
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_games, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<SearchItem> topGames = new ArrayList<>();
        if (getArguments() != null && getArguments().containsKey("games")) {
            topGames = getArguments().getParcelableArrayList("games");
        }
        adapter = new TopGamesListAdapter(topGames);
        adapter.setListener(this);
        int spacingInPixels = mBinding.rvTopGames.getContext().getResources().getDimensionPixelSize(R.dimen.margin_16);
        mBinding.rvTopGames.addItemDecoration(new SearchItemLinearDecorator(spacingInPixels));
        mBinding.rvTopGames.setAdapter(adapter);

        if (getArguments() != null)
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_SEARCH_GAMES);
        baseProperties.put("isTop", false);

        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_SEARCH_GAMES, baseProperties);
    }

    @Override
    public void onTopGameCardClick(String gameName, String gameId) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("game", gameName);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_GAME_TAG_CLICKED, properties);
        Intent intent = new Intent(getActivity(), UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, gameName);
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, gameId);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SEARCH_GAMES);
        startActivity(intent);
    }

    public void updateContent(List<SearchItem> topGames) {
        adapter.submitItems(topGames);
        if (topGames.isEmpty()) {
            mBinding.errorText.setVisibility(View.VISIBLE);
            mBinding.rvTopGames.setVisibility(View.GONE);
        } else {
            mBinding.errorText.setVisibility(View.GONE);
            mBinding.rvTopGames.setVisibility(View.VISIBLE);
        }
    }
}
