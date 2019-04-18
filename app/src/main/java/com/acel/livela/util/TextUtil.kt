package com.acel.livela.util

import android.util.Log

object TextUtil {
    fun subString(str: String, startStr: String, endString: String): String? {
        val startIndex = str.indexOf(startStr)
        val endIndex = str.indexOf(endString, startIndex)
        if (startIndex == -1 || endIndex == -1)
            return null
        else
            return str.substring(startIndex + startStr.length, endIndex)
    }
}