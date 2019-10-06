package com.acel.streamlivetool.util

import java.util.ArrayList
import java.util.regex.Pattern

object UnicodeUtil {
    //Unicode转中文
    fun decodeUnicode(unicode: String): String {
        var str = unicode
        val list = ArrayList<String>()
        val reg = "\\\\u[0-9,a-f,A-F]{4}"
        val p = Pattern.compile(reg)
        val m = p.matcher(str)
        while (m.find()) {
            list.add(m.group())
        }
        var i = 0
        val j = 2
        while (i < list.size) {
            val code = list[i].substring(j, j + 4)
            val ch = Integer.parseInt(code, 16).toChar()
            str = str.replace(list[i], ch.toString())
            i++
        }
        return str
    }
}