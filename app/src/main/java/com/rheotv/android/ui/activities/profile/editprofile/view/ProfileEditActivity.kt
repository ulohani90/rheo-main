package com.rheotv.android.ui.activities.profile.editprofile.view

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult
import com.rheotv.android.databinding.ActivityProfileEditBinding
import com.rheotv.android.ui.activities.profile.editprofile.viewmodel.ProfileEditViewModel
import com.rheotv.android.ui.base.BaseActivity
import com.rheotv.android.utils.AppConstants
import javax.inject.Inject

class ProfileEditActivity : BaseActivity<ActivityProfileEditBinding, ProfileEditViewModel>() {
    @Inject
    lateinit var editViewModel: ProfileEditViewModel

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.activity_profile_edit

    override fun getViewModel() = editViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().add(R.id.container, ProfileEditFragment.newInstance(null, ""), ProfileEditFragment.javaClass.simpleName).commit()
    }
}