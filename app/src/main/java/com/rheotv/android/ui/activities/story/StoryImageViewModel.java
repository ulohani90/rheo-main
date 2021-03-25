package com.rheotv.android.ui.activities.story;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.objects.SearchSuggestionObject;
import com.rheotv.android.data.network.models.postlisting.responses.SearchApiResponse;
import com.rheotv.android.data.network.models.postlisting.responses.SearchItem;
import com.rheotv.android.data.network.models.postlisting.responses.SearchResponse;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.story.Constants;
import com.rheotv.story.model.Story;
import com.rheotv.story.model.StoryCTA;
import com.rheotv.story.model.StoryCTAData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoryImageViewModel extends BaseViewModel {
    public ObservableField<Story> story = new ObservableField<>();
    public final MutableLiveData<List<SearchSuggestionObject>> suggestionsLiveData;
    private String suggestionType = "user_tag";
    public boolean isInterested = false;

    public StoryImageViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        suggestionsLiveData = new MutableLiveData<>();
    }

    public void fetchSuggestions(String query) {
        getDataManager().getSuggestionsResponseCallWithType(query, suggestionType).enqueue(new Callback<SearchApiResponse>() {
            @Override
            public void onResponse(Call<SearchApiResponse> call, Response<SearchApiResponse> response) {
                List<SearchSuggestionObject> suggestions = new ArrayList<>();
                if (response != null && response.body() != null) {
                    for (SearchResponse searchResponse : response.body().getSearchResponse()) {
                        for (SearchItem searchItem : searchResponse.getSearchItemsResponse().getSearchItems()) {
                            suggestions.add(new SearchSuggestionObject(searchItem.getTitle(), searchItem.getUrl(), searchResponse.getItemType(), searchItem.getProfileId()));
                        }
                    }
                    suggestionsLiveData.setValue(suggestions);
                }
            }

            @Override
            public void onFailure(Call<SearchApiResponse> call, Throwable t) {

            }
        });
    }

    public void addMentionCTA(SearchSuggestionObject suggestionObject) {
        try {
            removeMentionCTA();

            Story story = this.story.get();
            if (story == null) return;

            ArrayList<StoryCTA> ctas = story.getStoryCTAS();
            if (ctas == null)
                ctas = new ArrayList<>();

            ctas.add(new StoryCTA(Constants.MENTION_CTA, new StoryCTAData(suggestionObject.getPostId())));
            story.setStoryCTAS(ctas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeMentionCTA() {
        removeCTA(Constants.MENTION_CTA);
    }

    public void addInterestedCTA() {
        try {
            removeInterestedCTA();

            Story story = this.story.get();
            if (story == null) return;

            ArrayList<StoryCTA> ctas = story.getStoryCTAS();
            if (ctas == null)
                ctas = new ArrayList<>();

            ctas.add(new StoryCTA(Constants.PLAY_REQUEST_INTERESTED_CTA, new StoryCTAData()));
            story.setStoryCTAS(ctas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeInterestedCTA() {
        removeCTA(Constants.PLAY_REQUEST_INTERESTED_CTA);
    }

    private void removeCTA(String ctaType) {
        try {
            Story story = this.story.get();
            if (story == null) return;

            ArrayList<StoryCTA> ctas = story.getStoryCTAS();
            if (ctas == null)
                ctas = new ArrayList<>();

            for (StoryCTA cta : ctas) {
                if (cta.getCtaType().equalsIgnoreCase(ctaType)) {
                    ctas.remove(cta);
                    story.setStoryCTAS(ctas);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StoryCTAData getMentionCTA() {
        StoryCTA cta = getCTA(Constants.MENTION_CTA);
        if (cta == null) return null;
        return cta.getStoryCTAData();
    }

    public String getInterestedCount() {
        StoryCTA cta = getCTA(Constants.PLAY_REQUEST_INTERESTED_CTA);
        if (cta == null) return null;
        StoryCTAData data = cta.getStoryCTAData();
        if (data != null) {
            int count = data.getInterestedCount();
            if (count == 0)
                return "0 Love";
            return CommonUtils.getPlural("Love", count, CommonUtils.formatValue(count));
        }

        return null;
    }

    public StoryCTA getCTA(String ctaType) {
        Story story = this.story.get();
        if (story == null) return null;

        ArrayList<StoryCTA> ctas = story.getStoryCTAS();
        if (ctas == null)
            ctas = new ArrayList<>();

        for (StoryCTA cta : ctas) {
            if (cta.getCtaType().equalsIgnoreCase(ctaType)) {
                return cta;
            }
        }

        return null;
    }
    public boolean shouldShowLoveCount() {
        return story.get().getState() != null && story.get().getState().equals(Constants.PUBLISHED);
    }
}
