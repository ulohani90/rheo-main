package com.rheotv.android.ui.activities.profile.viewprofile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import com.rheotv.android.R
import com.rheotv.android.databinding.ListItemOnlinePresenceBinding
import com.rheotv.android.ui.activities.profile.model.SocialMedia
import java.util.*

class OnlinePresenceSpinnerAdapter : BaseAdapter() {
    private val list = ArrayList<SocialMedia>()

    init {
        list.add(0, SocialMedia(name = "Select Social Media"))
    }

    fun submitList(list: List<SocialMedia>?) {
        list?.apply {
            this@OnlinePresenceSpinnerAdapter.list.clear()
            this@OnlinePresenceSpinnerAdapter.list.add(0, SocialMedia(name = "Select Social Media"))
            this@OnlinePresenceSpinnerAdapter.list.addAll(this)
        }

    }

    override fun getCount() = list.size

    override fun getItem(i: Int) = list[i]

    override fun getItemId(i: Int) = i.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        if (convertView == null)
            DataBindingUtil.inflate<ListItemOnlinePresenceBinding>(
                    LayoutInflater.from(parent?.context), R.layout.list_item_online_presence, parent, false
            ).also { it.media = list[position] }.run { return this.root }
        return convertView
    }
}