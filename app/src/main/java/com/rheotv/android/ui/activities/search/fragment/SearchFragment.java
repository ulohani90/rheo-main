package com.rheotv.android.ui.activities.search.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.objects.SearchSuggestionObject;
import com.rheotv.android.data.network.models.postlisting.responses.SearchItem;
import com.rheotv.android.data.network.models.postlisting.responses.SearchResponse;
import com.rheotv.android.databinding.SearchFragmentBinding;
import com.rheotv.android.ui.activities.gamify.RewardsTabAdapter;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.adapters.SearchSuggestionsAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.fragments.GamesFragment;
import com.rheotv.android.ui.fragments.PostSearchFragment;
import com.rheotv.android.ui.fragments.StreamerSearchFragment;
import com.rheotv.android.ui.fragments.TopSearchFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class SearchFragment extends BaseFragment<SearchFragmentBinding, SearchFragmentViewModel>
        implements SearchFragmentNavigator {
    SearchFragmentBinding searchFragmentBinding;

    LinearLayoutManager mLayoutManager;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private SearchFragmentViewModel mBlogViewModel;

    private long DELAY_SUGGESTIONS_CALL = 300;

    List<String> suggestions = new ArrayList<>();

    SearchSuggestionsAdapter autoCompleteSuggestionAdapter;

    public SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    private TopSearchFragment topSearchFragment;
    private GamesFragment gamesFragment;
    private StreamerSearchFragment streamerSearchFragment;
    private PostSearchFragment postSearchFragment;
    private HashMap<String, Object> properties = new HashMap<>();
    private String[] tabNames = new String[]{SegmentConstants.SCREEN_NAME_TOP_SEARCH, SegmentConstants.SCREEN_NAME_SEARCH_GAMES, SegmentConstants.SCREEN_NAME_SEARCH_STREAMER, SegmentConstants.SCREEN_NAME_SEARCH_POST};

    public static SearchFragment newInstance(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.search_fragment;
    }

    @Override
    public SearchFragmentViewModel getViewModel() {
        mBlogViewModel = ViewModelProviders.of(this, mViewModelFactory).get(SearchFragmentViewModel.class);
        return mBlogViewModel;
    }

    @Override
    public void handleError(Throwable throwable) {
        searchFragmentBinding.progressBar.setVisibility(View.GONE);
        searchFragmentBinding.containerLayout.setVisibility(View.GONE);
        searchFragmentBinding.offlineLayout.getRoot().setVisibility(View.VISIBLE);
    }

    @Override
    public void handleLogin() {

    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showReportPostSuccessToast() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBlogViewModel.setNavigator(this);
        properties = new HashMap<>();
        properties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_SEARCH);
        if (CommonUtils.isUserLoggedin())
            properties.put("userId", CommonUtils.getUserID(getContext()));
        if (getArguments() != null)
            properties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchFragmentBinding = getViewDataBinding();
        setUp();
        subscribeToLiveData();
        subscribeToSuggestionsLiveData();
    }

    private void setUp() {
        mBlogViewModel.fetchGamePage(0, "");
        autoCompleteSuggestionAdapter = new SearchSuggestionsAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line);
        //this.autoCompleteSuggestionAdapter.setNotifyOnChange(true);
        searchFragmentBinding.searchAutocomplete.setThreshold(1);
        searchFragmentBinding.searchAutocomplete.setAdapter(autoCompleteSuggestionAdapter);
        searchFragmentBinding.searchAutocomplete.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mBlogViewModel.fetchGamePage(0, searchFragmentBinding.searchAutocomplete.getText().toString());
                    CommonUtils.hideKeyboard(getActivity());
                    return true;
                }
                return false;
            }
        });
        searchFragmentBinding.searchAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SearchSuggestionObject suggestion = autoCompleteSuggestionAdapter.getItem(i);
                mBlogViewModel.recordSuggestionItemClick(getContext(), suggestion.getTitle());
                if (suggestion.getType() == AppConstants.SEARCH_ITEM_TYPE_STREAMER) {
                    openProfileActivity(suggestion.getTitle());
                } else if (suggestion.getType() == AppConstants.SEARCH_ITEM_TYPE_POST && suggestion.getPostId() != null) {
                    openPlayerActivity(suggestion.getPostId());
                } else {
                    mBlogViewModel.fetchGamePage(0, suggestion.getTitle());
                }
                searchFragmentBinding.searchAutocomplete.setText(autoCompleteSuggestionAdapter.getItem(i).getTitle());
                searchFragmentBinding.searchAutocomplete.setSelection(searchFragmentBinding.searchAutocomplete.getText().length());
                CommonUtils.hideKeyboard(getActivity());
            }
        });

        searchFragmentBinding.searchAutocomplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused) {
                    if (searchFragmentBinding.searchAutocomplete.getText() == null || searchFragmentBinding.searchAutocomplete.getText().toString().length() == 0) {
                        fetchSuggestionsData("");

                    }
                }
            }
        });

        searchFragmentBinding.searchAutocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence == null || charSequence.length() == 0) {

                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null && editable.toString().trim().length() > 0) {
                    if (searchApiHitHandler != null)
                        searchApiHitHandler.removeCallbacks(searchApiRunnable);
                    setUpSearchHandler();
                }
            }


        });

        String source = requireArguments().getString(AppConstants.SCREEN_SOURCE);

        RewardsTabAdapter tabAdapter = new RewardsTabAdapter(getChildFragmentManager());
        tabAdapter.addFragment(topSearchFragment = TopSearchFragment.getInstance(new ArrayList<>(), source), getString(R.string.top));
        tabAdapter.addFragment(gamesFragment = GamesFragment.getInstance(new ArrayList<>(), source), getString(R.string.games));
        tabAdapter.addFragment(streamerSearchFragment = StreamerSearchFragment.getInstance(new ArrayList<>(), source), getString(R.string.streamer));
        tabAdapter.addFragment(postSearchFragment = PostSearchFragment.getInstance(new ArrayList<>(), source), getString(R.string.post));
        searchFragmentBinding.viewpager.setOffscreenPageLimit(5);
        searchFragmentBinding.viewpager.setAdapter(tabAdapter);
        searchFragmentBinding.tabLayout.setupWithViewPager(searchFragmentBinding.viewpager);

        searchFragmentBinding.viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                HashMap<String, Object> pro = properties;
                pro.put("tab", tabNames[position]);
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_SEARCH_TAB_CHANGE, pro);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        searchFragmentBinding.offlineLayout.retryButton.setOnClickListener(view -> {
            searchFragmentBinding.progressBar.setVisibility(View.VISIBLE);
            searchFragmentBinding.offlineLayout.getRoot().setVisibility(View.GONE);
            searchFragmentBinding.containerLayout.setVisibility(View.GONE);
            mBlogViewModel.fetchGamePage(0, "");
        });

        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_SEARCH, properties);
        searchFragmentBinding.getRoot().post(() -> {
            searchFragmentBinding.searchAutocomplete.requestFocus();
        });
    }


    private void openProfileActivity(String title) {
        Intent intent = ProfileActivity.getCallingIntent(getActivity());
        intent.putExtra("author_name", title);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SEARCH);
        getActivity().startActivity(intent);
    }

    private void openPlayerActivity(String postId) {
        if (getActivity() == null) return;
        StreamPlayerActivity.Companion.startActivity(getActivity(),
                new StreamPlayerContainerFragment.Builder()
                        .addPost(postId)
                        .addSourceScreenName(SegmentConstants.SCREEN_NAME_SEARCH)
                        .buildExtras());
    }

    Handler searchApiHitHandler;

    public void setUpSearchHandler() {
        if (searchApiHitHandler == null) {
            searchApiHitHandler = new Handler();
        }
        searchApiHitHandler.postDelayed(searchApiRunnable, DELAY_SUGGESTIONS_CALL);
    }

    private boolean isInitialTextSearched = false;
    Runnable searchApiRunnable = new Runnable() {
        @Override
        public void run() {
            fetchSuggestionsData(searchFragmentBinding.searchAutocomplete.getText().toString());
            HashMap<String, Object> pro = new HashMap<>(properties);
            if (!isInitialTextSearched) {
                isInitialTextSearched = true;
                pro.put("textSearched", searchFragmentBinding.searchAutocomplete.getText().toString());
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_TEXT_SEARCHED, pro);
            }
        }
    };


    public void fetchSuggestionsData(String searchString) {
        mBlogViewModel.fetchSuggestions(searchString);
    }

    private void subscribeToLiveData() {
        mBlogViewModel.getsearchListLiveData().observe(getViewLifecycleOwner(), blogs -> {
            if (topSearchFragment != null && topSearchFragment.isAdded())
                topSearchFragment.updateContent(blogs);
            if (gamesFragment != null && gamesFragment.isAdded())
                gamesFragment.updateContent(getSearchItemForType(AppConstants.TYPE_TOP_GAMES, blogs));
            if (streamerSearchFragment != null && streamerSearchFragment.isAdded())
                streamerSearchFragment.updateContent(getSearchItemForType(AppConstants.TYPE_SEARCH_STREAMER, blogs));
            if (postSearchFragment != null && postSearchFragment.isAdded())
                postSearchFragment.updateContent(getSearchItemForType(AppConstants.TYPE_VIDEO_SNIPPETS, blogs));

            searchFragmentBinding.progressBar.setVisibility(View.GONE);
            searchFragmentBinding.offlineLayout.getRoot().setVisibility(View.GONE);
            searchFragmentBinding.containerLayout.setVisibility(View.VISIBLE);
//            if (blogs != null && blogs.size() > 0) {
//                mBlogViewModel.addBlogItemsToList(blogs);
//            } else {
//                searchFragmentBinding.searchResults.setVisibility(View.GONE);
//
//            }
        });
    }

    private void subscribeToSuggestionsLiveData() {
        mBlogViewModel.getSuggesstionsLiveData().observe(getViewLifecycleOwner(), suggestions -> {
            if (searchFragmentBinding.searchAutocomplete.getText().toString().trim().length() > 0)
                suggestions.add(0, new SearchSuggestionObject(searchFragmentBinding.searchAutocomplete.getText().toString(), null, AppConstants.SEARCH_ITEM_TYPE_POST, null));
            autoCompleteSuggestionAdapter.setData(suggestions);
            autoCompleteSuggestionAdapter.notifyDataSetChanged();
        });
    }

    private void addCurrentlySearchData(List<SearchSuggestionObject> suggestions) {

    }

    private List<SearchItem> getSearchItemForType(int type, List<SearchResponse> responses) {
        for (SearchResponse res : responses) {
            if (res.getItemType() == type)
                return res.getSearchItemsResponse().getSearchItems();
        }

        return new ArrayList<>();
    }

}
