package com.acel.streamlivetool.platform.impl.douyu.module

import com.acel.streamlivetool.platform.base.AbstractLoginImpl
import com.acel.streamlivetool.platform.base.CookieManager

class DouyuLoginModule(platform: String, cookieManager: CookieManager) :
        AbstractLoginImpl(platform, cookieManager) {
    override val pcAgent: Boolean = true
    override val loginUrl: String = "https://passport.douyu.com/index/login"
    override val loginTips: String = "斗鱼：昵称登录可能无法使用,cookie有效期约为7天。"

    override fun checkLoginOk(cookie: String): Boolean {
        if (cookie.contains("PHPSESSID") && cookie.contains("dy_auth"))
            return true
        return false
    }


}