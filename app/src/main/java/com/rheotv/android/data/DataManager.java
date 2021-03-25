/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 2:08 PM
 *
 */

package com.rheotv.android.data;

import com.rheotv.android.data.network.UserPermissionsResponse;
import com.rheotv.android.data.network.models.AnalyticsEventsResponse;
import com.rheotv.android.data.network.models.FollowResponse;
import com.rheotv.android.data.network.models.RecentlyRedeemedResponse;
import com.rheotv.android.data.network.models.StreamEndedResponse;
import com.rheotv.android.data.network.models.StreamerData;
import com.rheotv.android.data.network.models.TopStreamersResponse;
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
import com.rheotv.android.data.network.models.postlisting.responses.Comments;
import com.rheotv.android.data.network.models.postlisting.responses.LeaderboardResponse;
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
import com.rheotv.android.ui.activities.inAppBilling.model.BillingPurchase;
import com.rheotv.android.ui.activities.inAppBilling.model.BillingResponse;
import com.rheotv.android.ui.activities.moments.model.MomentsListResponse;
import com.rheotv.android.ui.activities.onboarding.v2.model.LatestPostResponse;
import com.rheotv.android.ui.activities.onboarding.v2.model.TopShowResponse;
import com.rheotv.android.ui.activities.player.activity.FollowStatusListener;
import com.rheotv.android.ui.activities.profile.model.PlayTimingDetail;
import com.rheotv.android.ui.activities.profile.model.SocialMedia;
import com.rheotv.android.ui.activities.profile.model.UserDonation;
import com.rheotv.android.ui.activities.profile.viewprofile.model.GameRule;
import com.rheotv.android.ui.activities.profile.viewprofile.utils.UserAction;
import com.rheotv.story.model.Story;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public interface DataManager {

    Observable<UniservalListingApiResponse> getGamePage(int offset, String gameId);

    Observable<SearchApiResponse> getSearchResponse(int offset, String searchKey);

    Observable<UniservalListingApiResponse> getInvoices(int offset, String userName);

    Observable<SupportChatResponse> getChatDetails(int offset, String userName);

    Observable<SupportChatResponse> postChat(String message, String userName);

    Call<BioResponse> setUserBio(String bio);

    Call<ResponseBody> requestPayout();

    // Observable<PostListingResponse> getHomePage();

    Observable<FeedListingObject> fetchHomePage(String url, HashMap<String, String> tags);

    Observable<TrendingPostResponse> getTrendingList(int offset);

    Observable<LeaderboardResponse> getLeaderBoardList(String gameId, int offset, String sortType);

    List<DistrictResult> getDistrictList();

    Call<ResponseBody> postLikeToggle(String postId);

    Call<ResponseBody> postShare(String postId);

    Call<ResponseBody> postFBShare(String postId, int source);

    Call<ResponseBody> postDownload(String postId);

    Call<ResponseBody> castVote(String id);

    Call<ResponseBody> postFcmToken(String token);

    Call<ResponseBody> postVideoView(Result res, String device_id, int duration, int timeElapsed,
                                     String macAddress, boolean isLive,
                                     String gameName, String gameId);

    Call<ResponseBody> postVideoView(Result res, String device_id, int duration, int timeElapsed,
                                     String macAddress, boolean isLive,
                                     String gameName, String gameId,
                                     String orientation, String videoQuality);

    Call<ResponseBody> postVideoView(ClipItem res, String device_id, int duration, int timeElapsed,
                                     String macAddress, boolean isLive,
                                     String gameName, String gameId);

    Call<ResponseBody> postAddCoins();

    Call<ResponseBody> deductCoins();

    Call<LoginUserResponse> authorizeLogin(LoginUserRequest user);

    Call<UserNameResult> checkUsernameAndSave(String username);

    Call<ProfileResult> getProfile(String authorUserName);

    Call<BioResponse> getProfileBio(String authorUserName);

    Call<StreamerData> getStreamerData(String authorUserName, String sortType);

    Call<ResponseBody> followAuthor(String authorId);

    Call<FollowResponse> checkFollowAuthor(String authorId);

    Call<ResponseBody> unFollowAuthor(String authorId);

    Call<ResponseBody> toggleFollow(String state, String authorId);

    Call<AppVersionResponse> checkVersionSupport();

    Call<AppVersionResponse> checkVersionSupport(String extraInfo);

    Call<ResponseBody> uploadUserInfo(User user);

    Call uploadImage(MultipartBody.Part multipart, String type);

    Call<Result> getSpecificPostWithUid(String uid);

    Call<Comments> getComments(String uid);

    Call<Comments> getPagedCommentsFromUrl(String url);

    Call<Comments> getStreamComments(String uid, String url);

    Call<SignedUrlResponse> getSignedUrl(int duration);

    Call<ResponseBody> createStory(String headline, String description, String videoUrl, String videoFileUrl, int duration, String video_mode);

    Call<List<GameDetails>> getGameDetails();

    Call<CodaShopGameResponse> getCodaGames();

    Call<RTMPDetails> createLivePost(String title, String gameId, boolean canRequestPlay, MultipartBody.Part part, boolean isMobileSelected, boolean isCustomRoomEnabled, boolean isCoHostFeatureEnabled, int coinCount);

    Observable<VideoListingResponse> getVideos(int userId, String gameId, boolean isLite, boolean isLive, String nextUrl);

    Observable<VideoListingResponse> getVideos(int userId, String gameId, boolean isLite, boolean isLive, String nextUrl, String postId);

    Observable<VideoListingResponse> getRecommendedVideos(int userId, String gameId, boolean isLite, boolean isLive, String nextUrl, String slug, String postId);

    Call<Result> getCompetitionPage(String competitionId);

    Call<ResponseBody> postReport(String postId);

    Observable<ResponseBody> pinComment(String postId, String username, String text);

    Call<ResponseBody> reportComment(String postId, String username, String comment);

    Call<ResponseBody> blockUser(String postId, String username, String comment);

    Call<ClipResponse> getClips(String nextUrl);

    Call<ResponseBody> likeClip(String clipId);

    Call<ClipItem> fetchClip(String clipId);

    Call<OnBoardingResponse> fetchOnBoardingData();

    Call<ResponseBody> setUserLanguage(List<String> languageId);

    Call<ResponseBody> postHeart(String postId, String segmentUrl);

    Call<StickersResponse> loadStickers(String postId, String url);

    Call<ResponseBody> downloadVideo(String postId, String resolution);

    Call<ResponseBody> deleteVideo(String postId);

    Call<Rewards> getRewards();

    Call<Rewards> getPagedRewardsFromUrl(String url);

    Call<DailyRewardsResponse> getDailyRewards();

    Call<RewardTakenResponse> updateDailyScratchCard(String rewardId);

    Call<ResponseBody> rateApp(int rating, String feedback);

    Call<StreamerLevelResponseBody> getStreamerLevelInfo(int userId);

    Call<RequestPlayResponse> getRequestPlayData(String postId);

    Call<RequestPlayResponse> getRequestPlayData(String postId, String url);

    Call<RequestPlayResponse> getPaginateRequestPlayData(String url);

    Call<ResultsItem> requestPlay(String postId, String gameUserName);

    Call<ResultsItem> requestPlayAction(String requestId, String action);

    Call<SkuResponse> getCodeShopSku(String gameId);

    Call<CodaShopValidationResponse> validateCodaShopUser(String gameId, String PlayerId, List<String> skus);

    Call<BaseTransactionResponse> codaShopTopupTransaction(String orderId, List<String> skus, String userAccount, String gameId, int coins, String transactionInterface);

    Call<BaseTransactionResponse> codaShopVoucherTransaction(List<String> skus, String gameId, int coins, String transactionInterface);

    Call<GameObject> getGameDetails(String gameId);

    Observable<SearchApiResponse> getSuggestionsResponse(int offset, String searchKey);

    Call<SearchApiResponse> getSuggestionsResponseCall(int offset, String searchKey);

    Call<SearchApiResponse> getSuggestionsResponseCallWithType(String searchKey, String type);

    Call<ResponseBody> postSearchQuery(String searchQuery, String username);

    Call<ResponseBody> setUserGames(List<String> gameIds);

    Call<RewardHistoryResponse> getRewardHistory(String url);

    Call<ResponseBody> postModeratorsData(String moderatorsString);

    Call<UploadStoryMediaResponse> uploadStoryMedia(String uri, String type, String metadata);

    Call<ResponseBody> publishStory(Story story);

    Call<ResponseBody> deleteStory(String id);

    Call<ResponseBody> watchedStory(String id);

    Call<ResponseBody> reportStory(String id);

    Call<ResponseBody> interestedStory(String id);

    Call<StoryResponse> loadUserStories(String userId, String storyId);

    Call<StoryAuthorResponse> loadStoryAuthors(String userName, String url);

    Call<LeaderboardResponse> loadFollowUser(String username, String type, String nextUrl);

    Call<LeaderboardResponse> loadStoryViewers(String storyId, String nextUrl);

    Call<VideoListingResponse> loadSimilarPosts();

    Call<StreamEndedResponse> checkStreamEnded(String postId);

    Observable<DailyRewardsResponse> loadAvailableScratchCards();

    Call<AnalyticsEventsResponse> getAnalyticsEventsList();

    Observable<Response<RewardTakenResponse>> updateScratchCardStatusShown(String rewardId);

    Observable<Response<FeedListingObject>> getGiveawayVideos(String url);

    Observable<ResponseBody> buySticker(String stickerId);

    Observable<ResponseBody> buySticker(String stickerId, String postId, String message);

    Observable<ResponseBody> redeemRequest(String upiId, String mobileNo, int amount);

    Observable<ResponseBody> downloadStatement();

    Call<RecentlyRedeemedResponse> getRecentlyRedeemedList();

    Call<AchievementsResponse> getAchievements();

    Call<ResponseBody> submitCustomRoomDetails(String postId, String roomId, String roomPass, boolean isEdit);

    Call<VideoResponse> loadPost(String postPath);

    Call<TenorResponse> loadRandomGif(String query);

    Call<ShareResponse> loadShareContent(String id);

    Call<ModeratorQuestionsResponse> loadModeratorQuestions(String pertains, String polarity);

    Call<ResponseBody> submitModeratorQuestionResponse(String postId, String questionId, List<String> selectedQuestionsId);

    Call<RewardCustomWebviewApiResponse> getRewardsWebviewUrl();

    Call<ClipResponse> getTopClips();

    Call<ResponseBody> setCustomRoomWinner(String requestId);

    Call<ResponseBody> postHeart(String postId);

    Call<ResponseBody> voteAsContentModerator(String userId);

    Call<ResponseBody> onPostShare(String postId);

    Call<AnalyticsDataResponse> getStreamAnalytics(String authorUserName);

    Call<WalletDetail> getUserWallet(String username);

    Call<ResponseBody> onChatAction(String id, String username, String comment, String action);

    Call<Comments> getComments(String id, String url);

    Call<List<GameDetails>> getUserSelectedGames(String username);

    Call<ResponseBody> onUserGameAction(String id, String gameUsername, String action);

    Call<List<GameRule>> getUserGameRules(String username);

    Call<ResponseBody> updateGameRule(GameRule rule, UserAction action);

    Call<UserDonation> getUserDonation(String username);

    Call<ResponseBody> updateUserDonation(UserDonation donation);

    Call<PlayTimingDetail> getUserPlayTimingDetail(String username);

    Call<PictureUploadResult> uploadFile(MultipartBody.Part multipart, String endpoint);

    Call<ResponseBody> deleteFile(String path, String id);

    Call<ChatGroupDetails> getConnectionDetails(String username);

    Call<Comments> getUserComments(String uid, String url);

    Call<List<SocialMedia>> getSocialMedia();

    Call<ResponseBody> updateOnlinePresence(SocialMedia media, UserAction action);

    Call<SignedUrlResponse> getSignedUrl(String uploadUrl, UserAction action, long duration);

    Call<CustomRoomResponse> fetchCustomRooms(String postId);

    Call<CustomRoomDetailResponse> createCustomRoom(String postId, String startTime, int entryCoinValue, int maxAllowedPlayer);

    Call<CustomRoomDetailResponse> addCustomRoomIdAndPassword(String customRoomId, String roomId, String roomPassword);

    Call<ResponseBody> requestToCustomRoom(String customRoomId, String gameUserName);

    Call<ResponseBody> refundCustomRoom(String body);

    Call<CustomRoomPlayerResponse> fetchCustomRoomPlayers(String customRoomId, String nextUrl);

    Call<CustomRoomPlayerResponse> searchCustomRoomPlayer(String customRoomId, String searchQuery);

    Call<ResponseBody> markCustomRoomWinner(String customRoomId, String winnerId);

    Call<ResponseBody> updateCustomRoomStartTime(String customRoomId, String updatedTime);

    Call<ResponseBody> updateGameSchedule(PlayTimingDetail detail);

    Call<ResponseBody> updateUserAttribute(String path, HashMap<String, Object> body, Object data);

    Call<RecentViewersResponse> getRecentViewers();

    Call<BillingResponse> getBillingSkus();

    Call<ResponseBody> buyProduct(BillingPurchase purchase);

    Call<TopFansResponse> fetchTopFans(String username);

    Call<TopStreamersResponse> fetchTopStreamers(String nextUrl, String selectedLanguages);

    Call<TopShowResponse> fetchTopShow(String selectedLanguages);

    Call<LatestPostResponse> fetchLatestPostByUser(int userId);

    Call<AudioRoomResponse> fetchAudioRoomList(String nextUrl);

    Call<CreateAudioRoomResponse> fetchAudioRoomDetail(String groupId, String chatRoomId);

    Call<CreateAudioRoomResponse> createAudioRoom(String groupId);

    Call<ChatRoomActionResponse> leaveChatRoom(String chatRoomId);

    Call<ChatRoomActionResponse> joinChatRoom(String groupId, String chatRoomId);

    Call<ChatRoomActionResponse> muteUnMuteParticipant(String groupId, String chatRoomId, String participantUsername, String participantId, String action);

    Call<ServerListResponse<OwnerDetail>> fetchAudioRoomConnectedUsers(String chatRoomId, String nextUrl);

    void toggleFollowState(String username, int userId, boolean status, boolean isAsync, FollowStatusListener followStatusListener);

    Call<VideoCallResponse> manageVideoCalls(String channelId, int userId, String postId, String callAction);

    Call<VideoCallUsersList> getVideoCallRequestedUsersList(String postId, String nextUrl);

    Call<UserPermissionsResponse> getPermissionsResponse();

    Call<ResponseBody> setShowReminder(List<String> ids, String source);

    Call<ResponseBody> updateAudioGroupName(String groupId, String name);

    Call<AudioRoomResponse> searchRoom(String keyword);

    Call<ResponseBody> startAudioRoomGame(String action, String chatRoomId, String gameId);

    Call<ResponseBody> highlightAudioRoomUser(String chatRoomId, String action, int userId);

    Call<SignedUrlResponse> getSignedUrl(String mineType, String sourceKey, String storageType);

    Call<ResponseBody> rewindEvent(String postId, String username, String authorname, long streamDate, long seekStartedAt, long seekEndedAt, String gameName, String authorLanguage, String postUrl);

    Call<MomentsListResponse> fetchMoments(String paginatedUrl, String authorName);

    Call<ResponseBody> updateMomentState(String momentId, long startTime, long endTime);

    Call<Comments> fetchMomentComments(String postId, Double createdAt, long seekStartedAt, long seekEndedAt, String paginatedUrl);

}
