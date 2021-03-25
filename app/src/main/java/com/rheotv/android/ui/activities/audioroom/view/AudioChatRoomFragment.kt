package com.rheotv.android.ui.activities.audioroom.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.Observable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat
import com.rheotv.android.databinding.FragmentAudioChatRoomBinding
import com.rheotv.android.databinding.LayoutAudioRoomGameDetailsBinding
import com.rheotv.android.helpers.ShareTaskHelper
import com.rheotv.android.services.*
import com.rheotv.android.ui.activities.audioroom.adapter.ChatRoomUserRecyclerAdapter
import com.rheotv.android.ui.activities.audioroom.model.*
import com.rheotv.android.ui.activities.audioroom.viewmodel.AudioChatRoomActivityViewModel
import com.rheotv.android.ui.activities.home.view.HomeActivity
import com.rheotv.android.ui.activities.inAppBilling.BillingActivity
import com.rheotv.android.ui.activities.inAppBilling.BuyCoinCallbackListener
import com.rheotv.android.ui.activities.inAppBilling.BuyCoinFragment
import com.rheotv.android.ui.activities.player.activity.*
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity.Companion.startActivity
import com.rheotv.android.ui.activities.profile.viewprofile.utils.CommentAction
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity
import com.rheotv.android.ui.activities.tabcontainer.videoUpload.VideoUploadFragment.FILTER_ACTION_KEY
import com.rheotv.android.ui.adapters.ChatListAdapter
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.customViews.Tooltip.SimpleTooltip
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog
import com.rheotv.android.ui.fragments.MediaPreviewFragment
import com.rheotv.android.utils.*
import com.rheotv.android.utils.AppConstants.CHOOSE_MEDIA
import com.rheotv.android.utils.segmentTracker.EqualSpaceItemDecorator
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentConstants.*
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.Serializable
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.collections.set

class AudioChatRoomFragment : BaseFragment<FragmentAudioChatRoomBinding, AudioChatRoomActivityViewModel>() {

    @Inject
    lateinit var mChatListAdapter: ChatListAdapter

    @Inject
    lateinit var mUserRecyclerAdapter: ChatRoomUserRecyclerAdapter

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    var mViewModel: AudioChatRoomActivityViewModel? = null
    var isFromDeeplink: Boolean = false

    private var mServiceIntent: Intent? = null

    private val fileUploadReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val progress = intent.extras?.getInt("contentData") ?: 0
            Log.i(TAG, "fileUpload: $progress")
            val url = intent.extras?.getString(AppConstants.UPLOAD_URL_VIDEO)?.substringBefore("?")
            mViewModel?.uploadProgress?.set(progress)
            if (progress == 200) {
                mChatListAdapter.setMediaStatus(mViewModel?.currentMessageId, Status.SUCCESS)
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
                viewModel.sendMedia(viewModel.currentMessageId, url, intent.extras?.getString(AppConstants.MIME_TYPE, AppConstants.IMAGE))
                SegmentTracker.getInstance().trackEvent(EvENT_CHATROOM_UPLOAD_SUCCESSFUL,
                        HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                            put("Upload url", url)
                            put("Type", intent.extras?.getString(AppConstants.MIME_TYPE, ""))
                        })
            } else if (progress in 0..100) {
                if (progress == 0) {
                    mChatListAdapter.setMediaStatus(mViewModel?.currentMessageId, Status.UPLOADING)
                } else {
                    mChatListAdapter.updateMediaProgress(mViewModel?.currentMessageId, progress)
                }
            }
        }
    }

    private val audioEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            p1 ?: return
            val d = p1.getParcelableExtra<AudioConnection>(AUDIO_ACTION) ?: return
            when (d) {
                is AudioConnection.CallConnected -> {
                    val map = HashMap<String, Any?>(mViewModel?.analyticsProperties ?: hashMapOf())
                            .apply {
                                if (!CommonUtils.isFirstAgoraAudioCallDone()) {
                                    CommonUtils.setFirstAgoraAudioCallDone()
                                    "is_first" to true
                                }
                            }
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_AGORA_CALL_STARTED, map)
                    viewDataBinding?.connectingTextView?.visibility = View.GONE
                }

                is AudioConnection.UserCountUpdate -> {
                    activity?.runOnUiThread {
                        mViewModel?.onlineMemberCount?.set(d.count.toDouble())
                    }
                }

                is AudioConnection.UserJoined -> {

                }

                is AudioConnection.FirstUser -> {
                    activity?.runOnUiThread {
                        if (!isEmptyUserEventSent) {
                            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_EMPTY_ROOM,
                                    HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                                        put("time", Date().toString())
                                    })
                            isEmptyUserEventSent = true
                        }
                    }
                }

                is AudioConnection.SpeakerIndicate -> {
                    activity?.runOnUiThread {
                        mUserRecyclerAdapter.onUserSpeak(d.speaks?.toTypedArray())
                    }
                }

                is AudioConnection.CallLeft -> {

                }

                is AudioConnection.CallDisconnected -> {
                    activity?.runOnUiThread {
//                        context?.showToast("Disconnected from audio chat room!")
                        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_AGORA_CALL_ENDED,
                                HashMap<String, Any?>(mViewModel?.analyticsProperties
                                        ?: hashMapOf()))
                    }
                }

                is AudioConnection.UserJoinRoom -> {
                    activity?.runOnUiThread {
                        updateConnectedUsers(d.user)
                        mViewModel?.audioGroup?.get()?.ownerDetails?.let { owner ->
                            mViewModel?.chatRoomDetail?.highlightedUser?.let { user ->
                                mViewModel?.highlightedUser = user
                                AudioRoomService.highlightedUser = user
                                mUserRecyclerAdapter.highlightedUser = user
                                updateHighlightUserView(listOf(user, owner))
                            }
                        }
                    }
                }

                is AudioConnection.UserLeaveRoom -> {
                    activity?.runOnUiThread {
                        updateUserList(d.action to d.data)
                    }
                }

                is AudioConnection.SelfMute -> {
                    activity?.runOnUiThread {
                        val ownerDetail = mUserRecyclerAdapter.getSelfItem(CommonUtils.getUserID())
                        muteUser(ownerDetail, d.isMuted, "notification_tray")
                    }
                }

                is AudioConnection.ExitRoom -> {
                    activity?.runOnUiThread {
                        d?.reason?.let { context?.showToast(it) }
                        activity?.finish()
                    }
                }
            }
        }
    }

    private val networkStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mViewModel?.connectChat()
        }
    }

    private val chatCallback = object : ChatListAdapter.ChatItemClickListenerV2 {

        override fun onMediaClicked(commentChat: CommentChat?) {
            val mediaFragment = MediaPreviewFragment.newInstance(commentChat?.message, commentChat?.message.mimeType, SCREEN_NAME_AUDIO_CHAT_ROOM)
            mediaFragment.show(childFragmentManager, "media")
        }

        override fun onUserClicked(commentChat: CommentChat?) {
            showMenuBottomSheet(commentChat)
        }

        override fun onCommentClicked(commentChat: CommentChat?) {
            showMenuBottomSheet(commentChat)
        }
    }

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_audio_chat_room

    override fun getViewModel(): AudioChatRoomActivityViewModel =
            if (mViewModel == null) {
                mViewModel = ViewModelProvider(this, mViewModelFactory)[AudioChatRoomActivityViewModel::class.java].also {
                    it.chatRoomId = arguments?.getString(AudioChatRoomActivity.ARG_CHAT_ROOM_ID)
                    it.onlineMemberCount.set(arguments?.getInt(AudioChatRoomActivity.ARG_ONLINE_COUNT)?.toDouble()
                            ?: 0.toDouble())
                    isFromDeeplink = arguments?.getBoolean(AppConstants.ARG_FROM_DEEPLINK) ?: false
                    it.analyticsProperties[AppConstants.SCREEN_SOURCE] = arguments?.getString(AppConstants.SCREEN_SOURCE)
                            ?: ""
                    it.audioGroupId = arguments?.getString(AudioChatRoomActivity.ARG_GROUP_ID)
                    it.audioGroup.set(arguments?.getParcelable<AudioGroup?>(AudioChatRoomActivity.ARG_GROUP_DETAILS)?.apply {
                        it.username = ownerDetails?.username
                        it.canChat = true
                        it.analyticsProperties["chatroom_author_name"] = ownerDetails?.username
                    })
                }
                mViewModel!!
            } else {
                mViewModel!!
            }

    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    fun registerBackPress() {
        activity?.onBackPressedDispatcher?.addCallback(this, backPressCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBackPress()
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
    }

    private var isEmptyUserEventSent = false
    private val mGameDetailViewBinding by lazy {
        LayoutAudioRoomGameDetailsBinding.inflate(LayoutInflater.from(context))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding?.apply {
            inviteButton.setOnClickListener {
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_INVITE_BUTTON_CLICKED,
                        HashMap(mViewModel?.analyticsProperties ?: hashMapOf()))
                inviteUser()
            }
            toolbarBackButton.setOnClickListener { onBackPressed() }
            gameButton.setOnClickListener {
                SegmentTracker.getInstance()
                        .trackEvent(EVENT_CHATROOM_GAME_ICON_CLICKED,
                                HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                                    put("isAuthor", mViewModel?.isStreamer == true)
                                    put("chatroomAuthorname", mViewModel?.audioGroup?.get()?.ownerDetails?.username)
                                })
                val audioRoomGame = AudioRoomGame()
                audioRoomGame.setupView(it.context, mViewModel?.chatRoomDetail?.availableSocialGames
                        ?: return@setOnClickListener)
                val tooltipBuilder = SimpleTooltip.Builder(context).anchorView(it)
                        .arrowColor(ContextCompat.getColor(it.context, R.color.white_text_color))
                        .dismissOnInsideTouch(false)
                        .contentView(audioRoomGame.rootView?.root, 0)
                        .animationDuration(300)
                        .autoHide(false)
                        .onShowListener {
                            gameButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(root.context, R.color.color_accent))
                        }
                        .onDismissListener {
                            gameButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(root.context, R.color.white_text_color))
                        }
                val tooltip = tooltipBuilder.build()
                audioRoomGame.registerActionListener { game ->
                    SegmentTracker.getInstance()
                            .trackEvent(EVENT_CHATROOM_GAME_CLICKED,
                                    HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                                        put("isAuthor", mViewModel?.isStreamer == true)
                                        put("chatroomAuthorname", mViewModel?.audioGroup?.get()?.ownerDetails?.username)
                                        put("gameName", game.name)
                                    })
                    if (game.id == AppConstants.AMONG_US_PACKAGE_NAME) {
                        try {
                            SegmentTracker.getInstance().trackEvent(EVENT_AMONG_US_GAME_CLICKED,
                                    HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                                        put("isAuthor", mViewModel?.isStreamer == true)
                                        put("chatroomAuthorname", mViewModel?.audioGroup?.get()?.ownerDetails?.username)
                                    })
                            val appIntent = activity?.packageManager?.getLaunchIntentForPackage(game.id)
                            startActivity(appIntent)
                            SegmentTracker.getInstance().trackEvent(EVENT_AMONG_US_GAME_DETECTED,
                                    HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                                        put("isAuthor", mViewModel?.isStreamer == true)
                                        put("chatroomAuthorname", mViewModel?.audioGroup?.get()?.ownerDetails?.username)
                                    })
                        } catch (e: ActivityNotFoundException) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${game.id}")))
                            e.printStackTrace()
                        } catch (ex: NullPointerException) {
                            try {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${game.id}")))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            ex.printStackTrace()
                        }
                        tooltip.dismiss()
                        return@registerActionListener
                    }
                    if (mViewModel?.isStreamer == false) {
                        context?.showToast("Only streamer can start the game!")
                        tooltip.dismiss()
                        return@registerActionListener
                    }
                    mViewModel?.changeGameState("start_game", game.id
                            ?: return@registerActionListener) {
                        startSocialGame(game)
                        tooltip.dismiss()
                    }
                }
                tooltip.show()
            }
            roomUserRecyclerView.apply {
                (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
                adapter = mUserRecyclerAdapter.also {
                    it.onItemClick = object : Function<Unit>, (OwnerDetail?, Boolean) -> Unit {
                        override fun invoke(ownerDetail: OwnerDetail?, isMuted: Boolean) {
                            if (ownerDetail == null) {
                                inviteUser()
                            } else
                                muteUser(ownerDetail, isMuted, null)
                        }
                    }
                }
                onEndPageReachedListener(onEndReached = {
                    if (!mUserRecyclerAdapter.isPaginating() && !mViewModel?.userNextUrl.isNullOrEmpty()) {
                        mUserRecyclerAdapter.setPaginating(true)
                        viewDataBinding?.userPaginationProgressBar?.visibility = View.VISIBLE
                        mViewModel?.loadConnectedUsers()
                    }
                })
            }
            recyclerView.apply {
                (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
                adapter = mChatListAdapter.apply {
                    setListener(chatCallback)
                    addInitialNote()
                    setChatStickerSize(context?.stickerDimension() ?: 60)
                }

                onEndPageReachedListener(onEndReached = {
                    if (!mChatListAdapter.isShowLoading && mViewModel?.isLoading?.get() != true && mViewModel?.nextCommentUrl != null) {
                        mChatListAdapter.isShowLoading = true
                        mViewModel?.loadComments()
                    }
                }, onFirstReach = {
                    mViewModel?.unreadChatCount?.set(0)
                })

                addItemDecoration(EqualSpaceItemDecorator(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()))
            }
            messageContainer.setOnClickListener {
                if (CommonUtils.isUserLoggedin())
                    ChatBoxBottomSheetDialog.newInstance(object : ChatBoxCallbackListener {
                        override fun onChatSend(message: String) {
                            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_COMMENT_SENT,
                                    HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                                        put("comment_message", message)
                                    })
                            mViewModel?.sendComment(message)
                        }
                    }, mViewModel?.username
                            ?: "").show(childFragmentManager, ChatBoxBottomSheetDialog.TAG)
            }
            giftButtonPortrait.setOnClickListener {
                mViewModel?.onGiftClick(it)
            }
            followButton.setOnClickListener {
                mViewModel?.followOwner()
            }
            pinComment.pinImageView.setOnClickListener {
                if (CommonUtils.getUserName() == mViewModel?.audioGroup?.get()?.ownerDetails?.username || mUserRecyclerAdapter.canUpdateUser) {
                    mViewModel?.unPinComment()
                }
            }
            watchNowBtn.setOnClickListener {
                activity ?: return@setOnClickListener
                context?.let { _ ->
                    val properties = HashMap<String, Any>(mViewModel?.analyticsProperties
                            ?: hashMapOf())
                    properties["author_name"] = mViewModel?.username ?: ""
                    SegmentTracker.getInstance().trackEvent(EVENT_WATCH_NOW_CLICKED_IN_AUDIO_CHATROOM, properties)

                    startActivity(requireContext(),
                            StreamPlayerContainerFragment.Builder()
                                    .addPost(mViewModel?.audioGroup?.get()?.ownerDetails?.livePostId
                                            ?: "")
                                    .addGameId(AppConstants.LIVE_GAME_ID)
                                    .addSourceScreenName(SegmentConstants.SCREEN_NAME_AUDIO_CHAT_ROOM)
                                    .addLoadMore(true)
                                    .buildExtras())
                }
            }
            toolbarUsernameTextView.setOnClickListener {
                if (CommonUtils.getUserName() == mViewModel?.username || mUserRecyclerAdapter.canUpdateUser) {
                    RenameAudioRoomNameFragment.newInstance(object : RenameAudioRoomNameListener {
                        override fun onNameChange(name: String) {
                            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_AUDIO_ROOM_NAME_CHANGED,
                                    HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                                        put("room_name", name)
                                    })
                            mViewModel?.updateGroupName(name)
                        }
                    }, mViewModel?.roomName
                            ?: "").show(childFragmentManager, ChatBoxBottomSheetDialog.TAG)
                }
            }
            addImageView.setOnClickListener {
                SegmentTracker.getInstance().trackEvent(EVENT_CHATROOM_UPLOAD_ICON_CLICKED,
                        HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                            put("chatroomAuthorname", mViewModel?.audioGroup?.get()?.ownerDetails?.username)
                        })
                chooseMedia()
            }
        }
    }

    private fun startSocialGame(game: SocialGame) {
        mViewModel?.hasGameStarted?.set(true)
        mUserRecyclerAdapter.socialGame = game
        with(mGameDetailViewBinding) {
            gameNameTextView.text = game.name
            gameRuleTextView.text = game.rules
            BindingUtils.setImageUrl(gameImageView, game.logoUrl)
            if (mViewModel?.isStreamer == true) {
                pickPlayerButton.visibility = View.VISIBLE
                exitGameButton.visibility = View.VISIBLE
            } else {
                pickPlayerButton.visibility = View.GONE
                exitGameButton.visibility = View.GONE
            }
            pickPlayerButton.setOnClickListener {
                mViewModel?.highlightUser("pick_random") { ownerDetail ->
                    SegmentTracker.getInstance().trackEvent(EVENT_CHATROOM_PICK_AGAIN_CLICKED,
                            HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                                put("GameName", game.name)
                                put("selectedUser", ownerDetail?.username)
                                put("participantCount", mViewModel?.onlineMemberCount?.get() ?: 0)
                            })
                    try {
                        AudioRoomService.highlightedUser = ownerDetail
                        mUserRecyclerAdapter.highlightedUser = ownerDetail
                        mViewModel?.highlightedUser = ownerDetail
                        mViewModel?.agoraConnectionUtils?.muteAllRemoteAudio()
                        mUserRecyclerAdapter.ownerDetail?.let {
                            updateHighlightUserView(listOf(ownerDetail ?: return@highlightUser, it))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            exitGameButton.setOnClickListener {
                SegmentTracker.getInstance().trackEvent(EVENT_CHATROOM_EXIT_GAME_CLICKED,
                        HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                            put("GameName", game.name)
                            put("participantCount", mViewModel?.onlineMemberCount?.get() ?: 0)
                        })
                mViewModel?.changeGameState("stop_game", game.id ?: "") {
                    endSocialGame()
                }
            }
            if (!root.isAttachedToWindow) {
                viewDataBinding?.gameDetailContainer?.addView(root)
            }
        }
    }

    private fun endSocialGame() {
        mUserRecyclerAdapter.socialGame = null
        if (mGameDetailViewBinding.root.isAttachedToWindow) {
            viewDataBinding?.gameDetailContainer?.removeView(mGameDetailViewBinding.root)
        }
        mViewModel?.hasGameStarted?.set(false)
    }

    fun inviteUser() {
        FirebaseDynamicLinkUtils.createDynamicLink(context, FirebaseDynamicLinkUtils.FirebaseDynamicLinkData().apply {
            shareUrl = "https://rheo.com/audio_chat_room/${mViewModel?.audioGroup?.get()?.id}/${mViewModel?.chatRoomId}/?group_title=${mViewModel?.audioGroup?.get()?.name}&shared_user_details=${CommonUtils.getUserName()}&owner_of_the_group=${mViewModel?.audioGroup?.get()?.ownerDetails?.username}"
            identifier = "audio_chat_room"
            imageUrl = mViewModel?.audioGroup?.get()?.ownerDetails?.profileImageUrl ?: ""
            title = "Come and join ${mViewModel?.audioGroup?.get()?.ownerDetails?.username}'s audio chat room!"
            description = "Let's have some fun!"
            packageName = "com.whatsapp"
            campaignInfo = CommonUtils.getUserName()
            isLive = true.toString()
        }, object : FirebaseDynamicLinkUtils.ShareLinkGenerateListener {
            override fun onLinkGenerationSuccess(shareUrl: String?) {
                ShareTaskHelper.getNewInstance(context).share(context, shareUrl, ShareTaskHelper.ShareTarget.Whatsapp)
            }

            override fun onLinkGenerationFailure(errorMessage: String?) {
                context?.showToast("$errorMessage")
            }
        })
        context?.showToast("Select friends you want to invite to this Chatroom!")
    }

    fun onBackPressed() {
        if (isFromDeeplink) {
            context?.let {
                HomeActivity.startActivity(it,
                        bundleOf(AppConstants.SCREEN_SOURCE to SegmentConstants.SCREEN_NAME_AUDIO_CHAT_ROOM),
                        listOf(Intent.FLAG_ACTIVITY_CLEAR_TOP, Intent.FLAG_ACTIVITY_NEW_TASK, Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            activity?.finish()
        } else {
            backPressCallback.remove()
            activity?.onBackPressed()
        }
    }

    private fun muteUser(ownerDetail: OwnerDetail?, muted: Boolean, segmentAction: String?) {
        if (ownerDetail == null) return
        if (ownerDetail.id == CommonUtils.getUserID()) {
            if (mUserRecyclerAdapter.highlightedUser != null) {
                if (mViewModel?.isStreamer == true) {
                    context?.showToast("You can unmute yourself after removing spotlight!")
                } else if (ownerDetail?.id == mUserRecyclerAdapter.highlightedUser?.id)
                    context?.showToast("Chatroom owner has switched on spotlight feature, you will be able to mute yourselves  only after the owner removes spotlight!")
                else
                    context?.showToast("Chatroom owner has switched on spotlight feature, you will be able to unmute yourselves only after the owner removes spotlight!")
                return
            }
            if (segmentAction.isNullOrEmpty()) {
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_MUTE_BUTTON_CLICKED,
                        HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                            put("muted_username", ownerDetail?.username)
                            put("referrer", "self_profile_click")
                        })
            }
            if (mUserRecyclerAdapter.canUnMuteSelf) {
                val action = if (!muted) {
                    "unmute"
                } else {
                    "mute"
                }
                mViewModel?.muteUnMuteParticipant(ownerDetail?.username, ownerDetail?.id, action)
                mUserRecyclerAdapter.updateSelfItem(muted)
                updateMuteStateInNotification(muted)
            } else {
                context?.showToast(mUserRecyclerAdapter.muteMessage)
            }
        } else {
            var action: String? = null
            if (mViewModel?.audioGroup?.get()?.ownerDetails?.id == CommonUtils.getUserID() ||
                    mUserRecyclerAdapter.canUpdateUser) {
                action = if (!muted) "unmute" else "mute"
            }
            showAudienceMenu(ownerDetail, action)
        }
    }

    private fun updateHighlightUserView(userList: List<OwnerDetail>) {
        val map = userList.mapNotNull { it.id?.let { _ -> it } }
                .associateBy { data -> data.id }
                .toMutableMap()
        var streamerPosition = -1
        var selfPosition = -1
        var userPosition = -1
        if (map.containsKey(CommonUtils.getUserID())) {
            updateMuteStateInNotification(false)
        } else {
            updateMuteStateInNotification(true)
        }
        with(mUserRecyclerAdapter) {
            for (index in mList.indices) {
                if (map.isEmpty()) break
                if (map.containsKey(mList[index].id) && mList[index].id != mViewModel?.audioGroup?.get()?.ownerDetails?.id) {
                    userPosition = index
                }
                if (mList[index].id == mViewModel?.audioGroup?.get()?.ownerDetails?.id) {
                    streamerPosition = index
                }
                if (mList[index].id == CommonUtils.getUserID()) {
                    selfPosition = index
                }
            }
            if (streamerPosition > -1 && streamerPosition != 0) {
                mList.add(0, mList.removeAt(streamerPosition))
            }
            if (selfPosition > -1 && selfPosition != streamerPosition) {
                mList.add(if (streamerPosition == -1) 0 else 1, mList.removeAt(selfPosition))
            }
            if (userPosition > -1 && userPosition != selfPosition) {
                mList.add(if (selfPosition < 1) 1 else 2, mList.removeAt(userPosition))
            }
        }

        mUserRecyclerAdapter.notifyDataSetChanged()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel?.apply {
            audioGroup.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    chatRoomDetail?.activeSocialGame?.let {
                        startSocialGame(it)
                    }
                    if (audioGroup.get() != null) {
                        if (mUserRecyclerAdapter.ownerDetail == null)
                            mUserRecyclerAdapter.addOwner(audioGroup.get()?.ownerDetails)
                    }
                }
            })
            connectUserListMutableLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                updateConnectedUsers(it)
            })
            userMutableLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                updateUserList(it)
            })
            connectAudioLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                if (it == true) {
                    startAudioCall()
                }
            })
            roomActionLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                when (it.first) {
                    AudioChatRoomActivityViewModel.AudioRoomAction.UserHighlighted -> {
                        if (mViewModel?.isStreamer == true) return@Observer
                        val userList: List<OwnerDetail> = (it.second as? List<*>)?.filterIsInstance<Participant>()
                                ?.mapNotNull { data ->
                                    if (data.participantDetails?.id != audioGroup.get()?.ownerDetails?.id) {
                                        highlightedUser = data.participantDetails
                                        AudioRoomService.highlightedUser = data.participantDetails
                                        mUserRecyclerAdapter.highlightedUser = data.participantDetails
                                    }
                                    data.participantDetails
                                } ?: return@Observer
                        updateHighlightUserView(userList)
                    }
                    AudioChatRoomActivityViewModel.AudioRoomAction.UserUnHighlighted -> {
                        mUserRecyclerAdapter.highlightedUser = null
                        highlightedUser = null
                        AudioRoomService.highlightedUser = null
                        updateMuteStateInNotification(mUserRecyclerAdapter.getSelfItem(CommonUtils.getUserID())?.isMuted == true)
                        mUserRecyclerAdapter.notifyDataSetChanged()
                    }
                    AudioChatRoomActivityViewModel.AudioRoomAction.GameStarted -> {
                        startSocialGame(it.second as? SocialGame ?: return@Observer)
                    }
                    AudioChatRoomActivityViewModel.AudioRoomAction.GameEnded -> {
                        endSocialGame()
                    }
                    else -> Unit
                }
            })
            comments.observe(viewLifecycleOwner, androidx.lifecycle.Observer { mChatListAdapter.addItems(it) })
            incomingComment.observe(viewLifecycleOwner, androidx.lifecycle.Observer { mChatListAdapter.addItem(it) })

            removeComment.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                mChatListAdapter.removeChatItem(it.message, it.username)
            })
            suggestions.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                viewDataBinding.tagChipGroup.addChips(it, onChipClick = { message ->
                    currentComment.set(message)
                    sendComment()
                })
            })

            isStreamerFollowed.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                viewDataBinding?.followStreamerLayout?.visibility = if (it == false) View.VISIBLE else View.GONE
            })

            isUserBlocked.observe(viewLifecycleOwner, androidx.lifecycle.Observer  {
                if (it == true) {
                    EventBus.getDefault().post(EventBusModel.RefreshAudioGroupList)
                    context?.showToast(R.string.blocked_from_room)
                    if (AudioRoomService.isRunning) stopService()
                    activity?.finish()
                }
                chatRoomDetail?.activeSocialGame?.let { game ->
                    startSocialGame(game)
                }
                audioGroup.get()?.ownerDetails?.let { owner ->
                    mUserRecyclerAdapter.ownerDetail = owner
                }
            })

            onAskUserLogin.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    LoginFragmentBottomDialog.getInstance("").show(childFragmentManager, TAG)
                }
            })
            mViewModel?.updateCheckViews?.observe(viewLifecycleOwner, androidx.lifecycle.Observer { check: Long? -> updateChatViews() })
            mViewModel?.ownerAudioAction?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                if (it is CommentAction.Block && it.username == CommonUtils.getUserName()) {
                    EventBus.getDefault().post(EventBusModel.RefreshAudioGroupList)
                    if (AudioRoomService.isRunning) stopService()
                    context?.showToast(R.string.blocked_from_room)
                    activity?.finish()
                }
            })

            mViewModel?.connectionAction?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                if (it == CommentAction.Connect && NetworkUtils.isNetworkConnected(context))
                    mViewModel?.connectChat()
            })

            mViewModel?.askToBuyCoins?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                askToBuyCoins()
            })

            mViewModel?.onStreamerWentLive?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                if (it) {
                    val properties = HashMap<String, Any>(mViewModel?.analyticsProperties
                            ?: hashMapOf())
                    properties["author_name"] = mViewModel?.username ?: ""
                    SegmentTracker.getInstance().trackEvent(EVENT_WATCH_NOW_SHOWED_IN_AUDIO_CHATROOM, properties)
                }
            })

            mViewModel?.onFollowerStream?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                if (it) {
                    startAudioCall()
                }
            })

            getGroupId()
            loadComments()
            fetchAudioChatRoomDetail()
        }
    }

    private fun updateConnectedUsers(it: List<OwnerDetail>?) {
        val result: MutableList<OwnerDetail> = it?.toMutableList() ?: mutableListOf()
        mUserRecyclerAdapter.setPaginating(false)
        viewDataBinding?.userPaginationProgressBar?.visibility = View.GONE
        mUserRecyclerAdapter.submitList(result)
        viewDataBinding?.roomUserRecyclerView?.visibility =
                if (mUserRecyclerAdapter.itemCount == 0) View.GONE else View.VISIBLE
    }

    private fun updateUserList(it: Pair<AudioChatRoomActivityViewModel.AudioRoomAction, AudioChatRoomActivityViewModel.UpdateData?>?) {
        when (it?.first) {
            AudioChatRoomActivityViewModel.AudioRoomAction.AddUser -> {
                it.second?.ownerDetail?.let { item ->
                    mUserRecyclerAdapter.submitList(listOf(item))
                }
            }
            AudioChatRoomActivityViewModel.AudioRoomAction.DeleteUser -> {
                it.second?.ownerDetail?.let { item ->
                    if (item.id != CommonUtils.getUserID())
                        mUserRecyclerAdapter.removeItem(item)
                }
            }
            AudioChatRoomActivityViewModel.AudioRoomAction.UpdateUser -> {
                it.second?.let { item ->
                    if (item.actionUserName != CommonUtils.getUserName() && item.ownerDetail?.id == CommonUtils.getUserID() && mViewModel?.isStreamer == false) {
                        mUserRecyclerAdapter.muteMessage = "Room owner has muted you. Only owner can unmute you!"
                        mUserRecyclerAdapter.canUnMuteSelf = item.ownerDetail?.isMuted == false
                    }
                    if (item.ownerDetail?.id == CommonUtils.getUserID()) {
                        startService(item.ownerDetail.id, item.ownerDetail?.isMuted, false)
                    }
                    mUserRecyclerAdapter.updateItem(item.ownerDetail ?: return@let, true)
                }
            }
            AudioChatRoomActivityViewModel.AudioRoomAction.FinishRoom -> activity?.finish()
        }
        viewDataBinding?.roomUserRecyclerView?.visibility =
                if (mUserRecyclerAdapter.itemCount == 0) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        mViewModel?.connectChat()
        context?.registerNetworkReceiver(networkStateReceiver)
        context?.registerReceiver(audioEventReceiver, IntentFilter(AUDIO_ACTION))
        if (AudioRoomService.isRunning) {
            EventBus.getDefault().post(EventBusModel.FetchLastAudioRoomState)
        }

        viewDataBinding?.connectingTextView?.visibility = if (!AudioRoomService.isConnected) View.VISIBLE else View.GONE
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(fileUploadReceiver, IntentFilter(FILTER_ACTION_KEY))
    }

    private fun startAudioCall() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, 123)) {
            checkForOverlay()
            context?.let {
//                mViewModel?.agoraConnectionUtils?.startAudioCall(it, CommonUtils.getUserID())
                startService(CommonUtils.getUserID(), mViewModel?.isAllowedToSpeak == false || AudioRoomService.isSelfMuted, true)

//                if (mViewModel?.agoraConnectionUtils?.isCallActive() == false)
                viewDataBinding?.connectingTextView?.visibility = if (!AudioRoomService.isConnected) View.VISIBLE else View.GONE

                // mViewModel?.joinChatRoom()
            }
        }
    }

    private fun checkForOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                AlertDialog.Builder(context).setTitle("For best experience in audio chatrooms")
                        .setMessage("Enable display over other apps permission in the next screen")
                        .setPositiveButton("Continue") { dialogInterface, _ ->
                            try {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context?.packageName))
                                startActivityForResult(intent, 0)
                            } catch (e: ActivityNotFoundException) {
                                e.printStackTrace()
                            } finally {
                                dialogInterface.dismiss()
                            }
                        }.setNegativeButton("Cancel") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }.show()
            }
        }
    }

    fun stopService() {
        mServiceIntent = Intent(context, AudioRoomService::class.java).apply {
            putExtra(STOP_SERVICE, true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context?.startForegroundService(mServiceIntent)
        else
            context?.startService(mServiceIntent)
    }

    private fun startService(
            muteUnMuteUID: Int? = null,
            muteUser: Boolean? = null,
            startAudioCall: Boolean = false, showRoomControlHead: Boolean = false,
            muteAll: Boolean? = null
    ) {
        if (mViewModel?.isUserBlocked?.value == true) return
        mServiceIntent = Intent(context, AudioRoomService::class.java).apply {
            putExtra(AUDIO_DETAIL, mViewModel?.roomDetails)
            putExtra(AUDIO_IS_SELF_MUTE, mViewModel?.isMicMuted)
            putExtra(AUDIO_START_CALL, startAudioCall)
            putExtra(AUDIO_ROOM_PROPERTIES, mViewModel?.analyticsProperties as? Serializable)
            putExtra(SHOW_AUDIO_ROOM_CONTROL_HEAD, showRoomControlHead)
            muteAll?.let { putExtra(MUTE_ALL, muteAll) }
            muteUnMuteUID?.let { putExtra(AUDIO_MUTE_UNMUTE_UID, muteUnMuteUID) }
            muteUnMuteUID?.let { putExtra(AUDIO_MUTE_USER, muteUser) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context?.startForegroundService(mServiceIntent)
        else
            context?.startService(mServiceIntent)
    }

    override fun onPause() {
        super.onPause()
        context?.unregisterReceiver(networkStateReceiver)
        context?.unregisterReceiver(audioEventReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(fileUploadReceiver)
    }

    override fun onStart() {
        super.onStart()
        if (AudioRoomService.isRunning) {
            EventBus.getDefault().post(EventBusModel.RemoveChatroomController)
        }
        mViewModel?.connectChat()
    }

    override fun onStop() {
        mViewModel?.disconnectChat()
        if (mViewModel?.isUserBlocked?.value == false && mViewModel?.isStayingConnected == true &&
                (mViewModel?.isStreamerFollowed?.value == true || mViewModel?.audioGroup?.get()?.ownerDetails?.id == CommonUtils.getUserID())) {
            if (AudioRoomService.isRunning) startService(showRoomControlHead = true)
        }
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            SegmentTracker.getInstance().trackEvent(EVENT_CHAT_ROOM_VOICE_CALL_PERMISSION_GIVEN, mViewModel?.analyticsProperties)
            context?.let {
                startAudioCall()
            }
        } else {
            if (requestCode == 123) {
                context?.showToast("Micro phone permission is must to enter audio chatrooms, please enable it from app settings")
                CoroutineScope(Dispatchers.IO).doAfter(1000) { activity?.finish() }
            }
        }
    }

    fun createNotification(ctx: Context, bitmap: Bitmap) {

    }

    private var isMicMuted = false

    private fun updateMuteStateInNotification(muted: Boolean) {
        mViewModel?.isMicMuted = muted
        startService(CommonUtils.getUserID(), muted, false)
        isMicMuted = muted
    }

    private fun dismissNotification(ctx: Context?) {
        try {
            (ctx?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(TAG, "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        permission
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            SegmentTracker.getInstance().trackEvent(EVENT_CHAT_ROOM_VOICE_CALL_PERMISSION_SHOWN, mViewModel?.analyticsProperties)
            requestPermissions(arrayOf(permission), requestCode)
            return false
        }
        return true
    }

    private fun showAudienceMenu(ownerDetail: OwnerDetail?, speakerAction: String?) {
        try {
            if (ownerDetail?.id == null) return
            val listOptions = ArrayList<ListOption>()
            listOptions.add(ListOption.Header(StreamPlayerFragment.VIEW_PROFILE))
            if (mUserRecyclerAdapter.highlightedUser == null || ownerDetail?.id == mUserRecyclerAdapter.highlightedUser?.id || CommonUtils.getUserID() == ownerDetail?.id) {
                if (speakerAction == "mute") {
                    listOptions.add(ListOption.Item(0x0012, "Mute", R.drawable.avd_mute))
                } else if (speakerAction == "unmute")
                    listOptions.add(ListOption.Item(0x0012, "Unmute", R.drawable.avd_unmute))
            }
            if (CommonUtils.getUserID() == mViewModel?.audioGroup?.get()?.ownerDetails?.id) {
                if (mViewModel?.highlightedUser?.id == null || mViewModel?.highlightedUser?.id != ownerDetail.id) {
                    listOptions.add(ListOption.Item(0x0100, "Bring to Highlight", R.drawable.avd_highlight))
                } else {
                    listOptions.add(ListOption.Item(0x0101, "Remove from Highlight", R.drawable.avd_highlight))
                }
            }
            listOptions.add(ListOption.Item(StreamPlayerFragment.REPORT_USER, "Report", R.drawable.avd_report))
            if (CommonUtils.getUserName() == ownerDetail.username || mUserRecyclerAdapter.canUpdateUser) {
                context?.let {
                    val drawable = ContextCompat.getDrawable(it, R.drawable.ic_block)
                    listOptions.add(ListOption.Item(StreamPlayerFragment.BLOCK_USER, "Block User",
                            -1, ViewUtils.setTint(drawable, Color.rgb(251, 251, 251))))
                }
            }

            val bottomSheet = ChatMenuOptionBottomSheet.newInstance(
                    listOptions
            ) { listOption: ListOption ->
                if (listOption is ListOption.Header) {
                    openProfile(ownerDetail.username)
                } else {
                    when ((listOption as ListOption.Item).id) {
                        StreamPlayerFragment.VIEW_PROFILE -> openProfile(ownerDetail.username)
                        StreamPlayerFragment.REPORT_USER -> {
                            SegmentTracker.getInstance().trackEvent(EVENT_CHAT_ROOM_REPORT_BUTTON_CLICKED,
                                    HashMap(mViewModel?.analyticsProperties
                                            ?: hashMapOf()).apply {
                                        put("reported_author_name", CommonUtils.getUserName())
                                        put("reported_username", ownerDetail.username)
                                    })
                            mViewModel?.ownerAction(ownerDetail, CommentAction.Report)
                        }
                        StreamPlayerFragment.BLOCK_USER -> {
                            SegmentTracker.getInstance().trackEvent(EVENT_CHAT_ROOM_BLOCK_CLICKED,
                                    HashMap(mViewModel?.analyticsProperties
                                            ?: hashMapOf()).apply {
                                        put("blocked_username", ownerDetail.username)
                                        put("blocker", (if (mViewModel?.audioGroup?.get()?.ownerDetails?.id == CommonUtils.getUserID()) "author" else "moderator"))
                                    })
                            mViewModel?.ownerAction(ownerDetail, CommentAction.Block())
                        }
                        0x0012 -> {
                            if (mUserRecyclerAdapter.highlightedUser != null && !(CommonUtils.getUserID() == mUserRecyclerAdapter.highlightedUser?.id || mViewModel?.isStreamer == true)) return@newInstance
                            if (speakerAction == "mute") {
                                mViewModel?.agoraConnectionUtils?.muteRemoteAudio(id)
                            } else if (speakerAction == "unmute") {
                                mViewModel?.agoraConnectionUtils?.unmuteRemoteAudio(id)
                            }
                            SegmentTracker.getInstance().trackEvent(EVENT_CHAT_ROOM_MUTE_BUTTON_CLICKED,
                                    HashMap(mViewModel?.analyticsProperties
                                            ?: hashMapOf()).apply {
                                        put("muted_username", ownerDetail?.username)
                                        put("referrer", when {
                                            mViewModel?.audioGroup?.get()?.ownerDetails?.id == CommonUtils.getUserID() -> "author"
                                            mUserRecyclerAdapter.canUpdateUser -> "moderator"
                                            else -> "none"
                                        })
                                    })
                            mViewModel?.muteUnMuteParticipant(ownerDetail.username, ownerDetail.id, speakerAction)
                            ownerDetail.isMuted = speakerAction == "mute"
                            mUserRecyclerAdapter.updateItem(ownerDetail, true)
                        }
                        0x0100 -> {
                            SegmentTracker.getInstance().trackEvent(EVENT_BRING_TO_SPOTLIGHT_CLICKED,
                                    HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                                        put("chatroomAuthorname", mViewModel?.audioGroup?.get()?.ownerDetails?.username)
                                        put("selectedUsername", ownerDetail?.username)
                                    })
                            mViewModel?.highlightUser("highlight_participant", ownerDetail.id) {
                                AudioRoomService.highlightedUser = ownerDetail
                                mUserRecyclerAdapter.highlightedUser = ownerDetail
                                mViewModel?.highlightedUser = ownerDetail
                                mViewModel?.agoraConnectionUtils?.muteAllRemoteAudio()
                                mUserRecyclerAdapter.ownerDetail?.let { updateHighlightUserView(listOf(ownerDetail, it)) }
                            }
                        }
                        0x0101 -> {
                            SegmentTracker.getInstance().trackEvent(EVENT_REMOVE_SPOTLIGHT_CLICKED,
                                    HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
                                        put("chatroomAuthorname", mViewModel?.audioGroup?.get()?.ownerDetails?.username)
                                        put("selectedUsername", ownerDetail?.username)
                                    })
                            mViewModel?.highlightUser("unhighlight", ownerDetail.id) {
                                AudioRoomService.highlightedUser = null
                                mUserRecyclerAdapter.highlightedUser = null
                                mViewModel?.highlightedUser = null
                                updateMuteStateInNotification(isMicMuted)
                                mUserRecyclerAdapter.updateSelfItem(isMicMuted)
                                mUserRecyclerAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
            bottomSheet.chatMenuOptionData =
                    mViewModel?.getChatOptionMenuBottomSheetData(ownerDetail?.username, ownerDetail?.profileImageUrl)
            bottomSheet.show(childFragmentManager, ChatMenuOptionBottomSheet.TAG)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun showMenuBottomSheet(commentChat: CommentChat?) {
        if (commentChat == null || commentChat.username == null)
            return
        try {
            val listOptions = ArrayList<ListOption>()
            if (commentChat?.username != CommonUtils.getUserName())
                listOptions.add(ListOption.Header(StreamPlayerFragment.VIEW_PROFILE))
            if (CommonUtils.getUserName() == mViewModel?.username || mUserRecyclerAdapter.canUpdateUser) {
                if (commentChat.username != CommonUtils.getUserName()) {
                    context?.let {
                        val drawable = ContextCompat.getDrawable(it, R.drawable.ic_block)
                        listOptions.add(ListOption.Item(StreamPlayerFragment.REPORT_USER, "Report", R.drawable.avd_report, null))
                        listOptions.add(ListOption.Item(StreamPlayerFragment.BLOCK_USER, "Block User",
                                -1, ViewUtils.setTint(drawable, Color.rgb(251, 251, 251))))
                        listOptions.add(ListOption.Item(StreamPlayerFragment.DELETE_COMMENT, "Delete Comment",
                                R.drawable.ic_delete_outline_white, null))
                    }
                }

                context?.let {
                    if (!commentChat.isMedia)
                        listOptions.add(ListOption.Item(StreamPlayerFragment.PIN_COMMENT, "Pin Comment", -1,
                                ViewUtils.setTint(ContextCompat.getDrawable(it, R.drawable.avd_pin),
                                        Color.rgb(251, 251, 251))))
                }
            } else {
                listOptions.add(ListOption.Item(StreamPlayerFragment.REPORT_USER, "Report", R.drawable.avd_report, null))
            }

            val bottomSheet = ChatMenuOptionBottomSheet.newInstance(
                    listOptions
            ) { listOption: ListOption ->
                if (listOption is ListOption.Header) {
                    openProfile(commentChat.username)
                } else {
                    when ((listOption as ListOption.Item).id) {
                        StreamPlayerFragment.VIEW_PROFILE -> openProfile(commentChat.username)
                        StreamPlayerFragment.REPORT_USER -> onReport(commentChat)
                        StreamPlayerFragment.BLOCK_USER -> onBlockUser(commentChat)
                        StreamPlayerFragment.DELETE_COMMENT -> discardComment(commentChat)
                        StreamPlayerFragment.PIN_COMMENT -> {
                            mViewModel?.pinComment(commentChat)
                        }
                    }
                }
            }
            bottomSheet.chatMenuOptionData = mViewModel?.getChatOptionMenuBottomSheetData(commentChat.username, commentChat.profile_pic)
            if (listOptions.isEmpty()) return
            bottomSheet.show(childFragmentManager, ChatMenuOptionBottomSheet.TAG)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun openProfile(username: String?) {
        username ?: return
        val intent = ProfileActivity.getCallingIntent(context)
        intent.putExtra("author_name", username)
        startActivity(intent)
    }

    private fun discardComment(commentChat: CommentChat) {
        mChatListAdapter.removeChatItem(commentChat)
        mViewModel?.sendActionMessage(commentChat)
    }

    private fun onReport(commentChat: CommentChat) {
        if (CommonUtils.getUserName() == commentChat.username) {
            commentChat.messageType = AppConstants.MSG_TYPE_DELETED
            SegmentTracker.getInstance()
                    .trackEvent(SegmentConstants.EVENT_REPORT_COMMENT_ON_SELF_STREAM,
                            HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).also {
                                it["reported_comment_user"] = commentChat.username
                                it["reported_comment"] = commentChat.message
                                it[AppConstants.SCREEN_NAME] = SegmentConstants.SCREEN_NAME_AUDIO_CHAT_ROOM
                            })
            discardComment(commentChat)
            mViewModel?.onCommentAction(commentChat, CommentAction.Delete)
        } else
            SegmentTracker.getInstance()
                    .trackEvent(SegmentConstants.EVENT_REPORT_COMMENT,
                            HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).also {
                                it["reported_comment_user"] = commentChat.username
                                it["reported_comment"] = commentChat.message
                                it[AppConstants.SCREEN_NAME] = SegmentConstants.SCREEN_NAME_AUDIO_CHAT_ROOM
                            })
        mViewModel?.onCommentAction(commentChat, CommentAction.Report)
    }

    private fun onBlockUser(commentChat: CommentChat) {
        commentChat.messageType = AppConstants.MSG_TYPE_BLOCKED
        SegmentTracker.getInstance()
                .trackEvent(SegmentConstants.EVENT_BLOCK_USER,
                        HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).also {
                            it["blocked_user"] = commentChat.username
                            it["blocked_msg"] = commentChat.message
                            it[AppConstants.SCREEN_NAME] = SegmentConstants.SCREEN_NAME_AUDIO_CHAT_ROOM
                        })
        discardComment(commentChat)
        mViewModel?.onCommentAction(commentChat, CommentAction.Block())
    }

    private fun updateChatViews() {
        if (viewDataBinding == null || isStateSaved || !isAdded) return
        if (viewDataBinding.recyclerView.layoutManager !is LinearLayoutManager) return
        val chatLayoutManager = viewDataBinding.recyclerView.layoutManager as LinearLayoutManager
        if (chatLayoutManager.findFirstVisibleItemPosition() != 0) {
            mViewModel?.unreadChatCount?.set(mViewModel?.unreadChatCount?.get()?.plus(1))
        } else {
            viewDataBinding.recyclerView.scrollToPosition(0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == AppConstants.PURCHASE_REQUEST_CODE) {
                    viewModel.loadDailyRewards()
                } else if (requestCode == CHOOSE_MEDIA && data != null) {
                    val params = CommonUtils.getMediaDetailFromIntent(data, context)
                    if (params == null) {
                        context?.showToast("No File Found")
                        return
                    }

                    if (File(params[1]).sizeInMb > 15) {
                        context?.showToast("File size should not exceed 15 mb")
                        return
                    }

                    viewModel.currentMessageId = "${System.currentTimeMillis()}"
                    mChatListAdapter.addItem(CommentChat(viewModel.currentMessageId, params[1], CommonUtils.getUserName(), CommonUtils.getUserProfilePic(), params[0], Status.COMPRESSING))
                    viewDataBinding.recyclerView?.scrollToPosition(0)
                    viewModel.compressFile(requireContext(), params[1]) {
                        viewModel.uploadFile(params[0], it, onFileUpload = requireContext()::uploadFile)
                        requireActivity().runOnUiThread {
                            mChatListAdapter.setMediaStatus(mViewModel?.currentMessageId, Status.UPLOADING)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun askToBuyCoins() {
        try {
            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_BUY_COIN_DIALOG_SHOWN, mViewModel?.analyticsProperties
                    ?: hashMapOf())
            BuyCoinFragment.newInstance(object : BuyCoinCallbackListener {
                override fun onBuyClicked() {
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_BUY_COIN_BUTTON_CLICKED, mViewModel?.analyticsProperties
                            ?: hashMapOf())
                    val intent = Intent(context, BillingActivity::class.java)
                    intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_AUDIO_CHAT_ROOM)
                    intent.putExtra(AppConstants.USER_NAME, mViewModel?.audioGroup?.get()?.ownerDetails?.username
                            ?: "")
                    intent.putExtra(AppConstants.KEY_POST_ID, mViewModel?.postId ?: "")
                    this@AudioChatRoomFragment?.startActivityForResult(intent, AppConstants.PURCHASE_REQUEST_CODE)
                }
            }).show(childFragmentManager, BuyCoinFragment.TAG)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val TAG = "AudioChatRoomFragment"
        const val NOTIFICATION_CHANNEL = "audio_chat_room"
        const val NOTIFICATION_ID = 0x010101
//        const val AUDIO_ROOM_FILTER = "audio_room_filter"
//        const val ACTION_SPEAKER = "action_speaker"
//        const val ACTION_MIC = "action_mic"
//        const val ACTION_LEAVE_ROOM = "action_leave_room"

        fun newInstance(bundle: Bundle?) = AudioChatRoomFragment().apply {
            arguments = bundle
        }
    }
}

