package com.acel.streamlivetool.platform.base

import com.acel.streamlivetool.util.defaultSharedPreferences

/**
 * @param platform ->the platform [AbstractPlatformImpl.platform]
 * @param cookieManager -> you can use the default manager [AbstractPlatformImpl.cookieManager]
 */
abstract class AbstractLoginImpl(platform: String, private val cookieManager: CookieManager) :
    LoginModule {

    private val keyLastLoginTime = "${platform}_last_login_time"

    final override fun tryLogin(cookie: String): Boolean {
        return if (checkLoginOk(cookie)) {
            cookieManager.saveCookie(cookie)
            saveLoginTime()
            true
        } else
            false
    }

    private fun saveLoginTime() {
        defaultSharedPreferences.edit()
            .putLong(keyLastLoginTime, System.currentTimeMillis())
            .apply()
    }

    fun getLastLoginTime(): Long = defaultSharedPreferences.getLong(keyLastLoginTime, -1)
}