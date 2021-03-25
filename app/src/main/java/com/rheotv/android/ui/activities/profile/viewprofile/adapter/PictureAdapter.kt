package com.rheotv.android.ui.activities.profile.viewprofile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.databinding.ListItemPictureBinding
import com.rheotv.android.ui.base.BaseViewHolder

class PictureAdapter : RecyclerView.Adapter<BaseViewHolder>() {
    var list: List<String> = ArrayList()

    fun submitList(list: List<String>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return PictureViewHolder(ListItemPictureBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class PictureViewHolder(val binding: ListItemPictureBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            binding.url = list[position]
        }
    }
}