package com.rheotv.android.utils.pager

import android.util.Log
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.rheotv.android.utils.ViewUtils
import com.rheotv.android.utils.viewpager2.transformer.TranslateEdgeToEdgeTransformerV1
import java.lang.ref.WeakReference

class ViewPagerOneMediator(private val viewPager: ViewPager,
                    private val tabLayout: TabLayout,
                    private val adapter: FragmentStatePagerAdapter,
                    private val startPosition: Int = 0,
                    private val onPageChangeListener: PageChangeListener? = null
) {
    private var onTabChangeListener: TabLayout.OnTabSelectedListener? = null
    fun attach() {
        onTabChangeListener = FragmentOnTabChangeCallback(adapter)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        viewPager.clipChildren = false
        viewPager.clipToPadding = false
        viewPager.offscreenPageLimit = 3
        viewPager.setPageTransformer(false, TranslateEdgeToEdgeTransformerV1(ViewUtils.dpToPx(10), ViewUtils.dpToPx(20)))
        onTabChangeListener?.let { tabLayout.addOnTabSelectedListener(it) }
        viewPager.setCurrentItem(startPosition, false)
    }

    /**
     * Unlink the ViewPager callback
     */
    fun detach() {
        onTabChangeListener?.let { tabLayout.removeOnTabSelectedListener(it) }
        onTabChangeListener = null
    }

    private inner class FragmentOnTabChangeCallback(adapter: FragmentStatePagerAdapter) : TabLayout.OnTabSelectedListener {
        private val adapterRef: WeakReference<FragmentStatePagerAdapter> = WeakReference(adapter)

        override fun onTabReselected(tab: TabLayout.Tab?) = onTabSelected(tab)

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            onPageChangeListener?.onPageUnselected(tab?.position ?: 0)
            (adapter.getItem(tab?.position
                    ?: 0) as? PageChangeListener)?.onPageUnselected(tab?.position ?: 0)
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            val adapter = adapterRef.get()

            Log.i(javaClass.simpleName, "pager_change: position ${tab?.position} and ${tab?.position?.minus(1)}")

            (adapter?.getItem(tab?.position
                    ?: 0) as? PageChangeListener)?.onPageSelected(tab?.position ?: 0)
            onPageChangeListener?.onPageSelected(tab?.position ?: 0)
        }
    }
}