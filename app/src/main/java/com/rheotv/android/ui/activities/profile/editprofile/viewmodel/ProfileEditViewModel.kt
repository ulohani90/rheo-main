package com.rheotv.android.ui.activities.profile.editprofile.viewmodel

import com.rheotv.android.data.DataManager
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.rx.SchedulerProvider

class ProfileEditViewModel constructor(
        dataManager: DataManager,
        schedulerProvider: SchedulerProvider
) : BaseViewModel<Any>(dataManager, schedulerProvider) {

}