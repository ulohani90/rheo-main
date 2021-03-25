package com.rheotv.android.ui.activities.player.activity.newPlayer.adapter

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.R
import com.rheotv.android.data.network.models.postlisting.responses.TopFans
import com.rheotv.android.databinding.ListItemPlayerBinding
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.utils.ViewUtils

class TopFansRecyclerAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    private val mList: MutableList<TopFans> = mutableListOf()

    private var mOnItemClick: ((TopFans) -> Unit)? = null
    var screenName: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return FansViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item_player, parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = mList.size

    fun submitList(list: List<TopFans>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    fun onItemSelectedListener(onItemClick: ((TopFans) -> Unit)) {
        mOnItemClick = onItemClick
    }

    inner class FansViewHolder(private val viewDataBinding: ListItemPlayerBinding) : BaseViewHolder(viewDataBinding.root) {
        override fun onBind(position: Int) {
            val item = mList[position]
            viewDataBinding.profilePicUrl = item.profilePic
            viewDataBinding.username = item.user?.username
            viewDataBinding.gameUserName = item.user?.username
            viewDataBinding.isFollowed = item.isFollowed
            viewDataBinding.followerCount = ViewUtils.getFollowersCountString(item.followersCount
                    ?: 0)
            viewDataBinding.followButton.setOnClickListener { mOnItemClick?.invoke(item) }
            viewDataBinding.root.setOnClickListener {
                ProfileActivity.startMe(it.context, "TopFansPage", item.user?.username)
            }

            val color: Int
            var guidelinePercentage = 0f
            if (position == 0 || position == 1 || position == 2) {
                viewDataBinding.rankIndicatorView.visibility = View.VISIBLE
                viewDataBinding.guideline.visibility = View.VISIBLE
                when (position) {
                    0 -> {
                        color = R.color.color_streamer_first_place
                        guidelinePercentage = 0.84f
                    }
                    1 -> {
                        color = R.color.color_streamer_second_place
                        guidelinePercentage = 0.76f
                    }
                    2 -> {
                        color = R.color.color_streamer_third_place
                        guidelinePercentage = 0.68f
                    }
                    else -> color = R.color.color_bottom_sheet_background
                }
                Log.i(javaClass.simpleName, "top_fans_position: $position $color $guidelinePercentage")
                viewDataBinding.rankIndicatorView.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                viewDataBinding.rankIndicatorView.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(viewDataBinding.root.context, color))
                viewDataBinding.guideline.setGuidelinePercent(guidelinePercentage)
            } else {
                viewDataBinding.rankIndicatorView.visibility = View.GONE
                viewDataBinding.guideline.visibility = View.GONE
            }
        }
    }
}