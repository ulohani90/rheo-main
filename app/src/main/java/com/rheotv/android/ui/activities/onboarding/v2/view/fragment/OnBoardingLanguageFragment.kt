package com.rheotv.android.ui.activities.onboarding.v2.view.fragment

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.shape.ShapeAppearanceModel
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.databinding.FragmentOnBoardingLanguageBinding
import com.rheotv.android.ui.activities.onboarding.v2.viewmodel.OnBoardingViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.ViewUtils
import com.rheotv.android.utils.hourglass.Hourglass
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.GrayscaleTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.random.Random

class OnBoardingLanguageFragment : BaseFragment<FragmentOnBoardingLanguageBinding, OnBoardingViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private var initialLiveCount = 5100

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_on_boarding_language

    override fun getViewModel(): OnBoardingViewModel =
            ViewModelProvider(parentFragment ?: this,
                    mViewModelFactory)[OnBoardingViewModel::class.java]

    val timer = object : Hourglass(System.currentTimeMillis()) {
        private var mCounterCount = 0

        override fun onTimerFinish() {
            Log.i("", "onTimerFinished")
        }

        override fun onTimerTick(timeRemaining: Long, passedTime: Long) {
            if (!isAdded) return
            CoroutineScope(Dispatchers.Main).launch {
                if (!isAdded) return@launch
                mCounterCount++
                val updateValue = Random.nextInt(1, 5)
                initialLiveCount = if (mCounterCount % 3 == 0) initialLiveCount - updateValue else initialLiveCount + updateValue
                viewDataBinding?.headerTextView?.text = SpannableString(getString(R.string.placeholder_people_are_watching_live_now, DecimalFormat("##,###").format(initialLiveCount)))
                        .also {
                            it.setSpan(ForegroundColorSpan(ContextCompat.getColor(RheoTvApp.getNonUiContext(), R.color.text_color)),
                                    it.indexOf("Live", ignoreCase = true),
                                    it.indexOf("Live", ignoreCase = true) + "Live".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Picasso.get().load(R.drawable.login_bg_1)
                .config(Bitmap.Config.RGB_565)
                .transform(GrayscaleTransformation())
                .into(viewDataBinding.backgroundImageView)
        val spannableString = SpannableString(getString(R.string.placeholder_people_are_watching_live_now, initialLiveCount.let {
            initialLiveCount = Random.nextInt(5100, 5400)
            return@let DecimalFormat("##,###").format(initialLiveCount)
        }))
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(RheoTvApp.getNonUiContext(), R.color.text_color)), 0, 0, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        viewDataBinding?.headerTextView?.text = spannableString
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.languageLiveData.observe(viewLifecycleOwner, {
            for (item in it ?: return@observe) {
                viewDataBinding.languageChipGroup.addView(Chip(viewDataBinding?.root?.context, null, R.attr.chipChoiceStyle_NoPadding).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewUtils.dpToPx(52))
                    setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white_text_color)))
                    setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
                    textSize = 15f
                    text = item.name
                    isChecked = item.isSelected
                    shapeAppearanceModel = ShapeAppearanceModel.builder()
                            .setAllCornerSizes(ViewUtils.dpToPx(10).toFloat())
                            .build()
                    chipStartPadding = ViewUtils.dpToPx(12).toFloat()
                    chipEndPadding = ViewUtils.dpToPx(12).toFloat()
                    textEndPadding = ViewUtils.dpToPx(12).toFloat()
                    viewModel.updateSelectedLanguage(item)
                    chipIcon = when (item.name) {
                        context.getString(R.string.language_english) -> ContextCompat.getDrawable(context, R.drawable.avd_english)
                        context.getString(R.string.language_hindi) -> ContextCompat.getDrawable(context, R.drawable.avd_hindi)
                        context.getString(R.string.language_tamil) -> ContextCompat.getDrawable(context, R.drawable.avd_tamil)
                        context.getString(R.string.language_telugu) -> ContextCompat.getDrawable(context, R.drawable.avd_telugu)
                        context.getString(R.string.language_bengali) -> ContextCompat.getDrawable(context, R.drawable.avd_bengali)
                        context.getString(R.string.language_marathi) -> ContextCompat.getDrawable(context, R.drawable.avd_marathi)
                        context.getString(R.string.language_malayalam) -> ContextCompat.getDrawable(context, R.drawable.avd_malayali)
                        else -> null
                    }
                    isChipIconVisible = true
                    chipIconSize = ViewUtils.dpToPx(40).toFloat()
                    setOnCheckedChangeListener { _, isChecked ->
                        item.isLanguageSelected = isChecked
                        viewModel.updateSelectedLanguage(item)

                    }
                })
            }
        })
        viewModel.userActionLiveData.observe(viewLifecycleOwner, {
            if (it is OnBoardingViewModel.UserAction.SubmitLanguage) {
                viewModel.updateLanguage()
            }
        })
        viewModel.fetchLanguage()
//        Log.i(javaClass.simpleName, "preferred_language: ${getResStringLanguage(R.string.people_watching_live, "hi")}")
    }

//    private fun getResStringLanguage(id: Int, lang: String?): String? {
//        //Get default locale to back it
//        val res = resources
//        val conf = res.configuration
//        val savedLocale = conf.locale
//        //Retrieve resources from desired locale
//        val confAr = resources.configuration
//        confAr.locale = Locale(lang)
//        val metrics = DisplayMetrics()
//        val resources = Resources(context?.assets, metrics, confAr)
//        //Get string which you want
//        val string = resources.getString(id)
//        //Restore default locale
//        conf.locale = savedLocale
//        res.updateConfiguration(conf, null)
//        //return the string that you want
//        return string
//    }

    override fun onResume() {
        super.onResume()
        timer.startTimer()
        viewModel.userActionLiveData.value = OnBoardingViewModel.UserAction.HideBackButton
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_NEW_SELECT_LANGUAGE_PAGE_SHOWED, HashMap())
    }

    override fun onPause() {
        super.onPause()
        timer.stopTimer()
    }
}