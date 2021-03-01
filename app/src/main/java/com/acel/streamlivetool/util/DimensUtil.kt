package com.acel.streamlivetool.util

import com.acel.streamlivetool.base.MyApplication

/**
 * sp转px
 */
internal fun Float.toPx(): Float {
    val fontScale = MyApplication.application.resources.displayMetrics.scaledDensity
    return (this * fontScale + 0.5f)
}
