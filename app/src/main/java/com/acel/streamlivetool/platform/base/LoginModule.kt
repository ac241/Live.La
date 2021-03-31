package com.acel.streamlivetool.platform.base

interface LoginModule {
    /**
     *  WebView 使用PC Agent
     */
    val pcAgent: Boolean
        get() = false

    /**
     * 登录提示
     */
    val loginTips: String?
        get() = null

    /**
     * 登录页面
     */
    val loginUrl: String

    /**
     *
     */
    val javascriptOnPageLoaded: String?
        get() = null

    /**
     * 检查登录是否成功
     */
    fun checkLoginOk(cookie: String): Boolean

    /**
     * 尝试登录
     * @return success or not
     */
    fun tryLogin(cookie: String): Boolean

}