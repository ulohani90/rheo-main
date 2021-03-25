package com.rheotv.android.ui.activities.customroom.view

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentCustomRoomBinding
import com.rheotv.android.ui.activities.customroom.adapter.CustomRoomRecyclerAdapter
import com.rheotv.android.ui.activities.customroom.model.CustomRoomDetail
import com.rheotv.android.ui.activities.customroom.model.CustomRoomUserAction
import com.rheotv.android.ui.activities.customroom.model.CustomRoomViewType
import com.rheotv.android.ui.activities.customroom.viewmodel.CustomRoomViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.RewardManager
import com.rheotv.android.utils.ViewUtils
import com.rheotv.android.utils.showToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class CustomRoomFragment : BaseFragment<FragmentCustomRoomBinding, CustomRoomViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mAdapter: CustomRoomRecyclerAdapter

    private lateinit var mSource: String

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int =
            R.layout.fragment_custom_room

    override fun getViewModel(): CustomRoomViewModel =
            ViewModelProvider(parentFragment ?: this,
                    mViewModelFactory)[CustomRoomViewModel::class.java]

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.post?.isLive != true && viewModel.post?.isStreamer == false) {
            viewModel.setLoading(false)
            viewDataBinding.refreshButton.visibility = View.GONE
            viewDataBinding.errorText = getString(R.string.custom_room_non_live_message)
            viewDataBinding.showErrorText = true
            viewDataBinding.addCustomRoomButton.visibility = View.GONE
            return
        }
        viewDataBinding.refreshButton.setOnClickListener {
            viewModel.roomPageRefresh = true
            viewModel.fetchCustomRoom()
        }
        viewDataBinding.addCustomRoomButton
        viewDataBinding.recyclerView.addItemDecoration(
                object : RecyclerView.ItemDecoration() {
                    var space: Int = 0
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        super.getItemOffsets(outRect, view, parent, state)
                        val itemCount = state.itemCount
                        val itemPosition = parent.getChildAdapterPosition(view)

                        // no position, leave it alone
                        if (itemPosition == RecyclerView.NO_POSITION) {
                            return
                        }

                        // first item
                        if (itemPosition == 0) {
                            outRect.set(0, 0, 0, if (itemCount == 1) space else space / 2)
                        }
                        //last item
                        else if (itemCount > 0 && itemPosition == itemCount - 1) {
                            outRect[0, space / 2, 0] = space
                        }
                        //every other item
                        else {
                            outRect.set(0, space / 2, 0, space / 2)
                        }
                    }
                }.also { it.space = ViewUtils.dpToPx(12) })
        viewDataBinding.recyclerView.adapter = mAdapter.also {
            it.isStreamer = viewModel.post?.isStreamer ?: false
            it.setItemClickListener { customRoomDetails, _ ->
                if (viewModel.post?.isStreamer == false) {

                    if (customRoomDetails.dataViewType == CustomRoomViewType.CustomRoomEnded || customRoomDetails.dataViewType == CustomRoomViewType.CustomRoomRefunded) {
                        context?.showToast("Room has already ended!")
                        return@setItemClickListener
                    }
                    if (customRoomDetails.dataViewType == CustomRoomViewType.CustomRoomStarted) {
                        context?.showToast("Room has started!")
                        return@setItemClickListener
                    }
                    if (customRoomDetails.dataViewType == CustomRoomViewType.CustomRoomFilled) {
                        context?.showToast("Room is already full!")
                        return@setItemClickListener
                    }
                    if (viewModel.requestedCustomRooms.contains(customRoomDetails.id)) {
                        context?.showToast("You have already requested for this Custom Room!")
                        return@setItemClickListener
                    }
                    if (!viewModel.canRequest) {
                        context?.showToast("You have already requested for a Custom Room. Wait until the Custom Room is finished!")
                        return@setItemClickListener
                    }
                    if (customRoomDetails.isFull) {
                        context?.showToast("This room is full!")
                        return@setItemClickListener
                    }
                    if (RewardManager.getInstance().totalCoin < customRoomDetails.entryCoins) {
                        context?.showToast("You don't have enough coin to request!")
                        return@setItemClickListener
                    }
                    val currentIndex = it.getItemPosition(customRoomDetails)
                    if (currentIndex > 0) {
                        it.getItem(currentIndex - 1)?.apply {
                            if ((dataViewType == CustomRoomViewType.CustomRoomCreated) && !isFull && RewardManager.getInstance().totalCoin > entryCoins) {
                                context?.showToast("You cannot request until the previous room is full!")
                                return@setItemClickListener
                            }
                        }
                    }
                }
                viewModel.openCustomRoom(customRoomDetails, mAdapter.getItemPosition(customRoomDetails) + 1)
            }
        }
        viewDataBinding.refreshButton.visibility = View.GONE
        viewDataBinding.addCustomRoomButton.visibility =
                if (viewModel.post?.isStreamer == true && viewModel.post?.isLive == true) View.VISIBLE else View.GONE
        viewDataBinding.addCustomRoomButton.setOnClickListener {
            viewModel.onAddCustomRoomClick(mAdapter.itemCount)
        }
        viewModel.customRoomUserAction.observe(viewLifecycleOwner, Observer {
            when (it) {
                CustomRoomUserAction.RefreshCustomRoom -> mAdapter.notifyDataSetChanged()
            }
        })
        viewModel.customRoomLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                viewDataBinding.errorText = if (viewModel.post?.isStreamer != true)
                    getString(R.string.custom_room_request_empty_message)
                else
                    "No Custom Room available!"
                viewDataBinding.showErrorText = true
                viewDataBinding.refreshButton.visibility = View.GONE
                return@Observer
            }
            viewDataBinding.errorText = ""
            viewDataBinding.refreshButton.visibility = View.VISIBLE
            viewDataBinding.showErrorText = false
            mAdapter.requestedCustomRooms = viewModel.requestedCustomRooms
            mAdapter.submitList(it, viewModel.roomPageRefresh)
            if (viewModel.roomPageRefresh) it.find { room -> viewModel.selectedRoomId == room.id }
                    ?.also { room -> viewModel.customRoomDetailLiveData.value = listOf(room) }
            viewModel.roomPageRefresh = false
        })
        viewModel.fetchCustomRoom()
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
        if (mAdapter.itemCount > 0)
            hideErrorView()
    }

    override fun onResume() {
        super.onResume()
        if (isAdded) {
            mAdapter.requestedCustomRooms = viewModel.requestedCustomRooms
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun onStop() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        super.onStop()
    }

    private fun hideErrorView() {
        viewDataBinding.errorText = ""
        viewDataBinding.showErrorText = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageReceived(customRoomDetail: CustomRoomDetail?) {
        if (!isAdded || isStateSaved || isDetached || isRemoving) return
        customRoomDetail?.let {
            it.setupViewType(viewModel.post?.isStreamer == true)
            mAdapter.updateItem(it)?.apply {
                viewDataBinding.showErrorText = false
                viewDataBinding.refreshButton.visibility = View.VISIBLE
                viewModel.customRoomDetailLiveData.value = listOf(this)
            }
        }
    }

    companion object {
        fun newInstance(source: String) = CustomRoomFragment().also {
            it.mSource = source
        }
    }
}