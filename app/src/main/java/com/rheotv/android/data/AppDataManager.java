/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 2:08 PM
 *
 */

package com.rheotv.android.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.rheotv.android.BuildConfig;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.UserPermissionsResponse;
import com.rheotv.android.data.network.models.AnalyticsEventsResponse;
import com.rheotv.android.data.network.models.FollowResponse;
import com.rheotv.android.data.network.models.RecentlyRedeemedResponse;
import com.rheotv.android.data.network.models.StreamEndedResponse;
import com.rheotv.android.data.network.models.StreamerData;
import com.rheotv.android.data.network.models.TopStreamersResponse;
import com.rheotv.android.data.network.models.common.Requests.FcmTokenWrapper;
import com.rheotv.android.data.network.models.directVideo.VideoResponse;
import com.rheotv.android.data.network.models.districtlisting.DistrictResult;
import com.rheotv.android.data.network.models.gamify.BaseTransactionResponse;
import com.rheotv.android.data.network.models.gamify.CodaShopGameResponse;
import com.rheotv.android.data.network.models.gamify.CodaShopValidationResponse;
import com.rheotv.android.data.network.models.gamify.DailyRewardsResponse;
import com.rheotv.android.data.network.models.gamify.RewardHistoryResponse;
import com.rheotv.android.data.network.models.gamify.RewardTakenResponse;
import com.rheotv.android.data.network.models.gamify.Rewards;
import com.rheotv.android.data.network.models.gamify.SkuResponse;
import com.rheotv.android.data.network.models.general.AppVersionResponse;
import com.rheotv.android.data.network.models.general.GameDetails;
import com.rheotv.android.data.network.models.general.RTMPDetails;
import com.rheotv.android.data.network.models.general.SignedUrlResponse;
import com.rheotv.android.data.network.models.login.LoginUserRequest;
import com.rheotv.android.data.network.models.login.LoginUserResponse;
import com.rheotv.android.data.network.models.login.UserNameResult;
import com.rheotv.android.data.network.models.objects.FeedListingObject;
import com.rheotv.android.data.network.models.objects.GameObject;
import com.rheotv.android.data.network.models.objects.VideoListingResponse;
import com.rheotv.android.data.network.models.onboarding.OnBoardingResponse;
import com.rheotv.android.data.network.models.play.RequestPlayResponse;
import com.rheotv.android.data.network.models.play.ResultsItem;
import com.rheotv.android.data.network.models.postlisting.Requests.CommentTypeRequestBody;
import com.rheotv.android.data.network.models.postlisting.Requests.PostDeleteRequestBody;
import com.rheotv.android.data.network.models.postlisting.Requests.PostDownloadRequestBody;
import com.rheotv.android.data.network.models.postlisting.Requests.PostShareTypeRequestBody;
import com.rheotv.android.data.network.models.postlisting.Requests.PostTypeRequestBody;
import com.rheotv.android.data.network.models.postlisting.responses.Comments;
import com.rheotv.android.data.network.models.postlisting.responses.LeaderboardResponse;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.postlisting.responses.SearchApiResponse;
import com.rheotv.android.data.network.models.postlisting.responses.SupportChatResponse;
import com.rheotv.android.data.network.models.postlisting.responses.TopFansResponse;
import com.rheotv.android.data.network.models.postlisting.responses.TrendingPostResponse;
import com.rheotv.android.data.network.models.postlisting.responses.UniservalListingApiResponse;
import com.rheotv.android.data.network.models.postlisting.responses.User;
import com.rheotv.android.data.network.models.useProfile.responses.ChatGroupDetails;
import com.rheotv.android.data.network.models.useProfile.responses.PictureUploadResult;
import com.rheotv.android.ui.activities.inAppBilling.model.BillingPurchase;
import com.rheotv.android.ui.activities.inAppBilling.model.BillingResponse;
import com.rheotv.android.ui.activities.inAppBilling.model.PurchaseDetail;
import com.rheotv.android.ui.activities.moments.model.MomentsListResponse;
import com.rheotv.android.ui.activities.profile.model.PlayTimingDetail;
import com.rheotv.android.ui.activities.profile.model.SocialMedia;
import com.rheotv.android.ui.activities.profile.model.UserDonation;
import com.rheotv.android.data.network.models.postlisting.responses.VideoCallResponse;
import com.rheotv.android.data.network.models.postlisting.responses.VideoCallUsersList;
import com.rheotv.android.data.network.models.share.ShareResponse;
import com.rheotv.android.data.network.models.share.TenorResponse;
import com.rheotv.android.data.network.models.stickers.StickersResponse;
import com.rheotv.android.data.network.models.story.StoryAuthorResponse;
import com.rheotv.android.data.network.models.story.StoryResponse;
import com.rheotv.android.data.network.models.story.UploadStoryMediaResponse;
import com.rheotv.android.data.network.models.useProfile.responses.AchievementsResponse;
import com.rheotv.android.data.network.models.useProfile.responses.AnalyticsDataResponse;
import com.rheotv.android.data.network.models.useProfile.responses.BioResponse;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.data.network.models.useProfile.responses.RecentViewersResponse;
import com.rheotv.android.data.network.models.useProfile.responses.StreamerLevelResponseBody;
import com.rheotv.android.data.network.models.useProfile.responses.WalletDetail;
import com.rheotv.android.data.network.models.vote.VoteRequestBody;
import com.rheotv.android.data.network.requestLayer.ApiService;
import com.rheotv.android.data.network.requestLayer.EventsApiService;
import com.rheotv.android.db.AppDatabase;
import com.rheotv.android.db.ClipItem;
import com.rheotv.android.db.ClipResponse;
import com.rheotv.android.db.UserFollowDao;
import com.rheotv.android.db.UserFollowItem;
import com.rheotv.android.helpers.JsonParseHelper;
import com.rheotv.android.ui.activities.audioroom.model.AudioRoomResponse;
import com.rheotv.android.ui.activities.audioroom.model.ChatRoomActionResponse;
import com.rheotv.android.ui.activities.audioroom.model.CreateAudioRoomResponse;
import com.rheotv.android.ui.activities.audioroom.model.OwnerDetail;
import com.rheotv.android.ui.activities.audioroom.model.ServerListResponse;
import com.rheotv.android.ui.activities.customroom.model.CustomRoomDetailResponse;
import com.rheotv.android.ui.activities.customroom.model.CustomRoomPlayerResponse;
import com.rheotv.android.ui.activities.customroom.model.CustomRoomResponse;
import com.rheotv.android.ui.activities.onboarding.v2.model.LatestPostResponse;
import com.rheotv.android.ui.activities.onboarding.v2.model.TopShowResponse;
import com.rheotv.android.ui.activities.player.activity.FollowStatusListener;
import com.rheotv.android.ui.activities.profile.viewprofile.model.GameRule;
import com.rheotv.android.ui.activities.profile.viewprofile.utils.UserAction;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.story.Constants;
import com.rheotv.story.model.Story;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

/*
 *   This is a class which is just a
 *   wrapper over all network and
 *   db calls.
 *   To make the code better, you can
 *   write your calls here and get
 *   response accordingly.
 *
 */

public class AppDataManager implements DataManager {

    private final ApiService mApiService;

    private final EventsApiService mEventsApiService;

    private final Context mContext;

    private final Gson mGson;

    private final SharedPrefsUtils mPreferencesHelper;

    private String lat;

    private String lng;

    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    @Inject
    public AppDataManager(Context context, SharedPrefsUtils preferencesHelper, ApiService apiHelper, EventsApiService eventsApiService, Gson gson) {
        mContext = context;
        mPreferencesHelper = preferencesHelper;
        mApiService = apiHelper;
        mEventsApiService = eventsApiService;
        mGson = gson;
    }

    @Override
    public Observable<UniservalListingApiResponse> getGamePage(int offset, String type) {
        return mApiService.getGamePage(10, offset, type);
    }

    @Override
    public Observable<UniservalListingApiResponse> getInvoices(int offset, String userName) {
        return mApiService.getInvoices(10, offset);
    }

    @Override
    public Observable<SupportChatResponse> getChatDetails(int offset, String userName) {
        return mApiService.getChatDetails(10, offset);
    }

    @Override
    public Observable<SupportChatResponse> postChat(String message, String userName) {
        HashMap chatMap = new HashMap();
        chatMap.put("message", message);
        return mApiService.createSupportChat(chatMap);
    }

    @Override
    public Call<BioResponse> setUserBio(String bio) {
        HashMap chatMap = new HashMap();
        chatMap.put("bio", bio);
        return mApiService.setUserBio(chatMap);
    }

    @Override
    public Call<ResponseBody> requestPayout() {
        return mApiService.requestPayout();
    }

    @Override
    public Observable<SearchApiResponse> getSearchResponse(int offset, String searchKey) {
        return mApiService.getSearchResults(10, offset, searchKey);
    }

    @Override
    public Observable<SearchApiResponse> getSuggestionsResponse(int offset, String searchKey) {
        return mApiService.getSuggestionsResults(10, offset, searchKey);
    }

    @Override
    public Call<SearchApiResponse> getSuggestionsResponseCall(int offset, String searchKey) {
        return mApiService.getSuggestionsResultsCall(10, offset, searchKey);
    }

    @Override
    public Call<SearchApiResponse> getSuggestionsResponseCallWithType(String searchKey, String type) {
        return mApiService.getSuggestionsResultsCallWithType(searchKey, type);
    }

    /* @Override
    public Observable<PostListingResponse> getHomePage() {
        lat = sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), AppConstants.LAT);
        if (lat == null || lat.isEmpty()) {
            lat = "28.6225983";
        }
        lng = sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), AppConstants.LNG);
        if (lng == null || lng.isEmpty()) {
            lng = "77.0544938";
        }
        Map<String, String> map = new HashMap<>();
        map.put("lat", lat);
        map.put("lng", lng);
        String savedDistrictId = mPreferencesHelper.getStringPreference(mContext, SharedPrefsUtils.SAVED_DISTRICT_ID);
        return mApiService.getAllPosts(map, savedDistrictId);
    }*/

    @Override
    public Observable<FeedListingObject> fetchHomePage(String url, HashMap<String, String> tags) {
        lat = sharedPrefsUtils.getStringPreference(getNonUiContext(), AppConstants.LAT);
        if (lat == null || lat.isEmpty()) {
            lat = "28.6225983";
        }
        lng = sharedPrefsUtils.getStringPreference(getNonUiContext(), AppConstants.LNG);
        if (lng == null || lng.isEmpty()) {
            lng = "77.0544938";
        }
        Map<String, String> map = new HashMap<>();
        map.put("lat", lat);
        map.put("lng", lng);
        if (tags != null && !tags.isEmpty()) {
            if (url == null || url.isEmpty())
                return mApiService.getPostsByTags(true, CommonUtils.mapToString(tags));
            else
                return mApiService.getPostsByTags(url);
        }

        if (url == null || url.isEmpty()) {
            return mApiService.getAllPosts(map, mPreferencesHelper.getStringPreference(mContext, SharedPrefsUtils.SAVED_DISTRICT_ID));
        }
        return mApiService.getPagedPostsFromGivenUrl(url, map, mPreferencesHelper.getStringPreference(mContext, SharedPrefsUtils.SAVED_DISTRICT_ID));
    }

    @Override
    public Observable<TrendingPostResponse> getTrendingList(int offset) {
        return mApiService.getTrendingList(20, offset, mPreferencesHelper.getStringPreference(mContext, SharedPrefsUtils.SAVED_DISTRICT_ID));
    }

    @Override
    public Observable<LeaderboardResponse> getLeaderBoardList(String gameId, int offset, String sortType) {
        return mApiService.getLeaderBoardListing(gameId, 20, offset, sortType);
    }

    public List<DistrictResult> getDistrictList() {
        Log.d("TAGGER", " districts data called -----" + new SharedPrefsUtils().getStringPreference(mContext, SharedPrefsUtils.HOME_LATEST_POSTS));
//        return new JsonParseHelper().parseDistrictList(new SharedPrefsUtils().getStringPreference(mContext, SharedPrefsUtils.HOME_LATEST_POSTS));
        return new JsonParseHelper().parseRegions(new SharedPrefsUtils().getStringPreference(mContext, SharedPrefsUtils.HOME_LATEST_POSTS));
    }

    @Override
    public Call<ResponseBody> postLikeToggle(String postId) {
        return mApiService.postLike(new PostTypeRequestBody(postId));
    }

    @Override
    public Call<ResponseBody> postShare(String postId) {
        return mApiService.postShare(new PostTypeRequestBody(postId));
    }

    @Override
    public Call<ResponseBody> postFBShare(String postId, int source) {
        return mApiService.postFBShare(new PostShareTypeRequestBody(postId, source));
    }

    @Override
    public Call<ResponseBody> postDownload(String postId) {
        return mApiService.postDownload(new PostTypeRequestBody(postId));
    }

    @Override
    public Call<ResponseBody> castVote(String id) {
        return mApiService.castVote(new VoteRequestBody(id));
    }

    @Override
    public Call<ResponseBody> postFcmToken(String token) {
        return mApiService.postFcmToken(new FcmTokenWrapper(token));
    }

    @Override
    public Call<ResponseBody> postAddCoins() {
        return mApiService.postAddCoins();
    }

    @Override
    public Call<ResponseBody> deductCoins() {
        return mApiService.deductCoins();
    }

    @Override
    public Call<ResponseBody> postVideoView(Result res, String device_id, int duration,
                                            int timeElapsed, String macAddress,
                                            boolean isLive, String gameName, String gameId) {
        JSONObject otherInfoJson = new JSONObject();
        try {
            otherInfoJson.put("post_id", res.getId());
            otherInfoJson.put("author_id", res.getAuthor().getUser().getId().toString());
            otherInfoJson.put("author_username", res.getAuthor().getUser().getUsername());
            otherInfoJson.put("viewer_username", device_id);
            otherInfoJson.put("duration", duration);
            otherInfoJson.put("time_elapsed", timeElapsed);
            otherInfoJson.put("mac_address", macAddress);
            otherInfoJson.put("device_id", device_id);
            otherInfoJson.put("is_live", isLive);
            otherInfoJson.put("game_name", gameName);
            otherInfoJson.put("game_id", gameId);
            otherInfoJson.put("format", "player");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String otherInfo = otherInfoJson.toString();
        RequestBody otherInfoReqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), otherInfo);
        Log.i(getClass().getSimpleName(), "postVideoView: " + otherInfoJson.toString());
        return mEventsApiService.postVideoView(otherInfoReqBody);
    }

    @Override
    public Call<ResponseBody> postVideoView(ClipItem res, String device_id, int duration,
                                            int timeElapsed, String macAddress,
                                            boolean isLive, String gameName, String gameId) {
        JSONObject otherInfoJson = new JSONObject();
        try {
            otherInfoJson.put("post_id", res.getId());
            if (res.getAuthor() != null && res.getAuthor().getUser() != null) {
                if (res.getAuthor().getUser().getId() != null)
                    otherInfoJson.put("author_id", res.getAuthor().getUser().getId().toString());
                otherInfoJson.put("author_username", res.getAuthor().getUser().getUsername());
            }
            otherInfoJson.put("viewer_username", device_id);
            otherInfoJson.put("duration", duration);
            otherInfoJson.put("time_elapsed", timeElapsed);
            otherInfoJson.put("mac_address", macAddress);
            otherInfoJson.put("device_id", device_id);
            otherInfoJson.put("is_live", isLive);
            otherInfoJson.put("game_name", gameName);
            otherInfoJson.put("game_id", gameId);
            otherInfoJson.put("format", "clip");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String otherInfo = otherInfoJson.toString();
        RequestBody otherInfoReqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), otherInfo);
        Log.i(getClass().getSimpleName(), "postVideoView: " + otherInfoJson.toString());
        return mEventsApiService.postVideoView(otherInfoReqBody);
    }

    @Override
    public Call<ResponseBody> postSearchQuery(String searchQuery, String username) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("username", username);
            obj.put("query", searchQuery);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonString = obj.toString();
        RequestBody searchQueryBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
        return mEventsApiService.postQuerySearch(searchQueryBody);
    }

    @Override
    public Call<LoginUserResponse> authorizeLogin(LoginUserRequest user) {
        HashMap<String, String> hmap = new HashMap<>();
        hmap.put("name", user.getName());
        hmap.put("email", user.getEmail());
        hmap.put("phone", user.getPhone());
        hmap.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        hmap.put("uid", user.getUid());
        return mApiService.authorizeLogin(hmap);
    }

    @Override
    public Call<UserNameResult> checkUsernameAndSave(String username) {
        HashMap<String, String> hmap = new HashMap<>();
        if (username != null)
            username = username.trim();
        hmap.put("username", username);
        return mApiService.checkUsernameAndSave(hmap);
    }

    @Override
    public Call<ProfileResult> getProfile(String authorUserName) {
        return mApiService.getProfile(authorUserName);
    }

    @Override
    public Call<BioResponse> getProfileBio(String authorUserName) {
        return mApiService.getBio(authorUserName);
    }

    @Override
    public Call<StreamerData> getStreamerData(String authorUserName, String sortType) {
        return mApiService.getStreamerData(authorUserName, sortType);
    }

    @Override
    public Call<ResponseBody> followAuthor(String authorId) {
        return mApiService.followAuthor(authorId);
    }

    @Override
    public Call<ResponseBody> toggleFollow(String state, String authorId) {
        return mApiService.toggleFollow(state, authorId);
    }

    @Override
    public Call<FollowResponse> checkFollowAuthor(String authorId) {
        return mApiService.checkFollowAuthor(authorId);
    }

    @Override
    public Call<ResponseBody> unFollowAuthor(String authorId) {
        return mApiService.unFollowAuthor(authorId);
    }

    @Override
    public Call<RTMPDetails> createLivePost(String title, String gameId, boolean canRequestPlay, MultipartBody.Part part, boolean isMobileSelected, boolean isCustomRoomEnabled, boolean isCohostFeatureEnabled, int coinCount) {
        //Map<String,String> otherInfoJson = new HashMap();
        JSONObject otherInfoJson = new JSONObject();
        try {
            otherInfoJson.put("title", title);
            otherInfoJson.put("game_id", gameId);
            otherInfoJson.put("streaming_platform", isMobileSelected ? "mobile" : "desktop");
            otherInfoJson.put("is_video_call_enabled", isCohostFeatureEnabled);
            if (isCustomRoomEnabled) {
                JSONObject customRoomObject = new JSONObject();
                customRoomObject.put("enabled", true);
                customRoomObject.put("entry_coins", coinCount);
                otherInfoJson.put("custom_room", customRoomObject);
            } else {
                otherInfoJson.put("accept_play_request", canRequestPlay);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String otherInfo = otherInfoJson.toString();
        RequestBody otherInfoReqBody = RequestBody.create(MediaType.parse("multipart/form-data"), otherInfo);

        return mApiService.createPost(part, otherInfoReqBody);
    }

    @Override
    public Observable<VideoListingResponse> getVideos(int userId, String gameId, boolean isLite, boolean isLive, String nextUrl) {
        if (nextUrl == null || nextUrl.isEmpty()) {
            if (userId != 0)
                return mApiService.getVideosByUser(isLite, userId, isLive);
            else if (gameId != null && !gameId.isEmpty())
                if (gameId.equalsIgnoreCase("is_live")) {
                    return mApiService.getRecommendedVideosByGame(isLite, gameId, isLive);
                } else {
                    return mApiService.getVideosByGame(isLite, gameId, isLive);
                }
        }
        return mApiService.getVideosByPage(nextUrl);
    }

    @Override
    public Observable<VideoListingResponse> getVideos(int userId, String gameId, boolean isLite, boolean isLive, String nextUrl, String postId) {
        if (nextUrl == null || nextUrl.isEmpty()) {
            if (userId != 0)
                return mApiService.getVideosByUser(isLite, userId, isLive);
            else if (gameId != null && !gameId.isEmpty())
                if (postId != null && !postId.isEmpty()) {
                    Log.i("Request_Service", "Post id");
                    return mApiService.getVideosByGame(isLite, gameId, isLive, postId);
                } else {
                    Log.i("Request_Service", "Without post");
                    return mApiService.getRecommendedVideosByGame(isLite, gameId, isLive);
                }
        }
        Log.i("Request_Service", "Next url");
        return mApiService.getVideosByPage(nextUrl);
    }

    @Override
    public Observable<VideoListingResponse> getRecommendedVideos(int userId, String gameId, boolean isLite, boolean isLive, String nextUrl, String slug, String postId) {
        if (nextUrl == null || nextUrl.isEmpty()) {
            if (userId != 0)
                return mApiService.getRecommendedVideosByUser(isLite, userId, isLive);
            else if (gameId != null && !gameId.isEmpty())
                if (postId != null && !postId.isEmpty()) {
                    Log.i("Request_Service", "Post id");
                    if (slug == null)
                        return mApiService.getRecommendedVideosWithoutSlug(isLite, gameId, isLive, postId);
                    return mApiService.getRecommendedVideosByGame(isLite, gameId, isLive, postId, slug);
                } else {
                    Log.i("Request_Service", "Without post");
                    if (slug == null)
                        return mApiService.getRecommendedVideosWithoutSlugAndPostId(isLite, gameId, isLive);
                    return mApiService.getRecommendedVideosByGame(isLite, gameId, isLive, slug);
                }
        }
        Log.i("Request_Service", "Next url");
        return mApiService.getVideosByPage(nextUrl);
    }

    @Override
    public Call<Result> getCompetitionPage(String competitionId) {
        return mApiService.getCompetitionPage(competitionId);
    }

    @Override
    public Call<ResponseBody> postReport(String postId) {
        return mApiService.postReport(new PostTypeRequestBody(postId));
    }

    @Override
    public Observable<ResponseBody> pinComment(String postId, String username, String text) {
        Map<String, String> body = new HashMap<>();
        body.put("text", text);
        body.put("post_id", postId);
        body.put("username", username);
        return mApiService.pinComment(body);
    }

    @Override
    public Call<ResponseBody> reportComment(String postId, String username, String comment) {
        return mApiService.reportComment(new CommentTypeRequestBody(postId, username, comment));
    }

    @Override
    public Call<ResponseBody> blockUser(String postId, String username, String comment) {
        return mApiService.blockUser(new CommentTypeRequestBody(postId, username, comment));
    }

    @Override
    public Call<ClipResponse> getClips(String nextUrl) {
        if (nextUrl == null || nextUrl.isEmpty()) {
            return mApiService.getClips();
        }
        return mApiService.getPagedClips(nextUrl);
    }

    @Override
    public Call<ResponseBody> downloadVideo(String postId, String resolution) {
        return mApiService.downloadVideo(new PostDownloadRequestBody(postId, resolution));
    }

    @Override
    public Call<ResponseBody> likeClip(String clipId) {
        return mApiService.likeClick(clipId);
    }

    @Override
    public Call<ClipItem> fetchClip(String clipId) {
        return mApiService.fetchClip(clipId);
    }

    @Override
    public Call<OnBoardingResponse> fetchOnBoardingData() {
        return mApiService.fetchOnBoardingData();
    }

    @Override
    public Call<ResponseBody> setUserLanguage(List<String> languageId) {
        HashMap map = new HashMap();
        map.put("language_ids", languageId);
        return mApiService.setUserLanguage(map);
    }

    @Override
    public Call<AppVersionResponse> checkVersionSupport() {
        String extraInfo = CommonUtils.getBranchExtraInfo(getNonUiContext());
        mApiService.checkSupportedAppVersion(String.valueOf(BuildConfig.VERSION_CODE));
        return mApiService.checkSupportedAppVersion(String.valueOf(BuildConfig.VERSION_CODE), extraInfo);
    }

    @Override
    public Call<AppVersionResponse> checkVersionSupport(String extraInfo) {
        if (extraInfo == null)
            return mApiService.checkSupportedAppVersion(String.valueOf(BuildConfig.VERSION_CODE));
        return mApiService.checkSupportedAppVersion(String.valueOf(BuildConfig.VERSION_CODE), extraInfo);
    }

    @Override
    public Call<ResponseBody> deleteVideo(String postId) {
        return mApiService.deleteVideo(new PostDeleteRequestBody(postId));
    }

    @Override
    public Call<ResponseBody> uploadUserInfo(User user) {
        return mApiService.uploadUserInfo(user);
    }

    @Override
    public Call<ResponseBody> uploadImage(MultipartBody.Part multipart, String type) {
        if (type.contentEquals(mContext.getString(R.string.edit_profile))) {
            return mApiService.uploadProfileImage(multipart);
        } else {
            return mApiService.uploadCoverImage(multipart);
        }
    }

    @Override
    public Call<Result> getSpecificPostWithUid(String uid) {
        return mApiService.getSpecificPostWithUid(uid);

    }

    @Override
    public Call<Comments> getComments(String id) {
        return mApiService.getComments(id);
    }

    @Override
    public Call<Comments> getPagedCommentsFromUrl(String url) {
        return mApiService.getPagedCommentsFromUrl(url);
    }

    @Override
    public Call<Comments> getUserComments(String username, String url) {
        if (url != null)
            return mApiService.getPagedCommentsFromUrl(url);
        return mApiService.getUserComments(username);
    }

    @Override
    public Call<Comments> getStreamComments(String uid, String url) {
        if (url != null)
            return mApiService.getPagedCommentsFromUrl(url);
        return mApiService.getComments(uid);
    }

    @Override
    public Call<SignedUrlResponse> getSignedUrl(int duration) {
        HashMap<String, String> hmap = new HashMap<>();
        hmap.put("filename", "default.mp4");
        hmap.put("duration", String.valueOf(duration));
        return mApiService.getSignedUrl(hmap);
    }

    @Override
    public Call<SignedUrlResponse> getSignedUrl(String mineType, String sourceKey, String storageType) {
        HashMap<String, String> hmap = new HashMap<>();
        hmap.put("data_type", mineType);
        hmap.put("source_id", sourceKey);
        hmap.put("storage_type", storageType);
        Log.i(getClass().getSimpleName(), "getSignedUrl: " + hmap);
        return mApiService.getSignedUrl(hmap);
    }

    @Override
    public Call<ResponseBody> createStory(String headline, String gameId, String videoUrl, String videoFileUrl, int duration, String videoMode) {
        HashMap<String, String> hmap = new HashMap<>();
        hmap.put("title", headline);
        hmap.put("video_url", videoUrl);
        hmap.put("game_id", gameId);
        hmap.put("duration", String.valueOf(duration));
        hmap.put("video_mode", videoMode);
        hmap.put("video_file_url", videoFileUrl);
        return mApiService.createStory(hmap);
    }


    @Override
    public Call<List<GameDetails>> getGameDetails() {
        return mApiService.getGameDetails();
    }

    @Override
    public Call<CodaShopGameResponse> getCodaGames() {
        return mApiService.getCodaGames();
    }

    @Override
    public Call<ResponseBody> postHeart(String postId, String segmentUrl) {
        return mApiService.postHeart(new PostTypeRequestBody(postId, segmentUrl));
    }

    @Override
    public Call<StickersResponse> loadStickers(String postId, String url) {
        if (url == null || url.isEmpty()) {
            return mApiService.loadStickers(postId);
        }
        return mApiService.loadPagedStickers(url);
    }

    @Override
    public Call<Rewards> getRewards() {
        return mApiService.getRewards();
    }

    @Override
    public Call<Rewards> getPagedRewardsFromUrl(String url) {
        return mApiService.getPagedRewardsFromUrl(url);
    }

    @Override
    public Call<DailyRewardsResponse> getDailyRewards() {
        return mApiService.getDailyRewards();
    }

    @Override
    public Call<RewardTakenResponse> updateDailyScratchCard(String rewardId) {
        HashMap<String, String> body = new HashMap<>();
        body.put("user_plan_id", rewardId);
        return mApiService.updateDailyScratchStatus(body);
    }

    @Override
    public Call<ResponseBody> rateApp(int rating, String feedback) {
        HashMap<String, String> body = new HashMap<>();
        body.put("rating", String.valueOf(rating));
        body.put("feedback", feedback);
        return mApiService.rateApp(body);
    }

    @Override
    public Call<StreamerLevelResponseBody> getStreamerLevelInfo(int userId) {
        return mApiService.getStreamerLevelInfo(userId);
    }

    @Override
    public Call<RequestPlayResponse> getRequestPlayData(String postId, String nextUrl) {
        if (nextUrl != null && !nextUrl.isEmpty()) {
            return mApiService.getPaginateRequestPlayData(nextUrl);
        }
        return mApiService.getRequestPlayData(postId);
    }

    @Override
    public Call<RequestPlayResponse> getRequestPlayData(String postId) {
//        if (nextUrl != null && !nextUrl.isEmpty()) {
//            return mApiService.getPaginateRequestPlayData(nextUrl);
//        }
        return mApiService.getRequestPlayData(postId);
    }

    @Override
    public Call<RequestPlayResponse> getPaginateRequestPlayData(String url) {
        return mApiService.getPaginateRequestPlayData(url);
    }

    @Override
    public Call<ResultsItem> requestPlay(String postId, String gameUserName) {
        HashMap<String, String> body = new HashMap<>();
        body.put("post_id", postId);
        body.put("game_username", gameUserName);
        return mApiService.requestPlay(body);
    }

    @Override
    public Call<ResultsItem> requestPlayAction(String requestId, String action) {
        HashMap<String, String> body = new HashMap<>();
////        body.put("play_request_id", requestId);
        body.put("action", action);
        return mApiService.requestPlayAction(requestId, body);
    }

    @Override
    public Call<SkuResponse> getCodeShopSku(String gameId) {
        return mApiService.getCodeShopSku(gameId);
    }

    @Override
    public Call<CodaShopValidationResponse> validateCodaShopUser(String gameId, String playerId, List<String> skus) {
        JSONObject requestParam = new JSONObject();
        try {
            requestParam.put("user_account", playerId);
            requestParam.put("item_sku", skus.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String bodyParam = requestParam.toString();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyParam);
//        Log.i(getClass().getSimpleName(), "validateCodaShopUser " + new Gson().toJson(bodyParam));
        return mApiService.validateCodaShopUser(gameId, body);
    }

    @Override
    public Call<BaseTransactionResponse> codaShopTopupTransaction(String orderId, List<String> skus, String userAccount, String gameId, int coins, String transactionInterface) {
        JSONObject requestParam = new JSONObject();
        try {
            requestParam.put("orderId", orderId);
            JSONArray items = new JSONArray();

            for (int i = 0; i < skus.size(); i++) {
                JSONObject item = new JSONObject();
                item.put("sku", skus.get(i));
                item.put("quantity", 1);
                items.put(item);
            }

            requestParam.put("items", items);
            requestParam.put("userAccount", userAccount);
            requestParam.put("coins", coins);
            requestParam.put("game_id", gameId);
            requestParam.put("codashop_interface", transactionInterface);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String bodyParam = requestParam.toString();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyParam);
        return mApiService.codaShopTopupTransaction(gameId, body);
    }

    @Override
    public Call<BaseTransactionResponse> codaShopVoucherTransaction(List<String> skus, String gameId, int coins, String transactionInterface) {
        JSONObject requestParam = new JSONObject();
        try {
            JSONArray items = new JSONArray();

            for (int i = 0; i < skus.size(); i++) {
                JSONObject item = new JSONObject();
                item.put("sku", skus.get(i));
                item.put("quantity", 1);
                items.put(item);
            }

            requestParam.put("items", items);
            requestParam.put("coins", coins);
            requestParam.put("game_id", gameId);
            requestParam.put("codashop_interface", transactionInterface);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        String bodyParam = requestParam.toString();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyParam);

//        Log.i(getClass().getSimpleName(), "codaShopVoucherTransaction " + new Gson().toJson(skus) + " gameId " + gameId + " coins " + coins + " body " + new Gson().toJson(bodyParam));
        return mApiService.codaShopVoucherTransaction(gameId, body);
    }

    public Call<GameObject> getGameDetails(String gameId) {
        return mApiService.getGameDetails(gameId);
    }

    @Override
    public Call<ResponseBody> setUserGames(List<String> gameIds) {
        HashMap map = new HashMap();
        map.put("game_ids", gameIds);
        return mApiService.setUserGames(map);
    }

    @Override
    public Call<RewardHistoryResponse> getRewardHistory(String url) {
        if (url == null || url.isEmpty())
            return mApiService.getRewardHistory();
        return mApiService.getRewardHistory(url);
    }

    @Override
    public Call<ResponseBody> postModeratorsData(String moderatorsString) {
        HashMap<String, String> hmap = new HashMap<>();
        hmap.put("emails", moderatorsString);
        return mApiService.postModeratorsData(hmap);
    }

    @Override
    public Call<UploadStoryMediaResponse> uploadStoryMedia(String uri, String type, String metadata) {
        try {
            File file = new File(uri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", URLEncoder.encode(file.getName(), "utf-8"), requestFile);
            RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), type);
            if (Constants.TEXT.equalsIgnoreCase(type)) {
                RequestBody metadataRequest = RequestBody.create(MediaType.parse("multipart/form-data"), metadata);
                return mApiService.uploadStoryText(body, description, metadataRequest);
            }
            return mApiService.uploadStoryMedia(body, description);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Call<ResponseBody> publishStory(Story story) {
        return mApiService.publishStory(story.getId(), story);
    }

    @Override
    public Call<ResponseBody> watchedStory(String id) {
        return mApiService.watchedStory(id);
    }

    @Override
    public Call<ResponseBody> deleteStory(String id) {
        return mApiService.deleteStory(id);
    }

    @Override
    public Call<StoryResponse> loadUserStories(String authorId, String storyId) {
        if (storyId == null)
            return mApiService.loadUserStories(authorId);
        return mApiService.loadUserStoriesById(storyId);
    }

    @Override
    public Call<StoryAuthorResponse> loadStoryAuthors(String username, String url) {
        if (url != null)
            return mApiService.loadStoryAuthorsByPage(url);
        return mApiService.loadStoryAuthors(username);
    }

    @Override
    public Call<ResponseBody> reportStory(String id) {
        return mApiService.reportStory(id);
    }

    @Override
    public Call<ResponseBody> interestedStory(String id) {
        return mApiService.interestedStory(id);
    }

    public Call<LeaderboardResponse> loadFollowUser(String username, String type, String nextUrl) {
        if (nextUrl != null)
            return mApiService.loadFollowUser(nextUrl);
        return mApiService.loadFollowUser(username.equals(CommonUtils.getUserName()) ? username : username, type);
    }

    @Override
    public Call<LeaderboardResponse> loadStoryViewers(String storyId, String nextUrl) {
        if (nextUrl != null)
            return mApiService.loadStoryViewerByPage(nextUrl);
        return mApiService.loadStoryViewer(storyId);
    }

    @Override
    public Call<VideoListingResponse> loadSimilarPosts() {
        return mApiService.loadSimilarPost();
    }

    @Override
    public Call<VideoResponse> loadPost(String postPath) {
        return mApiService.loadPost(postPath);
    }

    @Override
    public Call<StreamEndedResponse> checkStreamEnded(String postId) {
        return mApiService.checkStreamEnded(postId);
    }

    public Call<AnalyticsEventsResponse> getAnalyticsEventsList() {
        return mApiService.getAnalyticsEventsList();

    }

    @Override
    public Observable<DailyRewardsResponse> loadAvailableScratchCards() {
        return mApiService.loadAvailableScratchCards();
    }

    @Override
    public Observable<Response<RewardTakenResponse>> updateScratchCardStatusShown(String rewardId) {
        Map map = new HashMap();
        map.put("user_plan_id", rewardId);
        return mApiService.updateScratchStatusShown(map);
    }

    @Override
    public Observable<Response<FeedListingObject>> getGiveawayVideos(String url) {
        if (url == null || url.isEmpty())
            url = "v2/content/posts/?slug=giveaway-videos,all-tournaments&is_live=true&lite=true";
        return mApiService.getGiveawayVideos(url);
    }

    @Override
    public Observable<ResponseBody> buySticker(String stickerId) {
        Map<String, String> body = new HashMap<>();
        body.put("sticker_id", stickerId);
        return mApiService.buySticker(body);
    }

    @Override
    public Observable<ResponseBody> redeemRequest(String upiId, String mobileNo, int amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("upi_id", upiId);
        body.put("mobile_number", mobileNo);
        body.put("coins", amount);
        return mApiService.redeemRequest(body);
    }

    @Override
    public Call<RecentlyRedeemedResponse> getRecentlyRedeemedList() {
        return mApiService.getRecentlyRedeemed();
    }

    @Override
    public Observable<ResponseBody> downloadStatement() {
        return mApiService.downloadStatement();
    }

    @Override
    public Call<AchievementsResponse> getAchievements() {
        return mApiService.getAchievements();
    }

    public Call<ResponseBody> submitCustomRoomDetails(String postId, String roomId, String roomPass, boolean isEdit) {
        JSONObject requestParam = new JSONObject();
        try {
            requestParam.put("post_id", postId);
            requestParam.put("custom_room_username", roomId);
            requestParam.put("custom_room_password", roomPass);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String bodyParam = requestParam.toString();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyParam);
        if (isEdit) {
            return mApiService.editCustomRoomDetails(body);
        }
        return mApiService.submitCustomRoomDetails(body);
    }

    @Override

    public Observable<ResponseBody> buySticker(String stickerId, String postId, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("sticker_id", stickerId);
        body.put("post_id", postId);
        body.put("text", message);
        return mApiService.buySticker(body);
    }

    public Call<TenorResponse> loadRandomGif(String query) {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("q", query);
        queryMap.put("key", BuildConfig.TENOR_API_KEY);
        queryMap.put("media_filter", "minimal");
        queryMap.put("contentfilter", "medium");
        queryMap.put("ar_range", "standard");
        return mApiService.loadRandomGif(AppConstants.TENOR_BASE_URL, queryMap);
    }

    @Override
    public Call<ShareResponse> loadShareContent(String id) {
        return mApiService.loadShareContent(id);

    }

    @Override
    public Call<ModeratorQuestionsResponse> loadModeratorQuestions(String pertains, String polarity) {
        return mApiService.loadModeratorsQuestions(polarity, pertains);
    }

    @Override
    public Call<ResponseBody> submitModeratorQuestionResponse(String postId, String questionId, List<String> selectedQuestionsId) {

        try {
            JSONObject requestParam = new JSONObject();
            requestParam.put("content_id", postId);

            JSONArray questions = new JSONArray();

            JSONObject questionObject = new JSONObject();
            questionObject.put("question_id", questionId);
            JSONArray optionsArray = new JSONArray();
            for (String selectedQuestionId : selectedQuestionsId) {
                optionsArray.put(selectedQuestionId);
            }
            questionObject.put("options_ids", optionsArray);

            questions.put(questionObject);

            requestParam.put("questions", questions);
            String bodyParam = requestParam.toString();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyParam);
            return mApiService.submitModeratorQuestionOptions(body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Call<RewardCustomWebviewApiResponse> getRewardsWebviewUrl() {
        return mApiService.getRewardsCustomRoomPage();
    }

    @Override
    public Call<ClipResponse> getTopClips() {
        return mApiService.getTopClips();
    }

    @Override
    public Call<ResponseBody> setCustomRoomWinner(String requestId) {
        try {
            JSONObject requestParam = new JSONObject();
            requestParam.put("play_request_id", requestId);
            String bodyParam = requestParam.toString();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyParam);
            return mApiService.setCustomRoomWinner(body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Call<ResponseBody> postHeart(String postId) {
        try {
            JSONObject requestParam = new JSONObject();
            requestParam.put("post_id", postId);
            String bodyParam = requestParam.toString();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyParam);
            return mApiService.postHeart(body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Call<ResponseBody> voteAsContentModerator(String userId) {
        return mApiService.voteAsContentModerator(userId);
    }

    @Override
    public Call<AnalyticsDataResponse> getStreamAnalytics(String authorUserName) {
        return mApiService.getStreamAnalytics(authorUserName);
    }

    @Override
    public Call<WalletDetail> getUserWallet(String username) {
        return mApiService.getUserWallet(username);
    }

    @Override
    public Call<ResponseBody> onPostShare(String postId) {
        try {
            JSONObject requestParam = new JSONObject();
            requestParam.put("post_id", postId);
            String bodyParam = requestParam.toString();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyParam);
            return mApiService.sharePost(body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Call<ResponseBody> onChatAction(String id, String username, @Nullable String comment, String action) {
        Map<String, String> body = new HashMap<>();
        body.put("post_id", id);
        body.put("username", username);
        if (comment != null)
            body.put("text", comment);
        return mApiService.onChatAction(action, body);
    }

    @Override
    public Call<Comments> getComments(String id, String url) {
        if (url != null)
            return mApiService.getPagedCommentsFromUrl(url);
        return mApiService.getComments(id);
    }

    @Override
    public Call<List<GameDetails>> getUserSelectedGames(String username) {
        return mApiService.getGameDetails();
    }

    @Override
    public Call<ResponseBody> onUserGameAction(String id, String gameUsername, String action) {
        HashMap<String, String> body = new HashMap<>();
        body.put("id", id);
        body.put("game_user_name", gameUsername);
        body.put("action", action);
        return mApiService.onUpdateUserGame(body);
    }

    @Override
    public Call<List<GameRule>> getUserGameRules(String username) {
        return mApiService.getUserGameRules(username);
    }

    @Override
    public Call<ResponseBody> updateGameRule(GameRule rule, UserAction action) {
        HashMap<String, String> body = new HashMap<>();
        body.put("rule", rule.getRule());
        if (action instanceof UserAction.Delete) {
            body.put("id", rule.getId());
            body.put("action", action.toString());
        }
        return mApiService.updateGameRule(body);
    }

    @Override
    public Call<UserDonation> getUserDonation(String username) {
        return mApiService.getUserDonation(username);
    }

    @Override
    public Call<ResponseBody> updateUserDonation(UserDonation donation) {
        return mApiService.updateUserDonation(donation);
    }

    @Override
    public Call<PlayTimingDetail> getUserPlayTimingDetail(String username) {
        return mApiService.getUserPlayTimeDetails(username);
    }

    @Override
    public Call<PictureUploadResult> uploadFile(MultipartBody.Part multipart, String endpoint) {
        return mApiService.uploadFile(endpoint, multipart);
    }

    @Override
    public Call<ResponseBody> deleteFile(String path, String id) {
        HashMap<String, String> body = new HashMap<>();
        body.put("id", id);
        body.put("action", "delete");
        return mApiService.deleteFile(path, body);
    }

    @Override
    public Call<ChatGroupDetails> getConnectionDetails(String username) {
        return mApiService.getConnectionDetails(username);
    }

    @Override
    public Call<List<SocialMedia>> getSocialMedia() {
        return mApiService.getSocialMedia();
    }

    @Override
    public Call<ResponseBody> updateOnlinePresence(SocialMedia media, UserAction action) {
        HashMap<String, String> body = new HashMap<>();
        body.put("id", media.getId());
        body.put("link", media.getLink());
        if (action instanceof UserAction.Delete)
            body.put("action", action.toString());

        return mApiService.updateOnlinePresence(body);
    }

    @Override
    public Call<SignedUrlResponse> getSignedUrl(String uploadUrl, UserAction action, long duration) {
        Map<String, Object> body = new HashMap<>();
        if (action instanceof UserAction.Add) {
            body.put("url", uploadUrl);
            body.put("duration", duration);
        }
        body.put("action", action.toString());
        return mApiService.getSignedUrl(body);
    }

    @Override
    public Call<CustomRoomResponse> fetchCustomRooms(String postId) {
        return mApiService.fetchCustomRooms(postId);
    }

    @Override
    public Call<CustomRoomDetailResponse> createCustomRoom(String postId, String startTime, int entryCoinValue, int maxAllowedPlayer) {
        Map<String, String> body = new HashMap<>();
        body.put("post_id", postId);
        body.put("start_time", startTime);
        body.put("entry_coins", String.valueOf(entryCoinValue));
        body.put("max_allowed_users", String.valueOf(maxAllowedPlayer));
        return mApiService.createCustomRoom(body);
    }

    @Override
    public Call<CustomRoomDetailResponse> addCustomRoomIdAndPassword(String customRoomId, String roomId, String roomPassword) {
        Map<String, String> body = new HashMap<>();
        body.put("room_id", roomId);
        body.put("room_password", roomPassword);
        return mApiService.addCustomRoomIdAndPassword(customRoomId, body);
    }

    @Override
    public Call<ResponseBody> requestToCustomRoom(String customRoomId, String gameUserName) {
        Map<String, String> body = new HashMap<>();
        body.put("game_username", gameUserName);
        return mApiService.requestToCustomRoom(customRoomId, body);
    }

    @Override
    public Call<ResponseBody> refundCustomRoom(String customRoomId) {
        return mApiService.refundCustomRoom(customRoomId);
    }

    @Override
    public Call<CustomRoomPlayerResponse> fetchCustomRoomPlayers(String customRoomId, String nextUrl) {
        String url = "play_request/customroom/" + customRoomId + "/customroom_requests/";
        if (nextUrl != null) {
            url = nextUrl;
        }
        return mApiService.fetchCustomRoomPlayers(url);
    }

    @Override
    public Call<CustomRoomPlayerResponse> searchCustomRoomPlayer(String customRoomId, String searchQuery) {
        Map<String, String> body = new HashMap<>();
        body.put("query", searchQuery);
        return mApiService.searchCustomRoomPlayer(customRoomId, body);
    }

    @Override
    public Call<ResponseBody> markCustomRoomWinner(String customRoomId, String winnerId) {
        Map<String, String> body = new HashMap<>();
        body.put("winner_request_id", winnerId);
        return mApiService.markCustomRoomWinner(customRoomId, body);
    }

    @Override
    public Call<ResponseBody> updateCustomRoomStartTime(String customRoomId, String updatedTime) {
        Map<String, String> body = new HashMap<>();
        body.put("start_time", updatedTime);
        return mApiService.updateCustomRoomStartTime(customRoomId, body);
    }

    @Override
    public Call<ResponseBody> updateGameSchedule(PlayTimingDetail detail) {
        Map<String, Object> body = new HashMap<>();
        detail.setGamingDays(detail.getTotalDays());
        body.put("schedule", detail);
        return mApiService.updateGameSchedule(body);
    }

    @Override
    public Call<ResponseBody> updateUserAttribute(String path, HashMap<String, Object> body, Object data) {
        if (body != null) {
            return mApiService.updateUserAttribute(path, body);
        }
        return mApiService.updateUserAttribute(path, data);
    }

    @Override
    public Call<RecentViewersResponse> getRecentViewers() {
        return mApiService.getRecentViewers();
    }

    @Override
    public Call<BillingResponse> getBillingSkus() {
        return mApiService.getBillingSkus();
    }

    @Override
    public Call<ResponseBody> buyProduct(BillingPurchase purchase) {
        return mApiService.buyProduct(new PurchaseDetail(purchase));
    }

    public Call<TopFansResponse> fetchTopFans(String username) {
        return mApiService.fetchTopFans(username);
    }

    @Override
    public Call<TopStreamersResponse> fetchTopStreamers(String nextUrl, String selectedLanguages) {
        if (nextUrl != null) {
            return mApiService.fetchPaginatedTopStreamers(nextUrl);
        }
        return mApiService.fetchTopStreamers(selectedLanguages);
    }

    @Override
    public Call<TopShowResponse> fetchTopShow(String selectedLanguages) {
        return mApiService.fetchTopShow(selectedLanguages);
    }

    @Override
    public Call<LatestPostResponse> fetchLatestPostByUser(int userId) {
        return mApiService.fetchLatestPostByUser(userId);
    }

    @Override

    public Call<VideoCallResponse> manageVideoCalls(String channelId, int userId, String postId, String callAction) {
        if (channelId != null) {
            return mApiService.manageVideoCallsForChannelId(channelId, postId, callAction);
        }
        return mApiService.manageVideoCalls(userId, postId, callAction);
    }

    @Override
    public Call<VideoCallUsersList> getVideoCallRequestedUsersList(String postId, String nextUrl) {
        if (nextUrl == null) {
            return mApiService.getVideoCallUsersList(postId);
        }
        return mApiService.getVideoCallUsersListForUrl(nextUrl);
    }

    @Override
    public Call<UserPermissionsResponse> getPermissionsResponse() {
        return mApiService.checkFeaturesEnablePermission();
    }

    public Call<ResponseBody> postVideoView(Result res, String device_id, int duration,
                                            int timeElapsed, String macAddress,
                                            boolean isLive, String gameName,
                                            String gameId, String orientation,
                                            String videoQuality) {
        JSONObject otherInfoJson = new JSONObject();
        try {
            otherInfoJson.put("post_id", res.getId());
            otherInfoJson.put("author_id", res.getAuthor().getUser().getId().toString());
            otherInfoJson.put("author_username", res.getAuthor().getUser().getUsername());
            otherInfoJson.put("viewer_username", device_id);
            otherInfoJson.put("duration", duration);
            otherInfoJson.put("time_elapsed", timeElapsed);
            otherInfoJson.put("mac_address", macAddress);
            otherInfoJson.put("device_id", device_id);
            otherInfoJson.put("is_live", isLive);
            otherInfoJson.put("game_name", gameName);
            otherInfoJson.put("game_id", gameId);
            otherInfoJson.put("format", "player");
            otherInfoJson.put("orientation", orientation);
            otherInfoJson.put("video_quality", videoQuality);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String otherInfo = otherInfoJson.toString();
        RequestBody otherInfoReqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), otherInfo);
        Log.i(getClass().getSimpleName(), "postVideoView: " + otherInfoJson.toString());
        return mEventsApiService.postVideoView(otherInfoReqBody);
    }

    @Override
    public Call<AudioRoomResponse> fetchAudioRoomList(String url) {
        if (url != null)
            return mApiService.fetchAudioRoomList(url);
        return mApiService.fetchAudioRoomList();
    }

    @Override
    public Call<CreateAudioRoomResponse> fetchAudioRoomDetail(String groupId, String chatRoomId) {
        return mApiService.fetchAudioRoomDetail(groupId, chatRoomId);
    }

    @Override
    public Call<CreateAudioRoomResponse> createAudioRoom(String groupId) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        return mApiService.createAudioRoom(body);
    }

    @Override
    public Call<ChatRoomActionResponse> leaveChatRoom(String chatRoomId) {
        Map<String, Object> body = new HashMap<>();
        body.put("chatroom_id", chatRoomId);
        return mApiService.doChatRoomAction(body, "leave");
    }

    @Override
    public Call<ChatRoomActionResponse> joinChatRoom(String groupId, String chatRoomId) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("chatroom_id", chatRoomId);
        body.put("is_muted", false);
        return mApiService.doChatRoomAction(body, "join");
    }

    @Override
    public Call<ChatRoomActionResponse> muteUnMuteParticipant(String groupId, String chatRoomId, String participantUsername, String participantId, String action) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("chatroom_id", chatRoomId);
        body.put("participant_id", participantId);
        body.put("participant_username", participantUsername);
        return mApiService.doChatRoomAction(body, action);
    }

    @Override
    public Call<ServerListResponse<OwnerDetail>> fetchAudioRoomConnectedUsers(String chatRoomId, String nextUrl) {
        if (nextUrl != null && !nextUrl.isEmpty())
            return mApiService.fetchAudioRoomConnectedUsers(nextUrl);
        return mApiService.fetchAudioRoomConnectedUsers("audio_chatrooms/connected_viewers/?chatroom_id=" + chatRoomId);
    }

    @Override
    public void toggleFollowState(String username, int userId, boolean status, boolean isAsync, FollowStatusListener followStatusListener) {
        AppUtilsKt.INSTANCE.runOnIO(() -> {
            userFollowDao.updateUserEntry(new UserFollowItem(userId, username, status));
            if (isAsync) {
                AppUtilsKt.INSTANCE.runOnMain(() -> {
                    if (followStatusListener != null)
                        followStatusListener.followStatus(status);
                    return null;
                });
            }
            try {
                mApiService.toggleFollow(status ? "follow" : "unfollow", String.valueOf(userId)).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!isAsync) {
                AppUtilsKt.INSTANCE.runOnMain(() -> {
                    if (followStatusListener != null)
                        followStatusListener.followStatus(status);
                    return null;
                });
            }
            return null;
        });
    }

    private UserFollowDao userFollowDao = AppDatabase.Companion.getInstance(RheoTvApp.getNonUiContext()).userFollowDao();

    public Call<ResponseBody> setShowReminder(List<String> ids, String source) {
        HashMap map = new HashMap();
        map.put("slot_banner_ids", ids);
        map.put("source_type", source);
        return mApiService.setShowReminder(map);
    }

    @Override
    public Call<ResponseBody> updateAudioGroupName(String groupId, String name) {
        HashMap map = new HashMap();
        map.put("group_id", groupId);
        map.put("name", name);
        return mApiService.updateAudioGroupName(map);
    }

    @Override
    public Call<AudioRoomResponse> searchRoom(String keyword) {
        return mApiService.searchAudioRoom(keyword);
    }

    @Override
    public Call<ResponseBody> rewindEvent(String postId, String username, String authorname, long streamDate, long seekStartedAt, long seekEndedAt, String gameName, String authorLanguage, String postUrl) {
        try {
            HashMap map = new HashMap();
            map.put("post_id", postId);
            map.put("username", username);
            map.put("author_name", authorname);
            map.put("stream_date", streamDate);
            map.put("seek_started_at", seekStartedAt);
            map.put("seek_ended_at", seekEndedAt);
            map.put("game_name", gameName);
            map.put("author_language", authorLanguage);
            map.put("post_url", postUrl);
            Log.i(getClass().getSimpleName(), "postId: " + postId + " and stream_date " + streamDate + " and authorname " + authorname +
                    " and seekStartedAt " + seekStartedAt + " and seekEndedAt " + seekEndedAt + " and game_name " + gameName + " and author_language " + authorLanguage + " and " + postUrl);
            return mApiService.rewindEvent(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Call<ResponseBody> startAudioRoomGame(String action, String chatRoomId, String gameId) {
        Map<String, String> body = new HashMap<>();
        body.put("chatroom_id", chatRoomId);
        body.put("game_id", gameId);
        return mApiService.startAudioRoomGame(action, body);
    }

    @Override
    public Call<ResponseBody> highlightAudioRoomUser(String chatRoomId, String action, int userId) {
        Map<String, String> body = new HashMap<>();
        body.put("chatroom_id", chatRoomId);
        body.put("participant_id", String.valueOf(userId));
        return mApiService.highlightAudioRoomUser(body, action);
    }

    @Override
    public Call<MomentsListResponse> fetchMoments(String paginatedUrl, String authorName) {
        if (paginatedUrl != null) {
            return mApiService.fetchPagedMoment(paginatedUrl.replace("http://", "https://"));
        }
        if (authorName == null || authorName.isEmpty()) {
            return mApiService.fetchMoments();
        }
        return mApiService.fetchMomentsForAuthor(authorName);
    }

    @Override
    public Call<ResponseBody> updateMomentState(String momentId, long startTime, long endTime) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", momentId);
        body.put("clip_started_at", startTime);
        body.put("clip_ended_at", endTime);
        return mApiService.updateMomentState(body);
    }

    @Override
    public Call<Comments> fetchMomentComments(String postId, Double createdAt, long seekStartedAt, long seekEndedAt, String paginatedUrl) {
        if (paginatedUrl != null && !paginatedUrl.isEmpty())
            return mApiService.fetchMomentComments(paginatedUrl);
        return mApiService.fetchMomentComments("content/post_seeks/comments/?post_id=" + postId + "&created_at=" +
                createdAt + "&seek_started_at=" + seekStartedAt + "&seek_ended_at=" + seekEndedAt);
    }
}