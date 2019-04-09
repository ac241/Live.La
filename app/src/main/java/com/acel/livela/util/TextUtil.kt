package com.acel.livela.util

object TextUtil {
    fun subString(str: String, startStr: String, endString: String): String {
        val startIndex = str.indexOf(startStr)
        val endIndex = str.indexOf(endString, startIndex)
        return str.substring(startIndex + startStr.length, endIndex)
    }
}