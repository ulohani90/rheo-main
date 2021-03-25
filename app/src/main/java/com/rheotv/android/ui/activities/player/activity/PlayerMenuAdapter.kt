package com.rheotv.android.ui.activities.player.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.R
import com.rheotv.android.databinding.MenuListItemBinding
import com.rheotv.android.ui.base.BaseViewHolder

class PlayerMenuAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    private val mList: MutableList<ListOption> = mutableListOf()
    private var mListener: ((option: ListOption) -> Unit)? = null
    private var mHeaderView: ((option: ListOption.Header, root: ViewGroup) -> View?)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (viewType == VIEW_TYPE_HEADER) {
            return HeaderViewHolder(FrameLayout(parent.context).also {
                it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            })
        }
        return ItemViewHolder(MenuListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemViewType(position: Int): Int =
            if (mList[position] is ListOption.Header) {
                VIEW_TYPE_HEADER
            } else {
                VIEW_TYPE_ITEM
            }

    fun submitList(listOptions: List<ListOption>) {
        mList.clear()
        mList.addAll(listOptions)
        notifyDataSetChanged()
    }

    fun setClickListener(listener: ((option: ListOption) -> Unit)?) {
        mListener = listener
    }

    fun setHeaderViewCallback(listener: ((option: ListOption.Header, root: ViewGroup) -> View?)?) {
        mHeaderView = listener
    }

    inner class ItemViewHolder(val binding: MenuListItemBinding) : BaseViewHolder(binding.root) {

        override fun onBind(position: Int) {
            val option: ListOption.Item = mList[position] as ListOption.Item
            binding.textView.text = option.text
            if (option.imageResourceId != -1) {
                binding.icon.setImageResource(option.imageResourceId)
            } else if (option.imageResource != null) {
                binding.icon.setImageDrawable(option.imageResource)
            }
            binding.root.setOnClickListener { mListener?.invoke(mList[position]) }
        }
    }

    inner class HeaderViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun onBind(position: Int) {
            try {
                (itemView as? ViewGroup)?.addView(mHeaderView?.invoke(mList[position] as ListOption.Header, itemView))
                itemView.setOnClickListener { mListener?.invoke(mList[position]) }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0x0000
        private const val VIEW_TYPE_ITEM = 0x0002
    }
}