package com.acel.streamlivetool.platform.impl.huya.module

import com.acel.streamlivetool.platform.base.LoginModule
import com.acel.streamlivetool.platform.base.CookieManager

class HuyaLoginModule(platform: String, cookieManager: CookieManager) :
    LoginModule(platform, cookieManager) {

    override val pcAgent: Boolean = true
    override val loginUrl: String = "https://www.huya.com/333003"
    override val loginTips: String = "虎牙:显示不全请旋转屏幕，如果登录后没有自动关闭，可能是必须的cookie字段没有生成，请尝试点击几个直播间。cookie有效期约为7天，"
    override val javascriptOnPageLoaded: String = "$('#nav-login').click()"
    override fun checkLoginOk(cookie: String): Boolean {
        if (cookie.contains("udb_biztoken") && cookie.contains("udb_passport") && cookie.contains("guid="))
            return true
        return false
    }
}