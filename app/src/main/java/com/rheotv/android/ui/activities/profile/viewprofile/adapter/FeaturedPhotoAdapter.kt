package com.rheotv.android.ui.activities.profile.viewprofile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.databinding.ListItemAddPhotoBinding
import com.rheotv.android.databinding.ListItemFeaturedPhotoBinding
import com.rheotv.android.ui.activities.profile.model.FeaturedPhoto
import com.rheotv.android.ui.adapters.ClipsListAdapter.OnClipCardItemsClick
import com.rheotv.android.ui.base.BaseViewHolder

class FeaturedPhotoAdapter : RecyclerView.Adapter<BaseViewHolder>() {
    private var editMode: Boolean = true
    private val limit: Int = 8 //8+1=9
    var list: MutableList<FeaturedPhoto> = ArrayList()
    var onAddItem: (() -> Unit)? = null
    var onFeaturedPhotoClick: ((Int) -> Unit)? = null

    public fun enableEditMode() {
        this.editMode = true
        notifyDataSetChanged()
    }

    public fun disableEditMode() {
        this.editMode = false
        notifyDataSetChanged()
    }

    fun submitList(list: MutableList<FeaturedPhoto>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun addItem(item: FeaturedPhoto?) {
        item ?: return
        if (list.size > limit) {
            disableEditMode()
            return
        }
        if(!(item in list)) {
            list.add(0, item)
            notifyDataSetChanged()
        }


    }

    fun deleteItem(item: FeaturedPhoto?) {
        var pos: Int = 0
        if (item != null) {
            for (items in list) {
                if (item.id == items.id && (item.isDelete || item.pictureUrl == null)) {
                    break
                } else
                    pos++
            }
           if(list.size>pos)
            list.removeAt(pos)
            notifyDataSetChanged()
        }
        if (list.size <= limit)
           enableEditMode()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD -> AddPhotoViewHolder(ListItemAddPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> FeaturedPhotoViewHolder(ListItemFeaturedPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (editMode && list.size <= limit) list.size + 1 else list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (editMode && list.isEmpty() && position == 0) VIEW_TYPE_ADD
        else if (editMode && position >= list.size) VIEW_TYPE_ADD
        else VIEW_TYPE_ITEM
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class AddPhotoViewHolder(val binding: ListItemAddPhotoBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            binding.root.setOnClickListener {
                onAddItem?.invoke()
            }

        }
    }


    inner class FeaturedPhotoViewHolder(val binding: ListItemFeaturedPhotoBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            if (!list[position].isDelete)
                binding.imageUrl = list[position].pictureUrl
            binding.root.setOnClickListener {
                onFeaturedPhotoClick?.invoke(position)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_ADD = 0x0000
        private const val VIEW_TYPE_ITEM = 0x0001
    }
}