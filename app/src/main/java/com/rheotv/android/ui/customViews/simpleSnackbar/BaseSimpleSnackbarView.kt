package com.rheotv.android.ui.customViews.simpleSnackbar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.ContentViewCallback
import com.google.android.material.snackbar.Snackbar
import com.rheotv.android.R
import kotlinx.android.synthetic.main.view_snackbar_simple.view.*

@Suppress("SpellCheckingInspection")
abstract class BaseSimpleCustomSnackbarView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ContentViewCallback {

    override fun animateContentIn(delay: Int, duration: Int) {
        val scaleX = ObjectAnimator.ofFloat(message_text_view, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(action_button, View.SCALE_Y, 0f, 1f)
        val animatorSet = AnimatorSet().apply {
            interpolator = OvershootInterpolator()
            setDuration(500)
            playTogether(scaleX, scaleY)
        }
        animatorSet.start()
    }

    override fun animateContentOut(delay: Int, duration: Int) {
    }

    abstract fun populateView(snack: SimpleSnack)
}

data class SimpleSnack (
        var title: String? = null,

        var subtitle: String? = null,

        var actionText: String? = null,

        var icon: Drawable? = null,

        var background: Int? = null,

        var layoutId: Int = R.layout.layout_simple_snackbar,

        var duration: Int = Snackbar.LENGTH_INDEFINITE,

        var listener: (() -> Unit)? = null
)