package com.rheotv.android.ui.activities.player.activity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class CustomFragmentStateAdapter<T>(fragmentManager: FragmentManager, lifecycle: Lifecycle)
    : FragmentStateAdapter(fragmentManager, lifecycle), OnItemAdded<T> {
    abstract fun getItem(position: Int): Fragment?
    abstract fun getIdAt(position: Int): Long?
    abstract fun getPositionForId(Id: String?): Int
    open fun clearList() = Unit
    open fun containsId(Id: String?): Boolean = false
    open fun removeItem(positionToBeDeleted: Int) = Unit
    open fun addListItem(item: T, currentPosition: Int) = Unit
    open fun removeListItem(item: T) = Unit
    open fun addItemAt(item: T, position: Int) = Unit
}