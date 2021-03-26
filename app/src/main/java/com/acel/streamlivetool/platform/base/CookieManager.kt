package com.acel.streamlivetool.platform.base

import com.acel.streamlivetool.util.defaultSharedPreferences

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