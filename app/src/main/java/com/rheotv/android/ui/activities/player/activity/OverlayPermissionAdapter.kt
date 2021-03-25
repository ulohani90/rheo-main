package com.rheotv.android.ui.activities.player.activity

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.rheotv.android.R
import com.rheotv.android.databinding.LayoutOverlayPermissionItemBinding

class OverlayPermissionAdapter : PagerAdapter() {
    private var list = ArrayList<Drawable?>()

    fun submitList(items: List<Drawable?>) {
        list.addAll(items)
        notifyDataSetChanged()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding: LayoutOverlayPermissionItemBinding = DataBindingUtil.inflate(LayoutInflater.from(container.context), R.layout.layout_overlay_permission_item, container, false)
        binding.imageView.setImageDrawable(list[position])
        container.addView(binding.root)
        return binding.root
    }

    override fun getCount(): Int {
        return this.list.size
    }

    override fun isViewFromObject(@NonNull view: View, @NonNull `object`: Any): Boolean {
        return view === `object`
    }

}