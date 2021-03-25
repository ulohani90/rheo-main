package com.rheotv.android.ui.activities.player.activity.newPlayer.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.data.network.models.postlisting.responses.User
import com.rheotv.android.data.network.models.postlisting.responses.UserObject
import com.rheotv.android.data.network.models.postlisting.responses.VideoCallUsersListObject
import com.rheotv.android.databinding.BottomSheetRequestVideoCallBinding
import com.rheotv.android.ui.activities.home.view.HomeActivity
import com.rheotv.android.ui.activities.player.activity.newPlayer.activities.FullScreenVideoActivity
import com.rheotv.android.ui.activities.player.activity.newPlayer.adapter.VideoCallRequestsRVAdapter
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.RequestVideoCallViewModel
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.VideoCallAction
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.RewardManager
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentConstants.*
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.rheotv.android.utils.showToast
import kotlinx.android.synthetic.main.bottom_sheet_player_gift.view.*
import javax.inject.Inject

class RequestToVideoCallBottomSheet : BaseBottomSheetDialogFragment<BottomSheetRequestVideoCallBinding, RequestVideoCallViewModel>() {

    @Inject
    lateinit var mViewModel: RequestVideoCallViewModel

    @Inject
    lateinit var usersListAdapter: VideoCallRequestsRVAdapter

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.bottom_sheet_request_video_call

    var progressBar: ProgressDialog? = null

    var callback: OnRequestVideoCallListener? = null


    override fun getViewModel(): RequestVideoCallViewModel = mViewModel.also {
        it.postId = arguments?.getString(ARG_KEY_POST_ID)
        //it.isAuthor = arguments?.getString(ARG_KEY_AUTHOR_NAME)?.equals(CommonUtils.getUserName(), true) == true
        it.isAuthor = arguments?.getString(ARG_KEY_AUTHOR_NAME)?.equals(CommonUtils.getUserName(), true) == true
        it.authorName = arguments?.getString(ARG_KEY_AUTHOR_NAME)
        //it.postId = "374018fe-5ad8-4986-a8bb-61b77e32cd0a"
        //it.isAuthor = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(receiver, IntentFilter("call_request_action")) }
        context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(cameraPermissionReceiver, IntentFilter("ACTION_CAMERA_PERMISSION")) }

        viewDataBinding.refreshBtn.setOnClickListener { v ->

            SegmentTracker.getInstance().trackEvent(EVENT_COHOST_REFRESH_CLICKED, hashMapOf<String, Any?>(
                    "post_id" to viewModel?.postId,
                    "author_name" to viewModel.authorName,
                    "is_author" to viewModel.isAuthor,
                    "request_coin_value" to viewModel.finalCoinValue
            ))
            refreshList();
        }
        loadUsersList()
    }

    override fun onDetach() {
        super.onDetach()
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(receiver) }
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(cameraPermissionReceiver) }
    }


    var cameraPermissionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onRequestCohostClick()
        }
    }

    var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var obj: VideoCallUsersListObject? = intent?.getParcelableExtra("user_obj")
            if (obj?.state.equals(AppConstants.VIDEO_CALL_STATE_REQUESTED, true)) {
                if (viewModel.nextUrl == null && obj?.userProfile?.user?.username?.equals(CommonUtils.getUserName(), true) == false) {
                    if (viewDataBinding.videoCallRequestQueue.adapter != null) {
                        viewDataBinding.nullRequestsTv.visibility = View.GONE
                        usersListAdapter?.appendUser(obj)
                    } else {
                        addRVData(listOf(obj));
                    }

                }
            } else {
                usersListAdapter?.updateUserState(obj)
                if (obj?.userProfile?.user?.id?.equals(CommonUtils.getUserID()) == true) {
                    viewDataBinding.queuePos.visibility = View.VISIBLE
                    if (obj?.state.equals(AppConstants.VIDEO_CALL_STATE_INITIATED, true)) {
                        viewDataBinding.requestButton.visibility = View.VISIBLE
                        viewDataBinding.queuePos.text = "You request to become a co-host is accepted."
                    } else if (obj?.state.equals(AppConstants.VIDEO_CALL_STATE_ENDED, true)) {
                        viewDataBinding.requestButton.visibility = View.VISIBLE
                        viewDataBinding.queuePos.text = "You co-host request is completed."
                    } else if (obj?.state.equals(AppConstants.VIDEO_CALL_STATE_DENIED, true)) {
                        viewDataBinding.requestButton.visibility = View.VISIBLE
                        viewDataBinding.queuePos.text = "You request to become a co-host is denied.\nRheo coins have been refunded back to your account."
                    } else if (obj?.state.equals(AppConstants.VIDEO_CALL_STATE_IN_PROGRESS, true)) {
                        viewDataBinding.queuePos.text = "You are currently a co-host."
                    } else if (obj?.state.equals(AppConstants.VIDEO_CALL_STATE_REFUNDED, true)) {
                        viewDataBinding.requestButton.visibility = View.VISIBLE
                        viewDataBinding.queuePos.text = "${viewModel.authorName} could not accept your co-host request.\nRheo coins have been refunded back to your account."
                    }
                }
            }

        }
    }

    private fun refreshList() {
        usersListAdapter?.refreshData();
        viewModel.nextUrl = null
        viewDataBinding.loading.visibility = View.VISIBLE
        loadUsersList()
    }

    fun loadUsersList() {
        viewDataBinding.isAuthor = viewModel.isAuthor
        viewDataBinding.authorName = viewModel.authorName

        viewModel.getVideoRequestUsers(viewModel.postId) { list: List<VideoCallUsersListObject>?, sortedPos: Int?, error: String?, state: String? ->
            if (list != null) {
                mViewModel.isLoadingData = false
                viewDataBinding.loading.visibility = View.GONE
                if (list.isNotEmpty()) {
                    if (viewDataBinding.videoCallRequestQueue.adapter != null) {
                        usersListAdapter?.setShowLoading(false)
                    }
                    adjustViews(list, sortedPos, state);
                    viewDataBinding.nullRequestsTv.visibility = View.GONE
                } else {
                    if (viewModel.isAuthor && usersListAdapter.itemCount == 0) {
                        viewDataBinding.nullRequestsTv.visibility = View.VISIBLE
                    } else {
                        viewDataBinding.requestButton.visibility = View.VISIBLE
                        viewDataBinding.queuePos.visibility = View.GONE
                        viewDataBinding.nullRequestsTv.visibility = View.GONE
                    }
                }
            }
            //viewDataBinding?.coinValue = mViewModel.coinValue
            updateCoinsValue();
        }


        viewDataBinding?.request?.setOnClickListener {
            //

            /*val map: MutableMap<String, Any> = java.util.HashMap<String, Any>(*//*properties*//*)
            map["post_id"] = viewModel?.postId
            map["is_author"] = viewModel?.isAuthor
            map["author_name"] = viewModel.authorName

            var properties = HashMap<String, Any>()
            properties.put("post_id", viewModel?.postId)
            properties.put("is_author", viewModel?.isAuthor)
            properties.put("author_name", viewModel.authorName)*/
            SegmentTracker.getInstance().trackEvent(EVENT_COHOST_REQUEST_CLICKED, hashMapOf<String, Any?>(
                    "post_id" to viewModel?.postId,
                    "author_name" to viewModel.authorName,
                    "is_author" to viewModel.isAuthor,
                    "request_coin_value" to viewModel.finalCoinValue
            ))
            if (activity is HomeActivity) {
                if (!(activity as HomeActivity).checkPermissionForCamera()) {
                    return@setOnClickListener
                }

            } else if (activity is StreamPlayerActivity) {
                if (!(activity as StreamPlayerActivity).checkPermissionForCamera()) {
                    return@setOnClickListener
                }
            }
            onRequestCohostClick()
        }
    }

    private fun updateCoinsValue() {
        if (mViewModel.finalCoinValue == -1 && mViewModel.coinValue != -1) {
            viewDataBinding.sp.text = "${mViewModel.coinValue}"
            viewDataBinding.mrp.visibility = View.GONE
            viewDataBinding.discountText.visibility = View.GONE
        } else if (mViewModel.finalCoinValue == mViewModel.coinValue) {
            viewDataBinding.mrp.visibility = View.GONE
            viewDataBinding.discountText.visibility = View.GONE
            viewDataBinding.sp.text = "${mViewModel.finalCoinValue}"
        } else {
            viewDataBinding.mrp.visibility = View.VISIBLE
            viewDataBinding.mrp.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            viewDataBinding.mrp.text = "${viewModel.coinValue}"
            viewDataBinding.discountText.visibility = View.VISIBLE
            viewDataBinding.discountText.text = "${viewModel.discountText}"
            viewDataBinding.sp.text = "${mViewModel.finalCoinValue}"
        }

    }

    fun onRequestCohostClick() {
        if (RewardManager.getInstance().totalCoin >= mViewModel.finalCoinValue) {
            progressBar = ProgressDialog.show(context, null, "Processing request. Please wait..")
            mViewModel?.manageVideoCall(null, CommonUtils.getUserID(), VideoCallAction.Request) { error, isSuccessful, channelId, token, sortedPos ->

                progressBar?.dismiss()
                if (!error.isNullOrEmpty()) {
                    context?.showToast("Error in processing request\n$error")
                    return@manageVideoCall
                }
                if (isSuccessful) {
                    if (!CommonUtils.isRequestVideoCallDNDAlertShown()) {
                        CommonUtils.setRequestVideoCallDNDAlertShown()
                        showAlertDialogForNoDNDALert()
                    } else {
                        context?.showToast("Successfully requested for becoming co-host. To receive call, please make sure your device is not in DND mode.")
                    }

                    viewDataBinding.requestButton.visibility = View.GONE
                    viewDataBinding.queuePos.visibility = View.VISIBLE
                    viewDataBinding.queuePos.text = "You are at position $sortedPos in the queue."
                    if (viewDataBinding.videoCallRequestQueue.adapter != null) {
                        usersListAdapter?.appendUserAtPosition(VideoCallUsersListObject(AppConstants.VIDEO_CALL_STATE_REQUESTED, viewModel.postId, null, UserObject(User(CommonUtils.getUserID(), CommonUtils.getUserName()), CommonUtils.getUserProfilePic())), sortedPos!!)
                    } else {
                        val userObject = VideoCallUsersListObject(AppConstants.VIDEO_CALL_STATE_REQUESTED, viewModel.postId, null, UserObject(User(CommonUtils.getUserID(), CommonUtils.getUserName()), CommonUtils.getUserProfilePic()))
                        addRVData(listOf(userObject));
                    }
                }
                /*if (isAdded)
                    dismiss()*/
            }
        } else {
//            context?.showToast("You don't have enough coin to request for a call!")
            callback?.onLessCoin(mViewModel.coinValue)
            dismiss()
        }
    }

    private fun showAlertDialogForNoDNDALert() {
        AlertDialog.Builder(context).setTitle("Successfully Requested for Co-host.")
                .setMessage("For successfully receiving a call from the streamer, please make sure that your device is not on DND mode and all notifications are enabled for Rheo").setPositiveButton("Ok") { dialogInterface, _ -> dialogInterface.dismiss() }.show()
    }

    fun adjustViews(list: List<VideoCallUsersListObject>, sortedPos: Int?, state: String?) {
        addRVData(list);

        if (viewModel?.isAuthor) {
            viewDataBinding.requestButton.visibility = View.GONE
            viewDataBinding.queuePos.visibility = View.GONE
        } else {
            if (viewModel.isFirstRequest) {
                if (sortedPos == -1) {
                    viewDataBinding.requestButton.visibility = View.VISIBLE
                    viewDataBinding.queuePos.visibility = View.GONE
                } else {

                    viewDataBinding.queuePos.visibility = View.VISIBLE
                    if (state.equals(AppConstants.VIDEO_CALL_STATE_REQUESTED, true)) {
                        viewDataBinding.requestButton.visibility = View.GONE
                        viewDataBinding.queuePos.text = "You are at position $sortedPos in the queue."
                    } else if (state.equals(AppConstants.VIDEO_CALL_STATE_INITIATED, true)) {
                        viewDataBinding.requestButton.visibility = View.GONE
                        viewDataBinding.queuePos.text = "You request to become a co-host is accepted."
                    } else if (state.equals(AppConstants.VIDEO_CALL_STATE_ENDED, true)) {
                        viewDataBinding.requestButton.visibility = View.VISIBLE
                        viewDataBinding.queuePos.text = "You co-host request is completed."
                    } else if (state.equals(AppConstants.VIDEO_CALL_STATE_DENIED, true)) {
                        viewDataBinding.requestButton.visibility = View.VISIBLE
                        viewDataBinding.queuePos.text = "You request to become a co-host is denied.\nRheo coins have been refunded back to your account."
                    } else if (state.equals(AppConstants.VIDEO_CALL_STATE_IN_PROGRESS, true)) {
                        viewDataBinding.queuePos.text = "You are currently a co-host."
                        viewDataBinding.requestButton.visibility = View.GONE
                    } else if (state.equals(AppConstants.VIDEO_CALL_STATE_REFUNDED, true)) {
                        viewDataBinding.requestButton.visibility = View.VISIBLE
                        viewDataBinding.queuePos.text = "${viewModel.authorName} could not accept your co-host request.\nRheo coins have been refunded back to your account."
                    }
                }
            }
        }
    }

    private fun addRVData(list: List<VideoCallUsersListObject>) {
        viewDataBinding.nullRequestsTv.visibility = View.GONE

        if (viewDataBinding.videoCallRequestQueue.adapter == null) {
            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            viewDataBinding.videoCallRequestQueue.layoutManager = layoutManager
            viewDataBinding.videoCallRequestQueue.adapter = usersListAdapter
            viewDataBinding.videoCallRequestQueue.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visibleItemCount: Int = layoutManager.getChildCount()
                    val totalItemCount: Int = layoutManager.getItemCount()
                    val firstVisibleItemPosition: Int = layoutManager.findFirstVisibleItemPosition()

                    // Load more if we have reach the end to the recyclerView
                    if (!viewModel.isLoadingData && viewModel.nextUrl != null && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        Log.i("Next url ", viewModel.nextUrl)
                        viewModel.isLoadingData = true
                        usersListAdapter.setShowLoading(true)
                        loadUsersList()
                    }
                }
            })
            usersListAdapter.setOnActionClickListener { position, channelId, userId, userIcon, action, userName ->
                makeManageCallRequest(position, channelId, userId, action, userIcon, userName)
            }
            usersListAdapter.setAuthor(viewModel?.isAuthor)
        }

        usersListAdapter.addUser(list)
    }

    fun makeManageCallRequest(position: Int, channelId: String?, userId: Int, action: VideoCallAction, userIcon: String?, userName: String?) {
        var requestType: String?
        var properties = hashMapOf<String, Any?>(
                "post_id" to viewModel?.postId,
                "author_name" to viewModel.authorName,
                "is_author" to viewModel.isAuthor,
                "request_coin_value" to viewModel.finalCoinValue,
                "user_name" to userName,
                "channel_id" to channelId)
        if (action.equals(VideoCallAction.Start)) {
            requestType = "accepting"
            SegmentTracker.getInstance().trackEvent(EVENT_COHOST_REQUEST_ACCEPT_CLICKED, properties)
        } else if (action.equals(VideoCallAction.Deny)) {
            SegmentTracker.getInstance().trackEvent(EVENT_COHOST_REQUEST_DENY_CLICKED, properties)
            requestType = "rejecting"
        } else {
            SegmentTracker.getInstance().trackEvent(EVENT_COHOST_REQUEST_REFUND_CLICKED, properties)
            requestType = "refunding"
        }

        progressBar = ProgressDialog.show(context, null, "Processing request. Please wait..")

        viewModel.manageVideoCall(channelId, userId, action) { error, isSuccessful, channelId, agoraAccessToken, _ ->
            progressBar?.dismiss()
            if (!error.isNullOrEmpty()) {
                Toast.makeText(context, "Error in " + requestType + " co-host request\n$error", Toast.LENGTH_SHORT).show()
                return@manageVideoCall
            }
            if (isSuccessful) {
                if (action.equals(VideoCallAction.Start)) {
                    usersListAdapter?.updateState(position, AppConstants.VIDEO_CALL_STATE_INITIATED);
                    showCallingActivity(channelId, agoraAccessToken, userIcon, userName, userId)
                } else if (action.equals(VideoCallAction.Deny)) {
                    usersListAdapter?.updateState(position, AppConstants.VIDEO_CALL_STATE_DENIED)
                } else if (action.equals(VideoCallAction.Refund)) {
                    usersListAdapter?.updateState(position, AppConstants.VIDEO_CALL_STATE_REFUNDED)
                }
            }
        }
    }

    private fun showCallingActivity(channelId: String?, agoraAccessToken: String?, userIcon: String?, userName: String?, userId: Int?) {
        context?.let { FullScreenVideoActivity.startMe(it, bundleOf(ARG_KEY_POST_ID to viewModel.postId, ARG_KEY_AUTHOR_NAME to viewModel.authorName, FullScreenVideoActivity.ARG_KEY_CHANNEL_ID to channelId, FullScreenVideoActivity.ARG_KEY_AGORA_ACCESS_TOKEN to agoraAccessToken, ARG_KEY_USER_ICON to userIcon, ARG_KEY_USER_NAME to userName, "user_id" to userId)) }
    }

    companion object {
        const val TAG = "RequestToVideoCall"
        private const val ARG_KEY_POST_ID = "post_id"
        private const val ARG_KEY_AUTHOR_NAME = "author_name"
        private const val ARG_KEY_USER_ICON = "user_icon"
        private const val ARG_KEY_USER_NAME = "user_name"

        fun newInstance(postId: String, username: String, callback: OnRequestVideoCallListener): RequestToVideoCallBottomSheet = RequestToVideoCallBottomSheet().apply {
            arguments = bundleOf(ARG_KEY_POST_ID to postId, ARG_KEY_AUTHOR_NAME to username)
            this.callback = callback
        }
    }

}

interface OnRequestVideoCallListener {
    fun onLessCoin(requiredCoins : Int)
}