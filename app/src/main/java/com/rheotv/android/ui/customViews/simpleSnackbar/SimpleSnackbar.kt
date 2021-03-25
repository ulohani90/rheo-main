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
class SimpleSnackbar(
        parent: ViewGroup,
        content: SimpleCustomSnackbarView
) : BaseTransientBottomBar<SimpleSnackbar>(parent, content, content) {

    init {
        getView().setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))
        getView().setPadding(0, 0, 0, 0)
    }

    companion object {
        private var simpleSnackbar:SimpleSnackbar? = null

        fun make(view: View,
                 message: String,
                 duration: Int,
                 actionLabel: String?,
                 background: Int? = null,
                 listener : View.OnClickListener?
        ): SimpleSnackbar? {
            // First we find a suitable parent for our custom view
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                    "No suitable parent found from the given view. Please provide a valid view."
            )

            // We inflate our custom view
            try {
                val customView = LayoutInflater.from(view.context).inflate(
                        R.layout.layout_simple_snackbar,
                        parent,
                        false
                ) as SimpleCustomSnackbarView
                // We create and return our Snackbar
                customView.tvMsg.text = message
                actionLabel?.let {
                    customView.btAction.text = actionLabel
                    customView.btAction.setOnClickListener {
                        listener?.onClick(customView.btAction)
                        // Now dismiss the Snackbar
                        simpleSnackbar?.dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION)
                    }
                }
                background?.let { customView.layRoot.setBackgroundColor(ContextCompat.getColor(view.context, background)) }
                simpleSnackbar = SimpleSnackbar(
                        parent,
                        customView).setDuration(duration)
                return simpleSnackbar
            } catch (e: Exception) {
                Log.v(javaClass.simpleName, e.message)
            }
            return null
        }
    }
}