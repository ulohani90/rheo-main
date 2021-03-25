package com.rheotv.android.ui.customViews.simpleSnackbar

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.rheotv.android.R
import com.rheotv.android.utils.findSuitableParent

@Suppress("SpellCheckingInspection")
class BaseSimpleSnackbar(
        parent: ViewGroup,
        content: BaseSimpleCustomSnackbarView
) : BaseTransientBottomBar<BaseSimpleSnackbar>(parent, content, content) {

    init {
        getView().setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))
        getView().setPadding(0, 0, 0, 0)
    }

    companion object {
        private var simpleSnackbar: BaseSimpleSnackbar? = null

        fun make(view: View,
                 snack: SimpleSnack
        ): BaseSimpleSnackbar? {
            // First we find a suitable parent for our custom view
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                    "No suitable parent found from the given view. Please provide a valid view."
            )

            // We inflate our custom view
            try {
                val customView = LayoutInflater.from(view.context).inflate(
                        snack.layoutId,
                        parent,
                        false
                ) as BaseSimpleCustomSnackbarView
                // We create and return our Snackbar
                customView.populateView(snack)

                simpleSnackbar = BaseSimpleSnackbar(
                        parent,
                        customView).apply { duration = snack.duration }
                return simpleSnackbar
            } catch (e: Exception) {
                Log.v(javaClass.simpleName, e.message)
            }
            return null
        }
    }
}