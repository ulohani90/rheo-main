package com.rheotv.android.ui.activities.customroom.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentCustomRoomPlayerBinding
import com.rheotv.android.databinding.ListItemPlayerBinding
import com.rheotv.android.ui.activities.customroom.adapter.CustomRoomDetailHeaderRecyclerAdapter
import com.rheotv.android.ui.activities.customroom.adapter.CustomRoomPlayerRecyclerAdapter
import com.rheotv.android.ui.activities.customroom.model.CustomRoomDetail
import com.rheotv.android.ui.activities.customroom.model.CustomRoomDetailViewType
import com.rheotv.android.ui.activities.customroom.model.CustomRoomUserAction
import com.rheotv.android.ui.activities.customroom.model.CustomRoomViewType
import com.rheotv.android.ui.activities.customroom.viewmodel.CustomRoomViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.ViewUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CustomRoomDetailFragment : BaseFragment<FragmentCustomRoomPlayerBinding, CustomRoomViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mPlayerAdapter: CustomRoomPlayerRecyclerAdapter

    @Inject
    lateinit var mHeaderAdapter: CustomRoomDetailHeaderRecyclerAdapter

    private lateinit var mSource: String

    override fun getBindingVariable(): Int = BR.viewModel
    private var isPlayerApiCalled = false
    private var mSearchPublishSubject: PublishSubject<String>? = PublishSubject.create()

    override fun getLayoutId(): Int =
            R.layout.fragment_custom_room_player

    override fun getViewModel(): CustomRoomViewModel =
            ViewModelProvider(parentFragment ?: this,
                    mViewModelFactory)[CustomRoomViewModel::class.java]

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val screenHeight = ViewUtils.getScreenHeightInPx(context)
        with(viewDataBinding) {
            headerRecyclerView.adapter = mHeaderAdapter.also {
                it.setItemClickListener { customRoomDetails, customRoomUserAction ->
                    when (customRoomUserAction) {
                        CustomRoomUserAction.CreateCustomRoomClick -> {
                            HashMap<String, Any?>().apply {
                                put("post_id", viewModel.post?.id)
                                put("author_id", viewModel.post?.author?.user?.username)
                                put("entry_coin", customRoomDetails.entryCoins)
                                put("start_time", customRoomDetails.startTime)
                                put("max_player_count", customRoomDetails.maxPlayerCount)
                                SegmentTracker.getInstance(headerRecyclerView.context).trackEvent(SegmentConstants.EVENT_CUSTOM_ROOM_NEW_CREATE_ROOM, this)
                            }

                            viewModel.createCustomRoom(customRoomDetails)

                        }
                        CustomRoomUserAction.SubmitRoomIdPasswordClick -> {
                            HashMap<String, Any?>().apply {
                                put("post_id", viewModel.post?.id)
                                put("author_id", viewModel.post?.author?.user?.username)
                                put("room_id", customRoomDetails.customRoomId);
                                SegmentTracker.getInstance(headerRecyclerView.context).trackEvent(SegmentConstants.EVENT_CUSTOM_ROOM_NEW_SUBMIT_DETAILS, this)
                            }
                            viewModel.submitRoomIdAndPassword(customRoomDetails);
                        }
                        CustomRoomUserAction.SubmitGameUserName -> {
                            HashMap<String, Any?>().apply {
                                put("post_id", viewModel.post?.id)
                                put("author_id", viewModel.post?.author?.user?.username)
                                put("custom_room_id", customRoomDetails.id);
                                put("game_username", customRoomDetails.gameUserName)
                                SegmentTracker.getInstance(headerRecyclerView.context).trackEvent(SegmentConstants.EVENT_CUSTOM_ROOM_NEW_REQUEST_ACCESS, this)
                            }
                            viewModel.requestToCustomRoom(customRoomDetails)

                        }
                        CustomRoomUserAction.SubmitUpdatedStartTime -> {
                            viewModel.updateStartTime(customRoomDetails)
                        }
                    }
                }
            }
            refundButton.setOnClickListener { v ->
                AlertDialog.Builder(v.context)
                        .setTitle("Refund Custom Room")
                        .setMessage("Do you want to refund the custom room?")
                        .setPositiveButton("Refund") { dialog, _ ->
                            mHeaderAdapter.getItem(0)?.also {
                                viewModel.refundCustomRoom(it)
                                HashMap<String, Any?>().apply {
                                    put("post_id", viewModel.post?.id)
                                    put("author_id", viewModel.post?.author?.user?.username)
                                    put("custom_room_id", it.id);
                                    SegmentTracker.getInstance(headerRecyclerView.context).trackEvent(SegmentConstants.EVENT_CUSTOM_ROOM_NEW_REFUND_ROOM, this)
                                }
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
            }
            searchInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = Unit

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    mPlayerAdapter.apply {
                        mSearchPublishSubject?.onNext(s?.toString() ?: "")
                    }
                }
            })
            refreshButton.setOnClickListener { _ ->
                viewModel.roomPageRefresh = true
                viewModel.fetchCustomRoom()
                viewModel.setLoading(true)
                mHeaderAdapter.getItem(0)?.let { viewModel.fetchCustomRoomPlayers(it) }
            }
            if (mSearchPublishSubject == null)
                mSearchPublishSubject = PublishSubject.create()
            mSearchPublishSubject
                    ?.debounce(500, TimeUnit.MILLISECONDS)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe(object : DisposableObserver<String>() {
                        override fun onComplete() {

                        }

                        override fun onNext(query: String) {
                            mPlayerAdapter.setLoading(true)
                            viewModel.playerNextUrl = null
                            mPlayerAdapter.submitList(listOf(), true)
                            viewModel.searchPlayer(mHeaderAdapter.getItem(0)
                                    ?: return, query)
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }
                    })
            playerRecyclerView.adapter = mPlayerAdapter.also {
                it.setWinnerListener { player ->
                    if (!(mHeaderAdapter.getItem(0)?.dataViewType == CustomRoomViewType.CustomRoomStarted ||
                                    (mHeaderAdapter.getItem(0)?.dataViewType == CustomRoomViewType.CustomRoomEnded
                                            && mHeaderAdapter.getItem(0)?.winner == null)))
                        return@setWinnerListener
                    val builder = AlertDialog.Builder(root.context)
                            .setTitle(R.string.custom_room_user_info)
                    with(DataBindingUtil.inflate(LayoutInflater.from(builder.context),
                            R.layout.list_item_player,
                            null,
                            false) as ListItemPlayerBinding) {
                        username = player?.username
                        if (!player?.username.isNullOrBlank())
                            gameUserName = "${player?.username} (${player?.username})"
                        profilePicUrl = player?.profilePicUrl
                        winner = false
                        winnerButton.visibility = View.VISIBLE
                        cancelButton.visibility = View.VISIBLE
                        root.setPadding(ViewUtils.dpToPx(16), ViewUtils.dpToPx(12), ViewUtils.dpToPx(16), ViewUtils.dpToPx(8))
                        val dialog = builder.setView(root).create()
                        winnerButton.setOnClickListener {
                            player?.isWinner = true

                            viewModel.markWinner(mHeaderAdapter.getItem(0)?.apply {
                                winner = player
                                HashMap<String, Any?>().apply {
                                    put("post_id", viewModel.post?.id)
                                    put("author_id", viewModel.post?.author?.user?.username)
                                    put("customroom_id", id)
                                    put("winner_id", winner?.id)
                                    SegmentTracker.getInstance(headerRecyclerView.context)
                                            .trackEvent(SegmentConstants.EVENT_CUSTOM_ROOM_NEW_WINNER_SELECTED, this)
                                }
                            })
                            dialog.dismiss()
                        }
                        cancelButton.setOnClickListener { dialog.dismiss() }
                        dialog.show()
                    }

                }
            }
            backButton.setOnClickListener {
                viewModel.customRoomUserAction.value = CustomRoomUserAction.DetailPageBackClick
            }
            with(nestedScrollView) {
                viewTreeObserver.addOnScrollChangedListener {
                    val childView = getChildAt(childCount - 1) ?: return@addOnScrollChangedListener
                    val diff = childView.bottom - height - scrollY
                    if (diff <= screenHeight * 0.1 && !mPlayerAdapter.isPaginating &&
                            viewModel.progressLiveData.value == false &&
                            viewModel.playerNextUrl != null) {
                        mPlayerAdapter.setPaginating(true)
                        viewModel.fetchCustomRoomPlayers(mHeaderAdapter.getItem(0)
                                ?: return@addOnScrollChangedListener)
                    }
                }
            }
        }
        viewModel.customRoomUserAction.observe(viewLifecycleOwner, Observer {
            if (!it.headerText.isNullOrBlank())
                viewDataBinding.headerTextView.text = it?.headerText ?: "Custom Room"

            when (it) {
                CustomRoomUserAction.RemoveWinner -> mPlayerAdapter.addWinner(null)
                CustomRoomUserAction.RefreshPlayerList -> with(mPlayerAdapter) {
                    addWinner(mHeaderAdapter.getItem(0)?.winner)
                    mHeaderAdapter.getItem(0)?.let { customRoom -> updateCustomRoomView(customRoom) }
                    updateWinner()
                }
                CustomRoomUserAction.RefreshCustomRoom -> with(mHeaderAdapter) {
                    notifyDataSetChanged()
                    getItem(0)?.let { customRoom -> updateCustomRoomView(customRoom) }
                }
            }
        })

        viewModel.customRoomPlayerLiveData.observe(viewLifecycleOwner, Observer {
            with(mPlayerAdapter) {
                setLoading(false)
                val shouldClear = !isPaginating
                setPaginating(false)
                submitList(it ?: return@Observer, shouldClear)
                if (viewDataBinding.searchInput.text.isNullOrBlank()) {
                    isPlayerApiCalled = true
                    viewDataBinding.showSearch = itemCount != 0
                }
            }
        })
        viewModel.customRoomDetailLiveData.observe(viewLifecycleOwner, Observer {
            val list = it?.toMutableList() ?: return@Observer
            if (list.isNotEmpty()) {
                updateCustomRoomView(list[0])
            }
            mHeaderAdapter.submitList(list, true)
        })
    }

    override fun onDestroyView() {
        mSearchPublishSubject?.onComplete()
        super.onDestroyView()
    }

    private fun updateCustomRoomView(item: CustomRoomDetail) {
        if (viewModel.post?.isStreamer == true) {
            mPlayerAdapter.addWinner(item.winner)
            viewModel.playerNextUrl = null
            mPlayerAdapter.submitList(listOf(), true)
            viewDataBinding.showSearch = false
            isPlayerApiCalled = false
            if (!viewModel.roomPageRefresh) {
                viewModel.setLoading(true)
                viewModel.fetchCustomRoomPlayers(item)
            }
        }
        with(viewDataBinding) {
            refundedStateLabel.visibility =
                    if (item.dataViewType == CustomRoomViewType.CustomRoomRefunded) View.VISIBLE else View.GONE
            refundButton.visibility =
                    if (viewModel.post?.isStreamer == true && item.dataViewType != CustomRoomViewType.CustomRoomRefunded
                            && (item.canRefund && item.winner == null) && item.viewType != CustomRoomDetailViewType.CreateCustomRoom)
                        View.VISIBLE
                    else
                        View.GONE
            refreshButton.visibility =
                    if (item.viewType == CustomRoomDetailViewType.CreateCustomRoom) View.GONE else View.VISIBLE
        }
    }

    companion object {
        fun newInstance(source: String) = CustomRoomDetailFragment().also {
            it.mSource = source
        }
    }
}