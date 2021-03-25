package com.rheotv.android.ui.activities.profile.viewprofile.viewmodel

import com.rheotv.android.data.DataManager
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.rx.SchedulerProvider

class AboutUserViewModel  constructor(
        dataManager: DataManager?,
        schedulerProvider: SchedulerProvider?
) : BaseViewModel<Any>(dataManager, schedulerProvider) {

}
