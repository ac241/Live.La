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

    fun subStringAfterWhat(
        str: String,
        afterWhat: String,
        startStr: String,
        endString: String
    ): String? {
        val newString = str.substring(str.indexOf(afterWhat))
        val startIndex = newString.indexOf(startStr)
        val endIndex = newString.indexOf(endString, startIndex)
        return if (startIndex == -1 || endIndex == -1)
            null
        else
            newString.substring(startIndex + startStr.length, endIndex)
    }
}