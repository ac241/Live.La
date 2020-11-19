/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.util

object CookieUtil {
    /**
     * 获取cookie中的某个字段
     */
    fun getCookieField(str: String, fieldName: String): String? {
        if (str.indexOf(fieldName) == -1)
            return null
        val fieldList = str.split(";")
        fieldList.forEach {
            val fieldX = it.split("=")
            if (fieldX.size == 2)
                if (fieldX[0].trim() == fieldName)
                    return fieldX[1].trim()
        }
        return null
    }
}