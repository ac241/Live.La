package com.acel.streamlivetool.platform.impl.huya.module

import java.util.regex.Pattern

object HuyaPatternUtil {
    val showId: Pattern = Pattern.compile("<h2 class=\"roomid\">房间号 : (.*?)</h2>")
    val nickName: Pattern = Pattern.compile("ANTHOR_NICK = '(.*?)';")
    val uid: Pattern = Pattern.compile("ayyuid: '(.*?)',")
    val typeName: Pattern = Pattern.compile("gameName = '(.*?)'")
    val keyFrame: Pattern = Pattern.compile("picURL = '(.*?)'")
    val title: Pattern = Pattern.compile("liveRoomName = '(.*?)'")
    val status: Pattern = Pattern.compile("ISLIVE = (.*?);")
    val online: Pattern = Pattern.compile("liveTotalCount = '(.*?)'")
    val avatar: Pattern = Pattern.compile("<img src=\"(.*?)\"")

    fun String.getMatchString(pattern: Pattern): String? {
        val matcher = pattern.matcher(this)
        return if (matcher.find())
            matcher.group(1)
        else null
    }

}
