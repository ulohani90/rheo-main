/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 1:23 PM
 *
 */

package com.rheotv.android.data.network.requestLayer;

import com.rheotv.android.data.ModeratorQuestionsResponse;
import com.rheotv.android.data.RewardCustomWebviewApiResponse;
import com.rheotv.android.data.network.UserPermissionsResponse;
import com.rheotv.android.data.network.models.AnalyticsEventsResponse;
import com.rheotv.android.data.network.models.FollowResponse;
import com.rheotv.android.data.network.models.RecentlyRedeemedResponse;
import com.rheotv.android.data.network.models.StreamEndedResponse;
import com.rheotv.android.data.network.models.StreamerData;
import com.rheotv.android.data.network.models.TopStreamersResponse;
import com.rheotv.android.data.network.models.common.Requests.FcmTokenWrapper;
import com.rheotv.android.data.network.models.directVideo.VideoResponse;
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
import com.rheotv.android.data.network.models.postlisting.responses.PostListingResponse;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.postlisting.responses.SearchApiResponse;
import com.rheotv.android.data.network.models.postlisting.responses.SupportChatResponse;
import com.rheotv.android.data.network.models.postlisting.responses.TopFansResponse;
import com.rheotv.android.data.network.models.postlisting.responses.TrendingPostResponse;
import com.rheotv.android.data.network.models.postlisting.responses.UniservalListingApiResponse;
import com.rheotv.android.data.network.models.postlisting.responses.User;
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
import com.rheotv.android.data.network.models.useProfile.responses.ChatGroupDetails;
import com.rheotv.android.data.network.models.useProfile.responses.PictureUploadResult;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.data.network.models.useProfile.responses.RecentViewersResponse;
import com.rheotv.android.data.network.models.useProfile.responses.StreamerLevelResponseBody;
import com.rheotv.android.data.network.models.useProfile.responses.WalletDetail;
import com.rheotv.android.data.network.models.vote.VoteRequestBody;
import com.rheotv.android.db.ClipItem;
import com.rheotv.android.db.ClipResponse;
import com.rheotv.android.ui.activities.audioroom.model.AudioRoomResponse;
import com.rheotv.android.ui.activities.audioroom.model.ChatRoomActionResponse;
import com.rheotv.android.ui.activities.audioroom.model.CreateAudioRoomResponse;
import com.rheotv.android.ui.activities.audioroom.model.OwnerDetail;
import com.rheotv.android.ui.activities.audioroom.model.ServerListResponse;
import com.rheotv.android.ui.activities.customroom.model.CustomRoomDetailResponse;
import com.rheotv.android.ui.activities.customroom.model.CustomRoomPlayerResponse;
import com.rheotv.android.ui.activities.customroom.model.CustomRoomResponse;
import com.rheotv.android.ui.activities.inAppBilling.model.BillingResponse;
import com.rheotv.android.ui.activities.inAppBilling.model.PurchaseDetail;
import com.rheotv.android.ui.activities.moments.model.MomentsListResponse;
import com.rheotv.android.ui.activities.onboarding.v2.model.LatestPostResponse;
import com.rheotv.android.ui.activities.onboarding.v2.model.TopShowResponse;
import com.rheotv.android.ui.activities.profile.model.PlayTimingDetail;
import com.rheotv.android.ui.activities.profile.model.SocialMedia;
import com.rheotv.android.ui.activities.profile.model.UserDonation;
import com.rheotv.android.ui.activities.profile.viewprofile.model.GameRule;
import com.rheotv.story.model.Story;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/*
    Sample API requests of type GET, PUT and POST
    follow the same pattern to write your API calls.

    @GET("restaurants/{id}")
    Call<BaseResponse<ModelResponse>> getRestaurantDetails(@Path("id") int id);

    @POST("payments")
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    Call<BaseResponse<ModelResponse>> submitRazorPayResponse(@Body RequestBody updatedDAta);

    @PUT("orders/{id}")
    Call<BaseResponse<ModelResponse>>updateOrderAction(@Body RequestBody updatedDAta, @Path("id") String id);

*/

public interface ApiService {
    // Declare all APIs here

    @GET("item/posts/game-page/")
    Observable<UniservalListingApiResponse> getGamePage(@Query("limit") int limit, @Query("offset") int offset, @Query("game_id") String gameId);

    @GET("search/items")
    Observable<SearchApiResponse> getSearchResults(@Query("limit") int limit, @Query("offset") int offset, @Query("q") String searchKey);

    @GET("user/me/get-invoices/")
    Observable<UniservalListingApiResponse> getInvoices(@Query("limit") int limit, @Query("offset") int offset);


    @GET("user/me/get-support-chats/")
    Observable<SupportChatResponse> getChatDetails(@Query("limit") int limit, @Query("offset") int offset);

    @GET("item/posts/home-page/")
    Observable<FeedListingObject> getAllPosts(@HeaderMap Map<String, String> headers, @Query("region_id") String region_id);

    @GET("v2/content/posts/")
    Observable<FeedListingObject> getPostsByTags(
            @Query("lite") boolean isLite,
            @Query(value = "slug", encoded = true) String tags
    );

    @GET
    Observable<FeedListingObject> getPostsByTags(@Url String url);

    @POST("item/chat/post/")
    Observable<SupportChatResponse> createChat(@Body HashMap chatBody);

    @POST("user/me/contact-support/")
    Observable<SupportChatResponse> createSupportChat(@Body HashMap chatBody);

    @POST("user/me/set-user-bio/")
    Call<BioResponse> setUserBio(@Body HashMap chatBody);

    @POST("user/me/request-payout/")
    Call<ResponseBody> requestPayout();

    @GET
    Observable<FeedListingObject> getPagedPostsFromGivenUrl(@Url String url, @HeaderMap Map<String, String> headers, @Query("region_id") String region_id);

    @GET("item/posts/trending/?limit=10")
    Observable<TrendingPostResponse> getTrendingList(@Query("limit") int limit, @Query("offset") int offset, @Query("region_id") String region_id);

    @GET("analytics/leaderboard/")
    Observable<LeaderboardResponse> getLeaderBoardListing(@Query("game_id") String gameId, @Query("limit") int limit, @Query("offset") int offset, @Query("sortType") String sortType);

    @POST("item/posts/like-toggle/")
    Call<ResponseBody> postLike(@Body PostTypeRequestBody postTypeRequestBody);

    @POST("item/posts/downloaded/")
    Call<ResponseBody> postDownload(@Body PostTypeRequestBody postTypeRequestBody);

    @POST("item/posts/poll/")
    Call<ResponseBody> castVote(@Body VoteRequestBody postTypeRequestBody);

    @POST("item/posts/shared/")
    Call<ResponseBody> postShare(@Body PostTypeRequestBody postTypeRequestBody);

    @POST("item/posts/shared/")
    Call<ResponseBody> postFBShare(@Body PostShareTypeRequestBody postShareTypeRequestBody);

    @GET("common/check-version/{version_code}/")
    Call<AppVersionResponse> checkSupportedAppVersion(
            @Path("version_code") String versionCode
    );

    @GET("common/check-version/{version_code}")
    Call<AppVersionResponse> checkSupportedAppVersion(
            @Path("version_code") String versionCode,
            @Query("extra_info") String extraInfo
    );

    @POST("user/me/set-notification-token/")
    Call<ResponseBody> postFcmToken(@Body FcmTokenWrapper fcmTokenWrapper);

    @POST("user/me/add-coins/")
    Call<ResponseBody> postAddCoins();

    @POST("user/me/deduct-coins/")
    Call<ResponseBody> deductCoins();

    @POST("user/me/set-moderators/")
    Call<ResponseBody> postModeratorsData(@Body HashMap moderatorString);

    @POST("user/login/")
    Call<LoginUserResponse> authorizeLogin(@Body HashMap user);

    @POST("user/me/check-username/")
    Call<UserNameResult> checkUsernameAndSave(@Body HashMap user);

    @GET("user/{authorUserName}/")
    Call<ProfileResult> getProfile(@Path("authorUserName") String authorUserName);

    @GET("user/{authorUserName}/get-user-videos/")
    Observable<PostListingResponse> getVideos(@Path("authorUserName") String authorUserName);

    @GET("v2/content/posts/")
    Observable<VideoListingResponse> getVideosByUser(
            @Query("lite") boolean isLite,
            @Query("user_id") int userId,
            @Query("live") boolean isLive
    );

    @GET("v2/content/posts/")
    Observable<VideoListingResponse> getVideosByGame(
            @Query("lite") boolean isLite,
            @Query("game_id") String gameId,
            @Query("is_live") boolean isLive
    );

    @GET("v2/content/posts/")
    Observable<VideoListingResponse> getVideosByGame(
            @Query("lite") boolean isLite,
            @Query("game_id") String gameId,
            @Query("is_live") boolean isLive,
            @Query("post_id") String postId
    );

    @GET
    Observable<VideoListingResponse> getVideosByPage(@Url String url);

    @GET("recommendation/posts/")
    Observable<VideoListingResponse> getRecommendedVideosByUser(
            @Query("lite") boolean isLite,
            @Query("user_id") int userId,
            @Query("live") boolean isLive
    );

    @GET("recommendation/posts/")
    Observable<VideoListingResponse> getRecommendedVideosByGame(
            @Query("lite") boolean isLite,
            @Query("game_id") String gameId,
            @Query("is_live") boolean isLive,
            @Query("slug") String slug
    );

    @GET("recommendation/posts/")
    Observable<VideoListingResponse> getRecommendedVideosByGame(
            @Query("lite") boolean isLite,
            @Query("game_id") String gameId,
            @Query("is_live") boolean isLive
    );

    @GET("recommendation/posts/")
    Observable<VideoListingResponse> getRecommendedVideosByGame(
            @Query("lite") boolean isLite,
            @Query("game_id") String gameId,
            @Query("is_live") boolean isLive,
            @Query("post_id") String postId,
            @Query("slug") String slug
    );

    @GET("recommendation/posts/")
    Observable<VideoListingResponse> getRecommendedVideosByGame(
            @Query("lite") boolean isLite,
            @Query("game_id") String gameId,
            @Query("post_id") String postId,
            @Query("is_live") boolean isLive
    );

    @GET("recommendation/posts/")
    Observable<VideoListingResponse> getRecommendedVideosWithoutSlug(
            @Query("lite") boolean isLite,
            @Query("game_id") String gameId,
            @Query("is_live") boolean isLive,
            @Query("post_id") String postId
    );

    @GET("recommendation/posts/")
    Observable<VideoListingResponse> getRecommendedVideosWithoutSlugAndPostId(
            @Query("lite") boolean isLite,
            @Query("game_id") String gameId,
            @Query("is_live") boolean isLive
    );

    @POST("user/check-follow/{authorId}/")
    Call<FollowResponse> checkFollowAuthor(@Path("authorId") String authorId);

    @POST("user/follow/{authorId}/")
    Call<ResponseBody> followAuthor(@Path("authorId") String authorId);

    @POST("user/unfollow/{authorId}/")
    Call<ResponseBody> unFollowAuthor(@Path("authorId") String authorId);

    @POST("user/{state}/{authorId}/")
    Call<ResponseBody> toggleFollow(@Path("state") String state, @Path("authorId") String authorId);

    @POST("user/me/set-basic-info/")
    Call<ResponseBody> uploadUserInfo(@Body User user);

    @Multipart
    @POST("user/me/set-profile-photo/")
    Call<ResponseBody> uploadProfileImage(@Part MultipartBody.Part file);

    @Multipart
    @POST("user/me/{param}/")
    Call<PictureUploadResult> uploadFile(@Path("param") String type, @Part MultipartBody.Part file);

    @POST("user/me/{param}/")
    Call<ResponseBody> deleteFile(@Path("param") String type, @Body HashMap body);

    @Multipart
    @POST("user/me/set-cover-photo/")
    Call<ResponseBody> uploadCoverImage(@Part MultipartBody.Part file);

    @GET("v2/content/posts/{post_id}")
    Call<Result> getSpecificPostWithUid(@Path("post_id") String uid);

    @GET("item/posts/comments/")
    Call<Comments> getComments(@Query("post_id") String postId);

    @GET("user/{username}/get-profile-chats/")
    Call<Comments> getUserComments(@Path("username") String username);

    @GET
    Call<Comments> getPagedCommentsFromUrl(@Url String url);

    @GET("item/posts/game-list/")
    Call<List<GameDetails>> getGameDetails();

    @GET("item/posts/{username}/game-list/")
    Call<List<GameDetails>> getUserSelectedGames(@Path("username") String username);

    @GET("codashop/game/")
    Call<CodaShopGameResponse> getCodaGames();

    @GET("codashop/recently-redeemed/")
    Call<RecentlyRedeemedResponse> getRecentlyRedeemed();

    @POST("item/posts/signed-url/")
    Call<SignedUrlResponse> getSignedUrl(@Body HashMap fileName);

    @POST("item/posts/create_clip/")
    Call<ResponseBody> createStory(@Body HashMap storyBody);

    @Multipart
    @POST("item/posts/create_post/")
    Call<RTMPDetails> createPost(@Part MultipartBody.Part file, @Part("other_info") RequestBody otherInfo);

    @GET("item/posts/get-competition/")
    Call<Result> getCompetitionPage(@Query("id") String competitionId);

    @GET("user/{user_name}/get-user-bio/")
    Call<BioResponse> getBio(@Path("user_name") String userName);

    @GET("user/{authorUserName}/get-streamer-data/")
    Call<StreamerData> getStreamerData(@Path("authorUserName") String authorUserName, @Query("sortType") String sortType);

    @GET("user/{authorUserName}/get-streamer-data/")
    Call<AnalyticsDataResponse> getStreamAnalytics(@Path("authorUserName") String authorUserName);

    @GET
    Observable<PostListingResponse> getVideosForGivenUrl(@Url String url, @HeaderMap Map<String, String> headers, @Query("region_id") String region_id);

    @POST("item/posts/report-post/")
    Call<ResponseBody> postReport(@Body PostTypeRequestBody postTypeRequestBody);

    @POST("item/comments/report-comment/")
    Call<ResponseBody> reportComment(@Body CommentTypeRequestBody commentTypeRequestBody);

    @POST("item/posts/pin-comment/")
    Observable<ResponseBody> pinComment(@Body Map<String, String> body);

    @GET("content/clips/")
    Call<ClipResponse> getClips();

    @GET("content/clips/fetch-top-clips")
    Call<ClipResponse> getTopClips();

    @GET
    Call<ClipResponse> getPagedClips(@Url String url);

    @POST("content/clips/{clip_id}/clap/")
    Call<ResponseBody> likeClick(@Path("clip_id") String clipId);

    @GET("content/clips/{clip_id}")
    Call<ClipItem> fetchClip(@Path("clip_id") String clipId);

    @GET("common/onboarding-view")
    Call<OnBoardingResponse> fetchOnBoardingData();

    @POST("user/me/set-language/")
    Call<ResponseBody> setUserLanguage(@Body HashMap body);

    @POST("user/me/set-games/")
    Call<ResponseBody> setUserGames(@Body HashMap body);

    @POST("item/posts/heart/")
    Call<ResponseBody> postHeart(@Body PostTypeRequestBody postTypeRequestBody);

    @GET("content/stickers/")
    Call<StickersResponse> loadStickers(@Query("post_id") String postId);

    @GET
    Call<StickersResponse> loadPagedStickers(@Url String url);

    @POST("item/comments/block-user/")
    Call<ResponseBody> blockUser(@Body CommentTypeRequestBody commentTypeRequestBody);


    @GET("user/me/get-coin-transactions/")
    Call<RewardHistoryResponse> getRewardHistory();

    @GET
    Call<RewardHistoryResponse> getRewardHistory(@Url String url);

    @GET("rewards/user_plans/history/")
    Call<Rewards> getRewards();

    @GET
    Call<Rewards> getPagedRewardsFromUrl(@Url String url);

    @GET("rewards/user_plans/current/")
    Call<DailyRewardsResponse> getDailyRewards();

    @POST("rewards/mark_success/")
    Call<RewardTakenResponse> updateDailyScratchStatus(@Body HashMap body);

    @GET("rewards/user_plans/current/")
    Observable<DailyRewardsResponse> loadAvailableScratchCards();

    @POST("rewards/mark_shown/")
    Observable<Response<RewardTakenResponse>> updateScratchStatusShown(@Body Map body);

    @POST("item/posts/download-video/")
    Call<ResponseBody> downloadVideo(@Body PostDownloadRequestBody postDownloadRequestBody);

    @POST("item/posts/delete-post/")
    Call<ResponseBody> deleteVideo(@Body PostDeleteRequestBody postDeleteRequestBody);

    @POST("user/me/set-user-feedback/")
    Call<ResponseBody> rateApp(@Body HashMap body);

    @GET("streamer_level/streamer_level_data/{user_id}/")
    Call<StreamerLevelResponseBody> getStreamerLevelInfo(@Path("user_id") int userId);

    @GET("play_request/play_request/")
    Call<RequestPlayResponse> getRequestPlayData(@Query("post_id") String postId);

    @GET
    Call<RequestPlayResponse> getPaginateRequestPlayData(@Url String paginatedUrl);

    @POST("play_request/play_request/")
    Call<ResultsItem> requestPlay(@Body HashMap body);

    @PUT("play_request/play_request/{id}/")
    Call<ResultsItem> requestPlayAction(
            @Path("id") String requestId,
            @Body HashMap body
    );


    @GET("codashop/game/{gameId}/list-sku/")
    Call<SkuResponse> getCodeShopSku(@Path("gameId") String gameId);

    @POST("codashop/game/{gameId}/validate/")
    Call<CodaShopValidationResponse> validateCodaShopUser(
            @Path("gameId") String gameId,
            @Body RequestBody body
    );

    @POST("codashop/game/{gameId}/topup/")
    Call<BaseTransactionResponse> codaShopTopupTransaction(
            @Path("gameId") String gameId,
            @Body RequestBody body
    );

    @POST("codashop/game/{gameId}/voucher/")
    Call<BaseTransactionResponse> codaShopVoucherTransaction(
            @Path("gameId") String gameId,
            @Body RequestBody body
    );

    @GET("v2/content/games/{game_id}/")
    Call<GameObject> getGameDetails(@Path("game_id") String gameId);

    @GET("search/suggest")
    Observable<SearchApiResponse> getSuggestionsResults(@Query("limit") int limit, @Query("offset") int offset, @Query("q") String searchKey);


    @GET("search/suggest/")
    Call<SearchApiResponse> getSuggestionsResultsCall(@Query("limit") int limit, @Query("offset") int offset, @Query("q") String searchKey);

    @GET("search/suggest/")
    Call<SearchApiResponse> getSuggestionsResultsCallWithType(@Query("q") String searchKey, @Query("suggest_type") String suggestType);

    @Multipart
    @POST("content/stories/")
    Call<UploadStoryMediaResponse> uploadStoryMedia(@Part MultipartBody.Part file, @Part("file_type") RequestBody fileType);

    @Multipart
    @POST("content/stories/")
    Call<UploadStoryMediaResponse> uploadStoryText(@Part MultipartBody.Part file, @Part("file_type") RequestBody fileType, @Part("type_meta") RequestBody metadata);

    @POST("content/stories/{id}/publish/")
    Call<ResponseBody> publishStory(@Path("id") String storyId, @Body Story story);

    @POST("content/stories/{id}/delete/")
    Call<ResponseBody> deleteStory(@Path("id") String storyId);

    @POST("content/stories/{id}/watched/")
    Call<ResponseBody> watchedStory(@Path("id") String storyId);

    @POST("content/stories/{id}/report/")
    Call<ResponseBody> reportStory(@Path("id") String storyId);

    @POST("content/stories/{id}/interested/")
    Call<ResponseBody> interestedStory(@Path("id") String storyId);

    @GET("content/stories/")
    Call<StoryResponse> loadUserStories(@Query("author_id") String authorId);

    @GET("content/stories/")
    Call<StoryResponse> loadUserStoriesById(@Query("story_id") String authorId);

    @GET("user/{username}/story-users/")
    Call<StoryAuthorResponse> loadStoryAuthors(@Path("username") String username);

    @GET
    Call<StoryAuthorResponse> loadStoryAuthorsByPage(@Url String url);

    @GET("user/{username}/{type}/")
    Call<LeaderboardResponse> loadFollowUser(@Path("username") String username, @Path("type") String type);

    @GET
    Call<LeaderboardResponse> loadFollowUser(@Url String url);

    @GET("content/stories/{story_id}/viewed-user-list/")
    Call<LeaderboardResponse> loadStoryViewer(@Path("story_id") String storyId);

    @GET
    Call<LeaderboardResponse> loadStoryViewerByPage(@Url String url);

    @GET("content/posts/similar-posts/")
    Call<VideoListingResponse> loadSimilarPost();

    @GET("content/posts/{post_path}/")
    Call<VideoResponse> loadPost(@Path("post_path") String path);

    @GET("content/posts/similar-posts/")
    Call<StreamEndedResponse> checkStreamEnded(String postId);

    @GET("user/me/get-user-events/")
    Call<AnalyticsEventsResponse> getAnalyticsEventsList();

    @GET
    Observable<Response<FeedListingObject>> getGiveawayVideos(@Url String url);

    @POST("item/posts/buy-sticker/")
    Observable<ResponseBody> buySticker(@Body Map<String, String> body);

    @POST("user/me/redeem-amount/")
    Observable<ResponseBody> redeemRequest(@Body Map<String, Object> body);

    @GET("user/me/download-redeem-statement/")
    Observable<ResponseBody> downloadStatement();

    @GET("streamer_level/bonus_achievements")
    Call<AchievementsResponse> getAchievements();

    @POST("play_request/custom_room/new-custom-room/")
    Call<ResponseBody> submitCustomRoomDetails(@Body RequestBody body);

    @POST("play_request/custom_room/edit-custom-room/")
    Call<ResponseBody> editCustomRoomDetails(@Body RequestBody body);

    @GET
    Call<TenorResponse> loadRandomGif(@Url String url, @QueryMap Map<String, String> queryMap);

    @GET("content/posts/get-shareable-resource")
    Call<ShareResponse> loadShareContent(@Query("post_id") String id);

    @GET("user/user_nps/")
    Call<ModeratorQuestionsResponse> loadModeratorsQuestions(@Query("polarity") String polarity, @Query("pertains") String pertains);

    @POST("user/user_nps/")
    Call<ResponseBody> submitModeratorQuestionOptions(@Body RequestBody body);

    @GET("rewards/custom_page")
    Call<RewardCustomWebviewApiResponse> getRewardsCustomRoomPage();

    @POST("play_request/custom_room/custom-room-winner/")
    Call<ResponseBody> setCustomRoomWinner(@Body RequestBody body);

    @POST("item/posts/heart/")
    Call<ResponseBody> postHeart(@Body RequestBody body);

    @POST("user/content-moderator-vote/{user_id}/")
    Call<ResponseBody> voteAsContentModerator(@Path("user_id") String userId);

    @POST("user/me/game-username/")
    Call<ResponseBody> onUpdateUserGame(@Body HashMap body);

    @POST("user/me/game-rule/")
    Call<ResponseBody> updateGameRule(@Body HashMap body);

    @GET("user/{username}/get-profile-chat-connection/")
    Call<ChatGroupDetails> getConnectionDetails(@Path("username") String username);

    @GET("common/social-presence")
    Call<List<SocialMedia>> getSocialMedia();

    @POST("user/me/online-presence/")
    Call<ResponseBody> updateOnlinePresence(@Body HashMap body);

    @POST("user/me/audio-message/")
    Call<SignedUrlResponse> getSignedUrl(@Body Map body);

    @POST("item/posts/shared-shareable-resource/")
    Call<ResponseBody> sharePost(@Body RequestBody body);

    @GET("play_request/customroom/{post_id}/all/")
    Call<CustomRoomResponse> fetchCustomRooms(@Path("post_id") String postId);

    @POST("play_request/customroom/")
    Call<CustomRoomDetailResponse> createCustomRoom(@Body Map<String, String> body);

    @PUT("play_request/customroom/{custom_room_id}/id_pass/")
    Call<CustomRoomDetailResponse> addCustomRoomIdAndPassword(@Path("custom_room_id") String customRoomId, @Body Map<String, String> body);

    @POST("play_request/customroom/{custom_room_id}/add/")
    Call<ResponseBody> requestToCustomRoom(@Path("custom_room_id") String customRoomId, @Body Map<String, String> body);

    @PUT("play_request/customroom/{custom_room_id}/refund/")
    Call<ResponseBody> refundCustomRoom(@Path("custom_room_id") String customRoomId);

    @GET
    Call<CustomRoomPlayerResponse> fetchCustomRoomPlayers(@Url String url);

    @POST("play_request/customroom/{custom_room_id}/search/")
    Call<CustomRoomPlayerResponse> searchCustomRoomPlayer(@Path("custom_room_id") String customRoomId, @Body Map<String, String> body);

    @POST("play_request/customroom/{custom_room_id}/winner/")
    Call<ResponseBody> markCustomRoomWinner(@Path("custom_room_id") String customRoomId, @Body Map<String, String> body);

    @PUT("play_request/customroom/{custom_room_id}/start_time/")
    Call<ResponseBody> updateCustomRoomStartTime(@Path("custom_room_id") String customRoomId, @Body Map<String, String> body);

    @GET("user/{username}/get-wallet/")
    Call<WalletDetail> getUserWallet(@Path("username") String username);

    @POST("item/comments/{action}/")
    Call<ResponseBody> onChatAction(@Path("action") String action, @Body Map<String, String> body);

    @POST("item/posts/game/{action}/")
    Call<ResponseBody> onUpdateUserGame(@Path("action") String action, @Body GameDetails body);

    @GET("user/{username}/get-game-rules/")
    Call<List<GameRule>> getUserGameRules(@Path("username") String username);

    @POST("item/post/game_rule/")
    Call<ResponseBody> updateGameRule(@Body GameRule rule);

    @GET("user/{username}/get-donation/")
    Call<UserDonation> getUserDonation(@Path("username") String username);

    @POST("user/me/donation-link/")
    Call<ResponseBody> updateUserDonation(@Body UserDonation donation);

    @GET("user/{username}/get-play-detail/")
    Call<PlayTimingDetail> getUserPlayTimeDetails(@Path("username") String username);

    @POST("user/me/game-schedule/")
    Call<ResponseBody> updateGameSchedule(@Body Map<String, Object> body);

    @POST("user/me/{endpoint}/")
    Call<ResponseBody> updateUserAttribute(@Path("endpoint") String action, @Body HashMap<String, Object> body);

    @POST("user/me/{endpoint}/")
    Call<ResponseBody> updateUserAttribute(@Path("endpoint") String action, @Body Object body);

    @POST("user/me/set-user-phonebook/")
    Call<ResponseBody> uploadUserContacts(@Body HashMap userContactsBody);

    @GET("user/me/recent-visitors/")
    Call<RecentViewersResponse> getRecentViewers();

    @GET("user/product_purchase_update/")
    Call<BillingResponse> getBillingSkus();

    @POST("user/product_purchase_update/")
    Call<ResponseBody> buyProduct(@Body PurchaseDetail purchase);

    @GET("user/top_fans/{username}/")
    Call<TopFansResponse> fetchTopFans(@Path("username") String username);

    @GET("user/sorted_streamers/")
    Call<TopStreamersResponse> fetchTopStreamers(@Query("language_ids") String languagesId);

    @GET
    Call<TopStreamersResponse> fetchPaginatedTopStreamers(@Url String url);

    @GET("item/posts/fetch-top-shows/")
    Call<TopShowResponse> fetchTopShow(@Query("language_ids") String languagesId);

    @GET("item/posts/fetch_latest_post/")
    Call<LatestPostResponse> fetchLatestPostByUser(@Query("streamer_id") int userId);

    @GET("audio_chatrooms/streamers/")
    Call<AudioRoomResponse> fetchAudioRoomList();

    @GET
    Call<AudioRoomResponse> fetchAudioRoomList(@Url String url);

    @GET("audio_chatrooms/groups/{group_id}/chat_rooms/{chat_room_id}/")
    Call<CreateAudioRoomResponse> fetchAudioRoomDetail(@Path("group_id") String groupId, @Path("chat_room_id") String chatRoomId);

    @POST("audio_chatrooms/groups/create_chatroom/")
    Call<CreateAudioRoomResponse> createAudioRoom(@Body Map<String, Object> body);

    @POST("audio_chatrooms/groups/{action}/")
    Call<ChatRoomActionResponse> doChatRoomAction(@Body Map<String, Object> body, @Path("action") String action);

    @GET
    Call<ServerListResponse<OwnerDetail>> fetchAudioRoomConnectedUsers(@Url String url);

    @GET("item/posts/manage-call/")
    Call<VideoCallResponse> manageVideoCalls(@Query("user_id") int userId, @Query("post_id") String postId, @Query("call_action") String callAction);

    @GET("item/posts/manage-call/")
    Call<VideoCallResponse> manageVideoCallsForChannelId(@Query("channel_id") String channelId, @Query("post_id") String postId, @Query("call_action") String callAction);

    @GET("item/posts/list-call-requests")
    Call<VideoCallUsersList> getVideoCallUsersList(@Query("post_id") String postId);

    @GET()
    Call<VideoCallUsersList> getVideoCallUsersListForUrl(@Url String url);

    @GET("user/me/check-feature-enable-permission/")
    Call<UserPermissionsResponse> checkFeaturesEnablePermission();

    @POST("user/me/set-banner-alarms/")
    Call<ResponseBody> setShowReminder(@Body HashMap body);

    @POST("audio_chatrooms/groups/update_group/")
    Call<ResponseBody> updateAudioGroupName(@Body HashMap body);

    @GET("audio_chatrooms/streamers/")
    Call<AudioRoomResponse> searchAudioRoom(@Query("keyword") String keyword);

    @POST("item/posts/rewind/")
    Call<ResponseBody> rewindEvent(@Body HashMap body);

    @POST("audio_chatrooms/groups/{action}/")
    Call<ResponseBody> startAudioRoomGame(@Path("action") String action, @Body Map body);

    @POST("audio_chatrooms/groups/{action_name}/")
    Call<ResponseBody> highlightAudioRoomUser(@Body Map body, @Path("action_name") String actionName);

    @GET("content/post_seeks/seeks_list/")
    Call<MomentsListResponse> fetchMoments();


    @GET("content/post_seeks/seeks_list/")
    Call<MomentsListResponse> fetchMomentsForAuthor(@Query("author_username") String authorUsername);

    @GET
    Call<MomentsListResponse> fetchPagedMoment(@Url String url);

    @POST("content/post_seeks/update_details/")
    Call<ResponseBody> updateMomentState(@Body Map body);


    @GET
    Call<Comments> fetchMomentComments(@Url String url);


}

