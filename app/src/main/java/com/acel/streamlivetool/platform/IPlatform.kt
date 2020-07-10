package com.acel.streamlivetool.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.net.RetrofitUtils
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import org.jetbrains.annotations.Nullable
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
     */
    fun startApp(context: Context, anchor: Anchor)

    /**
     * 搜索直播间
     */
    @Nullable
    @Deprecated("wait to do")
    fun searchAnchor() {

    }

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
            context.runOnUiThread {
                toast("获取直播流失败")
            }
        }
    }

    /**
     * cookie方式获取列表
     * Use [readCookie] to get saved cookie
     * @return AnchorsCookieMode
     */
    fun getAnchorsWithCookieMode(): AnchorsCookieMode {
        return AnchorsCookieMode(false, null)
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
        MyApplication.application.defaultSharedPreferences.edit()
            .putString("${platform}_cookie", cookie)
            .apply()
    }

    /**
     * 读取cookie
     */
    fun readCookie(): String {
        val cookie = MyApplication.application.defaultSharedPreferences.getString(
            "${platform}_cookie",
            ""
        )
        return cookie ?: ""
    }

    /**
     * 清除cookie
     */
    fun clearCookie() {
        MyApplication.application.defaultSharedPreferences.edit().remove("${platform}_cookie")
            .apply()
    }
}