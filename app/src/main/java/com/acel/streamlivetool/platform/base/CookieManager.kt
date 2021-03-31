package com.acel.streamlivetool.platform.base

interface CookieManager {
    /**
     * 保存cookie
     */
    fun saveCookie(cookie: String)

    /**
     * 读取cookie
     */
    fun getCookie(): String

    /**
     * 清除cookie
     */
    fun clearCookie()
}