package com.rheotv.android.ui.activities.profile.editprofile.view

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.databinding.Observable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentGameTimingBinding
import com.rheotv.android.ui.activities.profile.model.Selectable
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class GameTimingFragment : BaseFragment<FragmentGameTimingBinding, UserProfileViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.fragment_game_timing

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
            startTimeEditText.setOnClickListener {
                if (isStateSaved || !isAdded || activity?.isFinishing == true ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity?.isActivityTransitionRunning == true))
                    return@setOnClickListener
                context?.showTimePicker { displayDate: Date, _: Date ->
                    val c = Calendar.getInstance().apply {
                        time = displayDate
                    }

                    viewModel?.playStartTime = displayDate.format(TimeUtils.HH_MM)
                    viewModel?.playStartAmPm = if (c[Calendar.AM_PM] == 0) "AM" else "PM"
                }
            }

            endTimeEditText.setOnClickListener {
                if (isStateSaved || !isAdded || activity?.isFinishing == true ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity?.isActivityTransitionRunning == true))
                    return@setOnClickListener
                context?.showTimePicker { displayDate: Date, _: Date ->
                    val c = Calendar.getInstance().apply {
                        time = displayDate
                    }

                    viewModel?.playEndTime = displayDate.format(TimeUtils.HH_MM)
                    viewModel?.playEndAmPm = if (c[Calendar.AM_PM] == 0) "AM" else "PM"
                }
            }

            actionBack.setOnClickListener { navController()?.popBackStack() }
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewModel) {
            viewDataBinding.dayChipGroup.addChips(totalGamingDays as MutableList<Selectable>, R.attr.chipChoiceFilterStyle)
            gameScheduleStatus.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    if (gameScheduleStatus.get() == Status.SUCCESS) {
                        viewModelScope.launch(Dispatchers.IO) {
                            delay(1000)
                            withContext(Dispatchers.Main) { navController()?.popBackStack() }
                        }
                    }
                }
            })
        }
    }

}