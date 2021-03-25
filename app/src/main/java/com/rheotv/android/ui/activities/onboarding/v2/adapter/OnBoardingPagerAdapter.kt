package com.rheotv.android.ui.activities.onboarding.v2.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.rheotv.android.ui.activities.onboarding.v2.view.fragment.OnBoardingLanguageFragment
import com.rheotv.android.ui.activities.onboarding.v2.view.fragment.OnBoardingLoginFragment
import com.rheotv.android.ui.activities.onboarding.v2.view.fragment.OnBoardingStreamerSelectionFragment
import com.rheotv.android.ui.activities.onboarding.v2.view.fragment.OnBoardingUsernameInputFragment

@SuppressLint("WrongConstant")
class OnBoardingPagerAdapter(fm: FragmentManager, val isReLogin: Boolean) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        {


//    override fun getItemCount(): Int = 3
//
//    override fun createFragment(position: Int): Fragment = when (position) {
//        1 -> OnBoardingLoginFragment()
//        2 -> OnBoardingUsernameInputFragment()
////            3 -> OnBoardingStreamerSelectionFragment()
//        else -> OnBoardingLanguageFragment()
//    }

    override fun getCount(): Int = 4

    override fun getItem(position: Int): Fragment = when (position) {
        1 -> OnBoardingLoginFragment.newInstance(isReLogin)
        2 -> OnBoardingUsernameInputFragment()
        3 -> OnBoardingStreamerSelectionFragment()
        else -> OnBoardingLanguageFragment()
    }

}