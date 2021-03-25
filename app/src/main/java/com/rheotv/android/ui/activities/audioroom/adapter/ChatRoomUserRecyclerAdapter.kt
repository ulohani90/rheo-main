package com.rheotv.android.ui.activities.audioroom.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.databinding.ListItemMentionUserBinding
import com.rheotv.android.ui.activities.audioroom.model.OwnerDetail
import com.rheotv.android.ui.activities.audioroom.model.SocialGame
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.ui.customViews.Tooltip.SimpleTooltip
import com.rheotv.android.utils.BindingUtils
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.ViewUtils
import io.agora.rtc.IRtcEngineEventHandler

class ChatRoomUserRecyclerAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var socialGame: SocialGame? = null
    val mList: MutableList<OwnerDetail> = mutableListOf()
    var onItemClick: ((OwnerDetail?, Boolean) -> Unit)? = null
    val dataSet: MutableSet<Int> = hashSetOf()
    var ownerDetail: OwnerDetail? = null
    private var mPaginating = false
    private var mScreenWidth = ViewUtils.getScreenWidthInPx(RheoTvApp.getNonUiContext())
    var canUpdateUser = false
    var canUnMuteSelf = true
    var muteMessage: String? = null
    var highlightedUser: OwnerDetail? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = ListItemMentionUserBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        view.root.layoutParams = ViewGroup.LayoutParams((mScreenWidth / 3.5).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        return ChatRoomUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = (if (mList.size > 8) mList.size else 8)

    fun isMuted(item: OwnerDetail?): Boolean =
            if (highlightedUser != null && (highlightedUser?.id == item?.id || ownerDetail?.id == item?.id)) {
                false
            } else {
                highlightedUser != null || item?.isMuted == true
            }

    var isStreamerAdded = false
    fun submitList(list: List<OwnerDetail>) {
        Log.i(javaClass.simpleName, "updateUserList: submitList")
        var shouldNotify = list.size == 1
        var hasNotified = false
        for (item in list) {
            if (item.id != null && item.isDuplex == true) {
                if (dataSet.add(item.id)) {
                    Log.i(javaClass.simpleName, "updateUserList: if ${item.isMuted == true} and ${item.id}")
                    if (item.id == ownerDetail?.id) {
                        if (item.id == CommonUtils.getUserID())
                            canUpdateUser = item.canUpdateUser ?: false
                        mList.add(0, item)
                        isStreamerAdded = true
                    } else {
                        if (item.id == CommonUtils.getUserID()) {
                            canUpdateUser = item.canUpdateUser ?: false
                            if (isStreamerAdded)
                                mList.add(1, item)
                            else
                                mList.add(0, item)
                        } else
                            mList.add(item)
                    }
                } else {
                    Log.i(javaClass.simpleName, "updateUserList: else ${item.isMuted == true} and ${item.id}")
                    hasNotified = shouldNotify
                    updateItem(item, shouldNotify)
                }
            }
        }
        if (!shouldNotify || (shouldNotify && !hasNotified))
            notifyDataSetChanged()
        Log.e(javaClass.simpleName, "submitList 2")
    }

    fun clearList() {
        mList.clear()
        dataSet.clear()
        setPaginating(false)
        notifyDataSetChanged()
    }

    fun setPaginating(paginating: Boolean) {
        this.mPaginating = paginating
    }

    fun isPaginating() = mPaginating

    fun removeMe() {
        Log.e(javaClass.simpleName, "removeMe 1")
        if (dataSet.contains(CommonUtils.getUserID())) {
            val index = mList.indexOfFirst { it.id == CommonUtils.getUserID() }
            if (index > -1) {
                dataSet.remove(mList[index].id)
                mList.removeAt(index)
                notifyDataSetChanged()
            }
        }

        Log.e(javaClass.simpleName, "removeMe 2")
    }

    fun removeItem(item: OwnerDetail) {
        Log.e(javaClass.simpleName, "removeItem 1")
        if (item.id != null && dataSet.contains(item.id)) {
            val index = mList.indexOfFirst { it.id == item.id }
            if (index > -1) {
                dataSet.remove(mList[index].id)
                mList.removeAt(index)
                notifyItemRemoved(index)
            }
        }
        Log.e(javaClass.simpleName, "removeItem 2")
    }

    fun addOwner(item: OwnerDetail?) {
        Log.e(javaClass.simpleName, "addOwner 1")
        if (dataSet.contains(item?.id ?: return)) {
            val index = mList.indexOfFirst { it.id == item.id }
            if (index > -1) {
                mList.removeAt(index)
                notifyItemRemoved(index)
                mList.add(0, item)
                notifyDataSetChanged()
            }
        } else {
            dataSet.add(item.id)
            mList.add(0, item)
            notifyDataSetChanged()
        }
        Log.e(javaClass.simpleName, "addOwner 1")
    }

    fun updateSelfItem(muted: Boolean) {
        Log.e(javaClass.simpleName, "updateSelfItem 1")
        if (dataSet.contains(CommonUtils.getUserID())) {
            val index = mList.indexOfFirst { it.id == CommonUtils.getUserID() }
            if (index != -1) {
                mList[index].isMuted = muted
                notifyItemChanged(index)
            }
        }
        Log.e(javaClass.simpleName, "updateSelfItem 2")
    }

    fun getSelfItem(id: Int) =
            if (dataSet.contains(id)) {
                mList.find { it.id == id }
            } else
                null

    fun updateItem(item: OwnerDetail, shouldNotify: Boolean) {
        Log.e(javaClass.simpleName, "updateItem 1")
        if (item.isDuplex != true) return
        if (item.id != null && dataSet.contains(item.id)) {
            val index = mList.indexOfFirst { it.id == item.id }
            Log.i(javaClass.simpleName, "updateUserList: index ${item.isMuted == true} and $index")
            if (index > -1) {
                if (item.id == CommonUtils.getUserID()) {
                    canUpdateUser = item.canUpdateUser == true || mList[index].canUpdateUser == true
                }
                mList[index] = item
                if (shouldNotify)
                    notifyItemChanged(index)
            }
        } else {
            if (item.id != null && dataSet.add(item.id)) {
                if (item.id == CommonUtils.getUserID()) {
                    canUpdateUser = item.canUpdateUser ?: false
                }
                mList.add(item)
                if (shouldNotify)
                    notifyDataSetChanged()
//                    notifyItemInserted(mList.size - 1)
                Log.i(javaClass.simpleName, "updateUserList: else index ${item.isMuted == true}")
            }
        }
        Log.e(javaClass.simpleName, "updateItem 2")
    }

    fun onUserSpeak(speakers: Array<out IRtcEngineEventHandler.AudioVolumeInfo>?) {
        Handler(Looper.getMainLooper()).post {
            speakers?.forEach {
                Log.i(javaClass.simpleName, "speak_id: ${it.uid}")
                if (dataSet.contains(it.uid)) {
                    val index = mList.indexOfFirst { item -> item.id == it.uid }
                    Log.i(javaClass.simpleName, "speaking_now $index")
                    if (index != -1) {
                        mList[index].isSpeaking = true
                        mList[index].isMuted = false
                        notifyItemChanged(index)
                    }
                }
            }
        }
    }

    fun onUserSpeak(speakers: Array<Int>?) {
        Log.e(javaClass.simpleName, "onUserSpeak 1")
        speakers?.forEach {
            Log.i(javaClass.simpleName, "speak_id: $it")
            if (dataSet.contains(it)) {
                val index = mList.indexOfFirst { item -> item.id == it }
                Log.i(javaClass.simpleName, "speaking_now $index")
                if (index != -1) {
                    mList[index].isSpeaking = true
                    if (highlightedUser == null)
                        mList[index].isMuted = false
                    notifyItemChanged(index)
                }
            }
        }
        Log.e(javaClass.simpleName, "onUserSpeak 2")
    }

    inner class ChatRoomUserViewHolder(private val viewDataBinding: ListItemMentionUserBinding) : BaseViewHolder(viewDataBinding.root) {

        private fun resetView() {
            viewDataBinding.authorNameTextView.text = ""
            viewDataBinding.indicatorImageView.visibility = View.GONE
            viewDataBinding.muteIndicatorView.visibility = View.GONE
            viewDataBinding.userAvatar.alpha = 1f
        }

        override fun onBind(position: Int) {
            val item: OwnerDetail? = if (position < mList.size) mList[position] else null
            if (item != null)
                BindingUtils.setRoundImageUri(viewDataBinding.userAvatar, item?.profileImageUrl, item?.username)
            else
                viewDataBinding.userAvatar.setImageResource(R.drawable.ic_add_user_dark)

            itemView.setOnClickListener {
                onItemClick?.invoke(item, item?.isMuted == false)
            }
            if (item == null) {
                resetView()
                return
            }
            viewDataBinding.username = item.username
            viewDataBinding.showIndicator = false
            viewDataBinding.isMuted = if (highlightedUser != null && (highlightedUser?.id == item.id || ownerDetail?.id == item.id)) {
                viewDataBinding.userAvatar.alpha = 1f
                false
            } else {
                val bool = (highlightedUser != null || item.isMuted == true)
                viewDataBinding.userAvatar.alpha = if (bool) 0.6f else 1f
                bool
            }
            viewDataBinding.gameSelectedView.visibility = if (highlightedUser != null && (highlightedUser?.id == item.id || item.id == ownerDetail?.id)) {
                View.VISIBLE
            } else View.GONE
            viewDataBinding.gameSelectedView.backgroundTintList = ColorStateList.valueOf(if (item.id == ownerDetail?.id)
                ContextCompat.getColor(itemView.context, R.color.color_accent)
            else Color.parseColor("#effd43"))
            viewDataBinding.authorNameTextView.setEms(5)

            Log.i(javaClass.simpleName, "speaking_update ${item.isSpeaking} ${viewDataBinding.speakingIndicatorView.visibility == View.GONE}")

            if (item.isSpeaking && viewDataBinding.speakingIndicatorView.visibility == View.GONE) {
                Log.i(javaClass.simpleName, "speaking_update")
                viewDataBinding.speakingIndicatorView.visibility = View.VISIBLE
                item.isSpeaking = false
                viewDataBinding.root.postDelayed({
                    if (viewDataBinding.speakingIndicatorView.visibility == View.VISIBLE)
                        viewDataBinding.speakingIndicatorView.visibility = View.GONE
                }, 1000)
            }
        }

        private fun toggleMute(item: OwnerDetail?) {
            item?.isMuted = item?.isMuted == false
            viewDataBinding.isMuted = item?.isMuted
            viewDataBinding.userAvatar.alpha = if (item?.isMuted == true) 0.6f else 1f
        }

        private fun showUnMuteToolTip(anchorView: View) {
            if (CommonUtils.isUnMuteAudioRoomTooltipShown()) return
            SimpleTooltip.Builder(anchorView.context)
                    .anchorView(anchorView)
                    .text("Unmute to start conversation")
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .onShowListener { CommonUtils.setUnMuteAudioRoomTooltipShown() }
                    .animationDuration(2 * 60 * 1000)
                    .textColor(ContextCompat.getColor(anchorView.context, android.R.color.white))
                    .arrowColor(ContextCompat.getColor(anchorView.context, R.color.color_accent))
                    .backgroundColor(ContextCompat.getColor(anchorView.context, R.color.color_accent))
                    .build()
                    .show()
        }
    }
}