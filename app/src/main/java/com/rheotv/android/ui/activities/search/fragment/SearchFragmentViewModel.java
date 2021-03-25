package com.rheotv.android.ui.activities.search.fragment;


import android.content.Context;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.objects.SearchSuggestionObject;
import com.rheotv.android.data.network.models.postlisting.responses.SearchApiResponse;
import com.rheotv.android.data.network.models.postlisting.responses.SearchItem;
import com.rheotv.android.data.network.models.postlisting.responses.SearchResponse;
import com.rheotv.android.ui.activities.universalActivity.fragment.UniversalFragmentNavigator;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.LinkHandler;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragmentViewModel extends BaseViewModel<UniversalFragmentNavigator> {

    public final ObservableList<SearchResponse> searchApiResults = new ObservableArrayList<>();

    private final MutableLiveData<List<SearchResponse>> searchListLiveData;

    private int offset = 0;

    private final MutableLiveData<List<SearchSuggestionObject>> suggesstionsLiveData;

    public SearchFragmentViewModel(DataManager dataManager,
                                   SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        searchListLiveData = new MutableLiveData<>();
        suggesstionsLiveData = new MutableLiveData<>();
        offset = 0;
    }

    public void addBlogItemsToList(List<SearchResponse> blogs) {
        if (offset == 0) {
            searchApiResults.clear();
        }
        searchApiResults.addAll(blogs);
    }

    public void fetchGamePage(int offset, String searchKey) {
        setIsLoading(true);
        this.offset = offset;
//        ArrayList<SearchResponse> searchResponses = new ArrayList<>();
//
//        for (int i = 0; i < 50; i++) {
//            int itemType = 101;
//            if (i % 2 == 0) {
//                itemType = 101;
//            } else if (i % 3 == 0) {
//                itemType = 102;
//            } else if (i % 5 == 0) {
//                itemType = 103;
//            }
//            SearchResponse searchResponse = new SearchResponse();
//            searchResponse.setItemType(itemType);
//            SearchItemsResponse searchItemsResponse = new SearchItemsResponse();
//            searchItemsResponse.setTitle("Search Items");
//            ActionItem actionItem = new ActionItem();
//            actionItem.setActionTitle("Clear");
//            actionItem.setActionType("CLEAR");
//            ArrayList<SearchItem> searchItems = new ArrayList<>();
//            searchItemsResponse.setActionItem(actionItem);
//            for (int j = 0; j < 10; j++) {
//                SearchItem searchItem = new SearchItem();
//                searchItem.setIsLive(true);
//                searchItem.setViewCount("1k+");
//                searchItem.setTitle("Angry Birds");
//                searchItem.setSubtitle("Tasty feathers");
//                searchItem.setTag("GameMode");
//                searchItem.setTagBackgroundColor("#7600a9");
//                searchItem.setUrl("https://cdn1.iconfinder.com/data/icons/flat-world-currency-1/432/Flat_Currency_Bitcoin-512.png");
//                searchItems.add(searchItem);
//            }
//            searchItemsResponse.setSearchItems(searchItems);
//            searchResponse.setSearchItemsResponse(searchItemsResponse);
//            searchResponses.add(searchResponse);
//        }
//
//        searchListLiveData.setValue(searchResponses);

        getCompositeDisposable().add(getDataManager()
                .getSearchResponse(offset, searchKey)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(blogResponse -> {
                    if (blogResponse != null && blogResponse.getSearchResponse() != null) {
                        if (blogResponse.getSearchResponse().size() > 0) {
                            searchListLiveData.setValue(blogResponse.getSearchResponse());
                        }
                    }
                    setIsLoading(false);
                }, throwable -> {
                    setIsLoading(false);
                    getNavigator().handleError(throwable);
                }));
    }

    public MutableLiveData<List<SearchResponse>> getsearchListLiveData() {
        return searchListLiveData;
    }

    public MutableLiveData<List<SearchSuggestionObject>> getSuggesstionsLiveData() {
        return suggesstionsLiveData;
    }

    public ObservableList<SearchResponse> getBlogObservableList() {
        return searchApiResults;
    }

    public void fetchSuggestions(String query) {
       /* getDataManager()
                .getSuggestionsResponse(offset, query)
                .subscribeOn(getSchedulerProvider().io())
                .flatMapIterable(new Function<SearchApiResponse, List<SearchSuggestionObject>>() {
                    @Override
                    public List<SearchSuggestionObject> apply(SearchApiResponse searchApiResponse) throws Exception {
                        List<SearchSuggestionObject> suggestions = new ArrayList<>();
                        for (SearchResponse searchResponse : searchApiResponse.getSearchResponse()) {

                            for (SearchItem response : searchResponse.getSearchItemsResponse().getSearchItems()) {
                                suggestions.add(new SearchSuggestionObject(response.getTitle(), response.getUrl(), searchResponse.getItemType()));
                            }
                        }
                        return suggestions;
                    }
                }).toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(suggestions -> {
                    suggesstionsLiveData.setValue(suggestions);
                }, throwable -> {
                    getNavigator().showToast(throwable.getMessage());
                });*/


        getDataManager().getSuggestionsResponseCall(offset, query).enqueue(new Callback<SearchApiResponse>() {
            @Override
            public void onResponse(Call<SearchApiResponse> call, Response<SearchApiResponse> response) {
                List<SearchSuggestionObject> suggestions = new ArrayList<>();
                if (response != null && response.body() != null) {
                    for (SearchResponse searchResponse : response.body().getSearchResponse()) {
                        for (SearchItem searchItem : searchResponse.getSearchItemsResponse().getSearchItems()) {
                            suggestions.add(new SearchSuggestionObject(searchItem.getTitle(), searchItem.getUrl(), searchResponse.getItemType(), searchItem.getPermalink() != null ? LinkHandler.getPostId(searchItem.getPermalink()) : null));
                        }
                    }
                    suggesstionsLiveData.setValue(suggestions);
                } else {
                    //getNavigator().showToast("No response found");
                }
            }

            @Override
            public void onFailure(Call<SearchApiResponse> call, Throwable t) {
                //getNavigator().showToast(t.getMessage());
            }
        });

    }

    public void recordSuggestionItemClick(Context context, String query) {
        String username = CommonUtils.getUserName(context);
        getDataManager().postSearchQuery(query, username).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null && response.body() != null) {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
