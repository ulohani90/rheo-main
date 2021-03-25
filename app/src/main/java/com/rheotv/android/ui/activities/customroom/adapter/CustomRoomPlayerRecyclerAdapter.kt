package com.rheotv.android.ui.activities.customroom.adapter

import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.databinding.ListItemPlayerBinding
import com.rheotv.android.databinding.ListItemSearchBinding
import com.rheotv.android.ui.activities.customroom.model.CustomRoomPlayer
import com.rheotv.android.ui.activities.player.activity.RequestToPlayDialogFragment
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.utils.showToast
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.sign

class CustomRoomPlayerRecyclerAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    private val mList: MutableList<CustomRoomPlayer> = mutableListOf()
    private var mWinnerListener: ((CustomRoomPlayer?) -> Unit)? = null
    private var mWinner: CustomRoomPlayer? = null
    var isLoading = false
        private set
    var isPaginating = false
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            0x002 -> FooterViewHolder(FrameLayout(parent.context).also { it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) })
            else -> PlayerViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item_player, parent, false))
        }
    }

    override fun getItemCount(): Int = mList.size + (if (isLoading || isPaginating) 1 else 0)

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return if ((isPaginating && position == mList.size) || (position == 0 && isLoading)) 0x002 else 0x001
    }

    fun setLoading(loading: Boolean) {
        if (isLoading != loading) {
            isLoading = loading
            if (isPaginating)
                notifyItemInserted(0)
            else
                notifyItemRemoved(0)
        }
    }

    fun setPaginating(paginating: Boolean) {
        if (isPaginating != paginating) {
            isPaginating = paginating
            if (isPaginating)
                notifyItemInserted(mList.size)
            else
                notifyItemRemoved(mList.size)
        }
    }

    fun setWinnerListener(winnerListener: ((CustomRoomPlayer?) -> Unit)) {
        mWinnerListener = winnerListener
    }

    fun submitList(list: List<CustomRoomPlayer>, clear: Boolean = false) {
        setLoading(false)
        if (clear) {
            mList.clear()
        }
        if (mList.isEmpty()) {
            mWinner?.let {
                mList.add(0, it)
            }
        }
        val mutableList: MutableList<CustomRoomPlayer> = list.toMutableList()
        if (mWinner != null)
            mutableList.removeAll { it.id == mWinner?.id }
        val startIndex = mList.size
        mList.addAll(mutableList)
        if (clear)
            notifyDataSetChanged()
        else
            notifyItemInserted(startIndex)
    }


    fun addWinner(winner: CustomRoomPlayer?) {
        mWinner = winner
        notifyDataSetChanged()
    }

    fun updateWinner() {
        mWinner?.apply {
            if (mList.removeAll { it.id == id }) {
                mList.add(0, this)
            }
        }
        notifyDataSetChanged()
    }

    inner class PlayerViewHolder(private val mBinding: ListItemPlayerBinding) : BaseViewHolder(mBinding.root) {

        override fun onBind(position: Int) {
            val item = mList[position - if (isLoading) 1 else 0]
            mBinding.profilePicUrl = item.profilePicUrl
            mBinding.winner = item.isWinner
            mBinding.username = item.username
            mBinding.gameUserName = "${item.username} (${item.gameUsername})"
            mBinding.root.setOnClickListener {
                if (mWinner == null) {
                    mWinnerListener?.invoke(item)
                }
            }
        }
    }

    inner class FooterViewHolder(view: View) : BaseViewHolder(view) {

        override fun onBind(position: Int) {
            (itemView as? ViewGroup)?.addView(ProgressBar(itemView.context).also {
                it.indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.color_accent))
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL))
        }
    }
}