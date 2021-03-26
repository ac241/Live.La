package com.acel.streamlivetool.platform.impl.bilibili.module

import com.acel.streamlivetool.platform.base.AbstractLoginImpl
import com.acel.streamlivetool.platform.base.CookieManager

class BiliLoginModule(platform: String, cookieManager: CookieManager) :
    AbstractLoginImpl(platform, cookieManager) {

    override val loginUrl: String = "https://passport.bilibili.com/login"

    override fun checkLoginOk(cookie: String): Boolean {
        if (cookie.contains("SESSDATA") && cookie.contains("DedeUserID"))
            return true
        return false
    }

}