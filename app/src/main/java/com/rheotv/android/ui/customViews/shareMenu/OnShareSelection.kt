package com.rheotv.android.ui.customViews.shareMenu

import android.content.Intent
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest

interface OnShareSelection {
    fun onCopy(packageName: String?)

    fun onMoreSelected(tag: String?)

    fun onShareSelected(packageIntent: Intent,optionRequest : OptionRequest)
}