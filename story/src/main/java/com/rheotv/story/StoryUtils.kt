package com.rheotv.story

import android.content.res.Resources
import kotlin.math.roundToInt

object StoryUtils {

    fun dpToPic(dm: Int): Int {
        val metrics = Resources.getSystem().displayMetrics
        val px = dm * (metrics.densityDpi / 160f)
        return px.roundToInt()
    }
}