package com.rheotv.android.ui.activities.customroom.adapter

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.R
import com.rheotv.android.databinding.ListItemCustomRoomBinding
import com.rheotv.android.ui.activities.customroom.model.CustomRoomDetail
import com.rheotv.android.ui.activities.customroom.model.CustomRoomUserAction
import com.rheotv.android.ui.activities.customroom.model.CustomRoomViewType
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.utils.TimeUtils
import com.rheotv.android.utils.ViewAnimationUtils

class CustomRoomRecyclerAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    private val mList: MutableList<CustomRoomDetail> = mutableListOf()
    private var mItemClickListener: ((customRoomDetail: CustomRoomDetail, customRoomUserAction: CustomRoomUserAction?) -> Unit)? = null
    var isStreamer = false
    var requestedCustomRooms: HashSet<String>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CustomRoomViewHolder(DataBindingUtil.inflate(inflater, R.layout.list_item_custom_room, parent, false))
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun getItemViewType(position: Int): Int {
        return mList[position].dataViewType?.value ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    fun submitList(list: List<CustomRoomDetail>, clear: Boolean = false) {
        if (clear) mList.clear()
        list.forEach { internalUpdateItem(it) }
        notifyDataSetChanged()
    }

    fun setItemClickListener(itemClickListener: ((customRoomDetail: CustomRoomDetail, customRoomUserAction: CustomRoomUserAction?) -> Unit)? = null) {
        mItemClickListener = itemClickListener
    }

    fun getItemPosition(customRoomDetail: CustomRoomDetail): Int = mList.indexOf(customRoomDetail)

    fun getItem(position: Int): CustomRoomDetail? = if (position < mList.size) mList[position] else null

    fun updateItem(customRoomDetail: CustomRoomDetail): CustomRoomDetail? {
        val item = internalUpdateItem(customRoomDetail)
        notifyDataSetChanged()
        return item
    }

    private fun internalUpdateItem(customRoomDetail: CustomRoomDetail): CustomRoomDetail? {
        for (index in 0 until mList.size) {
            val item = mList[index]
            if (item.id == customRoomDetail.id) {
                if (customRoomDetail.currentPlayerCount > item.currentPlayerCount)
                    mList[index].currentPlayerCount = customRoomDetail.currentPlayerCount
                if (!item.isFull && customRoomDetail.isFull)
                    mList[index].isFull = customRoomDetail.isFull
                if (customRoomDetail.dataViewType?.value?.compareTo(item.dataViewType?.value
                                ?: 0) == 1)
                    mList[index].dataViewType = customRoomDetail.dataViewType
                if ((item.customRoomId.isNullOrBlank() && !customRoomDetail.customRoomId.isNullOrBlank()) ||
                        item.customRoomId?.equals(customRoomDetail.customRoomId) == false ||
                        item.customRoomPassword?.equals(customRoomDetail.customRoomPassword) == false) {
                    mList[index].customRoomId = customRoomDetail.customRoomId
                    mList[index].customRoomPassword = customRoomDetail.customRoomPassword
                }
                if (item.winner == null && customRoomDetail.winner != null)
                    mList[index].winner = customRoomDetail.winner
                return mList[index]
            }
        }
        mList.add(customRoomDetail)
        return customRoomDetail
    }

    inner class CustomRoomViewHolder(private val mBinding: ListItemCustomRoomBinding) : BaseViewHolder(mBinding.root) {

        private fun hideMoreButton() {
            mBinding.bottomArrow.visibility = View.GONE
            mBinding.bottomArrow.setOnClickListener(null)
        }

        private var mViewHeight = 0
        override fun onBind(position: Int) {
            mBinding.customRoomName = "Custom Room ${position + 1}"
            mBinding.playersCount = "${mList[position].currentPlayerCount}/${mList[position].maxPlayerCount}"
            val timeText = when {
                mList[position].dataViewType == CustomRoomViewType.CustomRoomStarted -> "Room started at "
                mList[position].dataViewType == CustomRoomViewType.CustomRoomRefunded -> "Room has been refunded!"
                !mList[position].endTime.isNullOrBlank() -> "Room ended at "
                else -> "Room starts at "
            }
            mBinding.startTimeTextView.text = if (mList[position].dataViewType == CustomRoomViewType.CustomRoomRefunded) {
                SpannableString(timeText)
            } else {
                val spannableString = SpannableString(timeText + TimeUtils.getFormattedDate(TimeUtils.HH_MM_AA,
                        TimeUtils.getDateFromString(if (mList[position].endTime.isNullOrBlank()) mList[position].startTime else mList[position].endTime,
                                TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX)))
                spannableString.setSpan(StyleSpan(Typeface.BOLD), timeText.length, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableString
            }
            mBinding.root.setOnClickListener {
                mItemClickListener?.invoke(mList[position], null)
            }
            if (isStreamer) {
                hideMoreButton()
            } else {
                mBinding.bottomArrow.visibility = View.VISIBLE
                mBinding.bottomArrow.setOnClickListener {
                    if (mBinding.hidedView.visibility == View.VISIBLE) {
                        it.animate().rotationBy(-180f).setDuration(((100 / mBinding.hidedView.context.resources.displayMetrics.density).toInt() + 200).toLong()).setInterpolator(LinearInterpolator()).start()
                        ViewAnimationUtils.collapse(mBinding.hidedView)
                    } else {
                        it.animate().rotationBy(180f).setDuration(((100 / mBinding.hidedView.context.resources.displayMetrics.density).toInt() + 200).toLong()).setInterpolator(LinearInterpolator()).start()
                        ViewAnimationUtils.expand(mBinding.hidedView, mViewHeight)
                    }
                }
                mBinding.hidedView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (mViewHeight < mBinding.hidedView.measuredHeight) {
                            mViewHeight = mBinding.hidedView.measuredHeight
                        } else if (mViewHeight > 1 && mViewHeight == mBinding.hidedView.measuredHeight) {
                            mBinding.hidedView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                    }
                })
                if (mList[position].winner == null) {
                    mBinding.winnerLayout.root.visibility = View.GONE
                    if (mList[position].customRoomId.isNullOrBlank()) {
                        mBinding.roomDetailGroup.visibility = View.GONE
                        mBinding.messageTextView.visibility = View.VISIBLE
                    } else {
                        mBinding.roomDetailGroup.visibility = View.VISIBLE
                        mBinding.messageTextView.visibility = View.GONE
                    }
                    if (requestedCustomRooms?.contains(mList[position].id) == true) {
                        mBinding.roomIdTextView.text = mList[position].customRoomId
                        mBinding.roomPasswordTextView.text = mList[position].customRoomPassword
                    } else {
                        mBinding.roomIdTextView.text = "******"
                        mBinding.roomPasswordTextView.text = "******"
                    }
                    mBinding.winnerLayout.root.setOnClickListener(null)
                } else {
                    mBinding.roomDetailGroup.visibility = View.GONE
                    mBinding.messageTextView.visibility = View.GONE
                    mBinding.winnerLayout.root.visibility = View.VISIBLE
                    mBinding.winnerLayout.winner = true
                    mBinding.winnerLayout.profilePicUrl = mList[position].winner?.profilePicUrl
                    mBinding.winnerLayout.username = mList[position].winner?.username
                    mBinding.winnerLayout.root.setOnClickListener { }
                }
            }
        }
    }
}