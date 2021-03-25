package com.rheotv.android.ui.activities.player.activity.newPlayer.fragments

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentTopFansBinding
import com.rheotv.android.ui.activities.player.activity.newPlayer.TopFansViewModel
import com.rheotv.android.ui.activities.player.activity.newPlayer.adapter.TopFansRecyclerAdapter
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment
import com.rheotv.android.utils.*
import com.rheotv.android.utils.recyclerdecorators.VerticalLinearItemDecorationV2
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import javax.inject.Inject

class TopFansBottomSheet : BaseBottomSheetDialogFragment<FragmentTopFansBinding, TopFansViewModel>() {

    @Inject
    lateinit var mViewModel: TopFansViewModel

    @Inject
    lateinit var mAdapter: TopFansRecyclerAdapter

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_top_fans

    override fun getViewModel(): TopFansViewModel = mViewModel.also {
        it.username = arguments?.getString(AppConstants.USER_NAME)
        arguments?.getString(AppConstants.SCREEN_SOURCE)?.let { string -> it.baseProperties[AppConstants.SCREEN_SOURCE] = string }
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val lp = view.layoutParams as? ViewGroup.MarginLayoutParams
                    ?: ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT)
            lp.height = (ViewUtils.getScreenHeightInPx(view.context) / 2).toInt()
            view.layoutParams = lp
        }
        adjustWindow(view)

        viewDataBinding.headerTextView.text = getString(R.string.top_fans_of, viewModel.username)
//        val spannable = SpannableString(getString(R.string.top_fan_tip) + "  " + getString(R.string.top_fan_info))
//        AppUtilsKt.increaseFontSizeForPath(spannable, getString(R.string.top_fan_tip), 2f, Color.WHITE)
//        viewDataBinding.tipTextView.text = spannable

        with(viewDataBinding.recyclerView) {
            addItemDecoration(VerticalLinearItemDecorationV2(ViewUtils.dpToPx(12)))

            adapter = mAdapter.also {
                it.screenName = mViewModel.baseProperties[AppConstants.SCREEN_SOURCE].toString()
                it.onItemSelectedListener { topFan ->
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, mapOf(
                            "is_first" to CommonUtils.isFirstTimeFollow(),
                            "author" to topFan.user?.username,
                            "source" to "TopFansPage"
                    ))
                    mViewModel.followUser(topFan) {
                        topFan.isFollowed = topFan.isFollowed == false
                        mAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        viewDataBinding.errorTextView.setOnClickListener {
            mViewModel.fetchTopFans()
        }
        viewDataBinding.loadingState = mViewModel.viewStatus
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mViewModel.fansLiveData.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            mAdapter.submitList(it)
        })
        mViewModel.fetchTopFans()
    }

    companion object {
        const val TAG = "TopFansBottomSheet"
        fun newInstance(username: String?, screenName: String?) = TopFansBottomSheet().also {
            it.arguments = Bundle().apply {
                screenName?.let { string -> putString(AppConstants.USER_NAME, string.trim()) }
                username?.let { string -> putString(AppConstants.USER_NAME, string.trim()) }
            }
        }
    }
}