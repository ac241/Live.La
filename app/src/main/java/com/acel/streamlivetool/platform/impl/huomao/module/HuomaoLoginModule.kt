package com.acel.streamlivetool.platform.impl.huomao.module

import com.acel.streamlivetool.platform.base.AbstractLoginImpl
import com.acel.streamlivetool.platform.base.CookieManager

class HuomaoLoginModule(platform: String, cookieManager: CookieManager) :
    AbstractLoginImpl(platform, cookieManager) {
    override val pcAgent: Boolean = true
    override val loginUrl: String = "https://www.huomao.com/channel/all"
    override val javascriptOnPageLoaded = "$('#login-btn').click()"
    override fun checkLoginOk(cookie: String): Boolean {
        return cookie.contains("user_")
    }

}