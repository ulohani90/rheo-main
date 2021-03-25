package com.rheotv.android.ui.activities.audioroom.view

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.TypefaceSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.Observable
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.SimpleItemAnimator
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentAudioRoomListBinding
import com.rheotv.android.services.*
import com.rheotv.android.ui.activities.audioroom.adapter.AudioRoomListAdapter
import com.rheotv.android.ui.activities.audioroom.adapter.AudioRoomSuggestionAdapter
import com.rheotv.android.ui.activities.audioroom.adapter.ChatRoomUserRecyclerAdapter
import com.rheotv.android.ui.activities.audioroom.model.AudioConnection
import com.rheotv.android.ui.activities.audioroom.model.AudioRoom
import com.rheotv.android.ui.activities.audioroom.model.OwnerDetail
import com.rheotv.android.ui.activities.audioroom.viewmodel.AudioChatRoomActivityViewModel
import com.rheotv.android.ui.activities.audioroom.viewmodel.AudioRoomViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog
import com.rheotv.android.utils.*
import com.rheotv.android.utils.AppUtilsKt.boldFontSizeForPath
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.Serializable
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 * Use the [AudioRoomListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AudioRoomListFragment : BaseFragment<FragmentAudioRoomListBinding, AudioRoomViewModel>() {
    private val TAG = javaClass.simpleName

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mUserRecyclerAdapter: ChatRoomUserRecyclerAdapter

    lateinit var mAdapter: AudioRoomListAdapter
    lateinit var suggestionAdapter: AudioRoomSuggestionAdapter
    private var mServiceIntent: Intent? = null
    private val searchHandler by lazy { Handler(Looper.getMainLooper()) }
    private var searchRunnable = Runnable { viewModel?.searchResults() }
    private var isEmptyUserEventSent: Boolean = false

    private val audioEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            p1 ?: return
            val d = p1.getParcelableExtra<AudioConnection>(AUDIO_ACTION) ?: return
            when (d) {
                is AudioConnection.CallConnected -> {
                    val map = HashMap<String, Any?>(viewModel?.analyticsProperties ?: hashMapOf())
                            .apply {
                                if (!CommonUtils.isFirstAgoraAudioCallDone()) {
                                    CommonUtils.setFirstAgoraAudioCallDone()
                                    "is_first" to true
                                }
                            }
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_AGORA_CALL_STARTED, map)
//                    context?.showToast("You are connected to chatroom, Start talking to participate")
                }

                is AudioConnection.UserCountUpdate -> {
                    activity?.runOnUiThread {
                        viewModel?.featuredChatRoomJoinUserCount = d.count
                    }
                }

                is AudioConnection.UserJoined -> {

                }

                is AudioConnection.FirstUser -> {
                    activity?.runOnUiThread {
                        if (!isEmptyUserEventSent) {
                            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_EMPTY_ROOM,
                                    HashMap(viewModel?.analyticsProperties ?: hashMapOf()).apply {
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
                                HashMap<String, Any?>(viewModel?.analyticsProperties
                                        ?: hashMapOf()))
                    }
                }

                is AudioConnection.UserJoinRoom -> {
                    activity?.runOnUiThread {
                        Log.i(TAG, "updateUserList: UserJoinRoom")
                        updateConnectedUsers(d.user)
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
                        viewModel?.refresh()
                    }
                }
            }
        }
    }

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.fragment_audio_room_list

    override fun getViewModel(): AudioRoomViewModel? {
        return try {
            ViewModelProvider(this, viewModelFactory).get(AudioRoomViewModel::class.java).also {
                it.analyticsProperties[AppConstants.SCREEN_NAME] = SegmentConstants.SCREEN_NAME_AUDIO_CHAT_ROOM_LIST
                it.analyticsProperties[AppConstants.SCREEN_SOURCE] = arguments?.getString(AppConstants.SCREEN_SOURCE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        suggestionAdapter = AudioRoomSuggestionAdapter(requireActivity(), android.R.layout.simple_dropdown_item_1line)
        with(viewDataBinding) {
            progressBar.visibility = View.VISIBLE
            recyclerView.apply {
                adapter = AudioRoomListAdapter { it, position ->
                    openRoom(it, position)
                }.also { mAdapter = it }

                onEndPageReachedListener({
                    if (!mAdapter.loading && viewModel?.isLoading?.get() != true && viewModel?.nextUrl != null) {
                        mAdapter.setShowLoading(true)
                        viewModel?.fetchAudioRoomList()
                    }
                })
            }

            searchView.apply {
                threshold = 1
                setAdapter(suggestionAdapter)
                setOnKeyListener(View.OnKeyListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        viewModel?.searchResults()
                        CommonUtils.hideKeyboard(activity)
                        return@OnKeyListener true
                    }
                    false
                })

                setOnItemClickListener { _, _, i, _ ->
                    searchHandler.removeCallbacks(searchRunnable)
                    val room = suggestionAdapter.getItem(i)
                    viewModel?.searchQuery?.set("")
                    openRoom(room, i)
                    CommonUtils.hideKeyboard(activity)
                }

//                onFocusChangeListener = View.OnFocusChangeListener { _, isFocused ->
//                    if (isFocused) {
//                        if (this.text.toString().trim { it <= ' ' }.isNotEmpty()) {
//                            viewModel.searchResults()
//                        }
//                    }
//                }

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                    override fun afterTextChanged(editable: Editable) {
                        if (editable.toString().trim { it <= ' ' }.isNotEmpty()) {
                            searchWithDelay()
                        }
                    }
                })
            }

            searchImageView.setOnClickListener {
                viewModel?.isSearchVisible?.set(true)
                searchView.handler.postDelayed({
                    searchView.requestFocus()
                    val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    inputMethodManager?.toggleSoftInputFromWindow(
                            searchView.applicationWindowToken,
                            InputMethodManager.SHOW_FORCED, 0)
                }, 100)
            }

            featuredRecyclerView.apply {
                (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
                adapter = mUserRecyclerAdapter.also {
                    it.onItemClick = object : Function<Unit>, (OwnerDetail?, Boolean) -> Unit {
                        override fun invoke(ownerDetail: OwnerDetail?, isMuted: Boolean) {
                            openRoom()
                        }
                    }
                }

                onEndPageReachedListener(onEndReached = {
                    if (!mUserRecyclerAdapter.isPaginating() && !viewModel?.userNextUrl.isNullOrEmpty()) {
                        mUserRecyclerAdapter.setPaginating(true)
                        viewModel?.loadConnectedUsers()
                    }
                })
            }

            featuredCardView.setOnClickListener {
                openRoom()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel?.let { vm ->
            with(vm) {
                audioRoomListLiveData.observe(viewLifecycleOwner, {
                    viewDataBinding?.progressBar?.visibility = View.GONE
                    mAdapter.submitList(it ?: return@observe)
                })

                suggestions.observe(viewLifecycleOwner, {
                    suggestionAdapter.submitList(it)
                })

                isRefreshing.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                    override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                        if (isRefreshing.get() == true) {
                            mAdapter.clearList()
                            mUserRecyclerAdapter.clearList()
                        }
                    }
                })

                featuredRoomParticipant.observe(viewLifecycleOwner, {
                    updateConnectedUsers(it)
                })

                userMutableLiveData.observe(viewLifecycleOwner, {
                    Log.i(TAG, "updateUser: $it")
                    updateUserList(it)
                })

                connectAudioLiveData.observe(viewLifecycleOwner, {
                    Log.i(TAG, "connectAudio: $it")
                    if (it == true) {
                        if (viewModel?.currentChatRoomId == viewModel?.featuredChatRoomId &&
                                AudioRoomService.isNonFeaturedRoomRunning(viewModel?.featuredChatRoomId
                                        ?: ""))

                            return@observe
                        if (viewModel?.currentChatRoomId != viewModel?.featuredChatRoomId) {
                            if (viewModel?.currentChatRoomId == null) {
                                viewModel?.updateCurrentRoomId()
                            } else {
                                viewModel?.disconnectGrpc()
                                viewModel?.connectGrpc()
                            }
                        }
                        startAudioCall()
                    }
                })

                isMuted.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                    override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                        mUserRecyclerAdapter.updateSelfItem(isMuted.get() ?: false)
                        viewModel?.isSelfMuted = isMuted.get() ?: false
                        startService(CommonUtils.getUserID(), viewModel?.isSelfMuted, false)
                    }
                })

                fetchAudioRoomList()
                initAgora()
            }
        }
    }

    private fun initAgora() {
        /*if (!CommonUtils.isFeaturedRoomEnabled()) return
        viewModel?.agoraConnectionUtils?.apply {
            onCallConnected = {
                activity?.runOnUiThread {
                    val map = HashMap<String, Any?>(viewModel?.analyticsProperties ?: hashMapOf())
                            .apply {
                                if (!CommonUtils.isFirstAgoraAudioCallDone()) {
                                    CommonUtils.setFirstAgoraAudioCallDone()
                                    "is_first" to true
                                }
                            }
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_AGORA_CALL_STARTED, map)
                    context?.showToast("You are connected to chatroom, Start talking to participate")
                }
            }
            onCallDisconnected = {
                activity?.runOnUiThread {
                    context?.showToast("Disconnected from audio chat room!")
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_AGORA_CALL_ENDED,
                            HashMap<String, Any?>(viewModel?.analyticsProperties ?: HashMap<String, Any?>(viewModel?.analyticsProperties ?: hashMapOf())))
                }
            }
            onCallLeft = {
                activity?.runOnUiThread {
//                    mUserRecyclerAdapter.removeItem(it)
//                    context?.showToast("$it left the call")
//                    mViewModel?.leaveChatRoom(it)
                }
            }
            onUserJoined = {
                activity?.runOnUiThread {
//                    context?.showToast("$it joined the call")
//                    mViewModel?.joinChatRoom(it)
                }
            }

            onFirstUser = {
                activity?.runOnUiThread {
//                    if (!isEmptyUserEventSent) {
//                        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_EMPTY_ROOM,
//                                HashMap(mViewModel?.analyticsProperties ?: hashMapOf()).apply {
//                                    put("time", Date().toString())
//                                })
//                        isEmptyUserEventSent = true
//                    }
                }
            }

            onSpeakerIndicate = {
                if (AudioRoomService.connectedRoomId != viewModel?.featuredChatRoomId)
                    mUserRecyclerAdapter.onUserSpeak(it)
            }
        }*/
    }

    private fun searchWithDelay() {
        searchHandler.removeCallbacks(searchRunnable)
        searchHandler.postDelayed(searchRunnable, 300)
    }

    override fun onResume() {
        super.onResume()
        viewModel?.isVisible = true
        if (CommonUtils.isFeaturedRoomEnabled() && hasMicrophonePermission()) {
            if (!AudioRoomService.isRunning) {
                startAudioCall()
                viewModel?.connectGrpc()
            }
            context?.registerReceiver(audioEventReceiver, IntentFilter(AUDIO_ACTION))
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel?.isVisible = false
        if (CommonUtils.isFeaturedRoomEnabled() && hasMicrophonePermission() && AudioRoomService.isFeaturedRoomRunning(viewModel?.featuredChatRoomId
                        ?: "") && !AudioRoomService.isServiceInBackground) {
            viewModel?.disconnectGrpc()
            stopService()
            context?.unregisterReceiver(audioEventReceiver)
        }
    }

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    private fun hasMicrophonePermission() =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventModel: EventBusModel.RefreshAudioGroupList?) {
        if (!isDetached && !isRemoving) {
            viewModel?.nextUrl = null
            mAdapter.clearList()
            viewModel?.fetchAudioRoomList()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRoomConnected(param: EventBusModel.AudioRoomConnected) {
        activity?.runOnUiThread {
            if (viewModel?.featuredChatRoomId?.equals(param.id ?: "") == false)
                mUserRecyclerAdapter.removeMe()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRoomDisconnected(param: EventBusModel.AudioRoomDisconnected) {
        activity?.runOnUiThread {
            if (!AudioRoomService.isRunning) {
                viewModel?.connectGrpc()
                startAudioCall()
            }
        }
    }

    private fun openRoom(room: AudioRoom, position: Int) {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_CLICKED,
                HashMap<String, Any?>(viewModel?.analyticsProperties ?: hashMapOf()).apply {
                    "is_author" to (room.groupDetails?.ownerDetails?.id == CommonUtils.getUserID())
                    "group_title" to (room.groupDetails?.name)
                    "online_count" to (room.activeChatRooms?.totalActiveUsers)
                    "last_message" to (room.lastComment?.text)
                    "profile_pic_url" to (room.groupDetails?.ownerDetails?.profileImageUrl)
                    "chatroom_author_name" to (room.groupDetails?.ownerDetails?.username)
                    "listing_rank" to position
                })
        Log.i(TAG, "open_room: ${AudioRoomService.connectedRoomId == null} and ${viewModel?.featuredChatRoomId} and ${AudioRoomService.connectedRoomId}")
        if (CommonUtils.isUserLoggedin()) {
            if (room.groupDetails?.id?.equals(AppConstants.FEMALE_ONLY_GROUP) == true) {
                context?.showToast("This group is for female users only!")
            } else {
                if (room.activeChatRooms?.chatRoomList?.isEmpty() == true) {
                    context?.showToast("Room no longer exist!")
                } else if (AudioRoomService.connectedRoomId == null ||
                        room.activeChatRooms?.chatRoomList?.get(0)?.equals(AudioRoomService.connectedRoomId
                                ?: "") == true ||
                        viewModel?.featuredChatRoomId?.equals(AudioRoomService.connectedRoomId
                                ?: "") == true
                ) {
                    enterRoom(room)
                } else {
                    context?.showConfirmBottomSheetDialog(
                            title = String.format(getString(R.string.enter_audio_room_title), room.groupDetails?.ownerDetails?.username),
                            spannableMessage = getEnterRoomMessage(),
                            confirmLabel = "Enter",
                            denyLabel = "Cancel",
                            onConfirm = { enterRoom(room) }
                    )
                }
            }
        } else
            LoginFragmentBottomDialog.getInstance("").show(childFragmentManager, TAG)
    }

    private fun enterRoom(room: AudioRoom) {
        AudioChatRoomActivity.startMe(requireContext(),
                room.groupDetails,
                room.activeChatRooms?.totalActiveUsers,
                if (room.activeChatRooms?.chatRoomList.isNullOrEmpty()) null
                else room.activeChatRooms?.chatRoomList?.get(0),
                arguments?.getString(AppConstants.SCREEN_SOURCE)
        )
    }

    private fun openRoom() {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_FEATURED_ROOM_CLICKED,
                HashMap<String, Any?>(viewModel?.analyticsProperties ?: hashMapOf()).apply {
                    "is_author" to (viewModel?.audioGroup?.get()?.ownerDetails?.id == CommonUtils.getUserID())
                    "group_title" to (viewModel?.audioGroup?.get()?.name)
                    "online_count" to (viewModel?.featuredChatRoomJoinUserCount)
                    "profile_pic_url" to (viewModel?.audioGroup?.get()?.ownerDetails?.profileImageUrl)
                    "chatroom_author_name" to (viewModel?.featureRoomAuthor)
                })
        Log.i(TAG, "open_room: ${AudioRoomService.connectedRoomId == null} and ${viewModel?.featuredChatRoomId} and ${AudioRoomService.connectedRoomId}")
        if (CommonUtils.isUserLoggedin()) {
            if (AudioRoomService.connectedRoomId == null || viewModel?.featuredChatRoomId?.equals(AudioRoomService.connectedRoomId
                            ?: "") == true) {
                enterRoomWithIds()
            } else {
                context?.showConfirmBottomSheetDialog(
                        title = String.format(getString(R.string.enter_audio_room_title), viewModel?.featureRoomAuthor),
                        spannableMessage = getEnterRoomMessage(),
                        confirmLabel = "Enter",
                        denyLabel = "Cancel",
                        onConfirm = { enterRoomWithIds() }
                )
            }
        } else
            LoginFragmentBottomDialog.getInstance("").show(childFragmentManager, TAG)
    }

    private fun enterRoomWithIds() {
        AudioChatRoomActivity.startMe(requireContext(),
                viewModel?.audioGroup?.get()?.id,
                1,
                viewModel?.featuredChatRoomId,
                AppConstants.SCREEN_SOURCE,
                false)
    }

    private fun getEnterRoomMessage(): SpannableString? {
        val spannable = SpannableString(String.format(getString(R.string.enter_audio_room_message), AudioRoomService.connectedRoomOwner))
        boldFontSizeForPath(spannable, AudioRoomService.connectedRoomOwner, Color.WHITE)
        spannable.setSpan(TypefaceSpan("sans-serif-medium"), 0, AudioRoomService.connectedRoomOwner?.length
                ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun updateConnectedUsers(it: List<OwnerDetail>?) {
        it?.map {
            if (it.id == CommonUtils.getUserID())
                it.isMuted = viewModel?.isMuted?.get() ?: it.isMuted ?: false
        }
        Log.i(TAG, "updateUserList: updateConnectedUsers")
        val result: MutableList<OwnerDetail> = it?.toMutableList() ?: mutableListOf()
        mUserRecyclerAdapter.setPaginating(false)
        mUserRecyclerAdapter.submitList(result)
    }

    private fun updateUserList(it: Pair<AudioChatRoomActivityViewModel.AudioRoomAction, AudioChatRoomActivityViewModel.UpdateData?>?) {
        when (it?.first) {
            AudioChatRoomActivityViewModel.AudioRoomAction.AddUser -> {
                it.second?.ownerDetail?.let { item ->
                    if (item.id?.equals(CommonUtils.getUserID()) == true) {
                        Log.i(TAG, "updateUserList: ${item.isMuted} and ${viewModel?.isMuted?.get()}")
                        item.isMuted = viewModel?.isMuted?.get() ?: false
                    }
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
                    if (item.actionUserName != CommonUtils.getUserName() && item.ownerDetail?.id == CommonUtils.getUserID()) {
                        mUserRecyclerAdapter.muteMessage = "Room owner has muted you. Only owner can unmute you!"
                        mUserRecyclerAdapter.canUnMuteSelf = item.ownerDetail.isMuted == false
                    }
                    if (item.ownerDetail?.id == CommonUtils.getUserID()) {
                        if (item.ownerDetail.isMuted == true) {
                            viewModel?.agoraConnectionUtils?.muteLocalAudio()
                            viewModel?.isMuted?.set(true)
                        } else {
                            viewModel?.agoraConnectionUtils?.unMuteLocalAudio()
                            viewModel?.isMuted?.set(false)
                        }
                    }
                    mUserRecyclerAdapter.updateItem(item.ownerDetail ?: return@let, true)
                }
            }
            AudioChatRoomActivityViewModel.AudioRoomAction.FinishRoom -> activity?.finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 999 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_VOICE_CALL_PERMISSION_GIVEN, viewModel?.analyticsProperties)
            context?.let { startAudioCall() }
        } else {
            context?.showToast("Micro phone permission is must to enter audio chatrooms, please enable it from app settings")
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(TAG, "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_VOICE_CALL_PERMISSION_SHOWN, viewModel?.analyticsProperties
                    ?: hashMapOf())
            requestPermissions(arrayOf(permission), requestCode)
            return false
        }
        return true
    }

    private fun startAudioCall() {
        if (CommonUtils.isFeaturedRoomEnabled() && checkSelfPermission(Manifest.permission.RECORD_AUDIO, 999)) {
            context?.let {
                Log.i(TAG, "connectAudio: startAudioCall : $it")
                startService(CommonUtils.getUserID(), viewModel?.isAllowedToSpeak == false || viewModel?.isSelfMuted ?: false, true)
            }
        }
    }

    private fun startService(
            muteUnMuteUID: Int? = null,
            muteUser: Boolean? = null,
            startAudioCall: Boolean = false,
            enterInMuteState: Boolean = false
    ) {
        if (viewModel?.mAgoraChannelId.isNullOrEmptyOrBlank() || viewModel?.isVisible == false) return
        mServiceIntent = Intent(context, AudioRoomService::class.java).apply {
            putExtra(AUDIO_DETAIL, viewModel?.roomDetails)
            putExtra(AUDIO_START_CALL, startAudioCall)
            putExtra(AUDIO_IS_SELF_MUTE, enterInMuteState)
            putExtra(AUDIO_ROOM_PROPERTIES, viewModel?.analyticsProperties as? Serializable)
            putExtra(IS_BACKGROUND, false)
            muteUnMuteUID?.let { putExtra(AUDIO_MUTE_UNMUTE_UID, muteUnMuteUID) }
            muteUnMuteUID?.let { putExtra(AUDIO_MUTE_USER, muteUser) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context?.startForegroundService(mServiceIntent)
        else
            context?.startService(mServiceIntent)
    }

    fun stopService() {
        if (viewModel?.featuredChatRoomId?.equals(AudioRoomService.connectedRoomId) == false) return
        mServiceIntent = Intent(context, AudioRoomService::class.java).apply {
            putExtra(STOP_SERVICE, true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context?.startForegroundService(mServiceIntent)
        else
            context?.startService(mServiceIntent)
    }

    private fun muteUser(ownerDetail: OwnerDetail?, muted: Boolean, segmentAction: String?) {
        ownerDetail ?: return
        if (ownerDetail.id == CommonUtils.getUserID()) {
            if (segmentAction.isNullOrEmpty()) {
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_MUTE_BUTTON_CLICKED,
                        HashMap(viewModel?.analyticsProperties ?: hashMapOf()).apply {
                            put("muted_username", ownerDetail.username)
                            put("referrer", "self_profile_click")
                        })
            }
            if (mUserRecyclerAdapter.canUnMuteSelf) {
                val action = if (!muted) "unmute" else "mute"
                viewModel?.isMuted?.set(muted)
                viewModel?.muteUnMuteParticipant(ownerDetail.username, ownerDetail.id, action)
                mUserRecyclerAdapter.updateSelfItem(muted)
            } else {
                context?.showToast(mUserRecyclerAdapter.muteMessage)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AudioRoomListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(screenSource: String?) =
                AudioRoomListFragment().apply {
                    arguments = bundleOf(AppConstants.SCREEN_SOURCE to screenSource)
                }
    }
}