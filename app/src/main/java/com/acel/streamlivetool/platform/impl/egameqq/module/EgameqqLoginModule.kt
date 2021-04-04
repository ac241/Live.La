package com.acel.streamlivetool.platform.impl.egameqq.module

import com.acel.streamlivetool.platform.base.LoginModule
import com.acel.streamlivetool.platform.base.CookieManager

class EgameqqLoginModule(platform: String, cookieManager: CookieManager) :
    LoginModule(platform, cookieManager) {
    override val pcAgent: Boolean = true
    override val loginUrl: String = "https://egame.qq.com/usercenter/followlist"

    override fun checkLoginOk(cookie: String): Boolean {
        return cookie.contains("pgg_uid") && cookie.contains("pgg_access_token")
    }

}