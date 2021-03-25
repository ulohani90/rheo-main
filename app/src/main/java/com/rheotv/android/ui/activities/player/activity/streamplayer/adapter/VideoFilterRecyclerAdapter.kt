package com.rheotv.android.ui.activities.player.activity.streamplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.R
import com.rheotv.android.databinding.ListItemTagBinding
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.getContextDrawable
import java.lang.Exception
import java.util.*

class VideoFilterRecyclerAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    private val mList: MutableList<VideoFilter> = mutableListOf()
    private var mLastSelectedPosition = -1
    private var mOnItemSelected: ((VideoFilter) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
            GameTagViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.list_item_tag, parent, false))

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    fun resetFilterSelection() {
        try {
            if (mList.isNotEmpty()) {
                mLastSelectedPosition = -1
                mList.forEach { it.isSelected = false }
                mList[0].isSelected = true
                notifyDataSetChanged()
            }
        } catch (e: Exception) {

        }
    }

    fun submitList(list: List<VideoFilter>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    fun setItemSelectedListener(onItemSelected: ((VideoFilter) -> Unit)? = null) {
        mOnItemSelected = onItemSelected
    }

    fun refreshTag() {
        if (mLastSelectedPosition in 0..mList.size) {
            mList[mLastSelectedPosition].isSelected = false
        }
        mList[0].isSelected = true
        mLastSelectedPosition = 0
        notifyDataSetChanged()
    }

    inner class GameTagViewHolder(private val binding: ListItemTagBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            if (mLastSelectedPosition == -1 && position == 0) {
                mLastSelectedPosition = position
                mList[position].isSelected = true
            }
            binding.isSelected = mList[position].isSelected
            binding.chipText = mList[position].name
            if (mList[position].imageId != -1)
                binding.chipIcon = binding.root.context.getContextDrawable(mList[position].imageId)
            itemView.setOnClickListener {
                if (position != mLastSelectedPosition) {
                    if (!mList[position].isSelected) {
                        mList[mLastSelectedPosition].isSelected = false
                        mLastSelectedPosition = position
                        mList[position].isSelected = true
                    }
                    notifyDataSetChanged()
                }
                mOnItemSelected?.invoke(mList[position])
            }
        }
    }

    data class VideoFilter(val name: String, @DrawableRes val imageId: Int = -1,
                           var tag: String? = null, var isSelected: Boolean = false) {
        companion object {
            fun getDefault(): List<VideoFilter> = listOf(
                    VideoFilter("For You", R.drawable.avd_home, AppConstants.LIVE_GAME_ID),
                    VideoFilter("Trending", R.drawable.avd_home, "trending"),
                    VideoFilter("Giveaway", R.drawable.avd_leaderboard, "giveaway-videos"),
                    VideoFilter("Tournaments", R.drawable.avd_gift_white, "all-tournaments")
            )
        }
    }
}