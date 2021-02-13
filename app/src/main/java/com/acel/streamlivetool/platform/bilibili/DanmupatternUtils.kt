/**
 * @author https://github.com/DbgDebug/dbg-project
 */
package com.acel.streamlivetool.platform.bilibili

import java.util.regex.Matcher
import java.util.regex.Pattern

object DanmuPatternUtils {
    /**
     * 读取 CMD 的
     */
    val readCmd: Pattern = Pattern.compile("\"cmd\":\"(.*?)\"")
    val readStartTime: Pattern = Pattern.compile("\"start_time\":(\\d+)")
    val readUsername: Pattern = Pattern.compile("\"username\":\"(.*?)\"")
    val readGuardLevel: Pattern = Pattern.compile("\"guard_level\":(\\d+)")

    /**
     * 读取弹幕发送者UID
     */
    val readDanmuUid: Pattern = Pattern.compile(",\\[(\\d+)")

    /**
     * 读取弹幕发送者昵称
     */
    val readDanmuUser: Pattern = Pattern.compile("\\[\\d+,\"(.*?)\",\\d+")

    /**
     * 读取具体弹幕内容
     */
    val readDanmuInfo: Pattern = Pattern.compile("],\"(.*?)\",\\[")
    val readDanmuSendTime: Pattern = Pattern.compile("\\[\\[\\d+,\\d+,\\d+,\\d+,(\\d+)")

    /**
     * 送礼action
     */
    val readGiftAction: Pattern = Pattern.compile("\"action\":\"(.*?)\"")
    val readSuperGiftNum: Pattern = Pattern.compile("\"super_gift_num\":(\\d+)")

    /**
     * 读取礼物名称
     */
    val readGiftName: Pattern = Pattern.compile("\"giftName\":\"(.*?)\"")
    val readGuardGiftName: Pattern = Pattern.compile("\"gift_name\":\"(.*?)\"")

    /**
     * 读取礼物数量
     */
    val readGiftNum: Pattern = Pattern.compile("\"num\":(\\d+)")
    val readGiftPrice: Pattern = Pattern.compile("\"price\":(\\d+)")

    /**
     * 读取礼物Id
     */
    val readGiftId: Pattern = Pattern.compile("\"giftId\":(\\d+)")

    /**
     * 读取发送礼物者的
     */
    val readGiftUser: Pattern = Pattern.compile("\"uname\":\"(.*?)\"")

    /**
     * 读取发送者的uid
     */
    val readUserId: Pattern = Pattern.compile("\"uid\":(\\d+)")
    val readGiftSendTime: Pattern = Pattern.compile("\"timestamp\":(\\d+)")

    /**
     * 读取欢迎玩家的
     */
    val readWelcomeUser: Pattern = Pattern.compile("\"uname\":\"(.*?)\"")

    /**
     * 编码转换
     */
    val unicodePattern: Pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))")

    /**
     * 把十六进制Unicode编码字符串转换为中文字符串
     * Ref: https://blog.csdn.net/wccmfc123/article/details/11610393
     *
     * @param str 传入的字符串，可能包含 16 位 Unicode 码
     * @return 反转义后的字符串
     */
    fun unicodeToString(str: String): String {
        // 获取内部的 U 码
        var str = str
        val matcher: Matcher = unicodePattern.matcher(str)
        // 字符初始化
        var ch: Char
        // 开始逐个替换
        while (matcher.find()) {
            // 将扒出来的 Int 转换成 char 类型，因为 Java 默认是 UTF-8 编码，所以会自动转换成对应文字
            ch = matcher.group(2).toInt(16).toChar()
            // 将 Unicode 码替换成对应文字，注意后面用了一个隐式类型转换
            str = str.replace(matcher.group(1), ch.toString() + "")
        }
        return str
    }
}