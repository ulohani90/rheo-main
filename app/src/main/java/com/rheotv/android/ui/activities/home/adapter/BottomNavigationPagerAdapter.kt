package com.rheotv.android.ui.activities.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class BottomNavigationPagerAdapter(val fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val mList: MutableList<Fragment> = mutableListOf()
    override fun getItem(position: Int): Fragment = mList[position]

    override fun getCount(): Int = mList.size
    fun addFragment(fragment: Fragment) {
        mList.add(fragment)
    }

}