package com.rheotv.android.ui.activities.customroom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.R
import com.rheotv.android.databinding.ListItemCreateCustomRoomBinding
import com.rheotv.android.databinding.ListItemGameUsernameBinding
import com.rheotv.android.databinding.ListItemRequestRoomIdPasswordBinding
import com.rheotv.android.databinding.ListItemShowRoomIdPasswordBinding
import com.rheotv.android.ui.activities.customroom.model.CustomRoomDetail
import com.rheotv.android.ui.activities.customroom.model.CustomRoomDetailViewType
import com.rheotv.android.ui.activities.customroom.model.CustomRoomUserAction
import com.rheotv.android.ui.activities.customroom.model.CustomRoomViewType
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.utils.*


class CustomRoomDetailHeaderRecyclerAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    private val mList: MutableList<CustomRoomDetail> = mutableListOf()
    private var mItemClickListener: ((customRoomDetail: CustomRoomDetail, customRoomUserAction: CustomRoomUserAction?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CustomRoomDetailViewType.CreateCustomRoom.value -> CreateCustomRoomViewHolder(DataBindingUtil.inflate(inflater, R.layout.list_item_create_custom_room, parent, false))
            CustomRoomDetailViewType.RequestRoomIdAndPassword.value -> RequestRoomIdPasswordViewHolder(DataBindingUtil.inflate(inflater, R.layout.list_item_request_room_id_password, parent, false))
            CustomRoomDetailViewType.ShowRoomIdAndPassword.value -> ShowRoomIdPasswordViewHolder(DataBindingUtil.inflate(inflater, R.layout.list_item_show_room_id_password, parent, false))
            CustomRoomDetailViewType.GameUserInput.value -> GameUserInputViewHolder(DataBindingUtil.inflate(inflater, R.layout.list_item_game_username, parent, false))
            else -> CustomRoomViewHolder(DataBindingUtil.inflate(inflater, R.layout.list_item_request_play, parent, false))
        }
    }

    override fun getItemCount(): Int = mList.size

    override fun getItemViewType(position: Int): Int {
        return mList[position].viewType?.value ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    fun getItem(position: Int) = if (position < mList.size) mList[position] else null

    fun setItemClickListener(itemClickListener: ((customRoomDetail: CustomRoomDetail, customRoomUserAction: CustomRoomUserAction?) -> Unit)? = null) {
        mItemClickListener = itemClickListener
    }

    fun submitList(list: List<CustomRoomDetail>, clear: Boolean = false) {
        if (clear) mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    fun updateItem(customRoomDetail: CustomRoomDetail) {
        for (index in 0 until mList.size) {
            if (mList[index].id == customRoomDetail.id) {
                mList[index] = customRoomDetail
                notifyItemChanged(index)
                return
            }
        }
    }

    inner class CustomRoomViewHolder(binding: ViewDataBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {

        }
    }

    inner class CreateCustomRoomViewHolder(private val mBinding: ListItemCreateCustomRoomBinding) : BaseViewHolder(mBinding.root) {

        private var time: String? = null

        override fun onBind(position: Int) {
            with(mBinding.root.context) {
                mBinding.playerNumberInput.setText("")
                mBinding.entryValueInput.setText("")
                mBinding.startTimeInput.setText("")
                time = null
                mBinding.timePicker.setOnClickListener {
                    showTimePicker { displayTime, actualTime ->
                        mBinding.startTimeInput.setText(displayTime.format(TimeUtils.HH_MM_AA))
                        time = actualTime.format(TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX)
                    }
                }
                mBinding.submitButton.setOnClickListener {
                    CommonUtils.hideKeyboardFrom(it.context, mBinding.playerNumberInput)
                    CommonUtils.hideKeyboardFrom(it.context, mBinding.entryValueInput)
                    CommonUtils.hideKeyboardFrom(it.context, mBinding.startTimeInput)
                    if (mBinding.playerNumberInput.text.isNullOrBlank()) {
                        showToast("Player count cannot be empty!")
                        return@setOnClickListener
                    }
                    if (mBinding.entryValueInput.text.isNullOrBlank()) {
                        showToast("Entry coin cannot be empty!")
                        return@setOnClickListener
                    }
                    if (time.isNullOrBlank()) {
                        showToast("Start time cannot be empty!")
                        return@setOnClickListener
                    }
                    mList[position].maxPlayerCount =
                            mBinding.playerNumberInput.text?.toString()?.toInt() ?: 0
                    mList[position].entryCoins =
                            mBinding.entryValueInput.text?.toString()?.toInt() ?: 0
                    mList[position].startTime = time
                    mItemClickListener?.invoke(mList[position], CustomRoomUserAction.CreateCustomRoomClick)
                }
            }
        }
    }

    inner class RequestRoomIdPasswordViewHolder(private val mBinding: ListItemRequestRoomIdPasswordBinding) : BaseViewHolder(mBinding.root) {

        override fun onBind(position: Int) {
            val item = mList[position]
            mBinding.playerCount = "${item.currentPlayerCount}"
            mBinding.coinValue = "${item.entryCoins}"
            mBinding.startTime = TimeUtils.getFormattedDate(TimeUtils.HH_MM_AA, TimeUtils.getDateFromString(
                    if (mList[position].endTime.isNullOrBlank()) mList[position].startTime else mList[position].endTime,
                    TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX))
            mBinding.roomIdInput.setText(item.customRoomId ?: "")
            mBinding.roomPasswordInput.setText(item.customRoomPassword ?: "")
            mBinding.startTimeLabel.text = when {
                mList[position].dataViewType == CustomRoomViewType.CustomRoomStarted -> "Started At "
                !mList[position].endTime.isNullOrBlank() -> "Ended At "
                else -> "Starts At "
            }
            mBinding.submitButton.visibility =
                    if (item.dataViewType == CustomRoomViewType.CustomRoomRefunded || item.dataViewType == CustomRoomViewType.CustomRoomEnded) View.GONE else View.VISIBLE
            mBinding.submitButton.setOnClickListener {
                CommonUtils.hideKeyboardFrom(it.context, mBinding.roomIdInput)
                CommonUtils.hideKeyboardFrom(it.context, mBinding.roomPasswordInput)
                if (mBinding.roomIdInput.text.isNullOrBlank()) {
                    Toast.makeText(mBinding.root.context, "Room ID cannot be empty!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (mBinding.roomPasswordInput.text.isNullOrBlank()) {
                    Toast.makeText(mBinding.root.context, "Room password cannot be empty!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                mList[position].customRoomId = mBinding.roomIdInput.text?.toString()
                mList[position].customRoomPassword = mBinding.roomPasswordInput.text?.toString()
                mItemClickListener?.invoke(mList[position], CustomRoomUserAction.SubmitRoomIdPasswordClick)
            }
            with(mBinding.timePicker) {
                visibility = if (item.dataViewType == CustomRoomViewType.CustomRoomRefunded || item.dataViewType == CustomRoomViewType.CustomRoomEnded)
                    View.GONE else View.VISIBLE
                setOnClickListener {
                    it.context.showTimePicker { _, actualTime ->
                        item.startTime = actualTime.format(TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX)
                        mItemClickListener?.invoke(item, CustomRoomUserAction.SubmitUpdatedStartTime)
                    }
                }
            }
        }
    }

    inner class ShowRoomIdPasswordViewHolder(private val mBinding: ListItemShowRoomIdPasswordBinding) : BaseViewHolder(mBinding.root) {

        override fun onBind(position: Int) {
            val item = mList[position]
            mBinding.playerCount = "${item.currentPlayerCount}"
            mBinding.coinValue = "${item.entryCoins}"
            mBinding.startTime = TimeUtils.getFormattedDate(TimeUtils.HH_MM_AA,
                    TimeUtils.getDateFromString(if (mList[position].endTime.isNullOrBlank()) mList[position].startTime else mList[position].endTime,
                            TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX))
            mBinding.roomId = item.customRoomId ?: ""
            mBinding.roomPassword = item.customRoomPassword ?: ""
            mBinding.editCustomRoomDetails.setOnClickListener {
                item.viewType = CustomRoomDetailViewType.RequestRoomIdAndPassword
                notifyItemChanged(position)
            }
            mBinding.startTimeLabel.text = when {
                mList[position].dataViewType == CustomRoomViewType.CustomRoomStarted -> "Started At "
                !mList[position].endTime.isNullOrBlank() -> "Ended At "
                else -> "Starts At "
            }
            with(mBinding.timePicker) {
                visibility = if (item.dataViewType == CustomRoomViewType.CustomRoomRefunded || item.dataViewType == CustomRoomViewType.CustomRoomEnded)
                    View.GONE else View.VISIBLE
                setOnClickListener {
                    it.context.showTimePicker { _, actualTime ->
                        item.startTime = actualTime.format(TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX)
                        mItemClickListener?.invoke(item, CustomRoomUserAction.SubmitUpdatedStartTime)
                    }
                }
            }
            mBinding.editCustomRoomDetails.visibility =
                    if (item.dataViewType == CustomRoomViewType.CustomRoomRefunded || item.dataViewType == CustomRoomViewType.CustomRoomEnded) View.GONE else View.VISIBLE
        }
    }

    inner class GameUserInputViewHolder(private val mBinding: ListItemGameUsernameBinding) : BaseViewHolder(mBinding.root) {

        override fun onBind(position: Int) {
            mBinding.entryCoin = "${mList[position].entryCoins}"
            mBinding.requestToPlayButton.setOnClickListener {
                CommonUtils.hideKeyboardFrom(it.context, mBinding.gameUserNameInput)
                if (mBinding.gameUserNameInput.text.isNullOrBlank()) {
                    mBinding.root.context.showToast("Please enter game user name!")
                    return@setOnClickListener
                }
                if (RewardManager.getInstance().totalCoin >= mList[position].entryCoins) {
                    mList[position].gameUserName = mBinding.gameUserNameInput.text?.toString()
                    mItemClickListener?.invoke(mList[position], CustomRoomUserAction.SubmitGameUserName)
                } else
                    mBinding.root.context.showToast("You don't have enough coins to request!")
            }
        }
    }
}

