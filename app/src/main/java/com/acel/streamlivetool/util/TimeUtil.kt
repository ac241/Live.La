/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.util

import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    fun timestampToString(timeStamp: Long): String {
        if (timeStamp < 0)
            throw IllegalArgumentException("negative number illegal:$timeStamp")
        val fixStamp = when (timeStamp.toString().length) {
            10 -> timeStamp * 1000
            13 -> timeStamp
            else -> throw IllegalArgumentException("not support")
        }
        val diff = System.currentTimeMillis() - fixStamp
        return when {
            //小于60秒
            diff < 60_000 ->
                "刚刚"
            //小于一小时
            diff < 3_600_000 ->
                "${diff / 60_000}分钟前"
            //小于一天
            diff < 86_400_000 ->
                "${diff / 3_600_000}小时前"
            //大于一天
            else ->
                formatter.format(timeStamp)
        }
    }
}