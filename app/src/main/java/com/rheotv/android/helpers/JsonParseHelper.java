package com.rheotv.android.helpers;

import android.util.Log;

import com.google.gson.Gson;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.districtlisting.DistrictResult;
import com.rheotv.android.data.network.models.postlisting.responses.HomeResult;
import com.rheotv.android.data.network.models.postlisting.responses.PostListingResponse;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.postlisting.responses.TrendingPostResponse;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.utils.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JsonParseHelper {
    private Gson gson = new Gson();
    private List<DistrictResult> districtResults = new ArrayList<>();
    private String recentParentName = "";
    private List<Result> postList = new ArrayList<>(), mockList = new ArrayList<>();
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    @Inject
    public JsonParseHelper() {
    }

    public List<DistrictResult> parseRegions(String stringPreference) {
        PostListingResponse postListingResponse = gson.fromJson(stringPreference, PostListingResponse.class);
        if(postListingResponse == null) {
            return districtResults;
        }
        districtResults = postListingResponse.getRegions();
        return districtResults;
    }

    public DistrictResult getSavedDistrict() {
        String json = new SharedPrefsUtils().getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.SAVED_DISTRICT_ID);
        return gson.fromJson(json, DistrictResult.class);
    }

    public String flatOutObject(Object o) {
        return gson.toJson(o);
    }

    public void saveTrendingResponseJson(TrendingPostResponse postListingResponse) {
        new SharedPrefsUtils().setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.TRENDING_POSTS, flatOutObject(postListingResponse));
    }

    public void saveLatestResponseJson(PostListingResponse postListingResponse) {
        Log.d("TAGGER LATEST RESPONSE", gson.toJson(postListingResponse) + " response hai");
        new SharedPrefsUtils().setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.HOME_LATEST_POSTS, flatOutObject(postListingResponse));
    }

    public void saveAuthorResponseJson(ProfileResult profileResult) {
        Log.d("TAGGER LATEST RESPONSE", gson.toJson(profileResult) + " response hai");
        new SharedPrefsUtils().setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.AUTHOR_TOP_POSTS, flatOutObject(profileResult));
    }

    public List<Result> getTrendingPostsFromStorage() {

        TrendingPostResponse postListingResponse = gson.fromJson(new SharedPrefsUtils()
                .getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.TRENDING_POSTS), TrendingPostResponse.class);

        return postListingResponse.getResults();
    }

//    public List<Result> getLatestPostsFromStorage(String categoryName, boolean getOnlyNews) {
//        postList.clear();
//        PostListingResponse postListingResponse = gson.fromJson(new SharedPrefsUtils()
//                .getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.HOME_LATEST_POSTS), PostListingResponse.class);
//
//        HomeResult homeResult = postListingResponse.getHomeResultByName(categoryName);
//        if(homeResult!=null){
//            if (!getOnlyNews) {
//                return homeResult.getAuthors();
//            } else {
//                mockList = homeResult.getAuthors();
//                for (int position = 0; position < postListingResponse.getAuthors().size(); position++) {
//                    if (mockList.get(position).getType() == 0) {
//                        postList.add(mockList.get(position));
//                    }
//                }
//            }
//        }
//        return postList;
//    }

    public List<Result> getTopPostsFromStorage() {

        ProfileResult profileResult = gson.fromJson(new SharedPrefsUtils()
                .getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.AUTHOR_TOP_POSTS), ProfileResult.class);

//        Log.d("TOGGS home", "List is :" + gson.toJson(profileResult.getTopVideos()));
        return profileResult.getTopVideos();
    }

    public void saveCategoryWithKey(String key, HomeResult result) {
        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), key, gson.toJson(result));
    }

//    public HomeResult getCategoryResponseByKey(String key) {
//        HomeResult homeResult = getLatestPostResponse() !=null ? getLatestPostResponse().getHomeResultByName(key) : null;
//        if(homeResult==null){
//            Log.d("LLLL", "home result null aya in jsonparse helper");
//
//        }
//        else {
//            Log.d("LLLL", "home result null NAHI aya in jsonparse helper");
//
//        }
//        return homeResult;
//    }

    public PostListingResponse getLatestPostResponse(){
        return gson.fromJson(new SharedPrefsUtils()
                .getStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.HOME_LATEST_POSTS), PostListingResponse.class);
    }
}
