package com.acel.streamlivetool.platform.impl.douyu.danmu

import java.util.regex.Pattern

object DanmuPatternUtils {
    val readType: Pattern = Pattern.compile("type@=(.*?)/")

    val readDanmuUid: Pattern = Pattern.compile("uid@=(.*?)/")
    val readDanmuUser: Pattern = Pattern.compile("nn@=(.*?)/")
    val readDanmuInfo: Pattern = Pattern.compile("txt@=(.*?)/")
    val readDanmuSendTime: Pattern = Pattern.compile("cst@=(.*?)/")

}