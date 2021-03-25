package com.rheotv.android.ui.activities.profile.editprofile.view

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.data.network.models.general.GameDetails
import com.rheotv.android.databinding.FragmentPreferredGameBinding
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.GameSelectionAdapter
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.*
import com.rheotv.android.utils.recyclerdecorators.GridItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [PreferredGameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PreferredGameFragment : BaseFragment<FragmentPreferredGameBinding, UserProfileViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var gameAdapter: GameSelectionAdapter

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.fragment_preferred_game

    override fun getViewModel() = ViewModelProvider(parentFragment?.parentFragment
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
        with(viewDataBinding) {
            val itemDecorator = GridItemDecoration(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt(), 3)
            gameRecyclerView.addItemDecoration(itemDecorator)
            gameRecyclerView.adapter = GameSelectionAdapter().also { gameAdapter = it }
            continueButton.setOnClickListener { onContinueClick() }
            backButton.setOnClickListener { navController()?.popBackStack() }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewModel) {
            loadGameDetails()
            gameAdapter.apply {
                selectedGame = viewModel.profile.get()?.selectedGames?.associateBy({ it.id }, { it }) as MutableMap<String, GameDetails?>
            }

            gameResults.observe(viewLifecycleOwner, Observer {
                gameAdapter.submitList(it)
            })
            submittingGame.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    submittingGame.get()?.let {
                        if (it == Status.SUCCESS) {
                            viewModelScope.launch(Dispatchers.IO) {
                                delay(1000)
                                withContext(Dispatchers.Main) { navController()?.popBackStack() }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun onContinueClick() {
        if (gameAdapter.selectedGame.isNullOrEmpty()) {
            context?.showToast("Please select a game")
            return
        }
        gameAdapter.selectedGame.let { viewModel.uploadSelectedGame(it) }
    }
}