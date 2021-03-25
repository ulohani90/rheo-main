package com.rheotv.android.ui.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView

import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.rheotv.android.R

import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("ViewConstructor")
class AnalyticsMarkerView(context: Context, layoutResource: Int, private val shouldFormat: Boolean) : MarkerView(context, layoutResource) {
    private val dateTV: TextView
    private val valueTV: TextView
    private val mFormat = SimpleDateFormat("dd MMM")

    init {
        dateTV = findViewById(R.id.dateTV)
        valueTV = findViewById(R.id.valueTV)
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (shouldFormat) {
            if (e is CandleEntry) {
                val ce = e as CandleEntry?
                dateTV.text = mFormat.format(Date(ce!!.x.toLong()))
                valueTV.text = ce.high.toString()
            } else {
                dateTV.text = mFormat.format(Date(e!!.x.toLong()))
                valueTV.text = e.y.toString()
            }
        } else {
            if (e is CandleEntry) {
                val ce = e as CandleEntry?
                dateTV.text = mFormat.format(Date(ce!!.x.toLong()))
                valueTV.text = Utils.formatNumber(ce.high, 0, true)
            } else {
                dateTV.text = mFormat.format(Date(e!!.x.toLong()))
                valueTV.text = Utils.formatNumber(e.y, 0, true)
            }
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}