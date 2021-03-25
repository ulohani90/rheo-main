package com.rheotv.android.ui.activities.profile.viewprofile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.data.network.models.useProfile.responses.GameWiseUser
import com.rheotv.android.databinding.ListItemUserGameBinding
import com.rheotv.android.ui.activities.profile.viewprofile.utils.UserAction
import com.rheotv.android.ui.base.BaseViewHolder
import java.lang.IndexOutOfBoundsException

class GameWiseUserAdapter : RecyclerView.Adapter<BaseViewHolder>() {
    private val list: MutableList<GameWiseUser> = ArrayList()
    private var isInEditMode = false
    var onDeleteClick: ((GameWiseUser) -> Unit)? = null

    fun submitList(list: MutableList<GameWiseUser>?) {
        this.list.clear()
        list?.apply {
            this@GameWiseUserAdapter.list.addAll(this)
        }
        notifyDataSetChanged()
    }

    fun onUserAction(pair: Pair<UserAction, GameWiseUser>?) {
        if (list.isEmpty()) return
        pair?.apply {
            when (first) {
                UserAction.Add -> {
                    list.add(second)
                    notifyItemInserted(list.size)
                }

                else -> {
                    val index = list.indexOf(second)
                    if (index == -1) return@apply
                    list.removeAt(index)
                    notifyItemRemoved(index)
                }
            }
        }
    }

    fun toggleEditMode() {
        isInEditMode = !isInEditMode
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return GameWiseUserViewHolder(
                ListItemUserGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class GameWiseUserViewHolder(val binding: ListItemUserGameBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            with(binding) {
                val item = list[position]
                game = item
                inEditMode = isInEditMode
                deleteButton.setOnClickListener {
                    onDeleteClick?.invoke(item)
                }
            }
        }
    }
}