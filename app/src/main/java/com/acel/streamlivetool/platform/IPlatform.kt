package com.acel.streamlivetool.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.bean.ResultGetAnchorListByCookieMode
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.platform.bean.ResultUpdateAnchorByCookie
import com.acel.streamlivetool.ui.player.DanmuClient
import com.acel.streamlivetool.util.AppUtil.mainThread
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
     * icon
     */
    val iconRes: Int

    val platformName
        get() = MyApplication.application.getString(platformShowNameRes)

    /**
     *  支持cookie模式
     *  如果为true，需要复写 [getAnchorsWithCookieMode] [getLoginUrl] [checkLoginOk]
     *  可能需要[loginWithPcAgent]
     */
    val supportCookieMode: Boolean

    /**
     * Retrofit实例
     */
    val retrofit: Retrofit get() = RetrofitUtils.retrofit

    /**
     * 获取直播间信息
     * @param queryAnchor Anchor
     * @return Anchor ,if fail return null
     */
    fun getAnchor(queryAnchor: Anchor): Anchor?

    /**
     * 获取直播状态
     * @return AnchorStatus if fail return null
     */
    fun updateAnchorData(queryAnchor: Anchor): Boolean

    /**
     * 是否支持以cookie方式更新数据
     */
    fun supportUpdateAnchorsByCookie() = false

    /**
     * 主页以cookie方式获取主播列表
     * dependency [supportUpdateAnchorsByCookie],it must return true
     * use [getCookie] to get cookie string
     * 必须调用[setHintWhenFollowListDidNotContainsTheAnchor]给列表中不含的anchor设置提醒
     * @return 如果成功，返回对象的第一个参数应该为true
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("已弃用")
    fun updateAnchorsDataByCookie(queryList: List<Anchor>): ResultUpdateAnchorByCookie =
        ResultUpdateAnchorByCookie(false)

    /**
     * 设置提醒词
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("已弃用")
    fun Anchor.setHintWhenFollowListDidNotContainsTheAnchor() {
//        title = FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR
    }

    /**
     * 设置提醒词
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("已弃用")
    fun List<Anchor>.setHintWhenFollowListDidNotContainsTheAnchor() {
//        forEach {
////            it.title = FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR
//        }
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
            mainThread {
                toast(context.getString(R.string.streaming_url_is_null))
            }
        }
    }

    /**
     * cookie方式获取列表
     * Use [getCookie] to get saved cookie
     * @return AnchorsCookieMode
     */
    fun getAnchorsWithCookieMode(): ResultGetAnchorListByCookieMode {
        return ResultGetAnchorListByCookieMode(
            success = false,
            isCookieValid = false,
            anchorList = null,
            message = ""
        )
    }

    /**
     *  WebView 使用PC Agent
     */
    fun loginWithPcAgent(): Boolean {
        return false
    }

    val loginTips
        get() = ""

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

    /**
     * 关注
     */
    fun follow(anchor: Anchor): Pair<Boolean, String> = Pair(false, "该平台不支持。")

    /**
     * 取消关注
     */
    fun unFollow(anchor: Anchor): Pair<Boolean, String> = Pair(false, "该平台不支持。")

    /**
     * 弹幕接收器
     */
    val danmuManager: DanmuManager?
        get() = null

    /**
     * 弹幕开启
     * 默认以[danmuManager]实现
     * 如果你复写这个方法，你需要自行实现弹幕接收推送，并且需要同时复写[danmuStop]
     */
    fun danmuStart(
        anchor: Anchor,
        danmuClient: DanmuClient
    ): Boolean {
        return if (this.danmuManager != null) {
            this.danmuManager?.onDanmuStart(getCookie(), anchor, danmuClient)
            true
        } else
            false
    }

    /**
     * 弹幕关闭
     * & [danmuStart]
     */
    fun danmuStop(danmuClient: DanmuClient): Boolean {
        return if (this.danmuManager != null) {
            this.danmuManager?.onDanmuStop(danmuClient)
            true
        } else
            false
    }

    /**
     * 弹幕管理器，用于连接弹幕服务器、接收弹幕、推送弹幕给弹幕客户端
     */
    abstract class DanmuManager {
        private var danmuReceiver: DanmuReceiver? = null

        /**
         * 接收弹幕是否检查cookie
         */
        open val danmuAssertCookie: Boolean
            get() = false

        /**
         * 默认的弹幕接收器
         */
        interface DanmuReceiver {
            fun start()
            fun stop()
        }

        /**
         * 你必须
         * 生成一个[DanmuReceiver]
         */
        abstract fun generateReceiver(
            cookie: String,
            anchor: Anchor,
            danmuClient: DanmuClient
        ): DanmuReceiver?

        /**
         * 弹幕开启，默认实现[DanmuReceiver]
         */
        fun onDanmuStart(
            cookie: String,
            anchor: Anchor,
            danmuClient: DanmuClient
        ) {
            if (danmuAssertCookie && cookie.isEmpty()) {
                danmuClient.cookieMsgCallback("该平台登录后才能接收弹幕。")
                return
            }
            danmuReceiver?.stop()
            danmuReceiver = generateReceiver(cookie, anchor, danmuClient)
            danmuReceiver?.start()
        }


        /**
         * 弹幕关闭，默认关闭[DanmuReceiver]
         */
        @Suppress("UNUSED_PARAMETER")
        fun onDanmuStop(danmuClient: DanmuClient) {
            danmuReceiver?.stop()
        }
    }
}