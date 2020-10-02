/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.util

import java.text.DecimalFormat

object AnchorUtil {
    private val df = DecimalFormat("#.0")
    fun formatOnlineNumber(int: Int): String {
        return if (int > 9999) {
            "${df.format(int.toFloat() / 10000)}万"
        } else int.toString()
    }
}