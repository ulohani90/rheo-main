package com.rheotv.android.ui.activities.profile.viewprofile.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.Observable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavAction
import com.freshchat.consumer.sdk.Freshchat
import com.freshchat.consumer.sdk.exception.MethodNotAllowedException
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.databinding.FragmentUserProfileBinding
import com.rheotv.android.ui.activities.audioroom.view.AudioChatRoomActivity.Companion.startMe
import com.rheotv.android.ui.activities.follower.FollowActivity
import com.rheotv.android.ui.activities.gamify.RewardsActivity
import com.rheotv.android.ui.activities.moderators.AddModeratorsActivity
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.activities.rank.RankActivity
import com.rheotv.android.ui.activities.selectGame.GameSelectionActivity
import com.rheotv.android.ui.activities.share.SectionsStatePagerAdapter
import com.rheotv.android.ui.activities.tabcontainer.profile.videos.VideosFragment
import com.rheotv.android.ui.adapters.LevelType
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.customViews.Tooltip.SimpleTooltip
import com.rheotv.android.ui.customViews.bottomSheetMenu.BottomSheetMenuDialog
import com.rheotv.android.ui.customViews.bottomSheetMenu.Option
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest
import com.rheotv.android.ui.customViews.simpleSnackbar.BaseSimpleSnackbar
import com.rheotv.android.ui.customViews.simpleSnackbar.SimpleSnack
import com.rheotv.android.ui.customViews.simpleSnackbar.SimpleSnackbar
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog
import com.rheotv.android.utils.*
import com.rheotv.android.utils.AppConstants.REQUEST_CODE_ADD_MODERATORS
import com.rheotv.android.utils.EventBusModel.RefreshProfile
import com.rheotv.android.utils.pager.PageChangeListener
import com.rheotv.android.utils.pager.ViewPagerOneMediator
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class UserProfileFragment : BaseFragment<FragmentUserProfileBinding, UserProfileViewModel>() {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun getBindingVariable() = com.rheotv.android.BR.viewModel

    override fun getLayoutId() = R.layout.fragment_user_profile

    override fun getViewModel() = ViewModelProvider(parentFragment?.parentFragment
            ?: this, mViewModelFactory).get(UserProfileViewModel::class.java)

    var isProfileChangeListened = false
    private var streamerLiveSnackbar: BaseSimpleSnackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        trackProfile()
        System.gc()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (checkUserLoggedIn()) return
        with(viewDataBinding) {
            settingImageView.setOnClickListener { showSettings() }
            editProfileButton.setOnClickListener(object : DebouncedOnClickListener() {
                override fun onDebouncedClick(v: View?) {
                    editProfile()
                }
            })
            levelBadgeView.setOnClickListener { openRankActivity() }
//            reminderButton.setOnClickListener { viewModel?.onReminderClicked(WeakReference(this@UserProfileFragment)) }
            expandButton.setOnClickListener { viewDataBinding.appBar.setExpanded(true, true) }
            recentFollowersButton.setOnClickListener {
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_RECENT_VIEWERS_TAB_CLICKED, HashMap(this@UserProfileFragment.viewModel.analyticsProperties))
                showRecentFollower(false)
            }
            followerTextView.setOnClickListener {
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_FOLLOW_COUNT_CLICKED, HashMap(this@UserProfileFragment.viewModel.analyticsProperties))
                showRecentFollower(true)
            }
            streakBonusClickableArea.setOnClickListener {
                RewardsActivity.startMe(it.context, SCREEN_NAME)
            }

            errorText.setOnClickListener {
                this@UserProfileFragment.viewModel.loadProfile()
            }

            offlineLayout.retryButton.setOnClickListener {
                this@UserProfileFragment.viewModel.loadProfile()
            }

            liveTagOld.setOnClickListener {
                this@UserProfileFragment.viewModel.profile.get()?.liveStatus?.livePostId.let {
                    StreamPlayerActivity.startActivity(requireContext(),
                            StreamPlayerContainerFragment.Builder()
                                    .addPost(it)
                                    .addGameId(AppConstants.LIVE_GAME_ID)
                                    .addSourceScreenName(SCREEN_NAME)
                                    .addLoadMore(true)
                                    .buildExtras())
                }
            }
            followButton.setOnClickListener {
                if (CommonUtils.isUserLoggedin()) {
                    this@UserProfileFragment.viewModel.onFollowButtonClick()
                } else {
                    if (isStateSaved || !isAdded || activity?.isFinishing == true ||
                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity?.isActivityTransitionRunning == true))
                        return@setOnClickListener
                    LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE)
                            ?.show(childFragmentManager, LoginFragmentBottomDialog.TAG)
                }
            }
            moderatorTag.setOnClickListener {
                if (isStateSaved || !isAdded || activity?.isFinishing == true ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity?.isActivityTransitionRunning == true))
                    return@setOnClickListener
                SimpleTooltip.Builder(it.context)
                        .anchorView(it)
                        .text("Moderator")
                        .gravity(Gravity.TOP)
                        .animated(true)
                        .textColor(ContextCompat.getColor(it.context, android.R.color.white))
                        .arrowColor(ContextCompat.getColor(it.context, R.color.color_accent))
                        .backgroundColor(ContextCompat.getColor(it.context, R.color.color_accent))
                        .transparentOverlay(true)
                        .build()
                        .show()
                return@setOnClickListener
            }
            // different type of share actions
            shareButton.setOnClickListener {
                if (this@UserProfileFragment.viewModel.profile.get()?.isSelfProfile == true)
                    openShareDialog(AppConstants.SHARE_TITLE_PROFILE, AppConstants.SHARE_DESCRIPTION_PROFILE, SegmentConstants.EVENT_SELF_PROFILE_SHARE_CLICK)
                else {
                    val username = this@UserProfileFragment.viewModel.profile.get()?.user?.username
                            ?: "player"
                    openShareDialog(AppConstants.SHARE_TITLE_PROFILE_OTHER.replace("player", username),
                            AppConstants.SHARE_DESCRIPTION_PROFILE_OTHER.replace("player", username),
                            SegmentConstants.EVENT_USER_PROFILE_SHARE_CLICK)
                }
            }

            wantToBeModeratorLayout.actionButton.setOnClickListener {
                openShareDialog(AppConstants.SHARE_MODERATOR_TITLE_PROFILE, AppConstants.SHARE_MODERATOR_DESCRIPTION_PROFILE, SegmentConstants.EVENT_SELF_PROFILE_MODERATOR_SHARE_CLICK)
            }

            shareProfileTextView.setOnClickListener {
                openShareDialog(AppConstants.SHARE_TITLE_PROFILE, AppConstants.SHARE_DESCRIPTION_PROFILE, SegmentConstants.EVENT_SELF_PROFILE_FOR_RECENT_VIEWERS_SHARE_CLICK)
            }

            appBar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
                var isShow = true
                var scrollRange = -1

                override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                    if (scrollRange == -1)
                        scrollRange = appBarLayout?.totalScrollRange ?: 0
                    if (scrollRange + verticalOffset == 0) {
                        isShow = true
                        toolbar.visibility = View.VISIBLE
                    } else if (isShow) {
                        toolbar.visibility = View.GONE
                        isShow = false
                    }
                }
            })
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (isStateSaved || !isAdded || !isResumed || isRemoving) return
                if (checkUserLoggedIn()) return
                setPages()
                showSnackBars()
                if (viewModel.profile.get()?.isSelfProfile == true && !isProfileChangeListened) {
                    showSyncContactDialog()
                    isProfileChangeListened = true
                }
            }
        })

        /*viewModel.profile.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (!isProfileChangeListened) {
                    showSyncContactDialog()
                    isProfileChangeListened = true;
                }
            }
        })*/
    }

    override fun onStart() {
        super.onStart()
        isProfileChangeListened = false
    }

    override fun onStop() {
        super.onStop()
        isProfileChangeListened = false
    }

    override fun onResume() {
        super.onResume()
        Log.i("onresume", "onresume")
        Log.i("onresume", "logged in ${CommonUtils.isUserLoggedin()}")
        Log.i("onresume", "username in ${CommonUtils.getUserName() != viewModel.queryParam}")
        if (CommonUtils.isUserLoggedin() || CommonUtils.getUserName() != viewModel.queryParam) {
            if (!viewModel.isFirstApiCalled || viewModel.profile.get() == null) {
                Log.i("onresume", "first api call || ${viewModel.profile.get()}")
                viewModel.loadProfile()
            } else {
                Log.i("onresume", "update")
                viewModel.notifyChange()
            }
            Log.i("onresume", "logged in")
        }
    }

    private fun trackProfile() {
        val map: MutableMap<String, Any> = HashMap(viewModel.analyticsProperties)
        map["is_self"] =
                (viewModel.userName.equals(CommonUtils.getUserName(context), ignoreCase = true) ||
                        viewModel.userName.equals("me", ignoreCase = true))
        map["is_self_first"] = CommonUtils.isFirsTimeSelfProfileVisited(map["is_self"] as Boolean)
        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_PROFILE_PAGE_VISITED, map)
        CommonUtils.setFirsTimeSelfProfileVisited()
    }

    private fun checkUserLoggedIn(): Boolean {
        if (!CommonUtils.isUserLoggedin() && (viewModel.queryParam.isNullOrEmpty() || CommonUtils.getUserName() == viewModel.queryParam)) {
            navController()?.navigate(R.id.loginFragment, Bundle().also {
                it.putString(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_USER_PROFILE)
            })
            return true
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    private fun setPages() {
        if (isStateSaved || isDetached || context == null) return
        ViewPagerOneMediator(viewDataBinding.viewPager,
                viewDataBinding.tabLayout,
                SectionsStatePagerAdapter(this)
                        .also {
                            it.updateList(getPages())
                        },
                viewModel.tabPosition,
                object : PageChangeListener {
                    override fun onPageSelected(position: Int) {
                        viewModel.tabPosition = position
                        if (position == 1)
                            viewDataBinding.appBar.setExpanded(false, true)
                    }

                    override fun onPageUnselected(position: Int) = Unit
                }
        ).attach()
    }

    private fun getPages() =
            mutableListOf(
                    AdapterFragmentItem(AboutUserFragment.newInstance(), TAB_ABOUT),
                    AdapterFragmentItem(UserChatFragment.newInstance(viewModel.queryParam
                            ?: CommonUtils.getUserName(), SCREEN_NAME, viewModel.profile.get()?.profileDetail?.isChatAllowed
                            ?: false, viewModel.profile.get()?.profileDetail?.chatCriteria), TAB_CHAT),
                    AdapterFragmentItem(VideosFragment.newInstance(viewModel.userId, SCREEN_NAME), TAB_VIDEOS)
            ).also {
                if (viewModel.profile.get()?.isSelfProfile == true) {
                    if (viewModel.profile.get()?.paymentModel == 2)
                        it.add(AdapterFragmentItem(UserWalletFragment.newInstance(viewModel.queryParam
                                ?: CommonUtils.getUserName()), TAB_WALLET))
                    it.add(AdapterFragmentItem(UserAnalyticsFragment.newInstance(), TAB_ANALYTICS))
                }
            }

    private fun editProfile() {
        try {
            if (NavAction(R.id.action_userProfileFragment_to_profileEditFragment)?.destinationId == R.id.action_userProfileFragment_to_profileEditFragment)
                navController()?.navigate(R.id.action_userProfileFragment_to_profileEditFragment)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun showSettings() {
        if (isStateSaved || !isAdded || activity?.isFinishing == true ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity?.isActivityTransitionRunning == true))
            return
        val audioModeText = if (CommonUtils.getAudioModeFlag()) "Disable" else "Enable"
        val audioIcon = if (CommonUtils.getAudioModeFlag()) R.drawable.avd_mute else R.drawable.avd_unmute

        BottomSheetMenuDialog.Builder()
                .add(OptionRequest(AUDIO_MODE_ID, "$audioModeText Audio Mode", audioIcon))
                .add(R.menu.menu_profile_setting)
                .header("Profile Settings")
                .setListener { tag, option -> this.onSettingItemClicked(tag, option) }
                .show(childFragmentManager, "BottomSheetMenuDialog")
    }

    private fun showRecentFollower(isFollowerScreen: Boolean) {
        if (viewModel.profile.get()?.isSelfProfile == true) {
            val intent = Intent(activity, FollowActivity::class.java)
            intent.putExtra(AppConstants.ARG_IS_FOLLOW_SCREEN, isFollowerScreen)
            intent.putExtra(AppConstants.ARG_USERNAME, viewModel.userName)
            intent.putExtra(AppConstants.SCREEN_SOURCE, SCREEN_NAME)
            startActivity(intent)
        }
    }

    private fun onSettingItemClicked(tag: String, option: Option) {
        when (option.id) {
            R.id.action_contact_us -> {
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CONTACT_US_CLICKED, HashMap(viewModel.analyticsProperties))
                val freshChatUser = Freshchat.getInstance(requireContext()).user
                freshChatUser.firstName = CommonUtils.getUserName(requireContext())
                freshChatUser.lastName = "Mobile"
                try {
                    Freshchat.getInstance(requireContext()).user = freshChatUser
                } catch (e: MethodNotAllowedException) {
                    e.printStackTrace()
                }

                Freshchat.showConversations(requireContext())
            }

            R.id.action_logout -> {
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_LOGOUT_CLICKED, HashMap(viewModel.analyticsProperties))
                context?.signOut()
            }

            R.id.action_add_moderator -> {
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_ADD_MODERATE_CLICKED, HashMap(viewModel.analyticsProperties))
                val intent = Intent(activity, AddModeratorsActivity::class.java)
                intent.putExtra("moderators", viewModel.profile.get()?.moderators)
                intent.putExtra(AppConstants.SCREEN_SOURCE, SCREEN_NAME)
                startActivityForResult(intent, REQUEST_CODE_ADD_MODERATORS)
            }
            R.id.action_share_on_facebook -> {
                context?.openLink("https://www.facebook.com/getrheotv", AppConstants.FACEBOOK_KATANA_PACKAGE)
            }
            R.id.action_share_on_instagram -> {
                context?.openLink("https://www.instagram.com/getrheo/", AppConstants.INSTAGRAM_PACKAGE)
            }
            R.id.action_share_on_youtube -> {
                context?.openLink("https://www.youtube.com/channel/UCuWLrB7OHyCbcAORDzq4oUw", AppConstants.YOUTUBE_PACKAGE)
            }
            AUDIO_MODE_ID -> {
                viewModel.analyticsProperties["isAudioEnabled"] = !CommonUtils.getAudioModeFlag()
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_TOGGLE_AUDIO_OPTION, viewModel.analyticsProperties)
                CommonUtils.setAudioModeFlag(!CommonUtils.getAudioModeFlag())
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventBusModel: RefreshProfile?) = updateProfile()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventBusModel: EventBusModel.LoginSuccess?) {
        viewModel.isFirstApiCalled = false
        if (viewModel.queryParam == CommonUtils.getUserName() || viewModel.queryParam.isNullOrEmpty()) {
            viewModel.queryParam = CommonUtils.getUserName()
        }
        if (isStateSaved) return
        viewModel.loadProfile()
        viewModel.isFirstApiCalled = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventBusModel: EventBusModel.LogoutSuccess?) {
        /*viewModel.queryParam = null
        checkUserLoggedIn()
        viewModel.notifyChange()
        viewModel.isFirstApiCalled = false*/

//        Intent intent = new Intent(this, OnBoardingActivity.class);
        val intent = Intent(context, GameSelectionActivity::class.java)
        intent.putExtra("is_relogin", true)
        startActivity(intent)
        activity?.finish()
    }

    private fun updateProfile() {
        if (isStateSaved) return
        viewModel.loadProfile()
    }

    private fun showSnackBars() {
        if (isStateSaved || !isAdded || viewDataBinding?.root == null) return
        if (viewModel.profile.get()?.activeChatRooms != null && viewModel.profile.get()?.isSelfProfile == false) {
            showLiveInAudioRoom()
        } else {
            askModeratorVote()
        }
    }

    private fun askModeratorVote() {
        if (viewModel.canVoteForModerator) {
            with(viewDataBinding?.root) {
                this?.postDelayed({
                    try {
                        if (isStateSaved || !isAdded || isDetached || activity?.isFinishing == true ||
                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity?.isActivityTransitionRunning == true))
                            return@postDelayed
                        val snackBar = SimpleSnackbar.make(
                                this,
                                message = RheoTvApp.getNonUiContext().getString(R.string.moderator_vote_message),
                                actionLabel = RheoTvApp.getNonUiContext().getString(R.string.sure_thumbs_up),
                                duration = Snackbar.LENGTH_INDEFINITE,
                                listener = {
                                    if (isAdded && !isStateSaved) {
                                        val properties = HashMap<String, Any>(viewModel.analyticsProperties)
                                        properties["username"] = viewModel.userName
                                        SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_USER_PROFILE_VOTE_MODERATOR_CLICK, properties)
                                        viewModel.requestForContentModerator()
                                    }
                                }
                        )
                        snackBar?.show()
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(30000)
                            withContext(Dispatchers.Main) {
                                if (snackBar?.isShown == true)
                                    snackBar?.dismiss()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, 3000)
            }
        }
    }

    private fun showLiveInAudioRoom() {
        viewDataBinding?.root?.let {
            streamerLiveSnackbar = BaseSimpleSnackbar.make(
                    it,
                    SimpleSnack(
                            title = String.format(getString(R.string.talk_to_streamer), viewModel.userName),
                            subtitle = getString(R.string.available_in_audio_room),
                            actionText = getString(R.string.talk_now),
                            icon = ContextCompat.getDrawable(context
                                    ?: return, R.drawable.avd_community),
                            background = ContextCompat.getColor(context ?: return, R.color.purple),
                            layoutId = R.layout.layout_audio_room_live_author,
                            listener = {
                                trackEvent(SegmentConstants.EVENT_TALK_NOW_CLICKED_IN_STREAMER_PROFILE)
                                startMe(it.context,
                                        viewModel.groupId,
                                        1,
                                        viewModel.audioRoomId,
                                        SegmentConstants.SCREEN_NAME_USER_PROFILE,
                                        false)
                            }
                    )
            )

            streamerLiveSnackbar?.show()
        }
        trackEvent(SegmentConstants.EVENT_TALK_NOW_SHOWED_IN_STREAMER_PROFILE)
    }

    private fun openRankActivity() {
        viewModel.profile.get()?.let {
            if (!it.isSelfProfile) return
            RankActivity.startMe(this, activity, it.paymentModel, it.user.id
                    ?: 0, it.levelType ?: LevelType.Unassigned, SCREEN_NAME)
        }
    }

    private fun openShareDialog(title: String, description: String, eventName: String) {
        this@UserProfileFragment.viewModel?.profile?.get()?.let { p ->
            if (isStateSaved || !isAdded || activity?.isFinishing == true ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity?.isActivityTransitionRunning == true))
                return@let
            ProfileShareFragment.show(childFragmentManager, ProfileShareFragment.newInstance(
                    p,
                    SCREEN_NAME,
                    title,
                    description,
                    shareListener = { platform ->
                        val properties = HashMap<String, Any>(this@UserProfileFragment.viewModel.analyticsProperties)
                        properties["username"] = this@UserProfileFragment.viewModel.userName
                        properties["platform"] = platform
                        SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(eventName, properties)
                    }
            ))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        streamerLiveSnackbar?.dismiss()
    }

    private fun trackEvent(eventName: String) {
        val properties = HashMap<String, Any>(viewModel.analyticsProperties)
        properties["author_name"] = viewModel.userName
        SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(eventName, properties)
    }

    companion object {
        const val TAB_ABOUT = "About"
        const val TAB_CHAT = "Chat"
        const val TAB_VIDEOS = "Videos"
        const val TAB_WALLET = "Wallet"
        const val TAB_ANALYTICS = "Analytics"
        private const val SCREEN_NAME = SegmentConstants.SCREEN_NAME_PROFILE_SELF
        private const val AUDIO_MODE_ID = 0x0001
    }

}
