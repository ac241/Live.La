package com.acel.streamlivetool.util

object TextUtil {
    fun subString(str: String, startStr: String, endString: String): String? {
        val startIndex = str.indexOf(startStr)
        val endIndex = str.indexOf(endString, startIndex)
        return if (startIndex == -1 || endIndex == -1)
            null
        else
            str.substring(startIndex + startStr.length, endIndex)
    }
}