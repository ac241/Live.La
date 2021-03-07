package com.acel.streamlivetool.util

import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import com.acel.streamlivetool.R

object CommonColor {
    var resources: Resources? = null
    fun bindResource(resources: Resources) {
        this.resources = resources
    }

    fun unbindResource() = run { resources = null }

    val livingColor
        get() = resources?.let {
            ResourcesCompat.getColor(it, R.color.colorPrimary, null)
        }
    val notLivingColor
        get() = resources?.let {
            ResourcesCompat.getColor(it, R.color.item_secondary_text_color, null)
        }
}