package com.rheotv.android.ui.activities.player.activity

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.R
import com.rheotv.android.data.network.models.play.ResultsItem
import com.rheotv.android.databinding.LayoutRequestPlayBinding
import com.rheotv.android.databinding.ListItemRequestPlayBinding
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.BindingUtils
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.RewardManager
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import java.util.*

class PlayRequestAdapter : RecyclerView.Adapter<BaseViewHolder>() {
    private var listener: PlayRequestListener? = null
    private var coinRequired: String? = "0"
    private var game: String? = null
    private var waitingNumber: String? = null
    private var gamerUserName: String? = null
    private var postUserName: String? = null
    private var isCustomRoomEnabled = false
    private var customRoomUsername: String? = null
    private var customRoomPassword: String? = null
    private var isPaginating = false

    private val mAcceptedPlayerList: MutableList<ResultsItem> = mutableListOf()
    private val mPendingPlayerList: MutableList<ResultsItem> = mutableListOf()
    fun setListener(listener: PlayRequestListener?) {
        this.listener = listener
    }

    private val dataMap: MutableSet<String> = HashSet()

    private fun removeFromList(list: MutableList<ResultsItem>, item: ResultsItem) {
        val index = list.indexOfFirst { it.id == item.id }
        if (index != -1) {
            list.removeAt(index)
        }
    }

    fun updatePlayer(requestItemList: MutableList<ResultsItem>): Boolean {
        if (requestItemList.isEmpty()) return false
        val isStreamer = CommonUtils.getUserName().equals(postUserName, ignoreCase = true)
        if (isStreamer) {
            requestItemList.forEach {
                if (dataMap.add(it.id)) {
                    when {
                        it.state == "PENDING" -> mPendingPlayerList.add(0, it)
                        it.state == "ACCEPTED" -> mAcceptedPlayerList.add(0, it)
                        isCustomRoomEnabled && (it.state == "REJECTED" || it.state == "REFUNDED") -> {
                            removeFromList(mAcceptedPlayerList, it)
                            removeFromList(mPendingPlayerList, it)
                        }
                    }
                } else {
                    if (it.state == "ACCEPTED") {
                        val index = mPendingPlayerList.indexOfFirst { item -> item.id == it.id && item.state == "PENDING" }
                        if (index != -1) {
                            mPendingPlayerList.removeAt(index)
                            mAcceptedPlayerList.add(if (mAcceptedPlayerList.isEmpty()) 0 else 1, it)
                        }
                    } else if (it.state == "REJECTED" || it.state == "REFUNDED") {
                        if (isCustomRoomEnabled) {
                            removeFromList(mAcceptedPlayerList, it)
                            removeFromList(mPendingPlayerList, it)
                        } else {
                            val acceptedIndex = mAcceptedPlayerList.indexOfFirst { item -> item.id == item.id && item.state == "ACCEPTED" }
                            if (acceptedIndex != -1) {
                                mAcceptedPlayerList.removeAt(acceptedIndex)
                            } else {
                                val pendingIndex = mPendingPlayerList.indexOfFirst { item -> item.id == item.id && item.state == "PENDING" }
                                if (pendingIndex != -1) {
                                    mPendingPlayerList.removeAt(pendingIndex)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val me: ResultsItem = requestItemList.find {
                it.fromUserProfile?.user?.username == CommonUtils.getUserName() && it.toPost != null
            } ?: return false

            if (isCustomRoomEnabled && me.state == "REFUNDED") {
                dataMap.remove(me.id)
                var index = mAcceptedPlayerList.indexOfFirst { it.id == me.id }
                if (index != -1) {
                    mAcceptedPlayerList.removeAt(index)
                    updateAcceptedToPending(mPendingPlayerList)
                    updateAcceptedToPending(mAcceptedPlayerList)
                } else {
                    index = mPendingPlayerList.indexOfFirst { it.id == me.id }
                    if (index != -1) {
                        mPendingPlayerList.removeAt(index)
                        updateAcceptedToPending(mPendingPlayerList)
                        updateAcceptedToPending(mAcceptedPlayerList)
                    }
                }
                notifyDataSetChanged()
                return false
            }
            val isAdded = dataMap.add(me.id)
            if (isAdded) {
                removeInputPlank(mAcceptedPlayerList)
                removeInputPlank(mPendingPlayerList)
                if (mAcceptedPlayerList.isEmpty()) {
                    mAcceptedPlayerList.add(me)
                } else {
                    mAcceptedPlayerList[0] = me
                }
            } else {
                when {
                    me.isWinner || mAcceptedPlayerList.isNotEmpty() -> if (shouldUpdateUser(mAcceptedPlayerList[if (me.isWinner) 1 else 0], me) && (me.state == "ACCEPTED" || me.state == "REJECTED")) {
                        mAcceptedPlayerList[if (me.isWinner) 1 else 0] = me
                    }
                    mPendingPlayerList.isNotEmpty() -> if (shouldUpdateUser(mPendingPlayerList[0], me) && me.state == "ACCEPTED" || me.state == "REJECTED") {
                        mPendingPlayerList.removeAt(0)
                        mAcceptedPlayerList.add(0, me)
                        val index = mPendingPlayerList.indexOfFirst { shouldUpdateUser(it, me) }
                        if (index != -1) {
                            mPendingPlayerList[index] = me
                        }
                    }
                    else -> return mAcceptedPlayerList.isNotEmpty() || mPendingPlayerList.isNotEmpty()
                }
            }
            if (isCustomRoomEnabled && me.state == "ACCEPTED") {
                val item = if (requestItemList.size > 1) {
                    requestItemList[0]
                } else {
                    ResultsItem().also {
                        it.type = AppConstants.CUSTOM_ROOM_VIEW_TYPE_REQUEST_ACCEPTED
                        it.id = AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW
                        it.state = "ACCEPTED"
                    }
                }
                if (dataMap.add(item.id)) {
                    mAcceptedPlayerList.add(0, item)
                } else {
                    if (mAcceptedPlayerList.size >= 2) {
                        mAcceptedPlayerList[0] = item
                    }
                }
            }
        }
        notifyDataSetChanged()
        return mAcceptedPlayerList.isNotEmpty() || mPendingPlayerList.isNotEmpty()
    }

    private fun updateAcceptedToPending(list: MutableList<ResultsItem>) {
        val index = list.indexOfFirst { it.id == AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW }
        if (index != -1) {
            val resultsItem = ResultsItem()
            resultsItem.type = AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW
            resultsItem.state = "PENDING"
            resultsItem.id = AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW
            dataMap.remove(list[index].id)
            list.removeAt(index)
            list.add(0, resultsItem)
            dataMap.add(resultsItem.id)
        }
    }

    val item: ResultsItem?
        get() = if (mAcceptedPlayerList.size > 1) mAcceptedPlayerList[1] else if (mPendingPlayerList.size > 1) mPendingPlayerList[1] else null

    fun addPlayers(requestItemList: MutableList<ResultsItem>) {

        if (!isPaginating()) {
            dataMap.clear()
            mAcceptedPlayerList.clear()
            mPendingPlayerList.clear()
            requestItemList.forEach {
                val added = dataMap.add(it.id)
                if (added) {
                    when (it.state) {
                        "PENDING" -> {
                            if (it.fromUserProfile?.user?.username?.equals(CommonUtils.getUserName()) == true &&
                                    dataMap.contains(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW)) {
                                if (mAcceptedPlayerList.isNotEmpty() && mAcceptedPlayerList[0].type == AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW) {
                                    mAcceptedPlayerList.removeAt(0)
                                } else if (mPendingPlayerList.isNotEmpty() && mPendingPlayerList[0].type == AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW) {
                                    mPendingPlayerList.removeAt(0)
                                }
                            }
                            mPendingPlayerList.add(it)
                        }
                        else -> {
                            if (it.type == AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW || it.type == AppConstants.CUSTOM_ROOM_VIEW_TYPE_REQUEST_ACCEPTED) {
                                mAcceptedPlayerList.add(0, it)
                            } else if (it.fromUserProfile?.user?.username?.equals(CommonUtils.getUserName()) == true) {
                                mAcceptedPlayerList.add(if (dataMap.contains(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW)) 1 else 0, it)
                            } else
                                mPendingPlayerList.add(it)
                        }
                    }
                }
            }
            notifyDataSetChanged()
            return
        }
        setPaginating(false)
        appendPlayers(requestItemList)
    }

    private fun appendPlayers(requestItemList: MutableList<ResultsItem>) {

        requestItemList.forEach { item ->
            if (dataMap.add(item.id)) {
                when {
                    item.fromUserProfile?.user?.username == CommonUtils.getUserName() -> {
                        removeInputPlank(mAcceptedPlayerList)
                        removeInputPlank(mPendingPlayerList)
                        mAcceptedPlayerList.add(0, item)
                    }
                    item.state == "PENDING" -> {
                        mPendingPlayerList.add(item)
                    }
                    else -> {
                        mAcceptedPlayerList.add(item)
                    }
                }
            }
            if (mAcceptedPlayerList.isNotEmpty() && shouldUpdateUser(mAcceptedPlayerList[0], item)) {
                mAcceptedPlayerList[0] = item
            } else if (mPendingPlayerList.isNotEmpty() && shouldUpdateUser(mPendingPlayerList[0], item)) {
                mPendingPlayerList.removeAt(0)
                mAcceptedPlayerList[0] = item
            }
        }
        notifyDataSetChanged()
    }

    private fun removeInputPlank(list: MutableList<ResultsItem>) {
        if (list.isNotEmpty()) {
            if (list[0].type == AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW) {
                dataMap.remove(list[0].type)
                list.removeAt(0)
            }
        }
    }

    private fun shouldUpdateUser(firstItem: ResultsItem, item: ResultsItem): Boolean {
        return firstItem.id == item.id &&
                ((firstItem.type == AppConstants.PLAY_VIEW_TYPE_PENDING
                        && (item.type == AppConstants.PLAY_VIEW_TYPE_REQUESTED
                        || item.type == AppConstants.CUSTOM_ROOM_VIEW_TYPE_REQUEST_ACCEPTED))
                        || ((firstItem.type == AppConstants.PLAY_VIEW_TYPE_REQUESTED
                        || firstItem.type == AppConstants.CUSTOM_ROOM_VIEW_TYPE_REQUEST_ACCEPTED)
                        && ((firstItem.state == "ACCEPTED" && item.state == "REFUNDED")
                        || (firstItem.state == "PENDING" && (item.state == "ACCEPTED"
                        || item.state == "REJECTED" || item.state == "REFUNDED"))))
                        || (firstItem.state == "ACCEPTED" && !firstItem.isWinner && item.state == "ACCEPTED" && item.isWinner))
    }

    fun updateWaitingNumber(waitingNumber: String?) {
        waitingNumber?.let {
            if (!it.equals(this.waitingNumber, ignoreCase = true)) {
                this.waitingNumber = it
                notifyWaitingNumberChange(mAcceptedPlayerList)
                notifyWaitingNumberChange(mPendingPlayerList)
            }
        }
    }

    private fun notifyWaitingNumberChange(list: MutableList<ResultsItem>) {
        if (list.isEmpty() && CommonUtils.getUserName().equals(list[0].fromUserProfile.user.username, ignoreCase = true)) {
            notifyItemChanged(0)
        }
    }

    fun setCoinAndQueuePosition(coinRequired: String?, waitingNumber: String?, postUserName: String?, gamerUserName: String?, isCustomRoomEnabled: Boolean) {
        coinRequired?.let { this.coinRequired = it }
        waitingNumber?.let { this.waitingNumber = it }
        postUserName?.let { this.postUserName = it }
        gamerUserName?.let { this.gamerUserName = it }
        this.isCustomRoomEnabled = isCustomRoomEnabled
        notifyDataSetChanged()
    }

    fun setGame(game: String?) {
        this.game = game
        notifyDataSetChanged()
    }

    fun isPaginating(): Boolean = isPaginating

    fun setPaginating(paginating: Boolean) {
        if (paginating) {
            isPaginating = true
            notifyDataSetChanged()
        } else if ((itemCount) > mAcceptedPlayerList.size + mPendingPlayerList.size) {
            isPaginating = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (viewType == AppConstants.PLAYABLE_VIEW_TYPE_REQUEST_NOW || viewType == AppConstants.CUSTOM_ROOM_ACCEPTED_VIEW_TYPE) {
            val binding: LayoutRequestPlayBinding = DataBindingUtil.inflate(inflater, R.layout.layout_request_play, parent, false)
            return RequestNowViewHolder(binding)
        }
        if (viewType == AppConstants.VIEW_TYPE_LOADING_FOOTER) {
            return FooterViewHolder(FrameLayout(parent.context))
        }
        val binding: ListItemRequestPlayBinding = DataBindingUtil.inflate(inflater, R.layout.list_item_request_play, parent, false)
        return RequestStateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return if (isPaginating) {
            mAcceptedPlayerList.size + mPendingPlayerList.size + 1
        } else {
            mAcceptedPlayerList.size + mPendingPlayerList.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = if (position < mAcceptedPlayerList.size) mAcceptedPlayerList[position] else if ((position - mAcceptedPlayerList.size) < mPendingPlayerList.size) mPendingPlayerList[position - mAcceptedPlayerList.size] else null
        return if (isPaginating && position == mAcceptedPlayerList.size + mPendingPlayerList.size) {
            AppConstants.VIEW_TYPE_LOADING_FOOTER
        } else if ((item?.type == AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW)) {
            AppConstants.PLAYABLE_VIEW_TYPE_REQUEST_NOW
        } else if (item?.type.equals(AppConstants.CUSTOM_ROOM_VIEW_TYPE_REQUEST_ACCEPTED, ignoreCase = true)) {
            AppConstants.CUSTOM_ROOM_ACCEPTED_VIEW_TYPE
        } else {
            AppConstants.PLAYABLE_VIEW_TYPE_PLAYER
        }
    }

    fun setCustomRoomDetails(customRoomUsername: String?, customRoomPassword: String?) {
        this.customRoomUsername = customRoomUsername
        this.customRoomPassword = customRoomPassword
        notifyItemChanged(0)
    }

    fun updateState(requestId: String, action: String) {
        when (action) {
            AppConstants.PLAY_REQUEST_ACCEPT -> {
                val item = mPendingPlayerList.find { it.id == requestId }
                        ?.also {
                            it.state = "ACCEPTED"
                            it.type = AppConstants.PLAY_VIEW_TYPE_REQUESTED
                            mAcceptedPlayerList.add(it)
                        }
                mPendingPlayerList.remove(item)
                notifyDataSetChanged()
            }
            AppConstants.PLAY_REQUEST_REJECT -> {
                val item = mPendingPlayerList.find { it.id == requestId }
                        ?.also { dataMap.remove(it.id) }
                mPendingPlayerList.remove(item)
                notifyDataSetChanged()
            }
            AppConstants.PLAY_REQUEST_REFUND -> {
                val item = mAcceptedPlayerList.find { it.id == requestId }
                        ?.also { dataMap.remove(it.id) }
                mAcceptedPlayerList.remove(item)
                notifyDataSetChanged()
            }
        }
    }

    inner class FooterViewHolder(itemView: View?) : BaseViewHolder(itemView) {
        override fun onBind(position: Int) {
            itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
            val progressBar = ProgressBar(itemView.context)
            progressBar.indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.color_accent))
            (itemView as FrameLayout).addView(progressBar, layoutParams)
        }
    }

    private fun isAcceptedPlayer(position: Int, context: Context, list: MutableList<ResultsItem>): Boolean = list.isNotEmpty() && position < list.size && list[position].state?.equals(context.getString(R.string.state_accepted), ignoreCase = true) == true
    inner class RequestNowViewHolder internal constructor(private val binding: LayoutRequestPlayBinding) : BaseViewHolder(binding.root) {
        private var isTextChangeEventRecorded = false
        override fun onBind(position: Int) {
            val context = binding.root.context
            val customRoomEnabled = (isCustomRoomEnabled)
            binding.customRoomHeading = customRoomEnabled
            binding.isEditModeOn = false
            if (customRoomEnabled) {
                if (CommonUtils.getUserName(context).equals(postUserName, ignoreCase = true)) {
                    binding.customRoomDetailsSaved =
                            if (!customRoomUsername.isNullOrEmpty()
                                    && !customRoomPassword.isNullOrEmpty()) {
                                binding.setRoomId(customRoomUsername)
                                binding.addCustomRoomDetailsClicked = false
                                true
                            } else {
                                false
                            }
                    binding.addCustomRoom.setOnClickListener { binding.addCustomRoomDetailsClicked = true }
                    binding.submitCustomRoomDetails.setOnClickListener {
                        if (!binding.customRoomId.text?.toString()?.trim { it <= ' ' }.isNullOrEmpty()) {
                            if (!binding.customRoomPassword.text?.toString()?.trim { it <= ' ' }.isNullOrEmpty()) {
                                listener?.onSubmitCustomRoomDetailsClick(binding.customRoomId.text.toString(),
                                        binding.customRoomPassword.text.toString(),
                                        binding.isEditModeOn ?: false)
                            } else {
                                Toast.makeText(context, ("Please enter a custom room password."), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, ("Please enter a custom room id."), Toast.LENGTH_LONG).show()
                        }
                    }
                    binding.editCustomRoomDetails.setOnClickListener {
                        binding.setRoomId(customRoomUsername)
                        binding.roomPassword = customRoomPassword
                        binding.addCustomRoomDetailsClicked = true
                        binding.isEditModeOn = true
                    }
                    binding.cancelCustomRoomDetails.setOnClickListener {
                        binding.isEditModeOn = false
                        binding.addCustomRoomDetailsClicked = false
                    }
                    binding.addAnotherCustomRoom.setOnClickListener {
                        AlertDialog.Builder(context, R.style.AlertDialogDarkBackgroundStyle)
                                .setTitle("Alert")
                                .setMessage("Have you completed the current custom room with all the viewers whose request you had accepted?")
                                .setPositiveButton("Yes") { dialogInterface, _ ->
                                    listener?.recordSegmentAction(SegmentConstants.EVENT_ADD_ANOTHER_CUSTOM_ROOM_DETAILS)
                                    binding.customRoomDetailsSaved = false
                                    val item = mAcceptedPlayerList[0]
                                    mAcceptedPlayerList.clear()
                                    mPendingPlayerList.clear()
                                    mAcceptedPlayerList.add(item)
                                    binding.customRoomId.setText("")
                                    binding.customRoomPassword.setText("")
                                    dialogInterface.dismiss()
                                }
                                .setNegativeButton("No") { dialogInterface, _ ->
                                    Toast.makeText(context, "Please complete the current custom room first", Toast.LENGTH_LONG).show()
                                    dialogInterface.dismiss()
                                }.show()
                    }
                } else {
                    if (isAcceptedPlayer(position, context, mAcceptedPlayerList)) {
                        binding.customRoomRequestAccepted = true
                        binding.setRoomId(customRoomUsername)
                        binding.roomPassword = customRoomPassword
                        binding.roomIdTextViewLayout.setOnClickListener {
                            CommonUtils.copyToClipboard(context, "Room Id", customRoomUsername)
                            Toast.makeText(context, "Room Id copied to clipboard", Toast.LENGTH_SHORT).show()
                            listener?.onRoomDetailsCopied(game, true)
                        }
                        binding.roomPassLayout.setOnClickListener {
                            CommonUtils.copyToClipboard(context, "Room Pass", customRoomPassword)
                            Toast.makeText(context, "Room Password copied to clipboard", Toast.LENGTH_SHORT).show()
                            listener?.onRoomDetailsCopied(game, false)
                        }
                    } else {
                        binding.userNameEt.setText("")
                        binding.gamerUserName = gamerUserName
                        binding.gameName = game
                        val hasEnoughCoins: Boolean = (coinRequired?.toInt()
                                ?: 0) <= RewardManager.getInstance().totalCoin
                        binding.hasEnoughCoins = hasEnoughCoins
                        binding.requiredCoin = coinRequired
                        try {
                            val requireCoins = coinRequired?.toInt() ?: 0
                            binding.customRoomInfoHeader.text = if (requireCoins > 1) {
                                String.format(context.getString(R.string.custom_room_entry_cost_plural), coinRequired)
                            } else {
                                String.format(context.getString(R.string.custom_room_entry_cost_singular), coinRequired)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        binding.showCustomRoomRequestAccess = true
                        Log.i(javaClass.simpleName, "RequestNowViewHolder coinRequired: " + coinRequired + " gamerUserName: " + " postUserName: " + postUserName + " hasEnoughCoins " + hasEnoughCoins + " and " + RewardManager.getInstance().totalCoin)
                        binding.customRoomRequestAccessBtn.setOnClickListener {
                            if (!hasEnoughCoins) {
                                Toast.makeText(context, "You don't have enough coins", Toast.LENGTH_LONG).show()
                                return@setOnClickListener
                            }
                            if (binding.userNameEt.text?.toString()?.trim { it <= ' ' }.isNullOrEmpty()) {
                                binding.userNameEt.error = "Add game id"
                                Toast.makeText(context, "Please enter your$game id", Toast.LENGTH_LONG).show()
                                return@setOnClickListener
                            }
                            listener?.onPlayRequest(binding.userNameEt.text?.toString()?.trim { it <= ' ' })
                        }
                        binding.emojiEditText.onFocusChangeListener = View.OnFocusChangeListener { _: View?, b: Boolean ->
                            if (b && !isTextChangeEventRecorded) {
                                isTextChangeEventRecorded = true
                                SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_PLAY_REQUEST_GAME_ID_ENTER, HashMap())
                            }
                        }
                    }
                }
            } else {
                val hasEnoughCoins: Boolean = (coinRequired?.toInt()
                        ?: 0) <= RewardManager.getInstance().totalCoin
                binding.requiredCoin = coinRequired
                binding.gamerUserName = gamerUserName
                binding.gameName = game
                binding.hasEnoughCoins = hasEnoughCoins
                Log.i(javaClass.simpleName, "RequestNowViewHolder coinRequired: " + coinRequired + " gamerUserName: " + " postUserName: " + postUserName + " hasEnoughCoins " + hasEnoughCoins + " and " + RewardManager.getInstance().totalCoin)
                binding.requestButton.setOnClickListener {
                    if (!hasEnoughCoins) {
                        Toast.makeText(context, "You don't have enough coins to play.", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    val userName: String? = binding.emojiEditText.text?.toString()
                    if (userName.isNullOrEmpty()) {
                        Toast.makeText(context, ("Please enter your $game username"), Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    listener?.onPlayRequest(userName)
                }
                binding.emojiEditText.onFocusChangeListener = View.OnFocusChangeListener { _: View?, b: Boolean ->
                    if (b && !isTextChangeEventRecorded) {
                        isTextChangeEventRecorded = true
                        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_PLAY_REQUEST_GAME_ID_ENTER, HashMap())
                    }
                }
            }
            binding.executePendingBindings()
        }

    }

    inner class RequestStateViewHolder internal constructor(private val binding: ListItemRequestPlayBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            val context = binding.root.context
            // since 1 item is of type
            val current = when {
                position < mAcceptedPlayerList.size -> mAcceptedPlayerList[position]
                (position - mAcceptedPlayerList.size) < mPendingPlayerList.size -> mPendingPlayerList[position - mAcceptedPlayerList.size]
                else -> null
            } ?: return
            var preType: String? = null
            if (position > 0) preType = when {
                position - 1 < mAcceptedPlayerList.size -> mAcceptedPlayerList[position - 1]
                (position - 1 - mAcceptedPlayerList.size) < mPendingPlayerList.size -> mPendingPlayerList[position - mAcceptedPlayerList.size - 1]
                else -> null
            }?.type
            val isMe = (CommonUtils.getUserName(context) == postUserName)
            var playerStateMessage: String? = null
            var queueMessage: String? = null
            binding.waitingNumber = if (current.state == context.getString(R.string.state_pending))
                context.getString(R.string.queue_message_other, CommonUtils.getNumberOrdinal(waitingNumber).toLowerCase())
            else ""
            if ((current.type == AppConstants.PLAY_VIEW_TYPE_REQUESTED)) {
                playerStateMessage = if (isMe) {
                    if (isCustomRoomEnabled) context.getString(R.string.accpeted_custom_room_requests) else context.getString(R.string.accepted_play_requests)
                } else {
                    if ((current.state == context.getString(R.string.state_pending))) {
                        context.getString(R.string.play_request_submitted_message)
                    } else if ((current.state == context.getString(R.string.state_accepted))) {
                        context.getString(R.string.play_request_accepted_message, postUserName
                                ?: "", game ?: "")
                    } else if ((current.state == context.getString(R.string.state_rejected)) || (current.state == context.getString(R.string.state_refunded))) {
                        if (isCustomRoomEnabled) {
                            context.getString(R.string.custom_room_rejected_message, postUserName
                                    ?: "")
                        } else {
                            context.getString(R.string.play_request_rejected_message, postUserName
                                    ?: "")
                        }
                    } else ""
                }
            } else if ((current.type == AppConstants.PLAY_VIEW_TYPE_PENDING)) {
                queueMessage = if (isMe) {
                    if (isCustomRoomEnabled) {
                        context.getString(R.string.custom_room_queue_message_me)
                    } else context.getString(R.string.queue_message_me)
                } else if (preType?.equals(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW, ignoreCase = true) == true) {
                    "Player Queue"
                } else "Player Queue"
            }
            BindingUtils.setProfileImageUrlFromCache(binding.profileImageView, current.fromUserProfile.profilePic, true)
            binding.item = current
            binding.isCustomRoom = isCustomRoomEnabled
            binding.gameName = game
            binding.authorName = postUserName
            binding.gameUserName = current.gameUsername
            binding.playerStateMessage = playerStateMessage
            binding.queueMessage = queueMessage
            binding.showState = current.type != preType && (current.type == AppConstants.PLAY_VIEW_TYPE_REQUESTED)
            binding.showQueue = current.type != preType && (current.type == AppConstants.PLAY_VIEW_TYPE_PENDING)
            binding.isMe = isMe
            binding.tickImageView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    if (isCustomRoomEnabled) {
                        if (customRoomUsername.isNullOrEmpty() || customRoomPassword.isNullOrEmpty()) {
                            Toast.makeText(context, "Please enter the Room Id and Room Password", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    listener?.onAction(
                            current.id,
                            AppConstants.PLAY_REQUEST_ACCEPT,
                            current.fromUserProfile?.user?.username,
                            current.gameUsername,
                            current.fromUserProfile?.profilePic)
                }
            })
            binding.crossImageViw.setOnClickListener {
                AlertDialog.Builder(context, R.style.AlertDialogDarkBackgroundStyle)
                        .setTitle("Confirm")
                        .setMessage("Are you sure you want to reject this request?")
                        .setPositiveButton("Yes") { dialogInterface, _ ->
                            listener?.onAction(current.id, AppConstants.PLAY_REQUEST_REJECT, null, null, null)
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
                        .show()
            }
            binding.playerCardView.setOnClickListener {
                listener?.onPlayerClick(current.id,
                        current.fromUserProfile?.user?.username,
                        current.gameUsername,
                        current.fromUserProfile?.profilePic,
                        (current.state == context.getString(R.string.state_accepted)))
            }
            binding.notAbleToPlayButton.setOnClickListener {
                listener?.onAction(
                        current.id,
                        AppConstants.PLAY_REQUEST_REFUND,
                        current.fromUserProfile?.user?.username,
                        current.gameUsername,
                        current.fromUserProfile?.profilePic
                )
            }
            Log.i(javaClass.simpleName, "current_item preType " + preType + " currentType " + current.type + " isMe " + isMe + " playerStateMessage " + playerStateMessage + " queueMessage " + queueMessage + " gamerUserName: " + current.gameUsername)
            binding.executePendingBindings()
        }

    }

    companion object {
        private const val TAG = "PlayRequestAdapter"
    }

}