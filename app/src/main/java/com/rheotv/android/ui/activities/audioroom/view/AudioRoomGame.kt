package com.rheotv.android.ui.activities.audioroom.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.R
import com.rheotv.android.databinding.LayoutAudioRoomGameBinding
import com.rheotv.android.databinding.ListItemGameBinding
import com.rheotv.android.ui.activities.audioroom.model.SocialGame
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.BindingUtils
import com.rheotv.android.utils.ViewUtils

class AudioRoomGame {

    var rootView: LayoutAudioRoomGameBinding? = null
    private var mAction: ((SocialGame) -> Unit)? = null

    fun setupView(context: Context, list: MutableList<SocialGame>) {
        rootView = LayoutAudioRoomGameBinding.inflate(LayoutInflater.from(context))
        if (list.none { it.name?.equals(AppConstants.AMONG_US_APP_NAME, ignoreCase = true) == true }) {
            list.add(SocialGame(AppConstants.AMONG_US_PACKAGE_NAME, AppConstants.AMONG_US_APP_NAME, null, null))
        }
        rootView?.gameRecyclerView?.adapter = AudioRoomGameRecyclerAdapter().apply {
            onItemClicked = { item -> mAction?.invoke(item) }
            submitList(list)
        }
    }

    fun registerActionListener(function: (SocialGame) -> Unit) {
        mAction = function
    }
}

class AudioRoomGameRecyclerAdapter : RecyclerView.Adapter<BaseViewHolder>() {
    private val mList: MutableList<SocialGame> = mutableListOf()
    var onItemClicked: ((SocialGame) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ContentViewHolder(ListItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = mList.size

    fun submitList(list: List<SocialGame>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ContentViewHolder(private val binding: ListItemGameBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            val item = mList[position]
            if (item.id == AppConstants.AMONG_US_PACKAGE_NAME) {
                binding.gameImageView.setImageResource(R.drawable.avd_among_us)
                binding.gameNameTextView.text = item.name
            } else {
                BindingUtils.setProfileImageUrlRounded(binding.gameImageView, item.logoUrl,
                        ViewUtils.dpToPx(30), ViewUtils.dpToPx(30),
                        ContextCompat.getDrawable(itemView.context, R.drawable.placeholder))
                binding.gameNameTextView.text = item.name
            }
            itemView.setOnClickListener {
                onItemClicked?.invoke(item)
            }
        }

    }
}