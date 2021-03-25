package com.rheotv.android.ui.activities.player.activity

import com.rheotv.android.data.DataManager
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.rx.SchedulerProvider

class OverPermissionViewModel constructor(
        dataManager: DataManager?,
        schedulerProvider: SchedulerProvider?
) : BaseViewModel<Any>(dataManager, schedulerProvider) {

}