package com.iti.uc3.forecast.view


import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import kotlin.math.roundToInt

class SnapHorizontalScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

    var snapIntervalPx: Int = 0 // set this in activity (4 hours * interval)

    override fun fling(velocityX: Int) {
        super.fling(velocityX)
        post {
            snapToInterval()
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        // Optional: could snap here on scroll end with a handler or GestureDetector
    }

    fun snapToInterval() {
        if (snapIntervalPx <= 0) return
        val scrollX = scrollX
        val snapped = (scrollX / snapIntervalPx.toFloat()).roundToInt() * snapIntervalPx
        smoothScrollTo(snapped, 0)
    }
}