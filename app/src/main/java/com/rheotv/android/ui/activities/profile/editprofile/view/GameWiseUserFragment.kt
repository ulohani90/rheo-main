package com.rheotv.android.ui.activities.profile.editprofile.view

import android.os.Bundle
import android.view.View
import androidx.databinding.Observable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.data.network.models.general.GameDetails
import com.rheotv.android.databinding.GameWiseUserFragmentBinding
import com.rheotv.android.helpers.GameSpinner
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.GameWiseUserAdapter
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.activities.profile.viewprofile.utils.UserAction
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.addBackPressCallback
import com.rheotv.android.utils.navController
import com.rheotv.android.utils.onItemSelected
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import javax.inject.Inject

class GameWiseUserFragment : BaseFragment<GameWiseUserFragmentBinding, UserProfileViewModel>() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var userAdapter: GameWiseUserAdapter

    lateinit var gameListAdapter: GameSpinner

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.game_wise_user_fragment

    override fun getViewModel() = ViewModelProvider(parentFragment?.parentFragment ?: this,
            viewModelFactory).get(UserProfileViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBackPressCallback()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewDataBinding) {
            actionBack.setOnClickListener { navController()?.popBackStack() }
            userAdapter.onDeleteClick = {
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_GAME_WISE_USERNAME_REMOVED, viewModel?.analyticsProperties)
                this@GameWiseUserFragment.viewModel.updateUserGame(it, UserAction.Delete)
            }
            gameRecyclerView.adapter = userAdapter
            gameSpinner.apply {
                adapter = GameSpinner(context).also {
                    it.enableLightText()
                    gameListAdapter = it
                }
                onItemSelected {
                    viewModel?.currentGame = it as? GameDetails
                }
            }
            editButton.setOnClickListener {
                if (userAdapter.itemCount > 0) {
                    this@GameWiseUserFragment.viewModel.onEditGameClick()
                }
            }
        }
    }

    private fun checkEditButtonVisibility() {
        viewDataBinding.editButton.visibility = if (userAdapter.itemCount > 0)
            View.VISIBLE else View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewModel) {
            loadGames()
            profile.get()?.profileDetail?.gameUserNames?.let { userAdapter.submitList(it) }
            games.observe(viewLifecycleOwner, Observer {
                it ?: return@Observer
                gameListAdapter.submitList(it)
            })

            userAction.observe(viewLifecycleOwner, Observer {
                it ?: return@Observer
                userAdapter.onUserAction(it)
                if (userAdapter.itemCount == 0) {
                    userAdapter.toggleEditMode()
                    viewModel.onEditGameClick()
                }
                checkEditButtonVisibility()
            })

            inGameUserEditMode.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    userAdapter.toggleEditMode()
                }
            })
        }
    }

    override fun onDestroyView() {
        viewModel.clearGameWiseUserData()
        super.onDestroyView()
    }

    companion object {
        fun newInstance(username: String?, source: String): GameWiseUserFragment {
            val args = Bundle()
            args.putString(AppConstants.AUTHOR_NAME, username)
            args.putString(AppConstants.SCREEN_SOURCE, source)
            val fragment = GameWiseUserFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
