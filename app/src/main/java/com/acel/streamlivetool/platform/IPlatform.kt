package com.acel.streamlivetool.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.bean.AnchorsCookieMode
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.platform.bean.ResultUpdateAnchorByCookie
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import retrofit2.Retrofit

interface IPlatform {
    companion object {
        private const val FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR = "关注列表中没有这个主播，请关注该主播或关闭cookie方式"
    }

    /**
     * 平台名，例如"Douyu"
     */
    val platform: String

    /**
     * 平台显示名resId，例如：R.string.douyu
     */
    val platformShowNameRes: Int

    val platformName
        get() = MyApplication.application.getString(platformShowNameRes)

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
    fun updateAnchorData(queryAnchor: Anchor): Boolean

    /**
     * 是否支持updateAnchorsByCookie
     */
    fun supportUpdateAnchorsByCookie() = false

    /**
     * 主页以cookie方式获取主播列表
     * dependency [supportUpdateAnchorsByCookie],it must return true
     * use [getCookie] to get cookie string
     * 必须调用[setHintWhenFollowListDidNotContainsTheAnchor]给列表中不含的anchor设置提醒
     * @return 如果成功，返回对象的第一个参数应该为true
     */
    fun updateAnchorsDataByCookie(queryList: List<Anchor>): ResultUpdateAnchorByCookie =
        ResultUpdateAnchorByCookie(false)

    /**
     * 设置提醒词
     */
    fun Anchor.setHintWhenFollowListDidNotContainsTheAnchor() {
        title = FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR
    }

    /**
     * 设置提醒词
     */
    fun List<Anchor>.setHintWhenFollowListDidNotContainsTheAnchor() {
        forEach {
            it.title = FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR
        }
    }

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
     * Use [getCookie] to get saved cookie
     * @return AnchorsCookieMode
     */
    fun getAnchorsWithCookieMode(): AnchorsCookieMode {
        return AnchorsCookieMode(
            false,
            null,
            ""
        )
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
    fun getCookie(): String {
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