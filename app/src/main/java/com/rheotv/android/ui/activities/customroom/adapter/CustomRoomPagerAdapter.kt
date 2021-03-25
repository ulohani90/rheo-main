package com.rheotv.android.ui.activities.customroom.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.rheotv.android.ui.activities.customroom.view.CustomRoomFragment
import com.rheotv.android.ui.activities.customroom.view.CustomRoomDetailFragment
import com.rheotv.android.ui.activities.share.CustomFragmentPagerAdapter
import com.rheotv.android.utils.AdapterFragmentItem

class CustomRoomPagerAdapter(private val source: String, fragmentManager: FragmentManager, lifeCycle: Lifecycle) : CustomFragmentPagerAdapter(fragmentManager, lifeCycle) {

    private val mList: MutableList<AdapterFragmentItem> = mutableListOf()

    init {
        mList.addAll(getDefault(source))
    }

    override fun updateList(list: List<AdapterFragmentItem>) = Unit

    override fun getItemAtPosition(position: Int): AdapterFragmentItem = mList[position]

    override fun getItemCount(): Int = mList.size

    override fun createFragment(position: Int): Fragment =
            if (position < mList.size) mList[position].fragment else Fragment()


    companion object {
        fun getDefault(source: String): MutableList<AdapterFragmentItem> = mutableListOf(
                AdapterFragmentItem(CustomRoomFragment.newInstance(source), "Custom Room"),
                AdapterFragmentItem(CustomRoomDetailFragment.newInstance(source), "Custom Room Players")
        )
    }
}