package com.rheotv.android.ui.customViews.simpleSnackbar

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.rheotv.android.R

@Suppress("SpellCheckingInspection")
class LiveAuthorSnackbarView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
): BaseSimpleCustomSnackbarView(context, attrs, defStyleAttr) {
    private var rView: View? = null

    init {
        rView = View.inflate(context, R.layout.view_live_audio_author, this)
        clipToPadding = false
    }

    override fun populateView(snack: SimpleSnack) {
        rView?.findViewById<ImageView>(R.id.icon_image_view)?.setImageDrawable(snack.icon)
        rView?.findViewById<TextView>(R.id.title_text_view)?.text = snack.title
        rView?.findViewById<TextView>(R.id.subtitle_text_view)?.text = snack.subtitle
        rView?.findViewById<MaterialButton>(R.id.action_button)?.text = snack.actionText
        snack.background?.let { rView?.rootView?.backgroundTintList = ColorStateList.valueOf(it) }
        rView?.findViewById<MaterialButton>(R.id.action_button)?.setOnClickListener {
            snack.listener?.invoke()
        }
    }

}