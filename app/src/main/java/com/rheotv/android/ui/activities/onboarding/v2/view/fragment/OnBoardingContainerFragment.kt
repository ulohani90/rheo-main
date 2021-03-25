package com.rheotv.android.ui.activities.onboarding.v2.view.fragment

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import android.widget.MediaController
import android.widget.Scroller
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentOnBoardingContainerBinding
import com.rheotv.android.ui.activities.onboarding.v2.adapter.OnBoardingPagerAdapter
import com.rheotv.android.ui.activities.onboarding.v2.viewmodel.OnBoardingViewModel
import com.rheotv.android.ui.activities.selectGame.LanguageSelectionFragment
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.GrayscaleTransformation
import java.lang.reflect.Field
import javax.inject.Inject

class OnBoardingContainerFragment : BaseFragment<FragmentOnBoardingContainerBinding, OnBoardingViewModel>(), ViewPager.PageTransformer, ViewPager.OnPageChangeListener {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private var mListener: LanguageSelectionFragment.LanguageSelectionListener? = null

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_on_boarding_container

    override fun getViewModel(): OnBoardingViewModel =
            ViewModelProvider(this, mViewModelFactory)[OnBoardingViewModel::class.java]

    override fun transformPage(view: View, position: Float) {
        val pageWidth = view.width
        when {
            position < -1 -> view.alpha = 0f
            position <= 1 -> {

                view.findViewById<View?>(R.id.header_text_view)?.translationX = -(pageWidth * position)
//                view.findViewById<View?>(R.id.tv_app_subtitle)?.translationX = -(pageWidth * position)
//
//                view.findViewById<View?>(R.id.userView)?.translationX = pageWidth * position
//                view.findViewById<View?>(R.id.tv_user_label)?.translationX = pageWidth * position
//                view.findViewById<View?>(R.id.view_user_underline)?.translationX = pageWidth * position
//                view.findViewById<View?>(R.id.profileImage)?.translationX = pageWidth * position
//
//                view.findViewById<View?>(R.id.passView)?.translationX = pageWidth * position
//                view.findViewById<View?>(R.id.tv_pass_label)?.translationX = pageWidth * position
//                view.findViewById<View?>(R.id.view_pass_underline)?.translationX = pageWidth * position
//                view.findViewById<View?>(R.id.iv_pass_icon)?.translationX = pageWidth * position
//
//                view.findViewById<View?>(R.id.commandButton)?.translationX = -(pageWidth * position)
            }
            else -> view.alpha = 0f
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isReLogin = arguments?.getBoolean(AppConstants.ARG_IS_RELOGIN, false) == true
        viewDataBinding?.viewPager?.isPagingEnabled = false
        viewDataBinding?.viewPager?.adapter = OnBoardingPagerAdapter(childFragmentManager, isReLogin)
        try {
            val mScroller: Field = ViewPager::class.java.getDeclaredField("mScroller")
            mScroller.isAccessible = true
            val scroller = FixedSpeedScroller(viewDataBinding?.viewPager?.context)
            // scroller.setFixedDuration(5000);
            mScroller.set(viewDataBinding?.viewPager, scroller)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        viewDataBinding?.viewPager?.offscreenPageLimit = 4
        viewDataBinding?.viewPager?.addOnPageChangeListener(this)
        if (isReLogin) {
            viewDataBinding?.viewPager?.currentItem = 1

        }
//        viewDataBinding?.viewPager?.setPageTransformer(false, this)
//        viewDataBinding.viewPager.isUserInputEnabled = false
//        viewDataBinding.viewPager.setPageTransformer(ParallaxTransformer())
        viewDataBinding?.viewState = viewModel.viewState
        viewDataBinding?.nextButton?.setOnClickListener {
            if (CommonUtils.isOnBoardClipEnabled() && viewDataBinding?.demoVideoView?.isPlaying == true)
                viewDataBinding?.demoVideoView?.stopPlayback()
            when (viewDataBinding?.viewPager?.currentItem) {
                0 -> viewModel.userActionLiveData.value = OnBoardingViewModel.UserAction.SubmitLanguage
                2 -> viewModel.userActionLiveData.value = OnBoardingViewModel.UserAction.UsernameAdded
                3 -> viewModel.userActionLiveData.value = OnBoardingViewModel.UserAction.SubmitTopShow
            }
        }
        viewDataBinding?.backButton?.setOnClickListener {
            if ((viewDataBinding?.viewPager?.currentItem ?: 0) == 3 && CommonUtils.isTopShowUser())
                viewDataBinding?.viewPager?.currentItem = (viewDataBinding?.viewPager?.currentItem
                        ?: 1) - 3
            else if ((viewDataBinding?.viewPager?.currentItem ?: 0) > 0) {
                viewDataBinding?.viewPager?.currentItem = (viewDataBinding?.viewPager?.currentItem
                        ?: 1) - 1

            }
        }

        Picasso.get().load(R.drawable.ic_header_bg)
                .config(Bitmap.Config.RGB_565)
                .transform(GrayscaleTransformation())
                .into(viewDataBinding?.topImageViewBg)
        //val mediaController = MediaController(context)
        //mediaController.setAnchorView(viewDataBinding?.demoVideoView)

        //specify the location of media file

        //specify the location of media file
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val width = (viewDataBinding?.topImageViewBg?.width ?: 0)
        val x = ((width * position + positionOffsetPixels) * computeFactor())
//        viewDataBinding?.scrollView?.scrollTo(x.toInt() + width / 3, 0)
    }

    override fun onPageSelected(position: Int) {
        if (CommonUtils.isOnBoardClipEnabled()) {
            if (position == 0) {
                viewDataBinding.demoVideoView.visibility = View.VISIBLE
                viewDataBinding.demoProtectorView.visibility = View.VISIBLE
            } else {
                viewDataBinding.demoVideoView.visibility = View.GONE
                viewDataBinding.demoProtectorView.visibility = View.GONE
                setUpVideoBackground()
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    private fun computeFactor(): Float {
        val width = (viewDataBinding?.topImageViewBg?.width ?: 0)
        return (width / 2 - width) / (width * (viewDataBinding?.viewPager?.adapter?.count
                ?: 0)).toFloat()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.userActionLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is OnBoardingViewModel.UserAction.HideNextButton -> viewDataBinding?.nextButton?.visibility = View.GONE
                is OnBoardingViewModel.UserAction.ShowNextButton -> viewDataBinding?.nextButton?.visibility = View.VISIBLE
                is OnBoardingViewModel.UserAction.HideBackButton -> viewDataBinding?.backButton?.visibility = View.GONE
                is OnBoardingViewModel.UserAction.ShowBackButton -> viewDataBinding?.backButton?.visibility = View.VISIBLE
                is OnBoardingViewModel.UserAction.EnableNextButton -> {
                    viewDataBinding?.nextButton?.visibility = View.VISIBLE
                    viewDataBinding?.nextButton?.isEnabled = true
                }
                is OnBoardingViewModel.UserAction.DisableNextButton -> {
                    viewDataBinding?.nextButton?.visibility = View.VISIBLE
                    viewDataBinding?.nextButton?.isEnabled = false
                }
                is OnBoardingViewModel.UserAction.LanguageUpdated -> viewDataBinding?.viewPager?.setCurrentItem(1, true)
                is OnBoardingViewModel.UserAction.AskUsername -> {
                    viewDataBinding?.viewPager?.setCurrentItem(2, true)
                }
                is OnBoardingViewModel.UserAction.LoginSuccess -> {
                    if (CommonUtils.isTopShowUser()) {
                        if (viewModel.hasTopShows)
                            viewDataBinding?.viewPager?.setCurrentItem(3, true)
                        else {
                            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_NO_TOP_SHOWS_AVAILABLE, hashMapOf())
                            mListener?.onLanguageUpdated()
                        }
                    } else
                        mListener?.onLanguageUpdated()
                }
                is OnBoardingViewModel.UserAction.TopShowSelection -> {
                    mListener?.onLanguageUpdated()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (CommonUtils.isOnBoardClipEnabled()) {
            setUpVideoBackground()
        }
    }

    private fun setUpVideoBackground() {
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_ONBOARDING_OPEN_VIDEO_SHOWN, hashMapOf())
        val uri: Uri = Uri.parse("android.resource://" + context?.packageName + "/" + R.raw.onboarding_demo_video)
        //Setting MediaController and URI, then starting the videoView
        //Setting MediaController and URI, then starting the videoView
        //viewDataBinding?.demoVideoView?.setMediaController(mediaController)
        viewDataBinding?.demoVideoView?.setVideoURI(uri)
        viewDataBinding?.demoVideoView?.requestFocus()
        viewDataBinding?.demoVideoView?.start()
        viewDataBinding?.demoVideoView?.setOnPreparedListener { mp ->
            mp.setVolume(0.0f, 0.0f)
            mp.isLooping = true
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LanguageSelectionFragment.LanguageSelectionListener) {
            mListener = context
        }
    }

    inner class ParallaxTransformer : ViewPager2.PageTransformer {
        override fun transformPage(page: View, position: Float) {
//            if (position >= -1 && position <= 1) {
//                (page as? ViewGroup)?.findViewById<View?>(R.id.top_image_view_bg)?.translationX = -position * page.width / 2;
//            } else {
//                page.alpha = 1f
//            }
//            val parallaxView = (page as? ViewGroup)?.findViewById<View?>(R.id.top_image_view)
            val parallaxView = viewDataBinding.topImageViewBg
            Log.i("OnBoardingContainer", "position ---> $position")
            Log.i("OnBoardingContainer", "translation ---> ${(position * (parallaxView?.width ?: 0))}")
            if (position > -1f && position <= 1f) {
                val width: Int = parallaxView?.width ?: 0

                parallaxView?.translationX = -(position * width / 3)
//                if (position == 0f) {
//                    page.scaleX = 1f
//                    page.scaleY = 1f
//                } else {
                parallaxView?.scaleX = 1.5f
                parallaxView?.scaleY = 1.5f
//                }
            }
        }

    }

    companion object {
        fun newInstance(source: String?, isReLogin: Boolean?) = OnBoardingContainerFragment().also {
            it.arguments = Bundle().apply {
                putString(AppConstants.SCREEN_SOURCE, source)
                putBoolean(AppConstants.ARG_IS_RELOGIN, isReLogin == true)
            }
        }
    }

    class PagTransformer : ViewPager.PageTransformer {
        override fun transformPage(page: View, position: Float) {

        }

    }
}

class OnBoardingViewPager : ViewPager {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    var isPagingEnabled = true

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return isPagingEnabled && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return isPagingEnabled && super.onInterceptTouchEvent(ev)
    }
}

class FixedSpeedScroller : Scroller {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, interpolator: Interpolator?) : super(context, interpolator)
    constructor(context: Context?, interpolator: Interpolator?, flywheel: Boolean) : super(context, interpolator, flywheel)

    private val mDuration = 1500

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
        super.startScroll(startX, startY, dx, dy, mDuration)
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        Log.i("FixedSpeedScroller", "duration --> $duration")
        super.startScroll(startX, startY, dx, dy, mDuration)
    }
}