package com.acel.streamlivetool.platform.base

import com.acel.streamlivetool.util.defaultSharedPreferences

class DefaultCookieManager(platform: String) : CookieManager {
    private val keyCookie = "${platform}_cookie"

    /**
     * 保存cookie
     */
    override fun saveCookie(cookie: String) {
        defaultSharedPreferences.edit()
            .putString(keyCookie, cookie)
            .apply()
    }

    /**
     * 读取cookie
     */
    override fun getCookie(): String {
        val cookie = defaultSharedPreferences.getString(keyCookie, "")
        return cookie ?: ""
    }

    /**
     * 清除cookie
     */
    override fun clearCookie() {
        defaultSharedPreferences.edit().remove(keyCookie)
            .apply()
    }

    companion object {
        
    }
}