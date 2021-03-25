package com.rheotv.android.ui.customViews.shareMenu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.databinding.ListItemOptionBinding
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest

class ShareOptionAdapter(
        private val callback: ((OptionRequest) -> Unit)?
) : RecyclerView.Adapter<BaseViewHolder>() {
    private var list: List<OptionRequest> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            OptionHolder(ListItemOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    fun submitList(list: List<OptionRequest>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class OptionHolder(private val binding: ListItemOptionBinding) : BaseViewHolder(binding.root) {

        override fun onBind(position: Int) {
            binding.option = list[position]
            binding.executePendingBindings()
            binding.root.setOnClickListener { callback?.invoke(list[position]) }
        }
    }
}