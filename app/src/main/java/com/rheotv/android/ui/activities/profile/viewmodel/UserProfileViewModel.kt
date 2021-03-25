package com.rheotv.android.ui.activities.profile.viewmodel

import android.text.SpannableString
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.databinding.PropertyChangeRegistry
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.gamify.Reward
import com.rheotv.android.data.network.models.general.GameDetails
import com.rheotv.android.data.network.models.general.SignedUrlResponse
import com.rheotv.android.data.network.models.postlisting.responses.User
import com.rheotv.android.data.network.models.useProfile.responses.GameWiseUser
import com.rheotv.android.data.network.models.useProfile.responses.PictureUploadResult
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult
import com.rheotv.android.db.AppDatabase
import com.rheotv.android.db.UserFollowDao
import com.rheotv.android.db.UserFollowItem
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment.Companion.CODE_COVER_PICTURE
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment.Companion.CODE_FEATURED_IMAGE
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment.Companion.CODE_PROFILE_PICTURE
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment.Companion.ENDPOINT_BIO
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment.Companion.ENDPOINT_CONFIGURATION_USED
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment.Companion.ENDPOINT_COVER_PHOTO
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment.Companion.ENDPOINT_FEATURED_PHOTO
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment.Companion.ENDPOINT_LANGUAGE
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment.Companion.ENDPOINT_PHONE
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment.Companion.ENDPOINT_PROFILE_PHOTO
import com.rheotv.android.ui.activities.profile.editprofile.view.ProfileEditFragment.Companion.ENDPOINT_USER_INFO
import com.rheotv.android.ui.activities.profile.model.*
import com.rheotv.android.ui.activities.profile.viewprofile.model.GameRule
import com.rheotv.android.ui.activities.profile.viewprofile.utils.UserAction
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.*
import com.rheotv.android.utils.AppConstants.SHARE_MODERATOR_DESCRIPTION_PROFILE
import com.rheotv.android.utils.AppConstants.SHARE_MODERATOR_TITLE_PROFILE
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_GAME_RULE_ADDED
import com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_GAME_TIMING_SAVED
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class UserProfileViewModel constructor(
        dataManager: DataManager?,
        schedulerProvider: SchedulerProvider?
) : BaseViewModel<Any>(dataManager, schedulerProvider), Observable {
    private val callbacks = PropertyChangeRegistry()
    var profile: ObservableField<ProfileResult> = ObservableField()
    var analyticsProperties: MutableMap<String, Any> = hashMapOf()
    var queryParam: String? = null
    val games = MutableLiveData<List<GameDetails>?>()
    val socialMediaList = MutableLiveData<List<SocialMedia>?>()
    val userAction = MutableLiveData<Pair<UserAction, GameWiseUser>?>()
    val gameUsername = ObservableField<String>()
    var currentGame: GameDetails? = null
    var currentSocialMedia: SocialMedia? = null
    var currentSocialMediaLink = ObservableField<String>()
    var inGameUserEditMode = ObservableField<Boolean>(false)
    var inRuleEditMode = ObservableField<Boolean>(false)
    var inSocialMediaMode = ObservableField<Boolean>(false)
    val selectedRule = MutableLiveData<Pair<UserAction, GameRule>>()
    val selectedOnlinePresence = MutableLiveData<Pair<UserAction, SocialMedia>?>()
    var currentRule = ObservableField<String>()
    var source: String? = null
    val featuredPhoto = MutableLiveData<FeaturedPhoto?>()
    val profileState = ObservableField<Status>()
    val gameScheduleStatus: ObservableField<Status> = ObservableField()
    val map = mapOf(
            CODE_COVER_PICTURE to ENDPOINT_COVER_PHOTO,
            CODE_PROFILE_PICTURE to ENDPOINT_PROFILE_PHOTO,
            CODE_FEATURED_IMAGE to ENDPOINT_FEATURED_PHOTO

    )
    val apiSet = hashSetOf<String>()
    val dbFollowStatus = ObservableField<Boolean>(false)

    var isFirstApiCalled = false
    var tabPosition = 0

    val dao: UserFollowDao = AppDatabase.getInstance(RheoTvApp.getNonUiContext()).userFollowDao()
    var onSave = MutableLiveData<Status?>(Status.EMPTY)

    var gameResults = MutableLiveData<List<GameDetails>>()
    var submittingGame = ObservableField<Status>()
    var loadingGame = ObservableField<Status>()

    var isFollowed: Boolean
        @Bindable
        get() = profile.get()?.isFollowed ?: false
        set(value) {
            viewModelScope.launch(Dispatchers.IO) {
                dao.updateUserEntry(UserFollowItem(profile.get()?.user?.id ?: 0,
                        profile.get()?.user?.username, value))
                withContext(Dispatchers.Main) {
                    profile.get()?.isFollowed = value
                    notifyPropertyChanged(BR.followed)
                }
            }
        }

    var fullName: String
        @Bindable
        get() = profile.get()?.user?.name ?: ""
        set(value) {
            profile.get()?.user?.name = value
            notifyPropertyChanged(BR.fullName)
            apiSet.add(ENDPOINT_USER_INFO)
        }

    var userName: String
        @Bindable
        get() = profile.get()?.user?.username ?: ""
        set(value) {
            profile.get()?.user?.username = value
            notifyPropertyChanged(BR.userName)
            apiSet.add(ENDPOINT_USER_INFO)
        }

    val userId: Int
        @Bindable
        get() = profile.get()?.user?.id ?: 0

    val canVoteForModerator: Boolean
        get() = profile.get()?.isEnableContentModerator == true && profile.get()?.isSelfProfile == false && profile.get()?.contentModerator == false && profile.get()?.isVotedAsModerator == false

    var phoneNumber: String
        @Bindable
        get() = profile.get()?.phone ?: ""
        set(value) {
            profile.get()?.phone = value
            notifyPropertyChanged(BR.phoneNumber)
            apiSet.add(ENDPOINT_PHONE)
        }

    var intro: String
        @Bindable
        get() = profile.get()?.intro ?: ""
        set(value) {
            profile.get()?.intro = value
            notifyPropertyChanged(BR.intro)
            apiSet.add(ENDPOINT_USER_INFO)
        }

    var bio: String
        @Bindable
        get() = profile.get()?.bio ?: ""
        set(value) {
            profile.get()?.bio = value
            notifyPropertyChanged(BR.bio)
            apiSet.add(ENDPOINT_BIO)
        }

    var configurationUsed: String
        @Bindable
        get() = profile.get()?.profileDetail?.configurationUsed ?: ""
        set(value) {
            profile.get()?.profileDetail?.configurationUsed = value
            notifyPropertyChanged(BR.configurationUsed)
            apiSet.add(ENDPOINT_CONFIGURATION_USED)
        }

    var coverPicture: String?
        @Bindable
        get() = profile.get()?.coverPic
        set(value) {
            profile.get()?.coverPic = value
            notifyPropertyChanged(BR.coverPicture)
        }

    var profilePicture: String?
        @Bindable
        get() {
            Log.i(javaClass.simpleName, "profilePicture: ${profile.get()?.isSelfProfile} and ${CommonUtils.getUserProfilePic()} and ${profile.get()?.profilePic}")
            return if (profile.get()?.isSelfProfile == true) CommonUtils.getUserProfilePic() ?: profile.get()?.profilePic else profile.get()?.profilePic
        }
        set(value) {
            CommonUtils.setProfileImageUrl(value)
            profile.get()?.profilePic = value
            notifyPropertyChanged(BR.profilePicture)
        }

    var audioMessage: AudioMessage?
        @Bindable
        get() = profile.get()?.profileDetail?.audioMessage
        set(value) {
            profile.get()?.profileDetail?.audioMessage = value
            notifyPropertyChanged(BR.audioMessage)
        }

    var isLiveReminderSet: Boolean
        @Bindable
        get() = profile.get()?.profileDetail?.liveReminderAdded ?: false
        set(value) {
            profile.get()?.profileDetail?.liveReminderAdded = value
//            notifyPropertyChanged(BR.isLiveReminderSet)
        }

    var userDonation: UserDonation
        @Bindable
        get() = profile.get()?.profileDetail?.donation ?: UserDonation()
        set(value) {
            profile.get()?.profileDetail?.donation = value
            notifyPropertyChanged(BR.userDonation)
        }

    val totalGamingDays: MutableList<GamingDays>
        @Bindable
        get() = profile.get()?.profileDetail?.gameSchedule?.getTotalDays() ?: mutableListOf()

    var playStartTime: String
        @Bindable
        get() = profile.get()?.profileDetail?.gameSchedule?.startTime ?: ""
        set(value) {
            profile.get()?.profileDetail?.gameSchedule?.startTime = value
            notifyPropertyChanged(BR.playStartTime)
        }

    var playEndTime: String
        @Bindable
        get() = profile.get()?.profileDetail?.gameSchedule?.endTime ?: ""
        set(value) {
            profile.get()?.profileDetail?.gameSchedule?.endTime = value
            notifyPropertyChanged(BR.playEndTime)
        }

    var playStartAmPm: String
        @Bindable
        get() = profile.get()?.profileDetail?.gameSchedule?.startAMPM ?: "AM"
        set(value) {
            profile.get()?.profileDetail?.gameSchedule?.startAMPM = value
            notifyPropertyChanged(BR.playStartAmPm)
        }

    val streakReward: Reward?
        @Bindable
        get() = RewardManager.getInstance()?.userStreakReward

    var playEndAmPm: String
        @Bindable
        get() = profile.get()?.profileDetail?.gameSchedule?.endAMPM ?: "AM"
        set(value) {
            profile.get()?.profileDetail?.gameSchedule?.endAMPM = value
            notifyPropertyChanged(BR.playEndAmPm)
        }

    val gameDaysPreview: String
        get() = totalGamingDays.filter { it.isDaySelected }.joinToString {
            it.day?.subSequence(0, 2) ?: ""
        }

    val gameTimeSpannable: SpannableString?
        get() = profile.get()?.profileDetail?.gameSchedule?.getSpannableTime()

    val photoUrls: List<String>
        get() = profile.get()?.profileDetail?.featuredPhotos?.mapNotNull { it.pictureUrl }
                ?: mutableListOf()
    val photoIds: List<String>
        get() = profile.get()?.profileDetail?.featuredPhotos?.mapNotNull { it.id }
                ?: mutableListOf()
    val featuredPhotos: MutableList<FeaturedPhoto>?
        get() = profile.get()?.profileDetail?.featuredPhotos

    val selectedGameUrls: List<String>
        get() = profile.get()?.selectedGames?.map { it.thumbnail } ?: mutableListOf()

    val audioRoomId: String?
        get() = profile.get()?.activeChatRooms?.id

    val groupId: String?
        get() = profile.get()?.activeChatRooms?.groupDetails?.id

    fun loadProfile() {
        profileState.set(Status.LOADING)
        isFirstApiCalled = true
        dataManager.getProfile(queryParam ?: CommonUtils.getUserName())
                .enqueue(object : Callback<ProfileResult> {
                    override fun onFailure(call: Call<ProfileResult>, t: Throwable) {
                        if (NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext()))
                            profileState.set(Status.ERROR)
                        else
                            profileState.set(Status.OFFLINE)
                    }

                    override fun onResponse(call: Call<ProfileResult>, response: Response<ProfileResult>) {
                        if (response.isSuccessful) {
                            profile.set(response.body())
                            insertInitialFollowStatus()
                            notifyChange()
                            if (response.body()?.isSelfProfile == true) {
                                if ((response.body()?.followersCount ?: 0) < 100)
                                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_RECENT_VIEWERS_UNLOCK_VIEW_SHOWN, HashMap(analyticsProperties))
                                else
                                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_RECENT_VIEWERS_VIEW_TAB_SHOWN, HashMap(analyticsProperties));
                            }

                            profileState.set(Status.SUCCESS)
                        } else
                            profileState.set(Status.ERROR)
                    }
                })
    }

    fun insertInitialFollowStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.checkIfIsFollowedWithUserId(profile.get()?.user?.id ?: 0)?.also {
                viewModelScope.launch(Dispatchers.Main) {
                    profile.get()?.isFollowed = it.isFollowed
                    isFollowed = it.isFollowed
                }
                return@launch
            }
            dao.insertUserWithIgnore(UserFollowItem(profile.get()?.user?.id
                    ?: 0, profile.get()?.user?.username, profile.get()?.isFollowed == true))
        }
    }

    fun onFollowButtonClick() {
        if (CommonUtils.isUserLoggedin()) {
            viewModelScope.launch(Dispatchers.IO) {
                dao.updateUserEntry(UserFollowItem(profile.get()?.user?.id ?: 0,
                        profile.get()?.user?.username, !isFollowed))
                isFollowed = !isFollowed
                withContext(Dispatchers.Main) {
                    dataManager.toggleFollow(if (isFollowed) "unfollow" else "follow", profile.get()?.user?.id.toString()).enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            t.printStackTrace()
                        }
                    })
                }
            }

        }
    }

    fun onReminderClicked(fragment: WeakReference<Fragment>) {
        fragment.get()?.setReminder(
                eventId = profile.get()?.user?.id ?: 999,
                title = userName,
                description = "$userName is live on Rheo",
                weekDays = totalGamingDays.filter { it.isDaySelected }.joinToString(",") {
                    it.day?.toUpperCase(Locale.ROOT)?.subSequence(0, 2) ?: ""
                },
                from = TimeUtils.getDateFromString("$playStartTime $playStartAmPm", TimeUtils.HH_MM_AA),
                to = TimeUtils.getDateFromString("$playEndTime $playEndAmPm", TimeUtils.HH_MM_AA)
        )
    }

    fun onEditGameClick() {
        inGameUserEditMode.set(!(inGameUserEditMode.get() ?: false))
    }

    fun onEditSocialMediaClick() {
        inSocialMediaMode.set(!(inSocialMediaMode.get() ?: false))
    }

    fun onEditRuleClick() {
        inRuleEditMode.set(!(inRuleEditMode.get() ?: false))
    }

    fun clearGameWiseUserData() {
        games.value = null
        userAction.value = null
    }

    fun loadGames() {
        dataManager.gameDetails.enqueue(object : Callback<List<GameDetails>> {
            override fun onResponse(call: Call<List<GameDetails>>, response: Response<List<GameDetails>>) {
                if (response.isSuccessful) {
                    games.value = response.body()
                }
            }

            override fun onFailure(call: Call<List<GameDetails>>, t: Throwable) {

            }
        })
    }

    fun clearSocialMediaData() {
        socialMediaList.value = null
        selectedOnlinePresence.value = null
    }

    fun loadSocialMedia() {
        dataManager.socialMedia.enqueue(object : Callback<List<SocialMedia>> {
            override fun onResponse(call: Call<List<SocialMedia>>, response: Response<List<SocialMedia>>) {
                if (response.isSuccessful)
                    socialMediaList.value = response.body()
            }

            override fun onFailure(call: Call<List<SocialMedia>>, t: Throwable) {

            }
        })
    }

    fun onAddGameUser() {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_GAME_WISE_USERNAME_ADDED, analyticsProperties)
        when {
            currentGame == null && currentGame?.id.isNullOrEmpty() -> RheoTvApp.getNonUiContext().showToast(R.string.alert_select_game)
            gameUsername.get().isNullOrEmpty() -> RheoTvApp.getNonUiContext().showToast(R.string.alert_username)
            else -> {
                updateUserGame(
                        GameWiseUser(currentGame?.thumbnail, currentGame?.name, currentGame?.id, gameUsername.get()),
                        UserAction.Add)
            }
        }
    }

    fun updateUserGame(game: GameWiseUser, action: UserAction) {
        dataManager.onUserGameAction(game.id, game.gameUsername, action.toString()).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful)
                    userAction.value = Pair(action, game)
                if (response.code() == 400)
                    RheoTvApp.getNonUiContext().showToast(R.string.game_already_added)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun onAddRuleClicked() {
        SegmentTracker.getInstance().trackEvent(EVENT_GAME_RULE_ADDED, analyticsProperties)
        when {
            currentRule.get().isNullOrEmptyOrBlank() -> RheoTvApp.getNonUiContext().showToast(R.string.alert_game_rule)
            else -> updateGameRules(GameRule(rule = currentRule.get()), UserAction.Add)
        }
    }

    fun updateGameRules(gameRule: GameRule, action: UserAction) {
        dataManager.updateGameRule(gameRule, action).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    selectedRule.value = Pair(action, gameRule)
                    currentRule.set("")
                }
            }
        })
    }

    private fun updateUserDonation() {
        donationStatus.set(Status.LOADING)
        dataManager.updateUserDonation(userDonation).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                donationStatus.set(Status.ERROR)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    donationStatus.set(Status.SUCCESS)
                    RheoTvApp.getNonUiContext().showToast(R.string.donation_update_success)
                } else {
                    donationStatus.set(Status.ERROR)
                }
            }
        })
    }

    val donationStatus: ObservableField<Status> = ObservableField()

    fun onAddDonationClick() {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_ADD_DONATION_DETAIL_UPDATE, analyticsProperties)
        when {
            userDonation.link.isNullOrEmptyOrBlank() -> RheoTvApp.getNonUiContext().showToast("Please enter donation link")
            userDonation.link?.startsWith("http") == false -> RheoTvApp.getNonUiContext().showToast("Please enter a valid donation link")
            userDonation.title.isNullOrEmptyOrBlank() -> RheoTvApp.getNonUiContext().showToast("Please enter donation title")
            else -> updateUserDonation()
        }
    }

    fun onAddOnlinePresenceClicked() {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_ONLINE_PRESENCE_SOCIAL_MEDIA_ADDED, analyticsProperties)
        when {
            currentSocialMedia == null -> RheoTvApp.getNonUiContext().showToast("Please select a social media platform")
            currentSocialMediaLink.get().isNullOrEmptyOrBlank() -> RheoTvApp.getNonUiContext().showToast("Please enter a social media platform link")
            currentSocialMedia?.text?.toLowerCase(Locale.getDefault()) != AppConstants.WHATSAPP.toLowerCase(Locale.getDefault())
                    && currentSocialMediaLink.get()?.startsWith("http") == false -> RheoTvApp.getNonUiContext().showToast("Please enter a valid social media platform link")
            currentSocialMedia?.text?.toLowerCase(Locale.getDefault()) == AppConstants.WHATSAPP.toLowerCase(Locale.getDefault()) &&
                    currentSocialMediaLink.get()?.length != 10 -> RheoTvApp.getNonUiContext().showToast("Please enter 10 digit WhatsApp number")
            else -> updateOnlinePresence(currentSocialMedia.also { it?.link = currentSocialMediaLink.get() }!!)
        }
    }

    fun updateOnlinePresence(socialMedia: SocialMedia, action: UserAction = UserAction.Add) {
        dataManager.updateOnlinePresence(socialMedia, action).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    selectedOnlinePresence.value = Pair(action, socialMedia)
                    currentSocialMediaLink.set("")
                } else if (response.code() == 400)
                    RheoTvApp.getNonUiContext().showToast(R.string.social_media_already_added)
            }
        })
    }

    fun audioUrlTask(fileUri: String?, uploadUrl: String? = null, action: UserAction = UserAction.SignedUrl, onFileUpload: ((String?, String?) -> Unit)? = null) {
        dataManager.getSignedUrl(uploadUrl, action, File(fileUri).getMediaDuration()).enqueue(object : Callback<SignedUrlResponse> {
            override fun onFailure(call: Call<SignedUrlResponse>, t: Throwable) {

            }

            override fun onResponse(call: Call<SignedUrlResponse>, response: Response<SignedUrlResponse>) {
                if (response.isSuccessful)
                    onFileUpload?.invoke(fileUri, response.body()?.uploadUrl)
            }
        })
    }

    fun uploadFile(part: MultipartBody.Part, path: String, code: Int) {
        dataManager.uploadFile(part, map.getValue(code)).enqueue(object : Callback<PictureUploadResult> {
            override fun onResponse(call: Call<PictureUploadResult>, response: Response<PictureUploadResult>) {
                if (response.isSuccessful) {
                    when (code) {
                        CODE_COVER_PICTURE -> coverPicture = path
                        CODE_PROFILE_PICTURE -> profilePicture = path
                        CODE_FEATURED_IMAGE -> featuredPhoto.value = FeaturedPhoto(response.body()?.id, response.body()?.url)

                    }
                    if (code == CODE_FEATURED_IMAGE) {
                        response.body()?.id?.let { analyticsProperties.put("pictureId", it) }
                        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_FEATURED_PHOTO_ADDED, HashMap(analyticsProperties))
                    }
                    RheoTvApp.getNonUiContext().showToast("Success")
                }
            }

            override fun onFailure(call: Call<PictureUploadResult>, t: Throwable) {
                if (NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext()))
                    RheoTvApp.getNonUiContext().showToast("Error While Uploading:: " + t.localizedMessage)
                else
                    RheoTvApp.getNonUiContext().showToast("No Network" + t.localizedMessage)


                t.printStackTrace()
            }
        })
    }

    fun deletePhoto(id: String?) {
        dataManager.deleteFile("featured-photos", id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    this@UserProfileViewModel.featuredPhoto.value?.isDelete ?: true
                    this@UserProfileViewModel.featuredPhoto.value = FeaturedPhoto(id, null, true)
                    RheoTvApp.getNonUiContext().showToast("Photo deleted")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                if (NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext()))
                    RheoTvApp.getNonUiContext().showToast("Error While Deleting")
                else
                    RheoTvApp.getNonUiContext().showToast("No Network")


                t.printStackTrace()
            }
        })
    }

    fun onSaveGameSchedule() {
        SegmentTracker.getInstance().trackEvent(EVENT_GAME_TIMING_SAVED, analyticsProperties);
        when {
            playStartTime.isNullOrEmptyOrBlank() -> RheoTvApp.getNonUiContext().showToast("Please select start time")
            playEndTime.isNullOrEmptyOrBlank() -> RheoTvApp.getNonUiContext().showToast("Please select end time")
            totalGamingDays.isEmpty() -> RheoTvApp.getNonUiContext().showToast("Please select gaming days")
            else -> updateGameSchedule()
        }
    }

    private fun updateGameSchedule() {
        gameScheduleStatus.set(Status.LOADING)
        dataManager.updateGameSchedule(profile.get()?.profileDetail?.gameSchedule).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                gameScheduleStatus.set(Status.ERROR)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    gameScheduleStatus.set(Status.SUCCESS)
                    RheoTvApp.getNonUiContext().showToast("Game Schedule updated successfully!")
                } else {
                    gameScheduleStatus.set(Status.ERROR)
                }
            }
        })
    }


    fun saveUserProfile() {
        val setToBeRemoved = HashSet<String>()
        apiSet.forEach {
            when (it) {
                ENDPOINT_BIO -> if (!bio.isNullOrEmptyOrBlank()) updateProfileInfo(it, hashMapOf("bio" to bio)) else setToBeRemoved.add(ENDPOINT_BIO)
                ENDPOINT_PHONE -> if (!phoneNumber.isNullOrEmptyOrBlank()) updateProfileInfo(it, hashMapOf("phone" to phoneNumber)) else setToBeRemoved.add(ENDPOINT_PHONE)
                ENDPOINT_CONFIGURATION_USED -> if (!configurationUsed.isNullOrEmptyOrBlank()) updateProfileInfo(it, hashMapOf("config" to configurationUsed)) else setToBeRemoved.add(ENDPOINT_CONFIGURATION_USED)
                ENDPOINT_LANGUAGE -> updateProfileInfo(it, hashMapOf("language_ids" to (profile.get()?.languages?.filter { l -> l.isSelected }?.map { l -> l.id }
                        ?: emptyList<String>())))
                ENDPOINT_USER_INFO -> {
                    if (!intro.isNullOrEmptyOrBlank() || !fullName.isNullOrEmptyOrBlank() || !userName.isNullOrEmptyOrBlank())
                        updateProfileInfo(it, data = profile.get()?.user?.apply { description = intro })
                    else setToBeRemoved.add(ENDPOINT_USER_INFO)
                }
            }
        }
        apiSet.removeAll(setToBeRemoved)
        if (apiSet.isEmpty())
            onSave.value = Status.SUCCESS
    }

    private fun updateProfileInfo(key: String, body: HashMap<String, Any>? = null, data: Any? = null) {
        dataManager.updateUserAttribute(key, body, data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.code() == 200) {
                    apiSet.remove(key)
                    if (key == ENDPOINT_USER_INFO)
                        CommonUtils.setUserName((data as? User)?.username)
                    if (apiSet.isEmpty()) {
                        RheoTvApp.getNonUiContext().showToast("Success")
                        onSave.value = Status.SUCCESS
                    }
                }
            }
        })
    }

    fun requestForContentModerator() {
        dataManager.voteAsContentModerator("$userId").enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    profile.get()?.isVotedAsModerator = true
//                    notifyPropertyChanged()
                }

            }

            override fun onFailure(call: Call<ResponseBody?>, throwable: Throwable) {
                RheoTvApp.getNonUiContext()?.showToast("Please try again!")
            }
        })
    }

    fun loadGameDetails() {
        loadingGame.set(Status.LOADING)
        dataManager
                .gameDetails
                .enqueue(object : Callback<List<GameDetails>?> {
                    override fun onResponse(call: Call<List<GameDetails>?>, response: Response<List<GameDetails>?>) {
                        if (response.isSuccessful) {
                            gameResults.value = response.body()
                            loadingGame.set(Status.SUCCESS)
                        } else {
                            loadingGame.set(Status.ERROR)
                            Toast.makeText(RheoTvApp.getNonUiContext(), "Couldn't load games. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<GameDetails>?>, t: Throwable) {
                        Log.e(javaClass.simpleName, "failed to get games")
                        loadingGame.set(Status.ERROR)
                        Toast.makeText(RheoTvApp.getNonUiContext(), "Couldn't load games. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    fun uploadSelectedGame(games: MutableMap<String, GameDetails?>) {
        submittingGame.set(Status.LOADING)
        dataManager.setUserGames(ArrayList<String>(games.keys)).enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    profile.get()?.selectedGames = ArrayList(games.values)
                    RheoTvApp.getNonUiContext().showToast("Game updated successfully")
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_ONBOARD_GAME_SELECTED, analyticsProperties)
                    submittingGame.set(Status.SUCCESS)
                } else {
                    submittingGame.set(Status.ERROR)
                    RheoTvApp.getNonUiContext().showToast("Unable to set game preference. Please try Again.")
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                submittingGame.set(Status.ERROR)
                RheoTvApp.getNonUiContext().showToast("Unable to set game preference. Please try Again.")
            }
        })
    }

    fun onShare(view: View?) {
        shareTask(view, AppConstants.SHARE_TITLE_PROFILE, AppConstants.SHARE_DESCRIPTION_PROFILE)
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_RECENT_VIEWERS_SHARE_CLICKED, HashMap(analyticsProperties));
    }

    fun shareToBeModerator(view: View) {
        shareTask(view, SHARE_MODERATOR_TITLE_PROFILE, SHARE_MODERATOR_DESCRIPTION_PROFILE)
    }

    private fun shareTask(view: View?, title: String, description: String) {
        FirebaseDynamicLinkUtils.createDynamicLink(
                view?.context,
                profile.get()?.campaignInfo,
                AppConstants.IDENTIFIER_PROFILE_SHARE,
                title,
                description,
                profile.get()?.profilePic,
                hashMapOf(
                        AppConstants.BRANCH_PROFILE_URL_SHARE to profile.get()?.shareUrl,
                        AppConstants.BRANCH_SHARE_TYPE to AppConstants.BRANCH_SHARE_TYPE_PROFILE
                ),
                profile.get()?.shareUrl,
                true,
                "",
                false,
                null
        )
    }


    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.remove(callback)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }

    /**
     * Notifies observers that all properties of this instance have changed.
     */
    internal fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies observers that a specific property has changed. The getter for the
     * property that changes should be marked with the @Bindable annotation to
     * generate a field in the BR class to be used as the fieldId parameter.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    internal fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }
}
