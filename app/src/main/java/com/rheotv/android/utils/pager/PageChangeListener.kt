package com.rheotv.android.utils.pager

/**
 * A callback interface that must be implemented to set selected and un-selected page
 */
interface PageChangeListener {
    fun onPageSelected(position: Int)

    fun onPageUnselected(position: Int)
}
