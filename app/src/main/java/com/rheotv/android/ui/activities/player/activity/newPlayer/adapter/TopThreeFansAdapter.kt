package com.rheotv.android.ui.activities.player.activity.newPlayer.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.data.network.models.postlisting.responses.TopFans
import com.rheotv.android.databinding.ListItemTopThreeFansBinding
import com.rheotv.android.ui.base.BaseViewHolder

class TopThreeFansAdapter : RecyclerView.Adapter<BaseViewHolder>() {
    var list: List<TopFans> = ArrayList()

    fun submitList(list: List<TopFans>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return FanAdapter(ListItemTopThreeFansBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class FanAdapter(val binding: ListItemTopThreeFansBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            binding.fan = list[position]
            Log.i(javaClass.simpleName, "FanAdapter: ${list[position].profilePic}")
        }
    }
}