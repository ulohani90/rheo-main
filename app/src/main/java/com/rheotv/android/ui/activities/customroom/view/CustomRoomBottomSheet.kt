package com.rheotv.android.ui.activities.customroom.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.data.network.models.postlisting.responses.Result
import com.rheotv.android.databinding.BottomSheetCustomRoomBinding
import com.rheotv.android.ui.activities.customroom.adapter.CustomRoomPagerAdapter
import com.rheotv.android.ui.activities.customroom.model.CustomRoomUserAction
import com.rheotv.android.ui.activities.customroom.viewmodel.CustomRoomViewModel
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment
import com.rheotv.android.utils.ViewUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import javax.inject.Inject

class CustomRoomBottomSheet : BaseBottomSheetDialogFragment<BottomSheetCustomRoomBinding, CustomRoomViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private var mPagerAdapter: CustomRoomPagerAdapter? = null

    private var mScreenSource: String? = null
    private lateinit var mPost: Result

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.bottom_sheet_custom_room

    override fun getViewModel(): CustomRoomViewModel {
        return ViewModelProvider(this, mViewModelFactory).get(CustomRoomViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutParams = viewDataBinding.root.layoutParams
                ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.height = (ViewUtils.getScreenHeightInPx(context) * 0.6).toInt()
        viewDataBinding.root.layoutParams = layoutParams
        if (this::mPost.isInitialized)
            viewModel.post = mPost
        viewDataBinding.errorTextView.visibility = View.GONE
        mPagerAdapter = CustomRoomPagerAdapter(SegmentConstants.SCREEN_NAME_REQUEST_TO_PLAY, childFragmentManager, lifecycle)
        viewDataBinding.customRoomViewPager.adapter = mPagerAdapter
        viewDataBinding.customRoomViewPager.isUserInputEnabled = false
        viewModel.progressLiveData.observe(viewLifecycleOwner, Observer {
            viewDataBinding.loader.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.customRoomUserAction.observe(viewLifecycleOwner, Observer {
            val action = it ?: return@Observer
            if (action == CustomRoomUserAction.AddCustomRoomClick || action == CustomRoomUserAction.CustomRoomViewClick) {
                viewDataBinding.customRoomViewPager.setCurrentItem(1, true)
            } else if (action == CustomRoomUserAction.DetailPageBackClick) {
                viewModel.selectedRoomId = null
                viewDataBinding.customRoomViewPager.setCurrentItem(0, true)
            }
        })
    }

    companion object {

        const val TAG: String = "CustomRoomBottomSheet"

        fun newInstance(screenSource: String, post: Result): CustomRoomBottomSheet =
                CustomRoomBottomSheet().also {
                    it.mScreenSource = screenSource
                    it.mPost = post
                }
    }
}