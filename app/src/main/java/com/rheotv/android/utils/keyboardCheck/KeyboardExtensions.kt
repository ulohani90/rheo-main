@file:Suppress("unused")

package com.rheotv.android.utils.keyboardCheck

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment

fun Fragment.getRootView(): View? {
    return view
}

fun Activity.getRootView(): View? {
    return this.findViewById(android.R.id.content)
}

fun Context.convertDpToPx(dp: Float): Float {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            this.resources.displayMetrics
    )
}

fun Fragment.isKeyboardOpen(): Boolean {
    val visibleBounds = Rect()
    this.getRootView()?.getWindowVisibleDisplayFrame(visibleBounds)
    val heightWindow = this.activity?.window?.decorView?.height ?: 0
    val heightDiff = heightWindow - visibleBounds.height()
    val marginOfError = heightWindow * 0.15
    return heightDiff > marginOfError
}

fun Fragment.isKeyboardClosed(): Boolean {
    return !this.isKeyboardOpen()
}

fun Activity.isKeyboardOpen(): Boolean {
    val visibleBounds = Rect()
    this.getRootView()?.getWindowVisibleDisplayFrame(visibleBounds)
    val heightWindow = this.window?.decorView?.height ?: 0
    val heightDiff = heightWindow - visibleBounds.height()
    val marginOfError = heightWindow * 0.15
    return heightDiff > marginOfError
}

fun Activity.isKeyboardClosed(): Boolean {
    return !this.isKeyboardOpen()
}