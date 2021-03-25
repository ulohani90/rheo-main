package com.rheotv.android.ui.activities.profile.editprofile.view

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.ViewpagerEditprofileFeaturedphotoBinding
import com.rheotv.android.ui.activities.home.view.HomeActivity
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.ViewPagerOnFeaturedPhoto
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.ViewUtils
import com.rheotv.android.utils.addBackPressCallback
import com.rheotv.android.utils.navController
import javax.inject.Inject

class FeaturedPhotoFragment : BaseFragment<ViewpagerEditprofileFeaturedphotoBinding, UserProfileViewModel>()
{
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var featuredPhotoAdapter: ViewPagerOnFeaturedPhoto

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.viewpager_editprofile_featuredphoto

    override fun getViewModel()= ViewModelProvider(parentFragment?.parentFragment
            ?: this, viewModelFactory).get(UserProfileViewModel::class.java)
            .also {
                it.source = arguments?.getString(AppConstants.SCREEN_SOURCE)
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBackPressCallback()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var pos=0
        if(arguments?.containsKey("photoPosition")!!)
            pos= arguments?.getString("photoPosition")?.toInt() ?: 0
        else
            arguments?.let {pos=FeaturedPhotoFragmentArgs.fromBundle(it).photoPosition}
        if(activity is HomeActivity) {
            (viewDataBinding.viewPager.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
                it.bottomMargin = ViewUtils.dpToPx(70)
            }
            (viewDataBinding.actionBack.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
                it.topMargin = ViewUtils.dpToPx(24)

            }
        }
        else {
            (viewDataBinding.viewPager.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
                it.bottomMargin = ViewUtils.dpToPx(10)
            }
            (viewDataBinding.actionBack.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
                it.topMargin = ViewUtils.dpToPx(28)

            }
        }

        viewDataBinding.actionBack.setOnClickListener { navController()?.popBackStack() }
        viewDataBinding.viewPager.adapter = featuredPhotoAdapter
        Handler().post(Runnable { viewDataBinding.viewPager.setCurrentItem(pos,false) })
        featuredPhotoAdapter.submitList(viewModel.photoUrls)
        featuredPhotoAdapter.submitPhotos(viewModel.featuredPhotos)
        featuredPhotoAdapter.setPosition(pos)
    }

}