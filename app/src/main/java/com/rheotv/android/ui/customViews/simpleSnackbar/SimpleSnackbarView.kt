package com.rheotv.android.ui.customViews.simpleSnackbar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.ContentViewCallback
import com.rheotv.android.R
import kotlinx.android.synthetic.main.view_snackbar_simple.view.*

@Suppress("SpellCheckingInspection")
class SimpleCustomSnackbarView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ContentViewCallback {

    var tvMsg: TextView
    var btAction: MaterialButton
    var layRoot: ConstraintLayout

    init {
        View.inflate(context, R.layout.view_snackbar_simple, this)
        clipToPadding = false
        this.tvMsg = findViewById(R.id.message_text_view)
        this.btAction = findViewById(R.id.action_button)
        this.layRoot = findViewById(R.id.snack_constraint)
    }

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
}