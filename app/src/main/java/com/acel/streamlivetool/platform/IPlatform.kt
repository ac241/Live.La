package com.acel.streamlivetool.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import retrofit2.Retrofit

interface IPlatform {

    /**
     * 平台名，例如"Douyu"
     */
    val platform: String

    /**
     * 平台显示名resId，例如：R.string.douyu
     */
    val platformShowNameRes: Int

    /**
     *  支持cookie模式
     *  如果为true，需要复写 [getAnchorsWithCookieMode] [getLoginUrl] [checkLoginOk]
     *  可能需要[usePcAgent]
     */
    val supportCookieMode: Boolean

    /**
     * Retrofit实例
     */
    val retrofit: Retrofit get() = RetrofitUtils.retrofit

    /**
     * 获取直播间信息
     * @param queryAnchor Anchor
     * @return Anchor if fail return null
     */
    fun getAnchor(queryAnchor: Anchor): Anchor?

    /**
     * 获取直播状态
     * @return AnchorStatus if fail return null
     */
    fun getAnchorAttribute(queryAnchor: Anchor): AnchorAttribute?

    /**
     * 获取直播流
     */
    fun getStreamingLiveUrl(queryAnchor: Anchor): String?

    /**
     * 打开直播间
     * @param context maybe not activity context,
     * so you must set the flag as intent.flags = FLAG_ACTIVITY_NEW_TASK
     */
    fun startApp(context: Context, anchor: Anchor)

    /**
     * 搜索直播间
     * @return 返回null表示不支持搜索，无数据返回空List
     */
    fun searchAnchor(keyword: String): List<Anchor>? = null

    /**
     * 调用第三方播放器
     */
    fun callOuterPlayer(context: Context, anchor: Anchor) {
        val url = getStreamingLiveUrl(anchor)
        if (url != null) {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "video/*")
            context.startActivity(intent)
        } else {
            runOnUiThread {
                toast(context.getString(R.string.streaming_url_is_null))
            }
        }
    }

    /**
     * cookie方式获取列表
     * Use [readCookie] to get saved cookie
     * @return AnchorsCookieMode
     */
    fun getAnchorsWithCookieMode(): AnchorsCookieMode {
        return AnchorsCookieMode(false, null, "")
    }

    /**
     *  WebView 使用PC Agent
     */
    fun usePcAgent(): Boolean {
        return false
    }

    /**
     * 登录页面
     */
    fun getLoginUrl(): String {
        return ""
    }

    /**
     * 检查登录是否成功
     */
    fun checkLoginOk(cookie: String): Boolean {
        return false
    }

    /**
     * 保存cookie
     */
    fun saveCookie(cookie: String) {
        defaultSharedPreferences.edit()
            .putString("${platform}_cookie", cookie)
            .apply()
    }

    /**
     * 读取cookie
     */
    fun readCookie(): String {
        val cookie = defaultSharedPreferences.getString(
            "${platform}_cookie",
            ""
        )
        return cookie ?: ""
    }

    /**
     * 清除cookie
     */
    fun clearCookie() {
        defaultSharedPreferences.edit().remove("${platform}_cookie")
            .apply()
    }

}