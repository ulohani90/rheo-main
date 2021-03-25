package com.rheotv.android.data.network.requestLayer;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface EventsApiService {

    @POST("video-view/")
    Call<ResponseBody> postVideoView(@Body RequestBody jsonObj);

    @POST("search-query/")
    Call<ResponseBody> postQuerySearch(@Body RequestBody jsonBody);

    @POST("item/posts/heart/")
    Call<ResponseBody> postHeart(@Body RequestBody body);
}
