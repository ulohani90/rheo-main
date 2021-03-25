package com.rheotv.android.ui.activities.onboarding.v2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.databinding.ListItemStreamSelectionBinding
import com.rheotv.android.ui.activities.onboarding.v2.model.ShowData
import com.rheotv.android.ui.activities.onboarding.v2.model.StreamerShow
import com.rheotv.android.ui.base.BaseViewHolder

class TopStreamerSelectionAdapter(val onItemSelect: (ShowData) -> Unit) : RecyclerView.Adapter<BaseViewHolder>() {
    var list = ArrayList<ShowData>()
    val selection =  hashMapOf<Int?, ShowData?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return StreamerViewHolder(
                ListItemStreamSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = list.size

    fun submitList(list: List<ShowData>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun getSelectionId(): List<Int?> {
        return selection.keys.toList()
    }

    inner class StreamerViewHolder(val binding: ListItemStreamSelectionBinding) : BaseViewHolder(binding.root) {

        override fun onBind(position: Int) {
            val streamShow =  list[position]
                if (position == 0)
                    binding.isDayVisible = true
                else binding.isDayVisible = !streamShow.getDay().equals(list[position - 1].getDay(), true)
            with(binding) {
                show = streamShow
                root.setOnClickListener {
                    if (selection.containsKey(position)) {
                        selection.remove(position)
                    } else {
                        selection[position] = streamShow
                    }

                    binding.isSelected = !(binding.isSelected ?: false)
                    onItemSelect.invoke(streamShow)
                }
            }
        }
    }
}