package com.rheotv.android.ui.activities.profile.viewprofile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.databinding.ListItemGameRuleBinding
import com.rheotv.android.ui.activities.profile.model.SocialMedia
import com.rheotv.android.ui.activities.profile.viewprofile.model.GameRule
import com.rheotv.android.ui.activities.profile.viewprofile.utils.UserAction
import com.rheotv.android.ui.base.BaseViewHolder

class GameRuleAdapter : RecyclerView.Adapter<BaseViewHolder>() {
    private var list: MutableList<GameRule> = ArrayList()
    private var allowEdit: Boolean = false
    var onItemClick: ((GameRule, UserAction) -> Unit)? = null

    fun submitList(list: MutableList<GameRule>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun addRule(gameRule: GameRule) {
        this.list.add(gameRule)
        notifyItemInserted(list.size)
    }

    fun onUserAction(pair: Pair<UserAction, GameRule>) {
        try {
            when (pair.first) {
                UserAction.Add -> {
                    list.add(pair.second)
                    notifyItemInserted(list.size)
                }

                else -> {
                    val index = list.indexOf(pair.second)
                    list.removeAt(index)
                    notifyItemRemoved(index)
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    fun toggleAllowEdit() {
        allowEdit = !allowEdit
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return GameWiseUserViewHolder(
                ListItemGameRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
       holder.onBind(position)
    }

    inner class GameWiseUserViewHolder (val binding: ListItemGameRuleBinding): BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            with(binding) {
                val r = list[position]
                rule = r
                inEditMode = allowEdit
                deleteButton.setOnClickListener {
                    onItemClick?.invoke(r, UserAction.Delete)
                }
            }
        }
    }
}