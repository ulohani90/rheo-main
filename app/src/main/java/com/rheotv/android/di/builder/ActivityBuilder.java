/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 2:41 PM
 *
 */

package com.rheotv.android.di.builder;

import com.rheotv.android.ui.activities.alertInformation.AlertInformationActivity;
import com.rheotv.android.ui.activities.alertInformation.AlertInformationModule;
import com.rheotv.android.ui.activities.audioroom.di.AudioRoomListFragmentProvider;
import com.rheotv.android.ui.activities.audioroom.di.provider.AudioChatRoomFragmentProvider;
import com.rheotv.android.ui.activities.audioroom.view.AudioChatRoomActivity;
import com.rheotv.android.ui.activities.chatActivity.ChatFragmentProvider;
import com.rheotv.android.ui.activities.clips.ClipsActivity;
import com.rheotv.android.ui.activities.clips.ClipsActivityModule;
import com.rheotv.android.ui.activities.crop.CropActivityModule;
import com.rheotv.android.ui.activities.crop.CropImageActivity;
import com.rheotv.android.ui.activities.inAppBilling.BillingActivity;
import com.rheotv.android.ui.activities.inAppBilling.di.BillingActivityModule;
import com.rheotv.android.ui.activities.moments.di.MomentsContainerFragmentProvider;
import com.rheotv.android.ui.activities.moments.di.MomentsFragmentProvider;
import com.rheotv.android.ui.activities.moments.di.StreamPlayerFragmentV3Provider;
import com.rheotv.android.ui.activities.moments.view.activities.MomentsActivity;
import com.rheotv.android.ui.activities.onboarding.v2.di.provider.OnBoardingFragmentsProvider;
import com.rheotv.android.ui.activities.player.activity.newPlayer.CallReceivingActivity;
import com.rheotv.android.ui.activities.player.activity.newPlayer.VideoChatViewActivity;
import com.rheotv.android.ui.activities.player.activity.newPlayer.activities.FullScreenVideoActivity;
import com.rheotv.android.ui.activities.player.activity.newPlayer.di.FullScreenVideoActivityModule;
import com.rheotv.android.ui.activities.player.activity.newPlayer.di.RequestToVideoCallBottomSheetProvider;
import com.rheotv.android.ui.activities.player.activity.newPlayer.di.VideoChatViewActivityModule;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.DonationEditFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.FeaturedPhotoFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.OnlinePresenceFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.view.EditProfileActivity;
import com.rheotv.android.ui.activities.profile.editprofile.di.module.EditProfileModule;
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditActivity;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.GameRuleFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.GameTimingFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.GameWiseUserFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.ProfileEditFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.module.ProfileEditActivityModule;
import com.rheotv.android.ui.activities.customroom.di.CustomRoomBottomSheetProvider;
import com.rheotv.android.ui.activities.follower.FollowActivity;
import com.rheotv.android.ui.activities.follower.di.FollowActivityModule;
import com.rheotv.android.ui.activities.follower.di.FollowFragmentProvider;
import com.rheotv.android.ui.activities.gamify.RedeemActivity;
import com.rheotv.android.ui.activities.gamify.RewardsActivity;
import com.rheotv.android.ui.activities.gamify.RewardsActivityModule;
import com.rheotv.android.ui.activities.gamify.RewardsFragmentProvider;
import com.rheotv.android.ui.activities.gamify.di.RedeemActivityModule;
import com.rheotv.android.ui.activities.gamify.di.RedeemFragmentProvider;
import com.rheotv.android.ui.activities.home.di.HomeActivityModule;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardActivity;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardActivityModule;
import com.rheotv.android.ui.activities.leaderboard.LeaderboardFragmentProvider;
import com.rheotv.android.ui.activities.moderators.AddModeratorsActivity;
import com.rheotv.android.ui.activities.moderators.AddModeratorsModule;
import com.rheotv.android.ui.activities.onboarding.OnBoardingActivity;
import com.rheotv.android.ui.activities.onboarding.OnBoardingActivityModule;
import com.rheotv.android.ui.activities.onboarding.v2.di.provider.OnBoardingFragmentsProvider;
import com.rheotv.android.ui.activities.player.activity.PlayerActivity;
import com.rheotv.android.ui.activities.player.activity.PlayerActivityModule;
import com.rheotv.android.ui.activities.player.activity.PlayerGiftBottomSheetProvider;
import com.rheotv.android.ui.activities.player.activity.StickerBottomSheetProvider;
import com.rheotv.android.ui.activities.player.activity.di.PlayerFragmentProvider;
import com.rheotv.android.ui.activities.player.activity.di.RequestPlayFragmentProvider;
import com.rheotv.android.ui.activities.player.activity.di.StreamPlayerActivityProvider;
import com.rheotv.android.ui.activities.player.activity.newPlayer.di.StreamPlayerFragmentV2Provider;
import com.rheotv.android.ui.activities.player.activity.newPlayer.di.TopFansBottomSheetProvider;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.di.ProfileContainerFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.module.EditProfileModule;
import com.rheotv.android.ui.activities.profile.editprofile.di.module.ProfileEditActivityModule;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.DonationEditFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.FeaturedPhotoFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.GameRuleFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.GameTimingFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.GameWiseUserFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.OnlinePresenceFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.PreferredGameFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.di.provider.ProfileEditFragmentProvider;
import com.rheotv.android.ui.activities.profile.editprofile.view.EditProfileActivity;
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.di.module.ProfileActivityModule;
import com.rheotv.android.ui.activities.profile.viewprofile.di.provider.AboutUserFragmentProvider;
import com.rheotv.android.ui.activities.profile.viewprofile.di.provider.UserAnalyticsFragmentProvider;
import com.rheotv.android.ui.activities.profile.viewprofile.di.provider.UserChatFragmentProvider;
import com.rheotv.android.ui.activities.profile.viewprofile.di.provider.UserProfileFragmentProvider;
import com.rheotv.android.ui.activities.profile.viewprofile.di.provider.UserWalletFragmentProvider;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.rank.RankActivity;
import com.rheotv.android.ui.activities.rank.RankActivityModule;
import com.rheotv.android.ui.activities.rank.RankFragmentProvider;
import com.rheotv.android.ui.activities.scoreboard.di.ScoreFragmentProvider;
import com.rheotv.android.ui.activities.scoreboard.di.ScoreboardFragmentProvider;
import com.rheotv.android.ui.activities.search.SearchActivity;
import com.rheotv.android.ui.activities.search.SearchActivityModule;
import com.rheotv.android.ui.activities.search.fragment.SearchFragmentProvider;
import com.rheotv.android.ui.activities.selectGame.GameSelectionActivity;
import com.rheotv.android.ui.activities.selectGame.di.GameSelectionActivityModule;
import com.rheotv.android.ui.activities.selectGame.di.GameSelectionFragmentProvider;
import com.rheotv.android.ui.activities.selectGame.di.LanguageSelectionFragmentProvider;
import com.rheotv.android.ui.activities.share.di.ClipShareFragmentProvider;
import com.rheotv.android.ui.activities.share.di.PostShareFragmentProvider;
import com.rheotv.android.ui.activities.share.di.ShareFragmentProvider;
import com.rheotv.android.ui.activities.splash.SplashActivity;
import com.rheotv.android.ui.activities.splash.SplashActivityModule;
import com.rheotv.android.ui.activities.story.CreateStoryActivity;
import com.rheotv.android.ui.activities.story.CreateStoryActivityModule;
import com.rheotv.android.ui.activities.story.CreateStoryFragmentProvider;
import com.rheotv.android.ui.activities.story.CreateStoryTemplateActivity;
import com.rheotv.android.ui.activities.story.CreateStoryTemplateActivityModule;
import com.rheotv.android.ui.activities.story.StoryActivity;
import com.rheotv.android.ui.activities.story.StoryActivityModule;
import com.rheotv.android.ui.activities.story.StoryPagerFragmentProvider;
import com.rheotv.android.ui.activities.streamEnd.StreamEndActivity;
import com.rheotv.android.ui.activities.streamEnd.di.StreamEndActivityModule;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivityModule;
import com.rheotv.android.ui.activities.tabcontainer.clips.ClipsFragmentProvider;
import com.rheotv.android.ui.activities.tabcontainer.posts.PostListFragmentProvider;
import com.rheotv.android.ui.activities.tabcontainer.profile.ProfileProvider;
import com.rheotv.android.ui.activities.tabcontainer.profile.analytics.AnalyticsFragmentProvider;
import com.rheotv.android.ui.activities.tabcontainer.profile.bio.BioFragmentProvider;
import com.rheotv.android.ui.activities.tabcontainer.profile.container.ProfileContainerProvider;
import com.rheotv.android.ui.activities.tabcontainer.profile.videos.VideosFragmentProvider;
import com.rheotv.android.ui.activities.tabcontainer.profile.wallet.WalletDetailsInputActivity;
import com.rheotv.android.ui.activities.tabcontainer.profile.wallet.WalletDetailsInputActivityModule;
import com.rheotv.android.ui.activities.tabcontainer.profile.wallet.WalletFragmentProvider;
import com.rheotv.android.ui.activities.tabcontainer.profile.wallet.WalletFragmentV2Provider;
import com.rheotv.android.ui.activities.tabcontainer.trending.TrendingListFragmentProvider;
import com.rheotv.android.ui.activities.tabcontainer.videoUpload.VideoUploadFragmentProvider;
import com.rheotv.android.ui.activities.trim.TrimVideoActivity;
import com.rheotv.android.ui.activities.trim.TrimVideoActivityModule;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivityModule;
import com.rheotv.android.ui.activities.universalActivity.fragment.UniversalFragmentProvider;
import com.rheotv.android.ui.fragments.DownloadVideoFragmentProvider;
import com.rheotv.android.ui.fragments.LiveStreamProvider;
import com.rheotv.android.ui.fragments.LoginFragmentProvider;
import com.rheotv.android.ui.fragments.TopStreamerSelectionFragmentProvider;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuilder {

   /*
    * Shows how to add fragment dependencies
    *
    @ContributesAndroidInjector(modules = {
            FeedActivityModule.class,
            PostListFragmentProvider.class,
            OpenSourceFragmentProvider.class})
    abstract TabContainerActivity bindFeedActivity();*/

    @ContributesAndroidInjector(modules = SplashActivityModule.class)
    abstract SplashActivity bindSplashActivity();

    @ContributesAndroidInjector(modules = {
            TabContainerActivityModule.class,
            PostListFragmentProvider.class,
            TrendingListFragmentProvider.class,
            ProfileContainerProvider.class,
            ProfileProvider.class,
            BioFragmentProvider.class,
            ProfileEditFragmentProvider.class,
            DonationEditFragmentProvider.class,
            VideosFragmentProvider.class,
            DownloadVideoFragmentProvider.class,
            AnalyticsFragmentProvider.class,
            WalletFragmentV2Provider.class,
            WalletFragmentProvider.class,
            ClipsFragmentProvider.class,
            ClipShareFragmentProvider.class,
            ShareFragmentProvider.class,
            SearchFragmentProvider.class,
            LoginFragmentProvider.class,
            LiveStreamProvider.class,
            VideoUploadFragmentProvider.class,
            ProfileContainerFragmentProvider.class,
            UserProfileFragmentProvider.class,
            AboutUserFragmentProvider.class,
            UserChatFragmentProvider.class,
            UserAnalyticsFragmentProvider.class,
            UserWalletFragmentProvider.class,
            StickerBottomSheetProvider.class,
            GameTimingFragmentProvider.class,
            GameRuleFragmentProvider.class,
            GameWiseUserFragmentProvider.class,
            OnlinePresenceFragmentProvider.class
    })
    abstract TabContainerActivity bindBottomNavigationContainer();

    @ContributesAndroidInjector(modules = {
            ProfileProvider.class,
            LiveStreamProvider.class,
            HomeActivityModule.class,
            BioFragmentProvider.class,
            ScoreFragmentProvider.class,
            LoginFragmentProvider.class,
            ClipsFragmentProvider.class,
            ShareFragmentProvider.class,
            VideosFragmentProvider.class,
            WalletFragmentProvider.class,
            SearchFragmentProvider.class,
            ProfileContainerProvider.class,
            PostListFragmentProvider.class,
            WalletFragmentV2Provider.class,
            UserChatFragmentProvider.class,
            GameRuleFragmentProvider.class,
            ClipShareFragmentProvider.class,
            AnalyticsFragmentProvider.class,
            PostShareFragmentProvider.class,
            AboutUserFragmentProvider.class,
            TopFansBottomSheetProvider.class,
            ScoreboardFragmentProvider.class,
            StickerBottomSheetProvider.class,
            UserWalletFragmentProvider.class,
            GameTimingFragmentProvider.class,
            ProfileEditFragmentProvider.class,
            RequestPlayFragmentProvider.class,
            VideoUploadFragmentProvider.class,
            UserProfileFragmentProvider.class,
            //StreamPlayerFragmentProvider.class,
            StreamPlayerFragmentV2Provider.class,
            TrendingListFragmentProvider.class,
            StreamPlayerActivityProvider.class,
            DonationEditFragmentProvider.class,
            GameWiseUserFragmentProvider.class,
            CustomRoomBottomSheetProvider.class,
            PlayerGiftBottomSheetProvider.class,
            DownloadVideoFragmentProvider.class,
            UserAnalyticsFragmentProvider.class,
            OnlinePresenceFragmentProvider.class,
            ProfileContainerFragmentProvider.class,
            FeaturedPhotoFragmentProvider.class,
            PreferredGameFragmentProvider.class,
            AudioRoomListFragmentProvider.class,
            RequestToVideoCallBottomSheetProvider.class
    })
    abstract HomeActivity bindHomeActivity();

    @ContributesAndroidInjector(modules = {
            StreamPlayerActivityProvider.class,
//            StreamPlayerFragmentProvider.class,
            TopFansBottomSheetProvider.class,
            StreamPlayerFragmentV2Provider.class,
            StickerBottomSheetProvider.class,
            PlayerGiftBottomSheetProvider.class,
            RequestPlayFragmentProvider.class,
            ScoreFragmentProvider.class,
            ScoreboardFragmentProvider.class,
            LoginFragmentProvider.class,
            CustomRoomBottomSheetProvider.class,
            ShareFragmentProvider.class,
            PostShareFragmentProvider.class,
            RequestToVideoCallBottomSheetProvider.class
    })
    abstract StreamPlayerActivity bindStreamPlayerActivity();

    @ContributesAndroidInjector(modules = {
            MomentsContainerFragmentProvider.class,
            MomentsFragmentProvider.class,
            StreamPlayerFragmentV3Provider.class,
            TopFansBottomSheetProvider.class,
            StreamPlayerFragmentV2Provider.class,
            StickerBottomSheetProvider.class,
            PlayerGiftBottomSheetProvider.class,
            RequestPlayFragmentProvider.class,
            ScoreFragmentProvider.class,
            ScoreboardFragmentProvider.class,
            LoginFragmentProvider.class,
            CustomRoomBottomSheetProvider.class,
            ShareFragmentProvider.class,
            PostShareFragmentProvider.class,
            RequestToVideoCallBottomSheetProvider.class
    })
    abstract MomentsActivity bindMomentsActivity();

    @ContributesAndroidInjector(modules = {PlayerActivityModule.class, LeaderboardFragmentProvider.class,
            ProfileContainerProvider.class,
            ProfileProvider.class,
            BioFragmentProvider.class,
            VideosFragmentProvider.class,
            DownloadVideoFragmentProvider.class,
            AnalyticsFragmentProvider.class,
            ProfileContainerFragmentProvider.class,
            WalletFragmentV2Provider.class, WalletFragmentProvider.class,
            PlayerFragmentProvider.class, LoginFragmentProvider.class})
    abstract PlayerActivity bindPlayerActivity();

    @ContributesAndroidInjector(modules = {LeaderBoardActivityModule.class, LeaderboardFragmentProvider.class,
            ProfileContainerProvider.class,
            ProfileProvider.class,
            BioFragmentProvider.class,
            VideosFragmentProvider.class,
            DownloadVideoFragmentProvider.class,
            AnalyticsFragmentProvider.class,
            WalletFragmentV2Provider.class, WalletFragmentProvider.class, LoginFragmentProvider.class})
    abstract LeaderBoardActivity getLeaderBoardActivity();

    @ContributesAndroidInjector(modules = {UniversalActivityModule.class, UniversalFragmentProvider.class, ProfileContainerProvider.class,
            ProfileProvider.class,
            BioFragmentProvider.class,
            VideosFragmentProvider.class,
            DownloadVideoFragmentProvider.class,
            AnalyticsFragmentProvider.class,
            ChatFragmentProvider.class,
            WalletFragmentV2Provider.class,
            WalletFragmentProvider.class,
            LeaderboardFragmentProvider.class,
            LoginFragmentProvider.class})
    abstract UniversalActivity getUniversalActivity();


    @ContributesAndroidInjector(modules = {AlertInformationModule.class})
    abstract AlertInformationActivity getAlertInformationActivity();

    @ContributesAndroidInjector(modules = {
            ProfileActivityModule.class,
            UserProfileFragmentProvider.class,
            AboutUserFragmentProvider.class,
            UserChatFragmentProvider.class,
            UserAnalyticsFragmentProvider.class,
            UserWalletFragmentProvider.class,
            StickerBottomSheetProvider.class,
            LiveStreamProvider.class,
            ProfileContainerProvider.class,
            ProfileProvider.class,
            BioFragmentProvider.class,
            VideosFragmentProvider.class,
            DownloadVideoFragmentProvider.class,
            AnalyticsFragmentProvider.class,
            WalletFragmentProvider.class,
            WalletFragmentV2Provider.class,
            LoginFragmentProvider.class,
            VideoUploadFragmentProvider.class,
            ProfileContainerFragmentProvider.class,
            AboutUserFragmentProvider.class,
            UserAnalyticsFragmentProvider.class,
            UserChatFragmentProvider.class,
            UserProfileFragmentProvider.class,
            UserWalletFragmentProvider.class,
            DonationEditFragmentProvider.class,
            GameRuleFragmentProvider.class,
            GameTimingFragmentProvider.class,
            GameWiseUserFragmentProvider.class,
            OnlinePresenceFragmentProvider.class,
            ProfileEditFragmentProvider.class,
            FeaturedPhotoFragmentProvider.class,
            PreferredGameFragmentProvider.class})
    abstract ProfileActivity getProfileActivity();

    @ContributesAndroidInjector(modules = {
            ProfileEditFragmentProvider.class,
            ProfileEditActivityModule.class,
            GameWiseUserFragmentProvider.class,
            GameRuleFragmentProvider.class,
            GameTimingFragmentProvider.class,
            StickerBottomSheetProvider.class,
            PreferredGameFragmentProvider.class
    })
    abstract ProfileEditActivity getProfileEditActivity();

    @ContributesAndroidInjector(modules = {SearchActivityModule.class,
            SearchFragmentProvider.class,
            ProfileContainerProvider.class,
            ProfileProvider.class,
            BioFragmentProvider.class,
            VideosFragmentProvider.class,
            DownloadVideoFragmentProvider.class,
            AnalyticsFragmentProvider.class,
            WalletFragmentV2Provider.class, WalletFragmentProvider.class, LoginFragmentProvider.class})
    abstract SearchActivity getSearchActivity();


    @ContributesAndroidInjector(modules = {EditProfileModule.class})
    abstract EditProfileActivity editProfileActivity();


    @ContributesAndroidInjector(modules = {ClipsActivityModule.class,
            ClipShareFragmentProvider.class,
            ShareFragmentProvider.class,
            ClipsFragmentProvider.class, LoginFragmentProvider.class})
    abstract ClipsActivity clipsActivity();


    @ContributesAndroidInjector(modules = {OnBoardingActivityModule.class})
    abstract OnBoardingActivity onBoardingActivity();

//    @ContributesAndroidInjector(modules = {LoginActivityModule.class})
//    abstract LoginActivity bindLoginActivity();

    @ContributesAndroidInjector(modules = {RewardsActivityModule.class, RewardsFragmentProvider.class})
    abstract RewardsActivity bindRewardsActivity();

    @ContributesAndroidInjector(modules = {RedeemActivityModule.class, RedeemFragmentProvider.class})
    abstract RedeemActivity bindRedeemActivity();

    @ContributesAndroidInjector(modules = {RankActivityModule.class, RankFragmentProvider.class, LiveStreamProvider.class})
    abstract RankActivity bindRankActivity();

    @ContributesAndroidInjector(modules = {GameSelectionActivityModule.class,
            LoginFragmentProvider.class,
            GameSelectionFragmentProvider.class,
            LanguageSelectionFragmentProvider.class,
            TopStreamerSelectionFragmentProvider.class,
            OnBoardingFragmentsProvider.class,
            GameSelectionFragmentProvider.class,
            LanguageSelectionFragmentProvider.class})
    abstract GameSelectionActivity bindingGameSelectionActivity();

    @ContributesAndroidInjector(modules = {AddModeratorsModule.class})
    abstract AddModeratorsActivity bidingAddModeratorActivity();

    @ContributesAndroidInjector(modules = {StoryActivityModule.class, LoginFragmentProvider.class, StoryPagerFragmentProvider.class})
    abstract StoryActivity bidingStoryActivity();

    @ContributesAndroidInjector(modules = {CreateStoryActivityModule.class, CreateStoryFragmentProvider.class})
    abstract CreateStoryActivity bidingCreateStoryActivity();

    @ContributesAndroidInjector(modules = {CropActivityModule.class})
    abstract CropImageActivity bidingCropImageActivity();

    @ContributesAndroidInjector(modules = {TrimVideoActivityModule.class})
    abstract TrimVideoActivity bidingTrimVideoActivity();

    @ContributesAndroidInjector(modules = {CreateStoryTemplateActivityModule.class})
    abstract CreateStoryTemplateActivity bidingCreateStoryTemplateActivity();

    @ContributesAndroidInjector(modules = {FollowActivityModule.class, FollowFragmentProvider.class})
    abstract FollowActivity bidingFollowActivity();

    @ContributesAndroidInjector(modules = {StreamEndActivityModule.class})
    abstract StreamEndActivity bidingStreamEndActivity();

    @ContributesAndroidInjector(modules = {WalletDetailsInputActivityModule.class})
    abstract WalletDetailsInputActivity bindWalletDetailsInputActivity();

    @ContributesAndroidInjector(modules = {BillingActivityModule.class})
    abstract BillingActivity bindBillingActivity();

//    @ContributesAndroidInjector(modules = {LoginFragmentProvider.class})
//    abstract OnBoardingActivityV2 bindOnBoardingActivityV2();

    @ContributesAndroidInjector(modules = {
            AudioChatRoomFragmentProvider.class,
            StickerBottomSheetProvider.class
    })
    abstract AudioChatRoomActivity bindAudioChatRoomActivity();

    @ContributesAndroidInjector(modules = {VideoChatViewActivityModule.class})
    abstract VideoChatViewActivity bindVideoChatViewActivity();

    @ContributesAndroidInjector(modules = {VideoChatViewActivityModule.class})
    abstract CallReceivingActivity bindCallReceivingActivity();

    @ContributesAndroidInjector(modules = {FullScreenVideoActivityModule.class})
    abstract FullScreenVideoActivity bindFullScreenVideoActivity();

}
