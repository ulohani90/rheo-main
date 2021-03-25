package com.rheotv.android.ui.activities.profile.editprofile.view

import android.os.Bundle
import android.view.View
import androidx.databinding.Observable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.GameRuleFragmentBinding
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.GameRuleAdapter
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.addBackPressCallback
import com.rheotv.android.utils.navController
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import javax.inject.Inject

class GameRuleFragment : BaseFragment<GameRuleFragmentBinding, UserProfileViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var ruleAdapter: GameRuleAdapter

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.game_rule_fragment

    override fun getViewModel() = ViewModelProvider(parentFragment?.parentFragment
            ?: this, viewModelFactory)
            .get(UserProfileViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBackPressCallback()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewDataBinding) {
            ruleRecyclerView.adapter = ruleAdapter.also {
                it.onItemClick = { r, a ->
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_GAME_RULE_REMOVED, viewModel?.analyticsProperties)
                    viewModel?.updateGameRules(r, a)
                }
            }
            actionBack.setOnClickListener { navController()?.popBackStack() }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewModel) {
            profile.get()?.profileDetail?.gameRules?.let { ruleAdapter.submitList(it) }
            selectedRule.observe(viewLifecycleOwner, Observer {
                ruleAdapter.onUserAction(it)
            })
            inRuleEditMode.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    ruleAdapter.toggleAllowEdit()
                }
            })
        }
    }
}
