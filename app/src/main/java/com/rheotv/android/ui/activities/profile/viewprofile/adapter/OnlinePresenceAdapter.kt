package com.rheotv.android.ui.activities.profile.viewprofile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.data.network.models.useProfile.responses.GameWiseUser
import com.rheotv.android.ui.activities.profile.model.SocialMedia
import com.rheotv.android.databinding.ListItemEnterSocialMediaBinding
import com.rheotv.android.databinding.ListItemSocialMediaPresenceBinding
import com.rheotv.android.ui.activities.profile.viewprofile.utils.UserAction
import com.rheotv.android.ui.base.BaseViewHolder

class OnlinePresenceAdapter : RecyclerView.Adapter<BaseViewHolder>() {
    private var list: MutableList<SocialMedia> = ArrayList()
    private var inEdit = true
    private var allowEdit = false
    var onItemClick: ((SocialMedia, UserAction) -> Unit)? = null

    fun setInEdit(state: Boolean) {
        this.inEdit = state
        notifyDataSetChanged()
    }

    fun submitList(list: MutableList<SocialMedia>?) {
        this.list.clear()
        list?.apply {
            this@OnlinePresenceAdapter.list.addAll(this)
        }
        notifyDataSetChanged()
    }

    fun addItem(item: SocialMedia) {
        this.list.add(item)
        notifyItemInserted(list.size)
    }

    fun onUserAction(pair: Pair<UserAction, SocialMedia>?) {
        pair?.apply {
            when (first) {
                UserAction.Add -> {
                    list.add(second)
                    notifyItemInserted(list.size)
                }

                else -> {
                    val index = list.indexOf(second)

                    list.removeAt(index)
                    notifyItemRemoved(index)
                }
            }
        }
    }

    fun toggleAllowEdit() {
        allowEdit = !allowEdit
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (inEdit) {
            false -> OnlinePresenceViewHolder(
                    ListItemSocialMediaPresenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> EnterOnlinePresenceViewHolder(
                    ListItemEnterSocialMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class OnlinePresenceViewHolder(val binding: ListItemSocialMediaPresenceBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            with(binding) {
                val item = list[position]
                imageUrl = item.logo
                root.setOnClickListener {
                    onItemClick?.invoke(item, UserAction.Add)
                }
            }
        }
    }

    inner class EnterOnlinePresenceViewHolder(val binding: ListItemEnterSocialMediaBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            with(binding) {
                val item = list[position]
                media = item
                inEditMode = allowEdit
                deleteButton.setOnClickListener {
                    onItemClick?.invoke(item, UserAction.Delete)
                }
            }
        }
    }
}