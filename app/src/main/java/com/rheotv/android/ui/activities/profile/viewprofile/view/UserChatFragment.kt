package com.rheotv.android.ui.activities.profile.viewprofile.view

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.Observable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.R
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat
import com.rheotv.android.databinding.FragmentUserChatBinding
import com.rheotv.android.ui.activities.home.view.HomeActivity
import com.rheotv.android.ui.activities.inAppBilling.BillingActivity
import com.rheotv.android.ui.activities.inAppBilling.BuyCoinCallbackListener
import com.rheotv.android.ui.activities.inAppBilling.BuyCoinFragment
import com.rheotv.android.ui.activities.inAppBilling.BuyCoinFragment.Companion.newInstance
import com.rheotv.android.ui.activities.player.activity.ChatBoxBottomSheetDialog
import com.rheotv.android.ui.activities.player.activity.ChatBoxCallbackListener
import com.rheotv.android.ui.activities.player.activity.ChatMenuOptionBottomSheet
import com.rheotv.android.ui.activities.player.activity.ListOption
import com.rheotv.android.ui.activities.player.activity.StreamPlayerFragment.*
import com.rheotv.android.ui.activities.profile.viewprofile.utils.CommentAction
import com.rheotv.android.ui.activities.profile.viewprofile.viewmodel.UserChatViewModel
import com.rheotv.android.ui.adapters.ChatListAdapter
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog
import com.rheotv.android.utils.*
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class UserChatFragment : BaseFragment<FragmentUserChatBinding, UserChatViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mAdapter: ChatListAdapter

    override fun getBindingVariable() = com.rheotv.android.BR.viewModel

    override fun getLayoutId() = R.layout.fragment_user_chat

    override fun getViewModel() = ViewModelProvider(this, mViewModelFactory)
            .get(UserChatViewModel::class.java)
            .also {
                it.username = arguments?.getString(AppConstants.AUTHOR_NAME) ?: ""
                it.canChat = arguments?.getBoolean(AppConstants.CAN_CHAT) ?: false
                it.chatCriteriaMessage = arguments?.getString(AppConstants.CHAT_CRITERIA_MESSAGE)
                it.analyticsProperties[AppConstants.SCREEN_SOURCE] = arguments?.getString(AppConstants.SCREEN_SOURCE)
                        ?: ""
            }

    private val chatCallback = object : ChatListAdapter.ChatItemClickListenerV2 {
        override fun onUserClicked(commentChat: CommentChat?) {
            showMenuBottomSheet(commentChat)
        }

        override fun onMediaClicked(commentChat: CommentChat?) {

        }

        override fun onCommentClicked(commentChat: CommentChat?) {
            showMenuBottomSheet(commentChat)
        }
    }

    private val networkStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel?.connectChat()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SegmentTracker.getInstance(context)
                .recordScreenName(SegmentConstants.SCREEN_NAME_PROFILE_CHAT,
                        HashMap(viewModel.analyticsProperties).also {
                            it[AppConstants.SCREEN_NAME] = SegmentConstants.SCREEN_NAME_PROFILE_CHAT
                        })
        with(viewDataBinding) {
            recyclerView.apply {
                adapter = mAdapter.apply {
                    setListener(chatCallback)
                    setChatStickerSize(context?.stickerDimension() ?: 60)
                }
                onEndPageReachedListener(onEndReached = {
                    if (this@UserChatFragment.viewModel.isLoading?.get() != true && this@UserChatFragment.viewModel.nextCommentUrl != null) {
                        mAdapter.isShowLoading = true
                        this@UserChatFragment.viewModel.loadComments()
                    }
                })
            }

            if (activity is HomeActivity) {
                val lp = messageContainer.layoutParams as? ViewGroup.MarginLayoutParams
                lp?.let {
                    lp.bottomMargin = ViewUtils.dpToPx(72)
                    messageContainer.layoutParams = lp
                }
            }
            messageContainer.setOnClickListener { v ->
                if (CommonUtils.isUserLoggedin())
                    ChatBoxBottomSheetDialog.newInstance(object : ChatBoxCallbackListener {
                        override fun onChatSend(message: String) {
                            this@UserChatFragment.viewModel.sendComment(message)
                        }
                    }, this@UserChatFragment.viewModel.username
                            ?: "").show(childFragmentManager, ChatBoxBottomSheetDialog.TAG)
            }
            pinComment.pinImageView.setOnClickListener {
                if (CommonUtils.getUserName() == this@UserChatFragment.viewModel.username) {
                    this@UserChatFragment.viewModel.unPinComment()
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewModel) {
            getGroupId()
            loadComments()
            comments.observe(viewLifecycleOwner, Observer {
                mAdapter.submitItems(it)
            })

            removeComment.observe(viewLifecycleOwner, Observer {
                mAdapter.removeChatItem(it.message, it.username)
            })

            suggestions.observe(viewLifecycleOwner, Observer {
                viewDataBinding.tagChipGroup.addChips(it, onChipClick = { message ->
                    currentComment.set(message)
                    sendComment()
                })
            })

            onAskUserLogin.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_PROFILE_CHAT).show(childFragmentManager, TAG)
                }
            })

            askToBuyCoins.observe(viewLifecycleOwner, {
                askToBuyCoins()
            })

            connectionAction.observe(viewLifecycleOwner, {
                if (it == CommentAction.Connect && NetworkUtils.isNetworkConnected(context))
                    viewModel.connectChat()
            })
        }
    }

    private fun askToBuyCoins() {
        try {
            SegmentTracker.getInstance()
                    .trackEvent(SegmentConstants.EVENT_BUY_COIN_DIALOG_SHOWN,
                            HashMap(viewModel.analyticsProperties).also {
                                it[AppConstants.SCREEN_NAME] = SegmentConstants.SCREEN_NAME_PROFILE_CHAT
                            })

            newInstance(object : BuyCoinCallbackListener {
                override fun onBuyClicked() {
                    SegmentTracker.getInstance()
                            .trackEvent(SegmentConstants.EVENT_BUY_COIN_BUTTON_CLICKED,
                                    HashMap(viewModel.analyticsProperties).also {
                                        it[AppConstants.SCREEN_NAME] = SegmentConstants.SCREEN_NAME_PROFILE_CHAT
                                    })

                    val intent = Intent(context, BillingActivity::class.java)
                    intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_CHAT)
                    intent.putExtra(AppConstants.USER_NAME, viewModel.username)
                    intent.putExtra(AppConstants.KEY_POST_ID, viewModel.postId)
                    this@UserChatFragment.startActivityForResult(intent, AppConstants.PURCHASE_REQUEST_CODE)
                }
            }).show(childFragmentManager, BuyCoinFragment.TAG)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onReport(commentChat: CommentChat) {
        if (CommonUtils.getUserName() == commentChat.username) {
            commentChat.messageType = AppConstants.MSG_TYPE_DELETED
            SegmentTracker.getInstance()
                    .trackEvent(SegmentConstants.EVENT_REPORT_COMMENT_ON_SELF_STREAM,
                            HashMap(viewModel.analyticsProperties).also {
                                it["reported_comment_user"] = commentChat.username
                                it["reported_comment"] = commentChat.message
                                it[AppConstants.SCREEN_NAME] = SegmentConstants.SCREEN_NAME_PROFILE_CHAT
                            })
            discardComment(commentChat)
            viewModel.onCommentAction(commentChat, CommentAction.Delete)
        } else
            SegmentTracker.getInstance()
                    .trackEvent(SegmentConstants.EVENT_REPORT_COMMENT,
                            HashMap(viewModel.analyticsProperties).also {
                                it["reported_comment_user"] = commentChat.username
                                it["reported_comment"] = commentChat.message
                                it[AppConstants.SCREEN_NAME] = SegmentConstants.SCREEN_NAME_PROFILE_CHAT
                            })
        viewModel.onCommentAction(commentChat, CommentAction.Report)
    }

    private fun onBlockUser(commentChat: CommentChat) {
        commentChat.messageType = AppConstants.MSG_TYPE_BLOCKED
        SegmentTracker.getInstance()
                .trackEvent(SegmentConstants.EVENT_BLOCK_USER,
                        HashMap(viewModel.analyticsProperties).also {
                            it["blocked_user"] = commentChat.username
                            it["blocked_msg"] = commentChat.message
                            it[AppConstants.SCREEN_NAME] = SegmentConstants.SCREEN_NAME_PROFILE_CHAT
                        })
        discardComment(commentChat)
        viewModel.onCommentAction(commentChat, CommentAction.Block())
    }

    private fun discardComment(commentChat: CommentChat) {
        mAdapter.removeChatItem(commentChat)
        viewModel.sendActionMessage(commentChat)
    }

    private fun openProfile(username: String) {
        val intent = ProfileActivity.getCallingIntent(activity)
        intent.putExtra("author_name", username)
        startActivity(intent)
    }

    private fun showMenuBottomSheet(commentChat: CommentChat?) {
        if (commentChat?.username == null)
            return
        try {
            val listOptions = ArrayList<ListOption>()
            if (commentChat.username != CommonUtils.getUserName())
                listOptions.add(ListOption.Header(VIEW_PROFILE))
            if (CommonUtils.getUserName() == viewModel.username) {
                if (commentChat.username != CommonUtils.getUserName()) {
                    val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_block)
                    listOptions.add(ListOption.Item(REPORT_USER, "Report", R.drawable.avd_report, null))
                    listOptions.add(ListOption.Item(BLOCK_USER, "Block User", -1, ViewUtils.setTint(drawable, Color.rgb(251, 251, 251))))
                    listOptions.add(ListOption.Item(DELETE_COMMENT, "Delete Comment", R.drawable.ic_delete_outline_white, null))
                }
                listOptions.add(ListOption.Item(PIN_COMMENT, "Pin Comment", -1, ViewUtils.setTint(ContextCompat.getDrawable(requireContext(), R.drawable.avd_pin), Color.rgb(251, 251, 251))))
            } else {
                listOptions.add(ListOption.Item(REPORT_USER, "Report", R.drawable.avd_report, null))
            }

            val bottomSheet = ChatMenuOptionBottomSheet.newInstance(
                    listOptions
            ) { listOption: ListOption ->
                if (listOption is ListOption.Header) {
                    openProfile(commentChat.username)
                } else {
                    when ((listOption as ListOption.Item).id) {
                        VIEW_PROFILE -> openProfile(commentChat.username)
                        REPORT_USER -> onReport(commentChat)
                        BLOCK_USER -> onBlockUser(commentChat)
                        DELETE_COMMENT -> discardComment(commentChat)
                        PIN_COMMENT -> viewModel.pinComment(commentChat)
                    }
                }
                null
            }
            bottomSheet.chatMenuOptionData = viewModel.getChatOptionMenuBottomSheetData(commentChat.username, commentChat.profile_pic)
            bottomSheet.show(childFragmentManager, ChatMenuOptionBottomSheet.TAG)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.PURCHASE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel?.loadDailyRewards()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.connectChat()
        context?.registerNetworkReceiver(networkStateReceiver)
//        if (activity is HomeActivity) {
//            KeyboardEventListener(WeakReference(this)) {
//                val lp = (viewDataBinding.messageContainer.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
//                    bottomMargin = ViewUtils.dpToPx(if (it) 16 else 72)
//                }
//                viewDataBinding.messageContainer.layoutParams = lp
//            }
//        }
    }

    override fun onPause() {
        super.onPause()
        viewModel?.disconnectChat()
        context?.unregisterReceiver(networkStateReceiver)
    }

    companion object {
        private val TAG = this::class.simpleName

        fun newInstance(username: String?, source: String, canChat: Boolean = false, chatCriteriaMessage: String? = null): UserChatFragment {
            val args = Bundle()
            args.putString(AppConstants.AUTHOR_NAME, username)
            args.putBoolean(AppConstants.CAN_CHAT, canChat)
            args.putString(AppConstants.CHAT_CRITERIA_MESSAGE, chatCriteriaMessage)
            args.putString(AppConstants.SCREEN_SOURCE, source)
            val fragment = UserChatFragment()
            fragment.arguments = args
            return fragment
        }
    }
}